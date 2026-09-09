package com.geecee.escapelauncher.feature.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.repository.settings.TodoSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.todo.TodoRepository
import com.geecee.escapelauncher.core.model.TodoSyncStatus
import com.geecee.escapelauncher.core.model.TodoTask
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A top-level task together with its steps, in display order. */
data class TodoItemUi(
    val task: TodoTask,
    val steps: List<TodoTask>
)

data class TodoUiState(
    val items: List<TodoItemUi> = emptyList(),
    val syncStatus: TodoSyncStatus = TodoSyncStatus(),
    val projectName: String = "",
    val loaded: Boolean = false
)

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    settings: TodoSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository
) : ViewModel() {

    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

    /**
     * Completing a top-level task removes it from the list in Todoist. Keep it visible, struck
     * through, until the page is left so a mis-tap can be undone in place.
     */
    private val recentlyCompleted = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<TodoUiState> = combine(
        repository.tasks,
        repository.syncStatus,
        settings.todoistProjectName,
        recentlyCompleted
    ) { tasks, status, projectName, recent ->
        val steps = tasks.filter { it.parentId != null }.groupBy { it.parentId!! }
        val items = tasks
            .filter { it.parentId == null && (!it.checked || it.id in recent) }
            .sortedBy { it.order }
            .map { task -> TodoItemUi(task, steps[task.id].orEmpty().sortedBy { it.order }) }
        TodoUiState(items = items, syncStatus = status, projectName = projectName, loaded = true)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodoUiState()
    )

    fun onPageShown() {
        repository.requestSync()
    }

    fun onPageHidden() {
        recentlyCompleted.value = emptySet()
    }

    /** Hides the completed tasks that are still on screen. They are already done in Todoist. */
    fun clearDone() {
        recentlyCompleted.value = emptySet()
    }

    fun toggle(task: TodoTask) {
        val checked = !task.checked
        if (task.parentId == null) {
            recentlyCompleted.update { if (checked) it + task.id else it - task.id }
        }
        viewModelScope.launch { repository.setChecked(task.id, checked) }
    }

    fun add(content: String, parentId: String? = null) {
        viewModelScope.launch { repository.addTask(content, parentId) }
    }

    fun rename(task: TodoTask, content: String) {
        viewModelScope.launch { repository.rename(task.id, content) }
    }

    fun delete(task: TodoTask) {
        viewModelScope.launch { repository.delete(task.id) }
    }
}
