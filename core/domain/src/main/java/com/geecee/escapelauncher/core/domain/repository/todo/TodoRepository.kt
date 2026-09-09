package com.geecee.escapelauncher.core.domain.repository.todo

import com.geecee.escapelauncher.core.model.TodoProject
import com.geecee.escapelauncher.core.model.TodoSyncStatus
import com.geecee.escapelauncher.core.model.TodoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Offline-first to-do list backed by Todoist.
 *
 * Every edit is applied to the local copy immediately and queued for the remote; [requestSync]
 * pushes the queue and pulls remote changes. The UI only ever reads [tasks].
 */
interface TodoRepository {
    val tasks: Flow<List<TodoTask>>
    val projects: StateFlow<List<TodoProject>>
    val syncStatus: StateFlow<TodoSyncStatus>

    /** Debounced, fire-and-forget sync. Safe to call often (page shown, after each edit). */
    fun requestSync()

    /** Runs a sync right now and returns the resulting status. Used by the settings page. */
    suspend fun syncNow(): TodoSyncStatus

    suspend fun addTask(content: String, parentId: String? = null)
    suspend fun setChecked(id: String, checked: Boolean)
    suspend fun rename(id: String, content: String)
    suspend fun delete(id: String)

    /** Switches the project shown on the page. Local tasks are dropped and re-fetched. */
    suspend fun selectProject(project: TodoProject)

    /** Forgets the token, the local tasks and the pending queue. */
    suspend fun disconnect()
}
