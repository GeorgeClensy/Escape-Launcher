package com.geecee.escapelauncher.feature.appslist

import android.content.Context
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.model.AppAction
import com.geecee.escapelauncher.core.common.fuzzyMatch
import com.geecee.escapelauncher.core.common.getAppShortcuts
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.common.sortAppsByRelevance
import com.geecee.escapelauncher.core.common.startShortcut
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.geecee.escapelauncher.core.ui.R

@HiltViewModel
class AppsListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) : ViewModel() {
    // UI Events
    private val _uiEvent = MutableSharedFlow<AppsListUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

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
                val matchesQuery = fuzzyMatch(app.displayName, query)
                matchesQuery && (!isHidden || showHidden)
            }
        }

        if (query.isNotBlank()) {
            sortAppsByRelevance(filtered, query)
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

    // Actions
    val bottomSheetActions: StateFlow<List<AppAction>> = combine(
        _bottomSheetApp,
        isBottomSheetAppFavourite,
        doesBottomSheetAppHaveChallenge
    ) { app, isFavourite, hasChallenge ->
        if (app == null) return@combine emptyList()
        
        listOf(
            AppAction(
                labelRes = R.string.uninstall,
                onClick = { clickedApp ->
                    viewModelScope.launch {
                        _uiEvent.emit(AppsListUiEvent.UninstallApp(clickedApp))
                    }
                }
            ),
            AppAction(
                labelRes = if (isFavourite) R.string.rem_from_fav else R.string.add_to_fav,
                isVisible = { it.isMainUserApp() },
                onClick = { clickedApp ->
                    viewModelScope.launch {
                        if (isFavourite) {
                            modifiedAppsRepository.removeFavourite(clickedApp.packageName)
                        } else {
                            modifiedAppsRepository.addFavourite(clickedApp.packageName)
                            _uiEvent.emit(AppsListUiEvent.NavigateHome)
                        }
                        _showBottomSheet.value = false
                    }
                }
            ),
            AppAction(
                labelRes = R.string.hide,
                isVisible = { it.isMainUserApp() },
                onClick = { clickedApp ->
                    viewModelScope.launch {
                        modifiedAppsRepository.setHidden(clickedApp.packageName, true)
                        _showBottomSheet.value = false
                    }
                }
            ),
            AppAction(
                labelRes = R.string.app_info,
                onClick = { clickedApp ->
                    viewModelScope.launch {
                        _uiEvent.emit(AppsListUiEvent.ShowAppInfo(clickedApp))
                    }
                }
            ),
            AppAction(
                labelRes = R.string.add_open_challenge,
                isVisible = { it.isMainUserApp() && !hasChallenge },
                onClick = { clickedApp ->
                    viewModelScope.launch {
                        modifiedAppsRepository.setChallenge(clickedApp.packageName, true)
                        _showBottomSheet.value = false
                    }
                }
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shortcutActions: StateFlow<List<AppAction>> = _bottomSheetApp.map { app ->
        if (app == null || !app.isMainUserApp()) return@map emptyList()
        
        getAppShortcuts(context, app.packageName).map { shortcut ->
            AppAction(
                label = shortcut.label,
                onClick = { clickedApp ->
                    startShortcut(context, clickedApp.packageName, shortcut.id)
                    _showBottomSheet.value = false
                    viewModelScope.launch {
                        _uiEvent.emit(AppsListUiEvent.NavigateHome)
                    }
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Work Apps
    private val _showWorkApps = MutableStateFlow(false)
    val showWorkApps: StateFlow<Boolean> = _showWorkApps.asStateFlow()
    fun setShowWorkApps(show: Boolean) {
        _showWorkApps.value = show
    }
}

sealed class AppsListUiEvent {
    data object NavigateHome : AppsListUiEvent()
    data class UninstallApp(val app: InstalledApp) : AppsListUiEvent()
    data class ShowAppInfo(val app: InstalledApp) : AppsListUiEvent()
}
