package com.geecee.escapelauncher.feature.appslist

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.domain.apps.AppActionType
import com.geecee.escapelauncher.core.domain.apps.LaunchAppUseCase
import com.geecee.escapelauncher.core.domain.apps.GetAppActionsUseCase
import com.geecee.escapelauncher.core.domain.apps.GetAppShortcutsUseCase
import com.geecee.escapelauncher.core.domain.apps.StartShortcutUseCase
import com.geecee.escapelauncher.core.domain.search.SearchAppsUseCase
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.*
import com.geecee.escapelauncher.core.model.AppAction
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppsListViewModel @Inject constructor(
    appearanceRepository: AppearanceRepository,
    searchSettingsRepository: SearchSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val getAppActionsUseCase: GetAppActionsUseCase,
    private val getAppShortcutsUseCase: GetAppShortcutsUseCase,
    private val startShortcutUseCase: StartShortcutUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    searchAppsUseCase: SearchAppsUseCase
) : ViewModel() {
    // UI Events
    private val _uiEvent = MutableSharedFlow<AppsListUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Settings
    val showScreenTimeApp = screenTimeSettingsRepository.showScreenTimeApp
    val appsAlignment = appearanceRepository.appsAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }
    val showSearchBox = searchSettingsRepository.showSearchBox
    val searchAutoOpen = searchSettingsRepository.searchAutoOpen
    val bottomSearch = searchSettingsRepository.bottomSearch
    val automaticallyOpenAppsInSearch = searchSettingsRepository.automaticallyOpenAppsInSearch
    val hiddenAppsInSearch = searchSettingsRepository.showHiddenAppsInSearch
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

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
    val apps: StateFlow<List<InstalledApp>> = searchAppsUseCase(_searchText, hiddenAppsInSearch)
        .stateIn(
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
    val bottomSheetApp: StateFlow<InstalledApp?> = _bottomSheetApp.asStateFlow()

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
        
        getAppShortcutsUseCase(app.packageName).map { shortcut ->
            AppAction(
                label = shortcut.label,
                onClick = { clickedApp ->
                    startShortcutUseCase(clickedApp.packageName, shortcut.id)
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

    fun launchApp(app: InstalledApp, onAppOpened: ((String) -> Unit)? = null): Boolean {
        return launchAppUseCase(app, onAppOpened)
    }
}

sealed class AppsListUiEvent {
    data object NavigateHome : AppsListUiEvent()
    data class UninstallApp(val app: InstalledApp) : AppsListUiEvent()
    data class ShowAppInfo(val app: InstalledApp) : AppsListUiEvent()
}
