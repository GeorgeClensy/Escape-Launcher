package com.geecee.escapelauncher

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.composables.TabbedScreen
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.appslist.AppsListViewModel
import com.geecee.escapelauncher.feature.homescreen.HomeScreen
import com.geecee.escapelauncher.feature.screentime.ScreenTimeDashboard
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.feature.securefolder.SecureFolderButton
import com.geecee.escapelauncher.feature.securefolder.canUseSecureFolder
import com.geecee.escapelauncher.screens.ParentBottomSheetOverlay
import com.geecee.escapelauncher.feature.workapps.WorkApps
import com.geecee.escapelauncher.privatespace.PrivateSpace
import com.geecee.escapelauncher.screens.AppsListTabScreen
import com.geecee.escapelauncher.screens.HomeWidgetSlot
import com.geecee.escapelauncher.screens.OpenChallengeOverlay
import com.geecee.escapelauncher.screens.HomeWeatherImpl
import kotlinx.coroutines.launch

/**
 * Main composable for home screen:
 * contains a vertical pager with all the pages inside of it, contains bottom sheet, contains open challenge UI
 *
 * The pages stack top to bottom as described by [MainPagerLayout]: home on top,
 * the apps list directly below it (opened with a swipe up) and the optional
 * screen time dashboard below that.
 */
@Composable
fun MainPagerScreen(
    viewModel: MainPagerScreenViewModel = hiltViewModel(),
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val appslistScrollState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val hideScreenTimePage by viewModel.hideScreenTimePage.collectAsState()
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsState(initial = DefaultSettings.DOUBLE_TAP_TO_LOCK)
    val hapticFeedbackEnabled by viewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val homeAlignment by viewModel.homeAlignment.collectAsState(initial = DefaultSettingsUi.HOME_ALIGNMENT)
    val showWeather by viewModel.showWeather.collectAsState(initial = DefaultSettings.SHOW_WEATHER)
    val widgetHeight by viewModel.widgetHeight.collectAsState(initial = DefaultSettings.WIDGET_HEIGHT)
    val widgetWidth by viewModel.widgetWidth.collectAsState(initial = DefaultSettings.WIDGET_WIDTH)
    val widgetId by viewModel.widgetId.collectAsState(initial = DefaultSettings.WIDGET_ID)
    val widgetHostManager = viewModel.widgetHostManager
    val widgetOffsetPref by viewModel.widgetOffset.collectAsState(initial = DefaultSettings.WIDGET_OFFSET)
    val showWallpaper by viewModel.showWallpaper.collectAsState(initial = false)

    // Global action callbacks
    val handleAppClick: (InstalledApp) -> Unit = { app ->
        viewModel.openApp(
            app = app,
            overrideChallenge = false,
            onAppOpened = {
                screenTimeViewModel.onAppOpened(it)
                appsListViewModel.onSearchExpandedChanged(false)
                doHapticFeedBack(haptics, hapticFeedbackEnabled)
            }
        )
    }
    val handleAppLongClick: (InstalledApp) -> Unit = { app ->
        viewModel.setBottomSheetApp(app)
        viewModel.setBottomSheetVisible(true)
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    // Make it that if you go back you go back to main page
    BackHandler(enabled = true) {
        coroutineScope.launch {
            viewModel.animatedGoToMainPage()
        }
    }

    // A downward drag or fling on the apps list closes the drawer from any
    // scroll position: the pager takes it before the list can scroll back
    // towards its top, so going back home is always one short swipe down
    val appsListDismissConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero

                // Feed the downward drag straight to the pager so the drawer
                // starts dismissing immediately. Once the pager is displaced
                // its own nested scroll connection takes the gesture over
                val consumed = viewModel.pagerState.dispatchRawDelta(-available.y)
                return Offset(0f, consumed)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val dismissing = viewModel.pagerState.currentPage == MainPagerLayout.APPS_LIST_PAGE_INDEX ||
                    viewModel.pagerState.currentPageOffsetFraction != 0f

                if (available.y > 0f && dismissing) {
                    // The user let go of a dismiss: finish it with the pager's
                    // own animation and keep the list from flinging backwards
                    coroutineScope.launch {
                        viewModel.pagerState.animateScrollToPage(MainPagerLayout.HOME_PAGE_INDEX)
                    }
                    return available
                }

                return Velocity.Zero
            }
        }
    }

    // Generate Apps List tabs
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()
    val appsListTabs = listOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canUseSecureFolder(context = context)) {
            TabbedScreen(
                title = "Secure Folder", icon = Icons.Default.Lock, content = {
                    SecureFolderButton(
                        modifier = Modifier.fillMaxSize()
                    )
                })
        } else {
            null
        },
        if (isDefaultLauncher && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && viewModel.managedProfileExists(
                ManagedProfileType.PrivateSpace
            )
        ) {
            TabbedScreen(
                title = "Private", icon = Icons.Default.Lock, content = {
                    PrivateSpace(
                        modifier = Modifier.fillMaxSize(),
                        onAppClick = { app -> handleAppClick(app) },
                        onAppLongClick = { app -> handleAppLongClick(app) }
                    )
                })
        } else {
            null
        },
        if (viewModel.isManagedProfileSupported(type = ManagedProfileType.WorkApps) && viewModel.managedProfileExists(
                type = ManagedProfileType.WorkApps
            )
        ) {
            TabbedScreen(
                title = "Work", icon = Icons.Default.Work, content = {
                    WorkApps(
                        modifier = Modifier.fillMaxSize(),
                        onAppClick = { app -> handleAppClick(app) },
                        onAppLongClick = { app -> handleAppLongClick(app) }
                    )
                })
        } else {
            null
        }
    )

    // Dashboard page index, or no page when hidden (home and apps list indices live in MainPagerLayout)
    val screenTimePageIndex = MainPagerLayout.screenTimePageIndex(hideScreenTimePage)

    // Some apps list stuff
    // Settled page rather than current page: an animated scroll home from the
    // dashboard below sweeps past the apps list page, and with search auto
    // open enabled, reacting to the transient current page would flash the
    // drawer's search open mid-flight. Cleanup and auto-open search therefore
    // also run when a drag settles rather than halfway through it, so a
    // dismissed drawer keeps its search expanded until the release finishes it.
    val isAppsListSettled = viewModel.pagerState.settledPage == MainPagerLayout.APPS_LIST_PAGE_INDEX
    val autoOpenSearch by appsListViewModel.searchAutoOpen.collectAsState(initial = DefaultSettings.SEARCH_AUTO_OPEN)

    // Tidy up apps list when it closes or opens
    LaunchedEffect(isAppsListSettled) {
        if (!isAppsListSettled) {
            appsListViewModel.onSearchExpandedChanged(false)
            appslistScrollState.scrollToItem(0)
            viewModel.setBottomSheetVisible(false)
        } else if (autoOpenSearch) {
            appsListViewModel.onSearchExpandedChanged(true)
        }
    }

    // Launcher pages
    // No overscroll effect: the home page does not scroll, so a swipe down on
    // it is a drag the pager cannot scroll — with the default effect that
    // drag would be consumed by a top-edge stretch, and the home page's
    // notification shade observer never sees it. The axis change also
    // removed the old pager's horizontal edge stretch, so this keeps both
    // directions free of new artifacts
    VerticalPager(
        state = viewModel.pagerState,
        overscrollEffect = null,
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {},
                onLongClickLabel = "",
                onLongClick = {
                    viewModel.openSettings()
                    viewModel.setFirstTimeHelp(false)
                },
                indication = null,
                interactionSource = viewModel.interactionSource,
                onDoubleClick = {
                    // Turn screen off
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (doubleTapToLock) {
                            viewModel.lockScreen()
                        }
                    }
                })
    ) { page ->
        when (page) {
            screenTimePageIndex -> ScreenTimeDashboard()

            MainPagerLayout.HOME_PAGE_INDEX -> HomeScreen(
                onAppClick = handleAppClick,
                onAppLongClick = handleAppLongClick,
                widget = {
                    HomeWidgetSlot(
                        homeAlignment = homeAlignment,
                        widgetOffsetPref = widgetOffsetPref,
                        widgetId = widgetId,
                        widgetHostManager = widgetHostManager,
                        widgetWidth = widgetWidth,
                        widgetHeight = widgetHeight
                    )
                },
                glanceWidgets = {
                    if (showWeather) {
                        if (!viewModel.isFoss) {
                            HomeWeatherImpl(
                                alignment = homeAlignment,
                                shadow = showWallpaper,
                                color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )

            MainPagerLayout.APPS_LIST_PAGE_INDEX -> AppsListTabScreen(
                modifier = Modifier.nestedScroll(appsListDismissConnection),
                scrollState = appslistScrollState,
                appsListViewModel = appsListViewModel,
                appsListTabs = appsListTabs,
                handleAppClick = handleAppClick,
                handleAppLongClick = handleAppLongClick,
                hapticFeedbackEnabled = hapticFeedbackEnabled
            )
        }
    }

    // Single Parent-level Bottom Sheet
    ParentBottomSheetOverlay(viewModel = viewModel)

    // Open Challenge
    OpenChallengeOverlay(
        viewModel = viewModel,
        screenTimeViewModel = screenTimeViewModel,
        hapticFeedbackEnabled = hapticFeedbackEnabled
    )
}
