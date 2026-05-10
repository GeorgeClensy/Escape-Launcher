package com.geecee.escapelauncher

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppsListViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) : ViewModel() {
    // Settings
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
    val hapticFeedBackEnabled = settingsRepository.hapticFeedBackEnabled

    // Search
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _searchExpanded = MutableStateFlow(false)
    val searchExpanded: StateFlow<Boolean> = _searchExpanded.asStateFlow()
    fun onSearchTextChanged(query: String) {
        if (_searchExpanded.value) {
            _searchText.value = query
        }
    }
    fun onSearchExpandedChanged(expanded: Boolean) {
        _searchExpanded.value = expanded
        if (!_searchExpanded.value) {
            _searchText.value = ""
        }
    }

    // Apps
    val apps: StateFlow<List<InstalledApp>> = combine(
        appsRepository.mainUserApps,
        modifiedAppsRepository.getHiddenPackageIdsFlow(),
        _searchText,
        hiddenAppsInSearch
    ) { allApps, hiddenIds, query, showHidden ->
        val hiddenSet = hiddenIds.toSet()

        val filtered = if (query.isBlank()) {
            allApps.filter { !hiddenSet.contains(it.packageName) }
        } else {
            allApps.filter { app ->
                val isHidden = hiddenSet.contains(app.packageName)
                val matchesQuery = AppUtils.fuzzyMatch(app.displayName, query)
                matchesQuery && (!isHidden || showHidden)
            }
        }

        if (query.isNotBlank()) {
            AppUtils.sortAppsByRelevance(filtered, query)
        } else {
            filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Bottom sheet
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()
    fun setBottomSheetVisible(visibility: Boolean) {
        _showBottomSheet.value = visibility
    }
    fun setBottomSheetApp(app: InstalledApp?) {
        _bottomSheetApp.value = app
    }
    fun addFavourite(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.addFavourite(packageId)
        }
    }
    fun removeFavourite(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.removeFavourite(packageId)
        }
    }
    private val _bottomSheetApp = MutableStateFlow<InstalledApp?>(null)
    val botttomSheetApp: StateFlow<InstalledApp?> = _bottomSheetApp.asStateFlow()
    val isBottomSheetAppFavourite: StateFlow<Boolean> = combine(
        _bottomSheetApp,
        modifiedAppsRepository.getFavouriteAppsInOrderFlow()
    ) { app, favourites ->
        app?.let { selectedApp ->
            favourites.any { it.packageId == selectedApp.packageName }
        } ?: false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    val doesBottomSheetAppHaveChallenge: StateFlow<Boolean> = combine(
        _bottomSheetApp,
        modifiedAppsRepository.getChallengePackageIdsFlow()
    ) { app, challenges ->
        app?.let { selectedApp ->
            challenges.any { it == selectedApp.packageName }
        } ?: false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Work Apps
    private val _showWorkApps = MutableStateFlow(false)
    val showWorkApps: StateFlow<Boolean> = _showWorkApps.asStateFlow()
    fun setShowWorkApps(show: Boolean) {
        _showWorkApps.value = show
    }
}
