package com.geecee.escapelauncher.core.data.repository.todo

import android.util.Log
import com.geecee.escapelauncher.core.data.database.TodoDao
import com.geecee.escapelauncher.core.data.entity.TodoPendingCommandEntity
import com.geecee.escapelauncher.core.data.entity.TodoTaskEntity
import com.geecee.escapelauncher.core.data.network.TodoistSyncApi
import com.geecee.escapelauncher.core.di.ApplicationScope
import com.geecee.escapelauncher.core.domain.repository.settings.TodoSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.todo.TodoRepository
import com.geecee.escapelauncher.core.model.TodoProject
import com.geecee.escapelauncher.core.model.TodoSyncStatus
import com.geecee.escapelauncher.core.model.TodoTask
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val dao: TodoDao,
    private val api: TodoistSyncApi,
    private val settings: TodoSettingsRepository,
    @ApplicationScope private val scope: CoroutineScope
) : TodoRepository {

    override val tasks: Flow<List<TodoTask>> = dao.observeTasks().map { rows -> rows.map { it.toModel() } }

    private val _projects = MutableStateFlow<List<TodoProject>>(emptyList())
    override val projects: StateFlow<List<TodoProject>> = _projects.asStateFlow()

    private val _syncStatus = MutableStateFlow(TodoSyncStatus())
    override val syncStatus: StateFlow<TodoSyncStatus> = _syncStatus.asStateFlow()

    private val syncMutex = Mutex()
    private val syncRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        @OptIn(FlowPreview::class)
        scope.launch {
            syncRequests.debounce(SYNC_DEBOUNCE).collect { runSync() }
        }
        scope.launch {
            // Reflect the queue length even when no sync is running (e.g. edits made offline).
            dao.observePendingCount().collect { count -> _syncStatus.update { it.copy(pendingCount = count) } }
        }
    }

    override fun requestSync() {
        syncRequests.tryEmit(Unit)
    }

    override suspend fun syncNow(): TodoSyncStatus = runSync()

    // ---- local edits -------------------------------------------------------------------------

    override suspend fun addTask(content: String, parentId: String?) {
        val text = content.trim()
        if (text.isEmpty()) return
        val projectId = settings.todoistProjectId.first()
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        dao.upsertTask(
            TodoTaskEntity(
                id = id,
                content = text,
                checked = false,
                parentId = parentId,
                childOrder = dao.maxChildOrder(parentId) + 1,
                projectId = projectId,
                updatedAt = now
            )
        )
        enqueue("item_add", taskId = id, parentId = parentId, content = text, projectId = projectId.ifEmpty { null })
    }

    override suspend fun setChecked(id: String, checked: Boolean) {
        val now = System.currentTimeMillis()
        if (checked) dao.updateCheckedWithChildren(id, true, now) else dao.updateChecked(id, false, now)
        enqueue(if (checked) "item_close" else "item_uncomplete", taskId = id)
    }

    override suspend fun rename(id: String, content: String) {
        val text = content.trim()
        if (text.isEmpty()) return
        dao.updateContent(id, text, System.currentTimeMillis())
        enqueue("item_update", taskId = id, content = text)
    }

    override suspend fun delete(id: String) {
        dao.deleteTaskWithChildren(id)
        enqueue("item_delete", taskId = id)
    }

    override suspend fun connect(token: String): TodoSyncStatus {
        val trimmed = token.trim()
        if (trimmed.isEmpty()) return _syncStatus.value
        syncMutex.withLock {
            if (trimmed != settings.todoistToken.first()) {
                settings.setTodoistToken(trimmed)
                settings.setTodoistSyncToken("")
            }
            // Whatever was queued before belongs to an older connection (or none); the current
            // local list is the truth. Tasks with a local id have never been in Todoist, so they
            // are uploaded; tasks that already carry a Todoist id are reconciled by the full sync.
            dao.deleteAllPending()
            val open = dao.getOpenTasks().filter { it.id.isLocal() }
            val uploadable = open.filter { it.parentId == null } +
                open.filter { it.parentId != null && open.any { p -> p.id == it.parentId } }
            uploadable.forEach { task ->
                dao.insertPending(
                    TodoPendingCommandEntity(
                        uuid = UUID.randomUUID().toString(),
                        type = "item_add",
                        taskId = task.id,
                        parentId = task.parentId,
                        content = task.content,
                        projectId = null
                    )
                )
            }
        }
        return runSync()
    }

    override suspend fun selectProject(project: TodoProject) {
        settings.setTodoistProject(project.id, project.name)
        settings.setTodoistSyncToken("")
        dao.deleteAllTasks()
        requestSync()
    }

    override suspend fun disconnect() {
        settings.setTodoistToken("")
        settings.setTodoistSyncToken("")
        settings.setTodoistProject("", "")
        dao.deleteAllPending()
        _projects.value = emptyList()
        _syncStatus.value = TodoSyncStatus()
    }

    private suspend fun isConnected() = settings.todoistToken.first().isNotBlank()

    /** Queues a command for Todoist. Nothing is queued while the list is local-only. */
    private suspend fun enqueue(
        type: String,
        taskId: String,
        parentId: String? = null,
        content: String? = null,
        projectId: String? = null
    ) {
        if (!isConnected()) return
        dao.insertPending(
            TodoPendingCommandEntity(
                uuid = UUID.randomUUID().toString(),
                type = type,
                taskId = taskId,
                parentId = parentId,
                content = content,
                projectId = projectId
            )
        )
        requestSync()
    }

    // ---- sync --------------------------------------------------------------------------------

    private suspend fun runSync(): TodoSyncStatus = syncMutex.withLock {
        val apiToken = settings.todoistToken.first().trim()
        if (apiToken.isEmpty()) {
            // Local list: the only housekeeping is dropping old completed tasks.
            dao.purgeStaleCompleted(before = System.currentTimeMillis() - COMPLETED_RETENTION.inWholeMilliseconds)
            return setStatus(TodoSyncStatus.State.LOCAL)
        }
        _syncStatus.update { it.copy(state = TodoSyncStatus.State.SYNCING, message = null) }

        val pending = dao.getPendingCommands()
        val commands = pending.map { it.toCommand() }
        val syncToken = settings.todoistSyncToken.first().ifEmpty { "*" }

        val response = try {
            api.sync(apiToken, syncToken, listOf("items", "projects"), commands)
        } catch (e: TodoistSyncApi.AuthException) {
            return setStatus(TodoSyncStatus.State.AUTH_ERROR)
        } catch (e: TodoistSyncApi.HttpException) {
            Log.w(TAG, "Sync failed", e)
            return setStatus(TodoSyncStatus.State.ERROR, e.message)
        } catch (e: IOException) {
            Log.w(TAG, "Sync failed, treating as offline", e)
            return setStatus(TodoSyncStatus.State.OFFLINE)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            return setStatus(TodoSyncStatus.State.ERROR, e.message)
        }

        applyResponse(response, pending)
        return setStatus(TodoSyncStatus.State.IDLE, lastSyncedAt = System.currentTimeMillis())
    }

    private suspend fun applyResponse(response: TodoistSyncApi.Response, sent: List<TodoPendingCommandEntity>) {
        // 1. Server-assigned ids replace the temp ids everywhere they are referenced.
        response.tempIdMapping.forEach { (temp, real) -> dao.remapId(temp, real) }

        // 2. Commands the server has answered leave the queue, accepted or not. A rejected command
        //    (e.g. deleting a task that is already gone) must not block the queue forever.
        sent.forEach { command ->
            val result = response.syncStatus[command.uuid] ?: return@forEach
            if (result is TodoistSyncApi.CommandResult.Error) {
                Log.w(TAG, "Todoist rejected ${command.type} for ${command.taskId}: ${result.message}")
            }
            dao.deletePending(command.uuid)
        }

        // 3. Projects: full sync replaces the list, incremental sync patches it.
        response.projects?.let { incoming ->
            val mapped = incoming.map { TodoProject(it.id, it.name, it.isInbox) }
            _projects.update { current ->
                val base = if (response.fullSync) emptyList() else current
                val removed = incoming.filter { it.isDeleted || it.isArchived }.map { it.id }.toSet()
                (base.filter { p -> mapped.none { it.id == p.id } } + mapped)
                    .filter { it.id !in removed }
                    .sortedWith(compareByDescending<TodoProject> { it.isInbox }.thenBy { it.name.lowercase() })
            }
        }

        // First run: nobody has picked a project yet, so the page shows the Inbox.
        var projectId = settings.todoistProjectId.first()
        if (projectId.isEmpty()) {
            val inbox = _projects.value.firstOrNull { it.isInbox } ?: _projects.value.firstOrNull()
            if (inbox != null) {
                settings.setTodoistProject(inbox.id, inbox.name)
                projectId = inbox.id
            }
        }

        // 4. Items. Tasks with commands still queued (edited while the request was in flight)
        //    keep their local state; the next sync will reconcile them.
        val now = System.currentTimeMillis()
        val stillPending = dao.getPendingTaskIds()
        val wanted = response.items
            .filter { it.projectId == projectId && !it.isDeleted && it.id !in stillPending }
        if (response.fullSync) {
            dao.replaceAllExcept(wanted.filter { !it.checked }.map { it.toEntity(now) }, stillPending)
        } else {
            response.items.forEach { item ->
                if (item.id in stillPending) return@forEach
                if (item.isDeleted || item.projectId != projectId) dao.deleteTaskWithChildren(item.id)
            }
            dao.upsertTasks(wanted.map { it.toEntity(now) })
        }
        dao.purgeStaleCompleted(before = now - COMPLETED_RETENTION.inWholeMilliseconds)

        settings.setTodoistSyncToken(response.syncToken)
    }

    private suspend fun setStatus(
        state: TodoSyncStatus.State,
        message: String? = null,
        lastSyncedAt: Long? = null
    ): TodoSyncStatus {
        val status = TodoSyncStatus(
            state = state,
            lastSyncedAt = lastSyncedAt ?: _syncStatus.value.lastSyncedAt,
            pendingCount = dao.pendingCount(),
            message = message
        )
        _syncStatus.value = status
        return status
    }

    // ---- mapping -----------------------------------------------------------------------------

    private fun TodoPendingCommandEntity.toCommand(): TodoistSyncApi.Command {
        val args: Map<String, String?> = when (type) {
            "item_add" -> buildMap {
                put("content", content.orEmpty())
                projectId?.let { put("project_id", it) }
                parentId?.let { put("parent_id", it) }
            }
            "item_update" -> mapOf("id" to taskId, "content" to content.orEmpty())
            else -> mapOf("id" to taskId)
        }
        return TodoistSyncApi.Command(
            type = type,
            uuid = uuid,
            tempId = if (type == "item_add") taskId else null,
            args = args
        )
    }

    private fun TodoistSyncApi.Item.toEntity(now: Long) = TodoTaskEntity(
        id = id,
        content = content,
        checked = checked,
        parentId = parentId,
        childOrder = childOrder,
        projectId = projectId,
        updatedAt = now
    )

    /** Ids handed out locally are UUIDs; Todoist ids never contain a dash. */
    private fun String.isLocal() = '-' in this

    private fun TodoTaskEntity.toModel() = TodoTask(
        id = id,
        content = content,
        checked = checked,
        parentId = parentId,
        order = childOrder,
        projectId = projectId
    )

    companion object {
        private const val TAG = "TodoRepository"
        private val SYNC_DEBOUNCE = 600.milliseconds
        private val COMPLETED_RETENTION = 1.days
    }
}
