package com.geecee.escapelauncher

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map

@HiltViewModel
class AppsListViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val appsRepository: AppsRepository
) : ViewModel() {
    val showScreenTimeApp = settingsRepository.showScreenTimeApp
    val appsAlignment = settingsRepository.appsAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }
    val showSearchBox = settingsRepository.showSearchBox
    val searchAutoOpen = settingsRepository.searchAutoOpen
    val bottomSearch = settingsRepository.bottomSearch
    val automaticallyOpenAppsInSearch = settingsRepository.automaticallyOpenAppsInSearch
    val hiddenAppsInSearch = settingsRepository.showHiddenAppsInSearch

    //Apps
    val apps = appsRepository.mainUserApps
}