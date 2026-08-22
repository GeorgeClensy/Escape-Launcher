package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface ScreenTimeSettingsRepository {
    val showScreenTimeHome: Flow<Boolean>
    suspend fun setShowScreenTimeHome(enabled: Boolean)
    val showScreenTimeApp: Flow<Boolean>
    suspend fun setShowScreenTimeApp(enabled: Boolean)
    val hideScreenTimePage: Flow<Boolean>
    suspend fun setHideScreenTimePage(enabled: Boolean)
}
