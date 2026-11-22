package com.geecee.escapelauncher

import android.os.Build
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
    // The dashboard's scroll position is hoisted so the pager host keeps it
    // alive across page recompositions — the dashboard page itself only
    // exists while composed
    val dashboardScrollState = rememberScrollState()
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
    // Settled page rather than current page: an animated scroll home from
    // the dashboard below sweeps past the apps list page, and with search
    // auto open enabled, reacting to the transient current page would
    // flash the drawer's search open mid-flight. Cleanup therefore runs
    // when a scroll settles rather than halfway through it — and since
    // the dismiss gesture now drives the pager through real scroll
    // sessions (see [rememberPagerDismissConnection]), the settled page
    // stays frozen for every drag kind, so a dismissed drawer keeps its
    // search expanded until the release's settle finishes it.
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
    // rather than after it settles: the settle animation is most of the
    // wait, and the keyboard riding in alongside the drawer hides the
    // rest. The target page flips early in the opening drag — every drag
    // is a real scroll session now (see [rememberPagerDismissConnection]),
    // so the target page derived state reacts to the position threshold
    // while the drag is in flight, dismiss drags included — and it never
    // equals the apps page while an animated scroll sweeps past it on the
    // way home from the dashboard, because programmatic scrolls pin their
    // own target. An abandoned drag retracts here through the settled-page
    // cleanup above
    val isHeadingToAppsList = viewModel.pagerState.targetPage == appsListPageIndex
    LaunchedEffect(isHeadingToAppsList, autoOpenSearch) {
        if (isHeadingToAppsList && autoOpenSearch) {
            appsListViewModel.onSearchExpandedChanged(true)
        }
    }

    // The three pages' content, built once so both layouts share it: the
    // scrolling pages take their modifier as a parameter because only the
    // vertical layout builds and attaches the dismiss connections to them
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
        ScreenTimeDashboard(modifier = modifier, scrollState = dashboardScrollState)
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
        // built or attached here — the pages cannot dismiss, so constructing
        // the handlers would only be dead state no gesture could reach
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
        // The dismiss mechanism lives here only. The system's own view
        // configuration supplies the gesture constants: the touch slop
        // the takeover must first exceed, and the minimum fling velocity
        // below which a release settles by position alone
        val viewConfiguration = remember(context) { ViewConfiguration.get(context) }
        val touchSlopPx = viewConfiguration.scaledTouchSlop.toFloat()
        val flingVelocityThreshold = viewConfiguration.scaledMinimumFlingVelocity.toFloat()
        // A downward swipe on a vertically scrolling page dismisses it
        // towards the page above: the dismiss handler takes the swipe in
        // pre-scroll, before the page's own scrollable can eat it
        // scrolling its content back towards its top, so returning is
        // always one short swipe from any scroll position. The takeover
        // only happens once the gesture has carried more than touch slop
        // downward, and from there the drag drives the pager through a
        // real UserInput scroll session — a channel-drained scrollBy loop
        // — so a fresh grab cancels a settle in flight, the settled page
        // freezes for the drag's duration, the target page flips on the
        // position threshold like in a native drag, and the release
        // settles by position and velocity together: a dismissward flick
        // carries to the target page while the drag has not already swept
        // past it, and a slower release lands on the nearest one (see
        // [rememberPagerDismissConnection] for the session machinery)
        val appsListDismissConnection = rememberPagerDismissConnection(
            pagerState = viewModel.pagerState,
            coroutineScope = coroutineScope,
            targetPage = { MainPagerLayout.HOME_PAGE_INDEX },
            touchSlopPx = touchSlopPx,
            flingVelocityThreshold = flingVelocityThreshold
        )
        val dashboardDismissConnection = rememberPagerDismissConnection(
            pagerState = viewModel.pagerState,
            coroutineScope = coroutineScope,
            targetPage = { MainPagerLayout.APPS_LIST_PAGE_INDEX },
            touchSlopPx = touchSlopPx,
            flingVelocityThreshold = flingVelocityThreshold
        )
        // No overscroll effect on the pager: its job is keeping a downward
        // drag on the home page unconsumed for the home page's shade
        // observer. The home page does not scroll, so that drag is one the
        // pager cannot scroll — with a stretch effect in place the pager
        // would hand the drag's leftover to the stretch, consuming the
        // pointer events, and the home page's observer (which only counts
        // movement nothing else consumed) would never see them, so the
        // shade would never open. The scrolling pages keep their own
        // inner-list stretch effects, which do not get in the dismissal's
        // way: in the nested scroll phase order ancestors consume a delta
        // in pre-scroll before any overscroll sees the leftover, so the
        // dismiss connections still take the swipe first from any scroll
        // position. Dismissal itself does not lean on this null either —
        // it runs through real scroll sessions now. The axis change also
        // removed the old pager's horizontal edge stretch, so this keeps
        // both directions free of new artifacts
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
 * layouts: the vertical layout builds and attaches its dismiss connections
 * through [appsListModifier] and [screenTimeModifier] while the horizontal
 * layout passes plain modifiers and keeps the pages' own scrolling.
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

