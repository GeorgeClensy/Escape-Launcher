package com.geecee.escapelauncher.feature.settings.todoist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.TodoSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.todo.TodoRepository
import com.geecee.escapelauncher.core.model.TodoProject
import com.geecee.escapelauncher.core.model.TodoSyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoistSettingsUiState(
    val savedToken: String = "",
    val projectName: String = "",
    val projects: List<TodoProject> = emptyList(),
    val syncStatus: TodoSyncStatus = TodoSyncStatus()
)

@HiltViewModel
class TodoistSettingsViewModel @Inject constructor(
    settings: TodoSettingsRepository,
    private val repository: TodoRepository
) : ViewModel() {

    val uiState: StateFlow<TodoistSettingsUiState> = combine(
        settings.todoistToken,
        settings.todoistProjectName,
        repository.projects,
        repository.syncStatus
    ) { token, projectName, projects, status ->
        TodoistSettingsUiState(token, projectName, projects, status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoistSettingsUiState())

    private val _connecting = MutableStateFlow(false)
    val connecting: StateFlow<Boolean> = _connecting.asStateFlow()

    /** Saves the token and does a first sync so the status line tells the user whether it worked. */
    fun connect(token: String) {
        viewModelScope.launch {
            _connecting.value = true
            repository.connect(token)
            _connecting.value = false
        }
    }

    fun selectProject(project: TodoProject) {
        viewModelScope.launch { repository.selectProject(project) }
    }

    fun disconnect() {
        viewModelScope.launch { repository.disconnect() }
    }
}
