package com.geecee.escapelauncher.feature.appslist

import android.content.Context
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.fuzzyMatch
import com.geecee.escapelauncher.core.common.getAppShortcuts
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.common.sortAppsByRelevance
import com.geecee.escapelauncher.core.common.startShortcut
import com.geecee.escapelauncher.core.domain.apps.AppActionType
import com.geecee.escapelauncher.core.domain.apps.GetAppActionsUseCase
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.AppAction
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppsListViewModel @Inject constructor(
    @ApplicationContext context: Context,
    settingsRepository: SettingsRepository,
    appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val getAppActionsUseCase: GetAppActionsUseCase
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
    ) { allApps, hiddenIds, rawQuery, showHidden ->
        val query = rawQuery.trim()
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

    // Actions
    val bottomSheetActions: StateFlow<List<AppAction>> = _bottomSheetApp.flatMapLatest { app ->
        if (app == null) flowOf(emptyList())
        else getAppActionsUseCase(app.packageName).map { actionTypes ->
            actionTypes.map { type ->
                when (type) {
                    AppActionType.Uninstall -> AppAction(
                        labelRes = R.string.uninstall,
                        onClick = { clickedApp ->
                            viewModelScope.launch {
                                _uiEvent.emit(AppsListUiEvent.UninstallApp(clickedApp))
                            }
                        }
                    )
                    is AppActionType.ToggleFavorite -> AppAction(
                        labelRes = if (type.isFavorite) R.string.rem_from_fav else R.string.add_to_fav,
                        isVisible = { it.isMainUserApp() },
                        onClick = { clickedApp ->
                            viewModelScope.launch {
                                if (type.isFavorite) {
                                    modifiedAppsRepository.removeFavourite(clickedApp.packageName)
                                } else {
                                    modifiedAppsRepository.addFavourite(clickedApp.packageName)
                                    _uiEvent.emit(AppsListUiEvent.NavigateHome)
                                }
                                _showBottomSheet.value = false
                            }
                        }
                    )
                    AppActionType.Hide -> AppAction(
                        labelRes = R.string.hide,
                        isVisible = { it.isMainUserApp() },
                        onClick = { clickedApp ->
                            viewModelScope.launch {
                                modifiedAppsRepository.setHidden(clickedApp.packageName, true)
                                _showBottomSheet.value = false
                            }
                        }
                    )
                    AppActionType.AppInfo -> AppAction(
                        labelRes = R.string.app_info,
                        onClick = { clickedApp ->
                            viewModelScope.launch {
                                _uiEvent.emit(AppsListUiEvent.ShowAppInfo(clickedApp))
                            }
                        }
                    )
                    AppActionType.AddChallenge -> AppAction(
                        labelRes = R.string.add_open_challenge,
                        isVisible = { it.isMainUserApp() },
                        onClick = { clickedApp ->
                            viewModelScope.launch {
                                modifiedAppsRepository.setChallenge(clickedApp.packageName, true)
                                _showBottomSheet.value = false
                            }
                        }
                    )
                }
            }
        }
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
