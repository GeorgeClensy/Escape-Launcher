package com.geecee.escapelauncher.ui.views

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.AppsListViewModel
import com.geecee.escapelauncher.HiddenAppsViewModel
import com.geecee.escapelauncher.OpenChallengeViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.common.doesPrivateSpaceExist
import com.geecee.escapelauncher.core.common.doesWorkProfileExist
import com.geecee.escapelauncher.core.common.goToAppInfo
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.openPrivateSpaceApp
import com.geecee.escapelauncher.core.common.openWorkApp
import com.geecee.escapelauncher.core.common.uninstallApp
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.theme.transparentHalf
import com.geecee.escapelauncher.core.ui.composables.AnimatedPillSearchBar
import com.geecee.escapelauncher.core.ui.composables.AppAction
import com.geecee.escapelauncher.core.ui.composables.AppsListHeader
import com.geecee.escapelauncher.core.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.composables.ListGradient
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.feature.securefolder.SecureFolderButton
import com.geecee.escapelauncher.feature.securefolder.canUseSecureFolder
import com.geecee.escapelauncher.feature.workapps.WorkApps
import com.geecee.escapelauncher.feature.workapps.WorkAppsFab
import com.geecee.escapelauncher.privatespace.PrivateSpace
import com.geecee.escapelauncher.utils.AppShortcut
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime
import com.geecee.escapelauncher.utils.getAppShortcuts
import com.geecee.escapelauncher.utils.startShortcut

/**
 * Parent apps list composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsList(
    scrollState: LazyListState,
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel(),
    openChallengeViewModel: OpenChallengeViewModel = hiltViewModel(),
    isBeingShown: Boolean,
    onAppOpened: (app: InstalledApp) -> Unit = {},
    onGoHomeRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appUsageList by screenTimeViewModel.appUsageList.collectAsState()
    val showScreenTimeApp by appsListViewModel.showScreenTimeApp.collectAsState(initial = false)
    val appsListAlignment by appsListViewModel.appsAlignment.collectAsState(initial = Alignment.CenterHorizontally)
    val hapticFeedbackEnabled by appsListViewModel.hapticFeedBackEnabled.collectAsState(initial = true)
    val showSearchBox by appsListViewModel.showSearchBox.collectAsState(initial = true)
    val bottomSearchBox by appsListViewModel.bottomSearch.collectAsState(initial = false)
    val autoOpenSearch by appsListViewModel.searchAutoOpen.collectAsState(initial = false)
    val autoOpenAppInSearch by appsListViewModel.automaticallyOpenAppsInSearch.collectAsState(
        initial = false
    )
    val apps by appsListViewModel.apps.collectAsState()
    val searchText by appsListViewModel.searchText.collectAsState()
    val searchExpanded by appsListViewModel.searchExpanded.collectAsState()
    val showBottomSheet by appsListViewModel.showBottomSheet.collectAsState()
    val bottomSheetApp by appsListViewModel.botttomSheetApp.collectAsState()
    val isBottomSheetAppFavourite by appsListViewModel.isBottomSheetAppFavourite.collectAsState()
    val isBottomSheetAppChallenged by appsListViewModel.doesBottomSheetAppHaveChallenge.collectAsState()
    val showWorkApps by appsListViewModel.showWorkApps.collectAsState()

    LaunchedEffect(isBeingShown) {
        if (!isBeingShown) {
            appsListViewModel.onSearchExpandedChanged(false)
            appsListViewModel.setBottomSheetVisible(visibility = false)
            appsListViewModel.setShowWorkApps(show = false)
        } else if (autoOpenSearch) {
            appsListViewModel.onSearchExpandedChanged(true)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp, 0.dp),
            horizontalAlignment = appsListAlignment,
        ) {
            // Apps list title
            item {
                AppsListHeader(stringResource(R.string.all_apps))
            }

            // Search box
            item {
                if (showSearchBox && !bottomSearchBox) {
                    Spacer(modifier = Modifier.height(15.dp))

                    AnimatedPillSearchBar(
                        closedText = stringResource(R.string.search),
                        searchText = searchText,
                        isExpanded = searchExpanded,
                        autoFocus = autoOpenSearch,
                        onExpandedChange = {
                            appsListViewModel.onSearchExpandedChanged(it)
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        },
                        onSearchTextChanged = { query ->
                            appsListViewModel.onSearchTextChanged(query)
                            if (autoOpenAppInSearch && query.length >= 2 && apps.size == 1) {
                                onAppOpened(apps.first())
                                appsListViewModel.onSearchExpandedChanged(false)
                            }
                        },
                        onSearchDone = { _, keboardController ->
                            if (apps.isNotEmpty()) {
                                keboardController?.hide()
                                onAppOpened(apps.first())
                                appsListViewModel.onSearchExpandedChanged(false)
                            } else {
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            }
                        })

                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            // Apps
            items(apps, key = { app -> app.packageName }) { app ->
                val screenTime = remember(appUsageList) {
                    screenTimeViewModel.getScreenTime(app.packageName)
                }

                HomeScreenItem(
                    appName = app.displayName,
                    screenTime = formatScreenTime(screenTime),
                    onAppClick = {
                        onAppOpened(app)
                        appsListViewModel.onSearchExpandedChanged(false)
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    },
                    onAppLongClick = {
                        appsListViewModel.setBottomSheetVisible(true)
                        appsListViewModel.setBottomSheetApp(app)
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    },
                    showScreenTime = showScreenTimeApp,
                    modifier = Modifier,
                    alignment = appsListAlignment
                )

            }

            //Secure Folder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canUseSecureFolder(context = context)) {
                item {
                    SecureFolderButton()
                }
            }
            //Private Space
            else if (isDefaultLauncher(context = context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && doesPrivateSpaceExist(
                    context = context
                )
            ) {
                item {
                    PrivateSpace(modifier = Modifier, onAppClick = { app ->
                        openPrivateSpaceApp(installedApp = app, context = context)
                        onGoHomeRequest()
                    }, onAppLongClick = { app ->
                        appsListViewModel.setBottomSheetVisible(true)
                        appsListViewModel.setBottomSheetApp(app)
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    })
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }

            item {
                SettingsSpacer()
            }
        }

        ListGradient() // Adds a gradient to the bottom of the screen just to make it a bit nicer

        // Work apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (doesWorkProfileExist(context = context)) {
                WorkAppsFab(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(vertical = 55.dp, horizontal = 30.dp)
                ) {
                    appsListViewModel.setShowWorkApps(show = true)
                }
            }

            AnimatedVisibility(
                visible = showWorkApps, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            onClick = { appsListViewModel.setShowWorkApps(show = false) },
                            onLongClick = {},
                            indication = null,
                            interactionSource = null
                        )
                        .background(transparentHalf)
                ) {
                    WorkApps(modifier = Modifier.align(Alignment.Center), onAppClick = { app ->
                        openWorkApp(installedApp = app, context = context)
                        onGoHomeRequest()
                    }, onAppLongClick = { app ->
                        appsListViewModel.setBottomSheetVisible(true)
                        appsListViewModel.setBottomSheetApp(app)
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    })
                }
            }
        }

        // Bottom search box
        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(30.dp, 25.dp)
                .fillMaxWidth(), horizontalAlignment = appsListAlignment
        ) {
            if (showSearchBox && bottomSearchBox) {
                Spacer(modifier = Modifier.height(15.dp))

                AnimatedPillSearchBar(
                    closedText = stringResource(R.string.search),
                    searchText = searchText,
                    isExpanded = searchExpanded,
                    autoFocus = autoOpenSearch,
                    onExpandedChange = {
                        appsListViewModel.onSearchExpandedChanged(it)
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    },
                    onSearchTextChanged = { query ->
                        appsListViewModel.onSearchTextChanged(query)
                        if (autoOpenAppInSearch && query.length >= 2 && apps.size == 1) {
                            onAppOpened(apps.first())
                            appsListViewModel.onSearchExpandedChanged(false)
                        }
                    },
                    onSearchDone = { _, keboardController ->
                        if (apps.isNotEmpty()) {
                            keboardController?.hide()
                            onAppOpened(apps.first())
                            appsListViewModel.onSearchExpandedChanged(false)
                        } else {
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        }
                    })

                SettingsSpacer()
            }
        }
    }

    if (showBottomSheet) {
        // Get the app shortcuts - these are the bits like that when you long hold an app you see that let you jump to a bit within the app
        val shortcuts: List<AppShortcut?> =
            if (bottomSheetApp?.user == android.os.Process.myUserHandle()) {
                getAppShortcuts(context, bottomSheetApp!!.packageName)
            } else {
                listOf(null)
            }

        // Take the shortcuts and turn them into app actions that can be displayed on the list
        val shortcutActions: List<AppAction?> = shortcuts.map { shortcut ->
            if (shortcut != null) {
                AppAction(
                    label = shortcut.label, onClick = {
                        startShortcut(context, bottomSheetApp!!.packageName, shortcut.id)
                        appsListViewModel.setBottomSheetVisible(visibility = false)
                        onGoHomeRequest()
                    })
            } else {
                null
            }
        }

        val actions = listOf(
            AppAction(
                label = stringResource(id = R.string.uninstall), onClick = {
                    uninstallApp(context, bottomSheetApp!!)
                }), if (bottomSheetApp!!.user == android.os.Process.myUserHandle()) {
                AppAction(
                    label = stringResource(
                        id = if (isBottomSheetAppFavourite) R.string.rem_from_fav else R.string.add_to_fav
                    ), onClick = {
                        if (isBottomSheetAppFavourite) {
                            appsListViewModel.removeFavourite(bottomSheetApp!!.packageName)
                        } else {
                            appsListViewModel.addFavourite(bottomSheetApp!!.packageName)
                            onGoHomeRequest()
                        }
                        appsListViewModel.setBottomSheetVisible(false)
                    })
            } else {
                null
            }, if (bottomSheetApp!!.user == android.os.Process.myUserHandle()) {
                AppAction(
                    label = stringResource(R.string.hide), onClick = {
                        hiddenAppsViewModel.hideApp(bottomSheetApp!!.packageName)
                        appsListViewModel.setBottomSheetVisible(false)
                    })
            } else {
                null
            }, AppAction(
                label = stringResource(id = R.string.app_info), onClick = {
                    goToAppInfo(context, bottomSheetApp!!)
                }),

            if (!isBottomSheetAppChallenged && bottomSheetApp!!.user == android.os.Process.myUserHandle()) {

                AppAction(
                    label = stringResource(R.string.add_open_challenge), onClick = {
                        openChallengeViewModel.addChallengeToApp(
                            bottomSheetApp!!.packageName
                        )
                        appsListViewModel.setBottomSheetVisible(false)
                    })
            } else {
                null
            }
        )


        HomeScreenBottomSheet(
            title = bottomSheetApp!!.displayName,
            actions = actions.filterNotNull(),
            onDismissRequest = { appsListViewModel.setBottomSheetVisible(false) },
            shortcutActions = shortcutActions.filterNotNull(),
            sheetState = rememberModalBottomSheetState()
        )
    }
}