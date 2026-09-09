package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface TodoSettingsRepository {
    val showTodoPage: Flow<Boolean>
    suspend fun setShowTodoPage(enabled: Boolean)

    val todoistToken: Flow<String>
    suspend fun setTodoistToken(token: String)

    val todoistProjectId: Flow<String>
    val todoistProjectName: Flow<String>
    suspend fun setTodoistProject(id: String, name: String)

    /** Incremental sync cursor handed back by Todoist. Empty means "full sync next time". */
    val todoistSyncToken: Flow<String>
    suspend fun setTodoistSyncToken(token: String)
}
