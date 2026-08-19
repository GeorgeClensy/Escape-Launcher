package com.geecee.escapelauncher.core.domain.repository.settings

interface SettingsManager {
    suspend fun resetToDefaults()
}
