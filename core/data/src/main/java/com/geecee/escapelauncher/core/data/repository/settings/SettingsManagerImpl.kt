package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.domain.repository.settings.SettingsManager
import jakarta.inject.Inject

class SettingsManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsManager {
    override suspend fun resetToDefaults() {
        dataStore.edit { it.clear() }
    }
}
