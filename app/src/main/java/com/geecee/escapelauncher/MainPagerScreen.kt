package com.geecee.escapelauncher

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Main composable for home screen:
 * contains a pager with all the pages inside of it, contains bottom sheet, contains open challenge UI
 *
 * The pager follows the screen layout setting (see [MainPagerLayout]): the
 * vertical layout (the default) stacks the home page on top, the apps list
 * directly below it (opened with a swipe up) and the optional screen time
 * dashboard below that; the horizontal layout is the original screen order
 * with the dashboard, home page and apps list side by side, opened with
 * sideways swipes.
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
    val screenLayout by viewModel.screenLayout.collectAsState(initial = DefaultSettings.SCREEN_LAYOUT)
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

    // A downward swipe on a vertically scrolling page dismisses it towards
    // the page above: the pager takes the swipe before the page's own
    // scrollable can eat it scrolling its content back towards its top, so
    // returning is always one short swipe from any scroll position. The
    // horizontal layout keeps the original behaviour instead and attaches
    // neither connection
    val appsListDismissConnection = rememberPageDismissConnection(
        viewModel = viewModel,
        coroutineScope = coroutineScope,
        targetPage = { MainPagerLayout.HOME_PAGE_INDEX }
    )
    val dashboardDismissConnection = rememberPageDismissConnection(
        viewModel = viewModel,
        coroutineScope = coroutineScope,
        targetPage = { MainPagerLayout.APPS_LIST_PAGE_INDEX }
    )

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

    // Some apps list stuff
    // Settled page rather than current page: an animated scroll home from the
    // dashboard below sweeps past the apps list page, and with search auto
    // open enabled, reacting to the transient current page would flash the
    // drawer's search open mid-flight. Cleanup therefore runs when a drag
    // settles rather than halfway through it, so a dismissed drawer keeps its
    // search expanded until the release finishes it.
    // The apps list settles on a different page per layout — and in the
    // horizontal layout it shifts with the hide setting — so this has to
    // check the layout's own index
    val horizontalAppsListPageIndex = MainPagerLayout.horizontalAppsListPageIndex(hideScreenTimePage)
    val appsListPageIndex = if (screenLayout == DefaultSettings.SCREEN_LAYOUT_HORIZONTAL) {
        horizontalAppsListPageIndex
    } else {
        MainPagerLayout.APPS_LIST_PAGE_INDEX
    }
    val isAppsListSettled = viewModel.pagerState.settledPage == appsListPageIndex
    val autoOpenSearch by appsListViewModel.searchAutoOpen.collectAsState(initial = DefaultSettings.SEARCH_AUTO_OPEN)

    // Tidy up apps list when it stops being the settled page
    LaunchedEffect(isAppsListSettled) {
        if (!isAppsListSettled) {
            appsListViewModel.onSearchExpandedChanged(false)
            appslistScrollState.scrollToItem(0)
            viewModel.setBottomSheetVisible(false)
        }
    }

    // Auto open search the moment the pager is heading for the apps page
    // rather than after it settles: the settle animation is most of the wait,
    // and the keyboard riding in alongside the drawer hides the rest. The
    // target page flips early in the opening drag and never equals the apps
    // page while sweeping past it on the way home from the dashboard, and an
    // abandoned drag retracts here through the settled-page cleanup above
    val isHeadingToAppsList = viewModel.pagerState.targetPage == appsListPageIndex
    LaunchedEffect(isHeadingToAppsList, autoOpenSearch) {
        if (isHeadingToAppsList && autoOpenSearch) {
            appsListViewModel.onSearchExpandedChanged(true)
        }
    }

    // The three pages' content, built once so both layouts share it: the
    // scrolling pages take their modifier as a parameter because only the
    // vertical layout attaches the dismiss connections to them
    val home: @Composable () -> Unit = {
        HomeScreen(
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
    }
    val appsList: @Composable (Modifier) -> Unit = { modifier ->
        AppsListTabScreen(
            modifier = modifier,
            scrollState = appslistScrollState,
            appsListViewModel = appsListViewModel,
            appsListTabs = appsListTabs,
            handleAppClick = handleAppClick,
            handleAppLongClick = handleAppLongClick,
            hapticFeedbackEnabled = hapticFeedbackEnabled
        )
    }
    val dashboard: @Composable (Modifier) -> Unit = { modifier ->
        ScreenTimeDashboard(modifier = modifier)
    }

    // The long-press settings and double-tap lock gestures belong to the
    // pager itself in both layouts
    val pagerModifier = Modifier
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

    if (screenLayout == DefaultSettings.SCREEN_LAYOUT_HORIZONTAL) {
        // Original behaviour: the pages keep the default overscroll effect and
        // their own scrolling in both directions, with no dismiss connections
        HorizontalPager(
            state = viewModel.pagerState,
            modifier = pagerModifier
        ) { page ->
            LauncherPage(
                page = page,
                homePageIndex = MainPagerLayout.horizontalHomePageIndex(hideScreenTimePage),
                appsListPageIndex = horizontalAppsListPageIndex,
                screenTimePageIndex = MainPagerLayout.horizontalScreenTimePageIndex(hideScreenTimePage),
                appsListModifier = Modifier,
                screenTimeModifier = Modifier,
                home = home,
                appsList = appsList,
                dashboard = dashboard
            )
        }
    } else {
        // No overscroll effect: the home page does not scroll, so a swipe down on
        // it is a drag the pager cannot scroll — with the default effect that
        // drag would be consumed by a top-edge stretch, and the home page's
        // notification shade observer never sees it. The axis change also
        // removed the old pager's horizontal edge stretch, so this keeps both
        // directions free of new artifacts
        VerticalPager(
            state = viewModel.pagerState,
            overscrollEffect = null,
            modifier = pagerModifier
        ) { page ->
            LauncherPage(
                page = page,
                homePageIndex = MainPagerLayout.HOME_PAGE_INDEX,
                appsListPageIndex = MainPagerLayout.APPS_LIST_PAGE_INDEX,
                screenTimePageIndex = MainPagerLayout.screenTimePageIndex(hideScreenTimePage),
                appsListModifier = Modifier.nestedScroll(appsListDismissConnection),
                screenTimeModifier = Modifier.nestedScroll(dashboardDismissConnection),
                home = home,
                appsList = appsList,
                dashboard = dashboard
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

/**
 * The content of one page of the launcher's main pager, shared by both
 * layouts: the vertical layout attaches its dismiss connections through
 * [appsListModifier] and [screenTimeModifier] while the horizontal layout
 * passes plain modifiers and keeps the pages' own scrolling.
 *
 * The when needs no else: the hidden dashboard reports its index as -1, which
 * never matches a page the pager composes, and the home and apps list
 * indices are distinct from each other in both layouts.
 */
@Composable
private fun LauncherPage(
    page: Int,
    homePageIndex: Int,
    appsListPageIndex: Int,
    screenTimePageIndex: Int,
    appsListModifier: Modifier,
    screenTimeModifier: Modifier,
    home: @Composable () -> Unit,
    appsList: @Composable (Modifier) -> Unit,
    dashboard: @Composable (Modifier) -> Unit
) {
    when (page) {
        screenTimePageIndex -> dashboard(screenTimeModifier)
        homePageIndex -> home()
        appsListPageIndex -> appsList(appsListModifier)
    }
}

/**
 * A nested scroll connection that dismisses the pager's current page towards
 * [targetPage] on a downward swipe, from any scroll position of the page's
 * own scrolling content.
 *
 * Nested scroll deltas and fling velocities follow the pointer's screen
 * convention, so a downward drag or fling arrives as positive y even though
 * it scrolls the content backwards. The pager is fed in onPreScroll because
 * a swipe started mid-content would otherwise be consumed by the content
 * scrolling itself back towards its top before the pager ever sees it.
 */
@Composable
private fun rememberPageDismissConnection(
    viewModel: MainPagerScreenViewModel,
    coroutineScope: CoroutineScope,
    targetPage: () -> Int
): NestedScrollConnection = remember {
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Only a downward user drag dismisses: an upward drag browses the
            // page's own content and must reach it untouched
            if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
            val pagerState = viewModel.pagerState
            // Once the pager is displaced its own nested scroll connection
            // takes the drag over in pre-scroll (it sits above this one, so
            // it has already run by now); consuming here as well would
            // double-drive the pager
            if (pagerState.currentPageOffsetFraction != 0f) return Offset.Zero
            // dispatchRawDelta takes the scroll-position convention: the
            // negated downward delta scrolls the pager backwards, towards
            // the target page
            val consumedByPager = pagerState.dispatchRawDelta(-available.y)
            // The pager reports what it consumed in its argument's sign
            // (negative here); flip it into available's sign and never claim
            // more than available, or the page would keep scrolling on top
            // of the dismissal
            return Offset(0f, (-consumedByPager).coerceIn(0f, available.y))
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            // An upward release fling belongs to the page's own content
            if (available.y <= 0f) return Velocity.Zero
            val pagerState = viewModel.pagerState
            val target = targetPage()
            // A downward fling scrolls the pager backwards, so it can only
            // carry the user to the target page while the pager sits at or
            // beyond it. A real dismissal also leaves the pager displaced —
            // its drag phase was fed above — so requiring a displacement
            // keeps the release of an upward browsing drag, whose velocity
            // dips downward as the finger lifts, from closing the drawer
            val dismissing = pagerState.currentPage >= target &&
                pagerState.currentPageOffsetFraction != 0f
            if (!dismissing) return Velocity.Zero
            // The drag displaced the pager through raw deltas, so no fling of
            // the page's own will settle it: finish the dismissal with the
            // pager's own animation and eat the whole downward velocity so
            // the page does not fling backwards too
            coroutineScope.launch {
                pagerState.animateScrollToPage(target)
            }
            return available
        }
    }
}
