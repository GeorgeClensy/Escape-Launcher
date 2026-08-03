package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScreenTimeSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ScreenTimeSettingsRepository {
    override val showScreenTimeHome: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_SCREEN_TIME_HOME] ?: false }
    override suspend fun setShowScreenTimeHome(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_SCREEN_TIME_HOME] = enabled }
    }
    override val showScreenTimeApp: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_SCREEN_TIME_APP] ?: false }
    override suspend fun setShowScreenTimeApp(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_SCREEN_TIME_APP] = enabled }
    }
    override val hideScreenTimePage: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.HIDE_SCREEN_TIME_PAGE] ?: false }
    override suspend fun setHideScreenTimePage(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.HIDE_SCREEN_TIME_PAGE] = enabled }
    }
}
