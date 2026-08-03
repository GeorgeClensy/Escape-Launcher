package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.SearchSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SearchSettingsRepository {
    override val showSearchBox: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_SEARCH_BOX] ?: true }
    override suspend fun setShowSearchBox(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_SEARCH_BOX] = enabled }
    }
    override val searchAutoOpen: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SEARCH_AUTO_OPEN] ?: false }
    override suspend fun setSearchAutoOpen(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SEARCH_AUTO_OPEN] = enabled }
    }
    override val bottomSearch: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.BOTTOM_SEARCH] ?: false }
    override suspend fun setBottomSearch(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.BOTTOM_SEARCH] = enabled }
    }
    override val automaticallyOpenAppsInSearch: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH] ?: false }
    override suspend fun setAutomaticallyOpenAppsInSearch(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH] = enabled }
    }
    override val showHiddenAppsInSearch: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: false }
    override suspend fun setShowHiddenAppsInSearch(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH] = enabled }
    }
}
