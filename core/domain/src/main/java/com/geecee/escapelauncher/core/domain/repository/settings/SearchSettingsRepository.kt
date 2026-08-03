package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface SearchSettingsRepository {
    val showSearchBox: Flow<Boolean>
    suspend fun setShowSearchBox(enabled: Boolean)
    val searchAutoOpen: Flow<Boolean>
    suspend fun setSearchAutoOpen(enabled: Boolean)
    val bottomSearch: Flow<Boolean>
    suspend fun setBottomSearch(enabled: Boolean)
    val automaticallyOpenAppsInSearch: Flow<Boolean>
    suspend fun setAutomaticallyOpenAppsInSearch(enabled: Boolean)
    val showHiddenAppsInSearch: Flow<Boolean>
    suspend fun setShowHiddenAppsInSearch(enabled: Boolean)
}
