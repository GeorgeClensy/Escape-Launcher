package com.geecee.escapelauncher.feature.homescreen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.core.common.getAppShortcuts
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.common.startShortcut
import com.geecee.escapelauncher.core.domain.apps.AppActionType
import com.geecee.escapelauncher.core.domain.apps.GetAppActionsUseCase
import com.geecee.escapelauncher.core.domain.apps.GetFavoriteAppsUseCase
import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.AppAction
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NewHomeScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    settingsRepository: SettingsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    getFavoriteAppsUseCase: GetFavoriteAppsUseCase,
    val widgetHostManager: WidgetHostManager,
    appConfiguration: AppConfiguration,
    private val getAppActionsUseCase: GetAppActionsUseCase
) : ViewModel() {
    val isFoss = appConfiguration.isFoss

    // UI Events
    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Settings
    val twelveHourClock = settingsRepository.twelveHourClock
    val showClock = settingsRepository.showClock
    val bigClock = settingsRepository.bigClock
    val showDate = settingsRepository.showDate
    val showScreenTimeHome = settingsRepository.showScreenTimeHome
    val showWeather = settingsRepository.showWeather
    val showScreenTimeApp = settingsRepository.showScreenTimeApp
    val firstTimeHelp = settingsRepository.firstTimeHelp
    val hapticFeedBackEnabled = settingsRepository.hapticFeedBackEnabled

    val homeAlignment = settingsRepository.homeAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }

    val homeVAlignment = settingsRepository.homeVAlignment.map { alignment ->
        when (alignment) {
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            else -> Arrangement.Bottom
        }
    }

    val widgetOffset = settingsRepository.widgetOffset
    val widgetHeight = settingsRepository.widgetHeight
    val widgetWidth = settingsRepository.widgetWidth
    val widgetId = settingsRepository.widgetId

    // Favorite Apps
    val favoriteApps: StateFlow<List<InstalledApp>> = getFavoriteAppsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Bottom Sheet State
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()
    fun setBottomSheetVisible(visibility: Boolean) {
        _showBottomSheet.value = visibility
    }

    private val _bottomSheetApp = MutableStateFlow<InstalledApp?>(null)
    val bottomSheetApp: StateFlow<InstalledApp?> = _bottomSheetApp.asStateFlow()
    fun setBottomSheetApp(app: InstalledApp?) {
        _bottomSheetApp.value = app
    }

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
                                _uiEvent.emit(HomeUiEvent.UninstallApp(clickedApp))
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
                                    _uiEvent.emit(HomeUiEvent.NavigateHome)
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
                                _uiEvent.emit(HomeUiEvent.ShowAppInfo(clickedApp))
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
                        _uiEvent.emit(HomeUiEvent.NavigateHome)
                    }
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

sealed class HomeUiEvent {
    data object NavigateHome : HomeUiEvent()
    data class UninstallApp(val app: InstalledApp) : HomeUiEvent()
    data class ShowAppInfo(val app: InstalledApp) : HomeUiEvent()
}
