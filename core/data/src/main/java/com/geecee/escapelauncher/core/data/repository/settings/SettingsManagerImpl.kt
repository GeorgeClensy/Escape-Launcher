package com.geecee.escapelauncher.core.data.repository.settings

import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys.HIDDEN_APPS_PASSWORD_RECORD
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys.HIDDEN_APPS_RECOVERY_MINUTES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.domain.repository.settings.SettingsManager
import jakarta.inject.Inject

class SettingsManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsManager {
    override suspend fun resetToDefaults() {
        dataStore.edit {
            // General appearance/settings resets must not disable hidden-app protection.
            val password = it[HIDDEN_APPS_PASSWORD_RECORD]
            val recoveryMinutes = it[HIDDEN_APPS_RECOVERY_MINUTES]
            it.clear()
            password?.let { value -> it[HIDDEN_APPS_PASSWORD_RECORD] = value }
            recoveryMinutes?.let { value -> it[HIDDEN_APPS_RECOVERY_MINUTES] = value }
        }
    }
}
