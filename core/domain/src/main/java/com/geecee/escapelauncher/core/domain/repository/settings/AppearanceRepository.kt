package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface AppearanceRepository {
    val theme: Flow<Int>
    suspend fun setTheme(theme: Int)
    val font: Flow<String>
    suspend fun setFont(value: String)
    val homeVAlignment: Flow<String>
    suspend fun setHomeVAlignment(alignment: String)
    val homeAlignment: Flow<String>
    suspend fun setHomeAlignment(alignment: String)
    val appsAlignment: Flow<String>
    suspend fun setAppsAlignment(alignment: String)
    val showStatusBar: Flow<Boolean>
    suspend fun setShowStatusBar(enabled: Boolean)
    val showWallpaper: Flow<Boolean>
    suspend fun setShowWallpaper(enabled: Boolean)
}
