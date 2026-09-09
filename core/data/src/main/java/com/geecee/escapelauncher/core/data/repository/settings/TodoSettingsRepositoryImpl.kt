package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.TodoSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TodoSettingsRepository {
    override val showTodoPage: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_TODO_PAGE] ?: DefaultSettings.SHOW_TODO_PAGE }
    override suspend fun setShowTodoPage(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_TODO_PAGE] = enabled }
    }

    override val todoistToken: Flow<String> = dataStore.data.map { it[PreferencesKeys.TODOIST_TOKEN] ?: DefaultSettings.TODOIST_TOKEN }
    override suspend fun setTodoistToken(token: String) {
        dataStore.edit { it[PreferencesKeys.TODOIST_TOKEN] = token }
    }

    override val todoistProjectId: Flow<String> = dataStore.data.map { it[PreferencesKeys.TODOIST_PROJECT_ID] ?: DefaultSettings.TODOIST_PROJECT_ID }
    override val todoistProjectName: Flow<String> = dataStore.data.map { it[PreferencesKeys.TODOIST_PROJECT_NAME] ?: DefaultSettings.TODOIST_PROJECT_NAME }
    override suspend fun setTodoistProject(id: String, name: String) {
        dataStore.edit {
            it[PreferencesKeys.TODOIST_PROJECT_ID] = id
            it[PreferencesKeys.TODOIST_PROJECT_NAME] = name
        }
    }

    override val todoistSyncToken: Flow<String> = dataStore.data.map { it[PreferencesKeys.TODOIST_SYNC_TOKEN] ?: DefaultSettings.TODOIST_SYNC_TOKEN }
    override suspend fun setTodoistSyncToken(token: String) {
        dataStore.edit { it[PreferencesKeys.TODOIST_SYNC_TOKEN] = token }
    }
}
