package com.geecee.escapelauncher.ui.views

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.ui.composables.AnimatedPillSearchBar
import com.geecee.escapelauncher.ui.composables.AppsListHeader
import com.geecee.escapelauncher.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.ui.composables.ListGradient
import com.geecee.escapelauncher.ui.composables.PrivateSpace
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.ui.composables.WorkApps
import com.geecee.escapelauncher.ui.composables.WorkAppsFab
import com.geecee.escapelauncher.core.ui.theme.transparentHalf
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.PrivateSpaceSettings
import com.geecee.escapelauncher.utils.canUseSecureFolder
import com.geecee.escapelauncher.utils.doesPrivateSpaceExist
import com.geecee.escapelauncher.utils.doesWorkProfileExist
import com.geecee.escapelauncher.utils.getAppsAlignment
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.launchSecureFolder
import com.geecee.escapelauncher.utils.unlockPrivateSpace
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel
import com.geecee.escapelauncher.utils.isPrivateSpaceUnlocked as isPrivateSpace

/**
 * Parent apps list composable
 */
@Composable
fun AppsList(
    mainAppModel: MainAppModel, homeScreenModel: HomeScreenModel
) {
    val haptics = LocalHapticFeedback.current

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
                    val isHidden = mainAppModel.hiddenAppsManager.isAppHidden(app.packageName)
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
                    homeScreenModel.updateSelectedApp(appInfo)
                    AppUtils.openApp(
                        app = appInfo,
                        overrideOpenChallenge = false,
                        openChallengeShow = homeScreenModel.showOpenChallenge,
                        mainAppModel = mainAppModel,
                        homeScreenModel = homeScreenModel
                    )
                    resetHome(homeScreenModel)
                }
            },
            onSearchDone = { query: String ->
                val showHiddenInSearch = getBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().getString(R.string.showHiddenAppsInSearch),
                    false
                )

                val matchedApps = homeScreenModel.installedApps.filter { app ->
                    val isHidden = mainAppModel.hiddenAppsManager.isAppHidden(app.packageName)
                    val matchesQuery = AppUtils.fuzzyMatch(app.displayName, query)
                    matchesQuery && (!isHidden || showHiddenInSearch)
                }
                val sortedResults = AppUtils.sortAppsByRelevance(matchedApps, query)

                if (sortedResults.isNotEmpty()) {
                    val firstAppInfo = sortedResults.first()
                    homeScreenModel.updateSelectedApp(firstAppInfo)
                    AppUtils.openApp(
                        app = firstAppInfo,
                        overrideOpenChallenge = false,
                        openChallengeShow = homeScreenModel.showOpenChallenge,
                        mainAppModel = mainAppModel,
                        homeScreenModel = homeScreenModel
                    )
                    resetHome(homeScreenModel)
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
            horizontalAlignment = getAppsAlignment(mainAppModel.getContext()),
        ) {
            // Apps list title
            item {
                AppsListHeader()
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

            items(homeScreenModel.filteredApps, key = { app -> app.packageName })
            { app ->

                val screenTime =
                    remember { mutableLongStateOf(mainAppModel.getCachedScreenTime(app.packageName)) }

                // Update screen time when app changes or shouldReloadScreenTime changes
                LaunchedEffect(app.packageName, mainAppModel.shouldReloadScreenTime.value) {
                    val time = mainAppModel.getScreenTimeAsync(app.packageName)
                    screenTime.longValue = time
                }

                HomeScreenItem(
                    appName = app.displayName,
                    screenTime = screenTime.longValue,
                    onAppClick = {
                        homeScreenModel.updateSelectedApp(app)

                        AppUtils.openApp(
                            app = app,
                            overrideOpenChallenge = false,
                            openChallengeShow = homeScreenModel.showOpenChallenge,
                            mainAppModel = mainAppModel,
                            homeScreenModel = homeScreenModel
                        )

                        resetHome(homeScreenModel)
                    },
                    onAppLongClick = {
                        homeScreenModel.showBottomSheet.value = true
                        homeScreenModel.updateSelectedApp(app)
                        doHapticFeedBack(haptics)
                    },
                    showScreenTime = getBooleanSetting(
                        context = mainAppModel.getContext(),
                        setting = stringResource(R.string.ScreenTimeOnApp)
                    ),
                    modifier = Modifier,
                    alignment = getAppsAlignment(mainAppModel.getContext())
                )

            }

            //Secure Folder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canUseSecureFolder(mainAppModel.getContext())) {

                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Button({
                        launchSecureFolder(mainAppModel.getContext())
                    }) {
                        Text(stringResource(R.string.launch_secure_folder))
                    }
                }

            } //Private Space
            else if (AppUtils.isDefaultLauncher(mainAppModel.getContext()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && doesPrivateSpaceExist(
                    mainAppModel.getContext()
                )
            ) {
                // Spacing
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Stores whether the private space is unlocked and whether to try and show it
                mainAppModel.isPrivateSpaceUnlocked.value =
                    isPrivateSpace(mainAppModel.getContext())

                item {
                    // Private space is locked, shows button to unlock it
                    if ((!mainAppModel.isPrivateSpaceUnlocked.value && !getBooleanSetting(
                            mainAppModel.getContext(),
                            stringResource(R.string.SearchHiddenPrivateSpace),
                            false
                        )) || (!mainAppModel.isPrivateSpaceUnlocked.value && homeScreenModel.searchText.value.contains(
                            stringResource(R.string.private_space_search_term)
                        ) && getBooleanSetting(
                            mainAppModel.getContext(),
                            stringResource(R.string.SearchHiddenPrivateSpace),
                            false
                        ))
                    ) {
                        Button({
                            unlockPrivateSpace(mainAppModel.getContext())
                        }) {
                            Text(stringResource(R.string.unlock_private_space))
                        }
                    }

                    // Private space itself
                    AnimatedVisibility(
                        visible = mainAppModel.isPrivateSpaceUnlocked.value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        PrivateSpace(mainAppModel, homeScreenModel)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }

            item {
                SettingsSpacer()
            }
        }

        // Private space settings
        AnimatedVisibility(
            visible = homeScreenModel.showPrivateSpaceSettings.value && mainAppModel.isPrivateSpaceUnlocked.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PrivateSpaceSettings(
                mainAppModel.getContext(),
                homeScreenModel.interactionSource
            ) {
                homeScreenModel.showPrivateSpaceSettings.value = false
            }
        }

        AnimatedVisibility(
            visible = !homeScreenModel.showPrivateSpaceSettings.value,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .height(
                    if (showSearch && bottomSearch) {
                        200.dp
                    } else {
                        40.dp
                    }
                )
        ) {
            ListGradient()
        }

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
                        mainAppModel,
                        homeScreenModel,
                        Modifier.align(Alignment.Center)
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
            horizontalAlignment = getAppsAlignment(mainAppModel.getContext())
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
