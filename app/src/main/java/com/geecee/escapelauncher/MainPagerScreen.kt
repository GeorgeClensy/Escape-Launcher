package com.geecee.escapelauncher

import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.EscapeAccessibilityService
import com.geecee.escapelauncher.core.common.doesWorkProfileExist
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.OpenChallenge
import com.geecee.escapelauncher.feature.appslist.AppsList
import com.geecee.escapelauncher.feature.homescreen.HomeScreen
import com.geecee.escapelauncher.feature.screentime.ScreenTimeDashboard
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.feature.securefolder.SecureFolderButton
import com.geecee.escapelauncher.feature.securefolder.canUseSecureFolder
import com.geecee.escapelauncher.feature.workapps.WorkApps
import com.geecee.escapelauncher.feature.workapps.WorkAppsFab
import com.geecee.escapelauncher.privatespace.PrivateSpace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 *  Main composable for home screen:
 *  contains a pager with all the pages inside of it, contains bottom sheet, contains open challenge UI
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MainPagerScreen(
    viewModel: MainPagerScreenViewModel = hiltViewModel(),
    globalViewModel: GlobalViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    onOpenSettings: () -> Unit
) {
    val hideScreenTimePage by viewModel.hideScreenTimePage.collectAsState()
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsState(initial = false)
    val hapticFeedbackEnabled by viewModel.hapticFeedBackEnabled.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current

    // Make it that if you go back you go back to main page
    BackHandler(enabled = true) {
        coroutineScope.launch {
            viewModel.animatedGoToMainPage()
        }
    }

    // Home Screen Pages
    HorizontalPager(
        state = viewModel.pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
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
                            val service = EscapeAccessibilityService.instance
                            if (service != null) {
                                service.lockScreen()
                            } else {
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.accessibility_not_granted_msg),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            )
    ) { page ->
        val screenTimePageIndex = if (!hideScreenTimePage) 0 else -1
        val homePageIndex = if (hideScreenTimePage) 0 else 1
        val appsListPageIndex = if (hideScreenTimePage) 1 else 2

        when (page) {
            screenTimePageIndex -> ScreenTimeDashboard()

            homePageIndex -> HomeScreen(
                onAppOpened = { app ->
                    viewModel.openApp(
                        app = app,
                        overrideChallenge = false,
                        onAppOpened = {
                            screenTimeViewModel.onAppOpened(it)
                        }
                    )
                },
                onGoHomeRequest = { globalViewModel.requestToGoHome() }
            )

            appsListPageIndex -> AppsList(
                scrollState = viewModel.appsListScrollState,
                isBeingShown = viewModel.pagerState.currentPage == appsListPageIndex,
                onGoHomeRequest = {
                    globalViewModel.requestToGoHome()
                },
                onAppOpened = { app ->
                    viewModel.openApp(
                        app = app,
                        overrideChallenge = false,
                        onAppOpened = {
                            screenTimeViewModel.onAppOpened(it)
                        }
                    )
                },
                extraListItems = { onClick, onLongClick ->
                    //Secure Folder
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canUseSecureFolder(context = context)) {
                        item {
                            SecureFolderButton()
                        }
                    }
                    //Private Space
                    else if (isDefaultLauncher(context = context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && viewModel.managedProfileExists(
                            ManagedProfileType.PrivateSpace
                        )
                    ) {
                        item {
                            PrivateSpace(
                                modifier = Modifier,
                                onAppClick = onClick,
                                onAppLongClick = onLongClick
                            )
                        }
                    }
                },
                floatingContent = { onShowWorkApps ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && doesWorkProfileExist(
                            context = context
                        )
                    ) {
                        WorkAppsFab(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(vertical = 55.dp, horizontal = 30.dp)
                        ) {
                            onShowWorkApps()
                        }
                    }
                },
                workAppsContent = { onClick, onLongClick ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        WorkApps(
                            modifier = Modifier.align(Alignment.Center),
                            onAppClick = onClick,
                            onAppLongClick = onLongClick
                        )
                    }
                }
            )
        }
    }

    //Open Challenge
    AnimatedVisibility(
        visible = viewModel.showOpenChallenge.value,
        enter = fadeIn(),
        exit = fadeOut()
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
                    }
                )
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
