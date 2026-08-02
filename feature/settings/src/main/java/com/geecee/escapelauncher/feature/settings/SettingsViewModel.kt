package com.geecee.escapelauncher.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.GetFavoriteAppsUseCase
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SettingsViewModel @Inject constructor(
    appsRepository: AppsRepository,
    val modifiedAppsRepository: ModifiedAppsRepository,
    getFavoriteAppsUseCase: GetFavoriteAppsUseCase
) : ViewModel() {
    val installedApps = appsRepository.mainUserApps

    val favoriteApps: StateFlow<List<InstalledApp>> = getFavoriteAppsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
