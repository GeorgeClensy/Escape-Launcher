package com.geecee.escapelauncher

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.ui.composables.OpenChallenge
import com.geecee.escapelauncher.core.ui.composables.TabbedScreen
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.appslist.AppsList
import com.geecee.escapelauncher.feature.appslist.AppsListViewModel
import com.geecee.escapelauncher.feature.homescreen.HomeScreen
import com.geecee.escapelauncher.feature.screentime.ScreenTimeDashboard
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.feature.securefolder.SecureFolderButton
import com.geecee.escapelauncher.feature.securefolder.canUseSecureFolder
import com.geecee.escapelauncher.feature.workapps.WorkApps
import com.geecee.escapelauncher.privatespace.PrivateSpace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 *  Main composable for home screen:
 *  contains a pager with all the pages inside of it, contains bottom sheet, contains open challenge UI
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class
)
@Composable
fun MainPagerScreen(
    viewModel: MainPagerScreenViewModel = hiltViewModel(),
    globalViewModel: GlobalViewModel = hiltViewModel(),
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    onOpenSettings: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val hideScreenTimePage by viewModel.hideScreenTimePage.collectAsState()
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsState(initial = DefaultSettings.DOUBLE_TAP_TO_LOCK)
    val hapticFeedbackEnabled by viewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Make it that if you go back you go back to main page
    BackHandler(enabled = true) {
        coroutineScope.launch {
            viewModel.animatedGoToMainPage()
        }
    }

    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

    val appsListTabs = listOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canUseSecureFolder(context = context)) {
            TabbedScreen(
                title = "Secure Folder", icon = Icons.Default.Lock, content = {
                    SecureFolderButton()
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
                        modifier = Modifier
                            .fillParentMaxSize()
                            .fillMaxSize(),
                        onAppClick = { app ->
                            viewModel.openApp(
                                app = app, overrideChallenge = false, onAppOpened = {
                                    screenTimeViewModel.onAppOpened(it)
                                    appsListViewModel.onSearchExpandedChanged(false)
                                    doHapticFeedBack(haptics, hapticFeedbackEnabled)
                                })
                        },
                        onAppLongClick = { app ->
                            appsListViewModel.setBottomSheetVisible(true)
                            appsListViewModel.setBottomSheetApp(app)
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        })

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
                    WorkApps(modifier = Modifier, onAppClick = { app ->
                        viewModel.openApp(
                            app = app, overrideChallenge = false, onAppOpened = {
                                screenTimeViewModel.onAppOpened(it)
                                appsListViewModel.onSearchExpandedChanged(false)
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            })
                    }, onAppLongClick = { app ->
                        appsListViewModel.setBottomSheetVisible(true)
                        appsListViewModel.setBottomSheetApp(app)
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    })

                })
        } else {
            null
        }
    )

    // Home Screen Pages
    HorizontalPager(
        state = viewModel.pagerState,
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {},
                onLongClickLabel = "",
                onLongClick = {
                    onOpenSettings()
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
        val screenTimePageIndex = if (!hideScreenTimePage) 0 else -1
        val homePageIndex = if (hideScreenTimePage) 0 else 1
        val appsListPageIndex = if (hideScreenTimePage) 1 else 2

        when (page) {
            screenTimePageIndex -> ScreenTimeDashboard()

            homePageIndex -> HomeScreen(onAppOpened = { app ->
                viewModel.openApp(
                    app = app, overrideChallenge = false, onAppOpened = {
                        screenTimeViewModel.onAppOpened(it)
                    })
            }, onGoHomeRequest = { globalViewModel.requestToGoHome() })

            appsListPageIndex -> AppsList(
                appsListViewModel = appsListViewModel,
                scrollState = viewModel.appsListScrollState,
                isBeingShown = viewModel.pagerState.currentPage == appsListPageIndex,
                onGoHomeRequest = {
                    globalViewModel.requestToGoHome()
                },
                onAppOpened = { app ->
                    viewModel.openApp(
                        app = app, overrideChallenge = false, onAppOpened = {
                            screenTimeViewModel.onAppOpened(it)
                        })
                },
                tabs = appsListTabs.filterNotNull()
            )
        }
    }

    //Open Challenge
    AnimatedVisibility(
        visible = viewModel.showOpenChallenge.value, enter = fadeIn(), exit = fadeOut()
    ) {
        OpenChallenge(
            haptics = LocalHapticFeedback.current,
            enabled = hapticFeedbackEnabled,
            openApp = {
                viewModel.openApp(
                    app = viewModel.currentSelectedApp.value,
                    overrideChallenge = true,
                    onAppOpened = {
                        screenTimeViewModel.onAppOpened(it)
                    })
                coroutineScope.launch {
                    delay(1000.milliseconds)
                    viewModel.showOpenChallenge.value = false
                }
            },
            goBack = {
                viewModel.showOpenChallenge.value = false
            })
    }
}
