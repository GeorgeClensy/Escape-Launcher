package com.geecee.escapelauncher.feature.homescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.domain.apps.*
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
import kotlin.collections.map
import com.geecee.escapelauncher.core.domain.apps.UninstallAppUseCase
import com.geecee.escapelauncher.core.domain.apps.OpenAppDetailsUseCase
import com.geecee.escapelauncher.core.navigation.AppNavKey
import com.geecee.escapelauncher.core.navigation.NavRouter

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NewHomeScreenViewModel @Inject constructor(
    appearanceRepository: AppearanceRepository,
    clockRepository: ClockRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    onboardingRepository: OnboardingRepository,
    screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    weatherSettingsRepository: WeatherSettingsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    getFavoriteAppsUseCase: GetFavoriteAppsUseCase,
    appConfiguration: AppConfiguration,
    private val getAppActionsUseCase: GetAppActionsUseCase,
    private val getAppShortcutsUseCase: GetAppShortcutsUseCase,
    private val startShortcutUseCase: StartShortcutUseCase,
    private val uninstallAppUseCase: UninstallAppUseCase,
    private val openAppDetailsUseCase: OpenAppDetailsUseCase,
    private val analyticsProxy: AnalyticsProxy,
    val navRouter: NavRouter
) : ViewModel() {
    val isFoss = appConfiguration.isFoss

    // Settings
    val twelveHourClock = clockRepository.twelveHourClock
    val showClock = clockRepository.showClock
    val bigClock = clockRepository.bigClock
    val showDate = clockRepository.showDate
    val showScreenTimeHome = screenTimeSettingsRepository.showScreenTimeHome
    val showWeather = weatherSettingsRepository.showWeather
    val showScreenTimeApp = screenTimeSettingsRepository.showScreenTimeApp
    val firstTimeHelp = onboardingRepository.firstTimeHelp
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled
    val showWallpaper = appearanceRepository.showWallpaper

    val homeAlignment = appearanceRepository.homeAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }

    val homeVAlignment = appearanceRepository.homeVAlignment.map { alignment ->
        when (alignment) {
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            else -> Arrangement.Bottom
        }
    }

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

    fun logException(e: Exception) {
        analyticsProxy.recordException(e)
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
                            uninstallAppUseCase(clickedApp)
                            _showBottomSheet.value = false
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
                                    navRouter.navigateTo(AppNavKey.Home, true)
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
                            openAppDetailsUseCase(clickedApp)
                            _showBottomSheet.value = false
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
                        navRouter.navigateTo(AppNavKey.Home, true)
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