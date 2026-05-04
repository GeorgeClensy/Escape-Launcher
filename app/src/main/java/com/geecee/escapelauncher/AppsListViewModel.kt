package com.geecee.escapelauncher

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map

@HiltViewModel
class AppsListViewModel @Inject constructor(
    repository: SettingsRepository
) : ViewModel() {
    val showScreenTimeApp = repository.showScreenTimeApp

    val appsAlignment = repository.appsAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }

    val showSearchBox = repository.showSearchBox
    val searchAutoOpen = repository.searchAutoOpen
    val bottomSearch = repository.bottomSearch
    val automaticallyOpenAppsInSearch = repository.automaticallyOpenAppsInSearch
    val hiddenAppsInSearch = repository.showHiddenAppsInSearch
}