package com.geecee.escapelauncher.ui.views

import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.AppsListViewModel
import com.geecee.escapelauncher.HiddenAppsViewModel
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.common.doesPrivateSpaceExist
import com.geecee.escapelauncher.core.common.doesWorkProfileExist
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.openPrivateSpaceApp
import com.geecee.escapelauncher.core.common.openWorkApp
import com.geecee.escapelauncher.core.ui.composables.AnimatedPillSearchBar
import com.geecee.escapelauncher.core.ui.composables.AppsListHeader
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.composables.ListGradient
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.theme.transparentHalf
import com.geecee.escapelauncher.feature.securefolder.SecureFolderButton
import com.geecee.escapelauncher.feature.securefolder.canUseSecureFolder
import com.geecee.escapelauncher.feature.workapps.WorkApps
import com.geecee.escapelauncher.feature.workapps.WorkAppsFab
import com.geecee.escapelauncher.privatespace.PrivateSpace
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

/**
 * Parent apps list composable
 */
@Composable
fun AppsList(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    appsViewModel: AppsListViewModel = hiltViewModel(),
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val haptics = LocalHapticFeedback.current
    val appUsageList by screenTimeViewModel.appUsageList.collectAsState()
    val showScreenTimeApp by appsViewModel.showScreenTimeApp.collectAsState(initial = false)
    val appsListAlignment by appsViewModel.appsAlignment.collectAsState(initial = Alignment.CenterHorizontally)
    val hiddenPacakgeIds by hiddenAppsViewModel.hiddenPackageIds.collectAsState()

    val bottomSearch = getBooleanSetting(
        mainAppModel.getContext(),
        stringResource(R.string.bottomSearch),
        false
    )
    val showSearch = getBooleanSetting(
        mainAppModel.getContext(),
        stringResource(R.string.ShowSearchBox),
        true
    )

    @Composable
    fun SearchBox() {
        AnimatedPillSearchBar(
            closedText = stringResource(R.string.search),
            isExpanded = homeScreenModel.searchExpanded.value,
            onExpandedChange = { it: Boolean ->
                homeScreenModel.searchExpanded.value = it
                homeScreenModel.searchText.value = ""
            },
            onSearchTextChanged = { query: String ->
                homeScreenModel.searchText.value = query

                if (query.isBlank()) return@AnimatedPillSearchBar

                val showHiddenInSearch = getBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().getString(R.string.showHiddenAppsInSearch),
                    false
                )

                // Get results synchronously for auto-open logic to avoid race conditions with ViewModel update
                val matchedApps = homeScreenModel.installedApps.filter { app ->
                    val isHidden = hiddenPacakgeIds.contains(app.packageName)
                    val matchesQuery = AppUtils.fuzzyMatch(app.displayName, query)
                    matchesQuery && (!isHidden || showHiddenInSearch)
                }
                val sortedResults = AppUtils.sortAppsByRelevance(matchedApps, query)

                // If autoOpen is enabled then open the app like you would normally
                val autoOpen = getBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().resources.getString(R.string.SearchAutoOpen),
                    false
                )

                if (autoOpen && sortedResults.size == 1) {
                    val appInfo = sortedResults.first()
                    homeScreenModel.openApp(
                        app = appInfo,
                        overrideChallenge = false,
                        onAppOpened = { screenTimeViewModel.onAppOpened(it) }
                    )
                }
            },
            onSearchDone = { query: String ->
                val showHiddenInSearch = getBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().getString(R.string.showHiddenAppsInSearch),
                    false
                )

                val matchedApps = homeScreenModel.installedApps.filter { app ->
                    val isHidden = hiddenPacakgeIds.contains(app.packageName)
                    val matchesQuery = AppUtils.fuzzyMatch(app.displayName, query)
                    matchesQuery && (!isHidden || showHiddenInSearch)
                }
                val sortedResults = AppUtils.sortAppsByRelevance(matchedApps, query)

                if (sortedResults.isNotEmpty()) {
                    val firstAppInfo = sortedResults.first()
                    homeScreenModel.openApp(
                        app = firstAppInfo,
                        overrideChallenge = false,
                        onAppOpened = { screenTimeViewModel.onAppOpened(it) }
                    )
                }
            },
            modifier = Modifier,
            initialText = homeScreenModel.searchText.value,
            autoFocus = getBooleanSetting(
                mainAppModel.getContext(),
                stringResource(R.string.appsListAutoSearch),
                false
            )
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = homeScreenModel.appsListScrollState,
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
                if (getBooleanSetting(
                        mainAppModel.getContext(),
                        stringResource(R.string.ShowSearchBox),
                        true
                    ) && !bottomSearch
                ) {
                    Spacer(modifier = Modifier.height(15.dp))

                    SearchBox()

                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            // Apps
            items(homeScreenModel.filteredApps, key = { app -> app.packageName })
            { app ->

                val screenTime = remember(appUsageList) {
                    screenTimeViewModel.getScreenTime(app.packageName)
                }

                HomeScreenItem(
                    appName = app.displayName,
                    screenTime = formatScreenTime(screenTime),
                    onAppClick = {
                        homeScreenModel.openApp(
                            app = app,
                            overrideChallenge = false,
                            onAppOpened = { screenTimeViewModel.onAppOpened(it) }
                        )
                    },
                    onAppLongClick = {
                        homeScreenModel.showBottomSheet.value = true
                        homeScreenModel.updateSelectedApp(app)
                        doHapticFeedBack(haptics)
                    },
                    showScreenTime = showScreenTimeApp,
                    modifier = Modifier,
                    alignment = appsListAlignment
                )

            }

            //Secure Folder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canUseSecureFolder(mainAppModel.getContext())) {

                item {
                    SecureFolderButton()
                }

            }
            //Private Space
            else if (isDefaultLauncher(mainAppModel.getContext()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && doesPrivateSpaceExist(
                    mainAppModel.getContext()
                )
            ) {
                item {
                    PrivateSpace(
                        modifier = Modifier,
                        onAppClick = { app ->
                            openPrivateSpaceApp(app, mainAppModel.getContext())
                            resetHome(homeScreenModel)
                        },
                        onAppLongClick = { app ->
                            homeScreenModel.updateSelectedApp(app)
                            homeScreenModel.showBottomSheet.value = true
                            doHapticFeedBack(haptics)
                        }
                    )
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
            if (doesWorkProfileExist(mainAppModel.getContext())) {
                WorkAppsFab(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(vertical = 55.dp, horizontal = 30.dp)
                ) {
                    homeScreenModel.showWorkApps.value = true
                }
            }

            AnimatedVisibility(
                homeScreenModel.showWorkApps.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            onClick = { homeScreenModel.showWorkApps.value = false },
                            onLongClick = {},
                            indication = null,
                            interactionSource = homeScreenModel.interactionSource
                        )
                        .background(transparentHalf)
                ) {
                    WorkApps(
                        modifier = Modifier.align(Alignment.Center),
                        onAppClick = { app ->
                            openWorkApp(app, mainAppModel.getContext())
                            resetHome(homeScreenModel)
                        },
                        onAppLongClick = { app ->
                            homeScreenModel.showBottomSheet.value = true
                            homeScreenModel.updateSelectedApp(app)
                            doHapticFeedBack(haptics)
                        }
                    )
                }
            }
        }

        // Bottom search box
        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(30.dp, 25.dp)
                .fillMaxWidth(),
            horizontalAlignment = appsListAlignment
        ) {
            if (showSearch && bottomSearch
            ) {
                Spacer(modifier = Modifier.height(15.dp))

                SearchBox()

                SettingsSpacer()
            }
        }
    }
}
