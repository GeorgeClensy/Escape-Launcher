package com.geecee.escapelauncher

import android.app.Application
import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.DefaultSettings
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    private val launcherBehaviorRepository: LauncherBehaviorRepository,
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

    // The PagerState's page count lambda runs inside composition's measure,
    // so it must read snapshot-observable state: a StateFlow is not
    // snapshot-observable, and reading its value in the lambda never
    // invalidates the pager's measure, so toggling the hide setting would
    // leave the pager measuring against the stale count — the first swipe
    // after hiding stops dead via the measure coercion. This snapshot starts
    // with the same default the flow starts with and is refreshed by the
    // collector below before it relocates, so the two always agree
    private var hideScreenTimePageSnapshot by mutableStateOf(false)
    val screenLayout = launcherBehaviorRepository.screenLayout.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DefaultSettings.SCREEN_LAYOUT
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

    var currentSelectedApp = mutableStateOf(InstalledApp("", "", android.content.ComponentName("", "")))

    var showOpenChallenge = mutableStateOf(false)

    val interactionSource = MutableInteractionSource()

    val pagerState = PagerState(
        currentPage = MainPagerLayout.HOME_PAGE_INDEX,
        currentPageOffsetFraction = 0f
    ) {
        // The snapshot mirror rather than the flow: a measure-side read of
        // the flow value would never be invalidated, so hiding or showing
        // the dashboard would not resize the pager until an unrelated
        // measure happened to run
        MainPagerLayout.pageCount(hideScreenTimePageSnapshot)
    }

    suspend fun goToMainPage() {
        pagerState.scrollToPage(MainPagerLayout.mainPageIndex(screenLayout.value, hideScreenTimePage.value))
    }

    suspend fun animatedGoToMainPage() {
        val targetPage = MainPagerLayout.mainPageIndex(screenLayout.value, hideScreenTimePage.value)

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
                .collect { hide ->
                    // Refresh the snapshot the pager's page count reads
                    // before anything below relocates, so the relocation
                    // (and the pager's next measure coercion) already sees
                    // the new count
                    hideScreenTimePageSnapshot = hide
                    if (screenLayout.value == DefaultSettings.SCREEN_LAYOUT_HORIZONTAL) {
                        // The horizontal layout's page indices shift with this
                        // setting, so always return to the main page like the
                        // original behaviour did
                        scrollToPageBestEffort(MainPagerLayout.horizontalHomePageIndex(hide))
                    } else if (MainPagerLayout.isPageBeingRemoved(
                            currentPage = pagerState.currentPage,
                            targetPage = pagerState.targetPage,
                            hideScreenTimePage = hide
                        )
                    ) {
                        // The vertical layout's page indices are stable across
                        // this setting, so only relocate the user when the page
                        // they are on (or heading towards mid-scroll) is removed.
                        // This intentionally deviates from the original
                        // behaviour, which snapped home on every toggle because
                        // its page indices shifted with it
                        scrollToPageBestEffort(MainPagerLayout.HOME_PAGE_INDEX)
                    }
                }
        }

        viewModelScope.launch {
            // A StateFlow already emits distinct values, so this needs no
            // distinctUntilChanged
            screenLayout
                .collect { layout ->
                    // The page indices differ between layouts — and in the
                    // horizontal layout they shift with the hide setting — so
                    // return to the main page whenever the layout changes
                    scrollToPageBestEffort(MainPagerLayout.mainPageIndex(layout, hideScreenTimePage.value))
                }
        }
    }

    /**
     * A jump to [page] is best-effort in the pager's MutatorMutex terms: a
     * mutation whose priority equals or exceeds the ongoing one's cancels
     * it, and a lower-priority caller is rejected outright, before its
     * block ever runs. User drags are UserInput-priority sessions — the
     * pager's own gesture machinery and the dismiss handler's
     * channel-drained drags alike — so they outrank these Default-priority
     * jumps: a jump attempted while the user is dragging is rejected, and a
     * drag started while a jump animates cancels the jump. Nothing
     * coexists silently with a live drag (the pre-redesign raw-delta
     * dismissals bypassed the mutex entirely and did). A fling in flight
     * is a Default-priority mutation too, so the newer jump cuts it off.
     * Swallow the rejection so the calling collector keeps serving future
     * emissions — the pager's measure coercion still lands the user on a
     * page that exists. Real cancellation of this scope propagates
     */
    private suspend fun scrollToPageBestEffort(page: Int) {
        try {
            pagerState.scrollToPage(page)
        } catch (cancellation: CancellationException) {
            if (!currentCoroutineContext().isActive) throw cancellation
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
            // The delayed snap fires blindly into whatever the user is
            // doing half a second after the launch, so guard it at fire
            // time: skip when a gesture is in flight (a jump into a live
            // drag session would be rejected by the pager's MutatorMutex
            // anyway — the user's hands win) and when the user is already
            // on the main page
            if (PagerScrollPolicy.shouldSnapToMainPageAfterAppLaunch(
                    currentPage = pagerState.currentPage,
                    mainPage = MainPagerLayout.mainPageIndex(screenLayout.value, hideScreenTimePage.value),
                    isScrollInProgress = pagerState.isScrollInProgress
                )
            ) {
                goToMainPage()
            }
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
