package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppearanceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppearanceRepository {
    override val theme: Flow<Int> = dataStore.data.map { it[PreferencesKeys.THEME] ?: DefaultSettings.THEME }
    override suspend fun setTheme(theme: Int) {
        dataStore.edit { it[PreferencesKeys.THEME] = theme }
    }
    override val font: Flow<String> = dataStore.data.map { it[PreferencesKeys.FONT] ?: DefaultSettings.FONT }
    override suspend fun setFont(value: String) {
        dataStore.edit { it[PreferencesKeys.FONT] = value }
    }
    override val homeVAlignment: Flow<String> = dataStore.data.map { it[PreferencesKeys.HOME_V_ALIGNMENT] ?: DefaultSettings.HOME_V_ALIGNMENT }
    override suspend fun setHomeVAlignment(alignment: String) {
        dataStore.edit { it[PreferencesKeys.HOME_V_ALIGNMENT] = alignment }
    }
    override val homeAlignment: Flow<String> = dataStore.data.map { it[PreferencesKeys.HOME_ALIGNMENT] ?: DefaultSettings.HOME_ALIGNMENT }
    override suspend fun setHomeAlignment(alignment: String) {
        dataStore.edit { it[PreferencesKeys.HOME_ALIGNMENT] = alignment }
    }
    override val appsAlignment: Flow<String> = dataStore.data.map { it[PreferencesKeys.APPS_ALIGNMENT] ?: DefaultSettings.APPS_ALIGNMENT }
    override suspend fun setAppsAlignment(alignment: String) {
        dataStore.edit { it[PreferencesKeys.APPS_ALIGNMENT] = alignment }
    }
    override val showStatusBar: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_STATUS_BAR] ?: DefaultSettings.SHOW_STATUS_BAR }
    override suspend fun setShowStatusBar(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_STATUS_BAR] = enabled }
    }
    override val showWallpaper: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_WALLPAPER] ?: DefaultSettings.SHOW_WALLPAPER }
    override suspend fun setShowWallpaper(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_WALLPAPER] = enabled }
    }
}
