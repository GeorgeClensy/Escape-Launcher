package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.domain.apps.AppActionType
import com.geecee.escapelauncher.core.domain.apps.GetAppActionsUseCase
import com.geecee.escapelauncher.core.domain.apps.GetAppShortcutsUseCase
import com.geecee.escapelauncher.core.domain.apps.LaunchAppUseCase
import com.geecee.escapelauncher.core.domain.apps.OpenAppDetailsUseCase
import com.geecee.escapelauncher.core.domain.apps.StartShortcutUseCase
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppResult
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppUseCase
import com.geecee.escapelauncher.core.domain.apps.UninstallAppUseCase
import com.geecee.escapelauncher.core.domain.launcher.GetIsDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.IsManagedProfileSupportedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileExistsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.WeatherSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.WidgetSettingsRepository
import com.geecee.escapelauncher.core.domain.system.LockScreenUseCase
import com.geecee.escapelauncher.core.model.AppAction
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.navigation.AppNavKey
import com.geecee.escapelauncher.core.navigation.NavRouter
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MainPagerScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val onboardingRepository: OnboardingRepository,
    private val screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    widgetSettingsRepository: WidgetSettingsRepository,
    appearanceRepository: AppearanceRepository,
    val widgetHostManager: WidgetHostManager,
    private val tryOpenAppUseCase: TryOpenAppUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val managedProfileExistsUseCase: ManagedProfileExistsUseCase,
    private val isManagedProfileSupportedUseCase: IsManagedProfileSupportedUseCase,
    private val getIsDefaultLauncherUseCase: GetIsDefaultLauncherUseCase,
    private val lockScreenUseCase: LockScreenUseCase,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val getAppActionsUseCase: GetAppActionsUseCase,
    private val getAppShortcutsUseCase: GetAppShortcutsUseCase,
    private val startShortcutUseCase: StartShortcutUseCase,
    private val uninstallAppUseCase: UninstallAppUseCase,
    private val openAppDetailsUseCase: OpenAppDetailsUseCase,
    val navRouter: NavRouter,
    appConfiguration: AppConfiguration,
    weatherSettingsRepository: WeatherSettingsRepository
) : AndroidViewModel(context as Application) {
    val isFoss = appConfiguration.isFoss
    val showWeather = weatherSettingsRepository.showWeather

    val showWallpaper = appearanceRepository.showWallpaper

    fun openSettings() {
        navRouter.navigateTo(AppNavKey.Settings.MainSettingsPage)
    }

    fun managedProfileExists(type: ManagedProfileType): Boolean = managedProfileExistsUseCase(type)
    fun isManagedProfileSupported(type: ManagedProfileType): Boolean = isManagedProfileSupportedUseCase(type)
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            onboardingRepository.setFirstTimeHelp(value)
        }
    }

    val hideScreenTimePage = screenTimeSettingsRepository.hideScreenTimePage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
    val doubleTapToLock = launcherBehaviorRepository.doubleTapToLock
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    fun updateLauncherStatus() {
        _isDefaultLauncher.value = getIsDefaultLauncherUseCase()
    }

    fun lockScreen() {
        lockScreenUseCase()
    }

    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))

    var showOpenChallenge = mutableStateOf(false)

    val interactionSource = MutableInteractionSource()

    val pagerState = PagerState(
        currentPage = if (hideScreenTimePage.value) 0 else 1,
        currentPageOffsetFraction = 0f
    ) {
        if (hideScreenTimePage.value) 2 else 3
    }

    private fun getMainPageIndex(): Int {
        return if (hideScreenTimePage.value) 0 else 1
    }

    suspend fun goToMainPage() {
        pagerState.scrollToPage(getMainPageIndex())
    }

    suspend fun animatedGoToMainPage() {
        val targetPage = getMainPageIndex()

        if (pagerState.currentPage == targetPage && pagerState.currentPageOffsetFraction == 0f) {
            return
        }

        pagerState.animateScrollToPage(
            targetPage,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    init {
        updateLauncherStatus()

        viewModelScope.launch {
            screenTimeSettingsRepository.hideScreenTimePage
                .distinctUntilChanged()
                .collect { hide -> pagerState.scrollToPage(if (hide) 0 else 1) }
        }
    }

    fun updateSelectedApp(app: InstalledApp) {
        currentSelectedApp.value = app
    }

    /**
     * Logic to handle what happens when an app is launched
     */
    fun onAppLaunched(app: InstalledApp) {
        updateSelectedApp(app)
        viewModelScope.launch {
            delay(500.milliseconds)
            goToMainPage()
        }
    }

    /**
     * High-level function to open an app, handling challenge checks asynchronously
     */
    fun openApp(
        app: InstalledApp,
        overrideChallenge: Boolean = false,
        onAppOpened: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            when (tryOpenAppUseCase(app.packageName, overrideChallenge)) {
                TryOpenAppResult.RequireHiddenAppsUnlock -> {
                    showOpenChallenge.value = false
                    navRouter.navigateTo(AppNavKey.Settings.HiddenApps)
                }
                TryOpenAppResult.ShowChallenge -> {
                    showOpenChallenge.value = true
                    updateSelectedApp(app)
                }
                TryOpenAppResult.Launch -> {
                    if (launchAppUseCase(app, onAppOpened)) {
                        onAppLaunched(app)

                        if (overrideChallenge) {
                            showOpenChallenge.value = false
                        }
                    }
                }
            }
        }
    }

    /** Apply the hidden-app gate to the launcher's clock, calendar and weather links too. */
    fun openHomeIntent(intent: Intent) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            try {
                val resolved = intent.resolveActivity(context.packageManager) ?: return@launch
                val packageName = resolved.packageName
                if (tryOpenAppUseCase(packageName, bypassChallenge = true) ==
                    TryOpenAppResult.RequireHiddenAppsUnlock) {
                    navRouter.navigateTo(AppNavKey.Settings.HiddenApps)
                } else {
                    val handlers = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    if (intent.component != null || handlers.any {
                        ComponentName(it.activityInfo.packageName, it.activityInfo.name) == resolved
                    }) {
                        context.startActivity(Intent(intent).setComponent(resolved))
                        return@launch
                    }
                    val excluded = handlers.filter {
                        tryOpenAppUseCase(it.activityInfo.packageName, bypassChallenge = true) ==
                            TryOpenAppResult.RequireHiddenAppsUnlock
                    }.map { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
                    val allowed = handlers.map {
                        ComponentName(it.activityInfo.packageName, it.activityInfo.name)
                    }.distinct() - excluded.toSet()
                    if (allowed.size == 1) {
                        context.startActivity(Intent(intent).setComponent(allowed.single()))
                    } else if (excluded.isEmpty()) {
                        context.startActivity(intent)
                    } else if (excluded.size == handlers.size) {
                        navRouter.navigateTo(AppNavKey.Settings.HiddenApps)
                    } else {
                        context.startActivity(Intent.createChooser(intent, null).apply {
                            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excluded.toTypedArray())
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                }
            } catch (error: Exception) {
                Log.e("HomeApp", "Unable to open home screen app", error)
            }
        }
    }

    val widgetOffset = widgetSettingsRepository.widgetOffset
    val widgetHeight = widgetSettingsRepository.widgetHeight
    val widgetWidth = widgetSettingsRepository.widgetWidth
    val widgetId = widgetSettingsRepository.widgetId

    val homeAlignment = appearanceRepository.homeAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }

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

    // Bottom Sheet Actions
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
                    _showBottomSheet.value = false
                    viewModelScope.launch {
                        if (tryOpenAppUseCase(clickedApp.packageName, bypassChallenge = true) ==
                            TryOpenAppResult.RequireHiddenAppsUnlock) {
                            navRouter.navigateTo(AppNavKey.Settings.HiddenApps)
                        } else {
                            startShortcutUseCase(clickedApp.packageName, shortcut.id)
                            navRouter.navigateTo(AppNavKey.Home, true)
                        }
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
