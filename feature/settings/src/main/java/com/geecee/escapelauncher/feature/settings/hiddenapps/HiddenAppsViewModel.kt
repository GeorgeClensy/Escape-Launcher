package com.geecee.escapelauncher.feature.settings.hiddenapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.apps.LaunchAppUseCase
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.SearchSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HiddenAppsViewModel @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository,
    val appsRepository: AppsRepository,
    private val searchSettingsRepository: SearchSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    private val launchAppUseCase: LaunchAppUseCase
) : ViewModel() {
    val hiddenPackageIds: StateFlow<Set<String>> = modifiedAppsRepository.getHiddenPackageIdsFlow()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val showHiddenAppsInSearch = searchSettingsRepository.showHiddenAppsInSearch
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

    fun setShowHiddenAppsInSearch(value: Boolean) {
        viewModelScope.launch {
            searchSettingsRepository.setShowHiddenAppsInSearch(value)
        }
    }

    fun hideApp(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.setHidden(packageId, true)
        }
    }

    fun unhideApp(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.setHidden(packageId, false)
        }
    }

    fun launchApp(app: InstalledApp) {
        launchAppUseCase(app)
    }

    val installedApps = appsRepository.mainUserApps
}