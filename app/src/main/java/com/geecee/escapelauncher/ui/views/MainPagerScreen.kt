package com.geecee.escapelauncher.ui.views

import android.os.Build
import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.HiddenAppsViewModel
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.common.goToAppInfo
import com.geecee.escapelauncher.core.common.uninstallApp
import com.geecee.escapelauncher.core.ui.composables.AppAction
import com.geecee.escapelauncher.core.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.utils.AppShortcut
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.EscapeAccessibilityService
import com.geecee.escapelauncher.utils.getAppShortcuts
import com.geecee.escapelauncher.core.ui.composables.OpenChallenge
import com.geecee.escapelauncher.utils.startShortcut
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel
import com.geecee.escapelauncher.MainPagerScreenViewModel
import com.geecee.escapelauncher.OpenChallengeViewModel

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
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    viewModel: MainPagerScreenViewModel = hiltViewModel(),
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel(),
    openChallengeViewModel: OpenChallengeViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    onOpenSettings: () -> Unit
) {
    val hideScreenTimePage by viewModel.hideScreenTimePage.collectAsState(initial = false)
    val autoOpenAppsInSearch by viewModel.automaticallyOpenAppsInSearch.collectAsState(initial = false)
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsState(initial = false)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val appsListPage = if (hideScreenTimePage) 1 else 2
    val coroutineScope = rememberCoroutineScope()

    // Control if the user can go back or not depending upon the page
    BackHandler(enabled = true) {
        coroutineScope.launch {
            homeScreenModel.animatedGoToMainPage()
        }
    }

    // Add effect to hide keyboard on page change or open search if needed
    LaunchedEffect(homeScreenModel.pagerState.currentPage) {
        if (homeScreenModel.pagerState.currentPage != appsListPage) {
            homeScreenModel.searchText.value = ""
            homeScreenModel.searchExpanded.value = false

            focusManager.clearFocus()
            keyboardController?.hide()
        } else {
            // If we are on the apps list page and auto search is enabled, open it
            if (autoOpenAppsInSearch) {
                homeScreenModel.searchExpanded.value = true
            }
        }
    }

    // Home Screen Pages
    HorizontalPager(
        state = homeScreenModel.pagerState,
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {}, onLongClickLabel = "",
                onLongClick = {
                    onOpenSettings()
                    viewModel.setFirstTimeHelp(false)
                },
                indication = null, interactionSource = homeScreenModel.interactionSource,
                onDoubleClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val context = mainAppModel.getContext()
                        if (doubleTapToLock) {
                            val service = EscapeAccessibilityService.instance
                            if (service != null) {
                                service.lockScreen()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.accessibility_not_granted_msg),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            )
    ) { page ->
        if (hideScreenTimePage) {
            when (page) {
                0 -> HomeScreen(
                    mainAppModel = mainAppModel,
                    homeScreenModel = homeScreenModel
                )

                1 -> AppsList(
                    mainAppModel = mainAppModel,
                    homeScreenModel = homeScreenModel
                )
            }
        } else {
            when (page) {
                0 -> ScreenTimeDashboard()

                1 -> HomeScreen(
                    mainAppModel = mainAppModel,
                    homeScreenModel = homeScreenModel
                )

                2 -> AppsList(
                    mainAppModel = mainAppModel,
                    homeScreenModel = homeScreenModel
                )
            }
        }
    }

    //Bottom Sheet
    if (homeScreenModel.showBottomSheet.value) {
        val selectedApp = homeScreenModel.currentSelectedApp.value
        val context = mainAppModel.getContext()


        val shortcuts: List<AppShortcut?> =
            if (homeScreenModel.currentSelectedApp.value.user == android.os.Process.myUserHandle()) {
                getAppShortcuts(context, selectedApp.packageName)
            } else {
                listOf(null)
            }
        val shortcutActions: List<AppAction?> = shortcuts.map { shortcut ->
            if (shortcut != null) {
                AppAction(
                    label = shortcut.label,
                    onClick = {
                        startShortcut(context, selectedApp.packageName, shortcut.id)
                        homeScreenModel.showBottomSheet.value = false
                        resetHome(homeScreenModel, false)
                    }
                )
            }
            else {
                null
            }
        }

        var actions = listOf(
            AppAction(
                label = stringResource(id = R.string.uninstall),
                onClick = {
                    uninstallApp(context, selectedApp)
                }
            ),
            if (homeScreenModel.currentSelectedApp.value.user == android.os.Process.myUserHandle()) {
                AppAction(
                    label = stringResource(if (homeScreenModel.isCurrentAppFavorite) R.string.rem_from_fav else R.string.add_to_fav),
                    onClick = {
                        val selectedApp = homeScreenModel.currentSelectedApp.value
                        homeScreenModel.coroutineScope.launch {
                            if (homeScreenModel.isCurrentAppFavorite) {
                                mainAppModel.modifiedAppsRepository.removeFavourite(selectedApp.packageName)
                            } else {
                                mainAppModel.modifiedAppsRepository.addFavourite(selectedApp.packageName)
                                homeScreenModel.goToMainPage()
                            }
                        }
                        homeScreenModel.showBottomSheet.value = false
                    }
                )
            } else {
                null
            },
            if (homeScreenModel.currentSelectedApp.value.user == android.os.Process.myUserHandle()) {
                AppAction(
                    label = stringResource(R.string.hide),
                    onClick = {
                        hiddenAppsViewModel.hideApp(homeScreenModel.currentSelectedApp.value.packageName)
                        homeScreenModel.showBottomSheet.value = false
                        resetHome(homeScreenModel, false)
                    }
                )
            } else {
                null
            },
            AppAction(
                label = stringResource(id = R.string.app_info),
                onClick = {
                    goToAppInfo(mainAppModel.getContext(), homeScreenModel.currentSelectedApp.value)
                    resetHome(homeScreenModel, false)
                }
            )
        )

        val challengeAppIds by openChallengeViewModel.challengeAppIds.collectAsState()
        val hasChallenge = challengeAppIds.contains(homeScreenModel.currentSelectedApp.value.packageName)

        if (!hasChallenge && homeScreenModel.currentSelectedApp.value.user == android.os.Process.myUserHandle()) {
            actions = actions +
                    AppAction(
                        label = stringResource(R.string.add_open_challenge),
                        onClick = {
                            openChallengeViewModel.addChallengeToApp(
                                homeScreenModel.currentSelectedApp.value.packageName
                            )
                            homeScreenModel.showBottomSheet.value = false
                        }
                    )
        }


        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedApp.value.displayName,
            actions = actions.filterNotNull(),
            onDismissRequest = { homeScreenModel.showBottomSheet.value = false },
            shortcutActions = shortcutActions.filterNotNull(),
            sheetState = rememberModalBottomSheetState()
        )
    }

    //Open Challenge
    AnimatedVisibility(
        visible = homeScreenModel.showOpenChallenge.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        OpenChallenge(
            haptics = LocalHapticFeedback.current,
            openApp = {
                homeScreenModel.openApp(
                    app = homeScreenModel.currentSelectedApp.value,
                    overrideChallenge = true,
                    onAppOpened = { screenTimeViewModel.onAppOpened(it) }
                )
                homeScreenModel.coroutineScope.launch {
                    delay(1000)
                    homeScreenModel.showOpenChallenge.value = false
                }
            },
            goBack = {
                homeScreenModel.showOpenChallenge.value = false
            })
    }
}
