package com.geecee.escapelauncher.ui.views

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.geecee.escapelauncher.ui.composables.AnimatedPillSearchBar
import com.geecee.escapelauncher.ui.composables.AppsListHeader
import com.geecee.escapelauncher.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.ui.composables.ListGradient
import com.geecee.escapelauncher.ui.composables.PrivateSpace
import com.geecee.escapelauncher.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.PrivateSpaceSettings
import com.geecee.escapelauncher.utils.doesPrivateSpaceExist
import com.geecee.escapelauncher.utils.getAppsAlignment
import com.geecee.escapelauncher.utils.getBooleanSetting
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

                    AnimatedPillSearchBar(
                        mainAppModel = mainAppModel,
                        textChange = { searchBoxText ->
                            homeScreenModel.searchText.value =
                                searchBoxText // Update text in search box

                            // Get the list of installed apps with the results filtered using fuzzy matching
                            var filteredApps = homeScreenModel.installedApps.filter { appInfo ->
                                AppUtils.fuzzyMatch(
                                    appInfo.displayName,
                                    homeScreenModel.searchText.value
                                )
                            }

                            // Remove  the launcher if present
                            filteredApps = filteredApps.filter { appInfo ->
                                !appInfo.packageName.contains("com.geecee.escapelauncher")
                            }

                            // If autoOpen is enabled then open the app like you would normally
                            val autoOpen = getBooleanSetting(
                                mainAppModel.getContext(),
                                mainAppModel.getContext().resources.getString(R.string.SearchAutoOpen),
                                false
                            )

                            if (autoOpen && filteredApps.size == 1) {

                                val appInfo = filteredApps.first()

                                var shouldShowHiddenApps =
                                    !mainAppModel.hiddenAppsManager.isAppHidden(
                                        appInfo.packageName
                                    )

                                if (!homeScreenModel.searchText.value.isBlank() && getBooleanSetting(
                                        mainAppModel.getContext(),
                                        mainAppModel.getContext()
                                            .getString(R.string.showHiddenAppsInSearch),
                                        false
                                    )
                                ) {
                                    shouldShowHiddenApps = true
                                }

                                if (shouldShowHiddenApps) {
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
                            }
                        },
                        keyboardDone = { _ ->
                            // Get the list of installed apps with the results filtered using fuzzy matching
                            var filteredApps = homeScreenModel.installedApps.filter { appInfo ->
                                AppUtils.fuzzyMatch(
                                    appInfo.displayName,
                                    homeScreenModel.searchText.value
                                )
                            }

                            // Remove the launcher if present
                            filteredApps = filteredApps.filter { appInfo ->
                                !appInfo.packageName.contains("com.geecee.escapelauncher")
                            }

                            if (filteredApps.isNotEmpty()) {
                                val firstAppInfo = filteredApps.first()

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
                        expanded = homeScreenModel.searchExpanded
                    )

                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            items(homeScreenModel.filteredApps, key = { app -> app.packageName })
            { app ->
                var shouldShowHiddenApps = !mainAppModel.hiddenAppsManager.isAppHidden(
                    app.packageName
                )

                if (!homeScreenModel.searchText.value.isBlank() && getBooleanSetting(
                        mainAppModel.getContext(),
                        stringResource(R.string.showHiddenAppsInSearch),
                        false
                    )
                ) {
                    shouldShowHiddenApps = true
                }

                // Draw app if its not hidden and not Escape itself
                if (!app.packageName.contains("com.geecee.escapelauncher") && shouldShowHiddenApps
                ) {
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
                            doHapticFeedBack(mainAppModel.getContext(), haptics)
                        },
                        showScreenTime = getBooleanSetting(
                            context = mainAppModel.getContext(),
                            setting = stringResource(R.string.ScreenTimeOnApp)
                        ),
                        modifier = Modifier,
                        alignment = getAppsAlignment(mainAppModel.getContext())
                    )
                }
            }

            //Private Space
            if (AppUtils.isDefaultLauncher(mainAppModel.getContext()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && doesPrivateSpaceExist(
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

        ListGradient(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .height(
                    if (showSearch && bottomSearch) {
                        200.dp
                    } else {
                        40.dp
                    }
                )
        )

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

                AnimatedPillSearchBar(
                    mainAppModel = mainAppModel,
                    textChange = { searchBoxText ->
                        homeScreenModel.searchText.value =
                            searchBoxText // Update text in search box

                        // Get the list of installed apps with the results filtered using fuzzy matching
                        var filteredApps = homeScreenModel.installedApps.filter { appInfo ->
                            AppUtils.fuzzyMatch(
                                appInfo.displayName,
                                homeScreenModel.searchText.value
                            )
                        }

                        // Remove  the launcher if present
                        filteredApps = filteredApps.filter { appInfo ->
                            !appInfo.packageName.contains("com.geecee.escapelauncher")
                        }

                        // If autoOpen is enabled then open the app like you would normally
                        val autoOpen = getBooleanSetting(
                            mainAppModel.getContext(),
                            mainAppModel.getContext().resources.getString(R.string.SearchAutoOpen),
                            false
                        )

                        if (autoOpen && filteredApps.size == 1) {

                            val appInfo = filteredApps.first()

                            var shouldShowHiddenApps =
                                !mainAppModel.hiddenAppsManager.isAppHidden(
                                    appInfo.packageName
                                )

                            if (!homeScreenModel.searchText.value.isBlank() && getBooleanSetting(
                                    mainAppModel.getContext(),
                                    mainAppModel.getContext()
                                        .getString(R.string.showHiddenAppsInSearch),
                                    false
                                )
                            ) {
                                shouldShowHiddenApps = true
                            }

                            if (shouldShowHiddenApps) {
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
                        }
                    },
                    keyboardDone = { _ ->
                        // Get the list of installed apps with the results filtered using fuzzy matching
                        var filteredApps = homeScreenModel.installedApps.filter { appInfo ->
                            AppUtils.fuzzyMatch(
                                appInfo.displayName,
                                homeScreenModel.searchText.value
                            )
                        }

                        // Remove the launcher if present
                        filteredApps = filteredApps.filter { appInfo ->
                            !appInfo.packageName.contains("com.geecee.escapelauncher")
                        }

                        if (filteredApps.isNotEmpty()) {
                            val firstAppInfo = filteredApps.first()

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
                    expanded = homeScreenModel.searchExpanded
                )
                SettingsSpacer()
            }
        }
    }
}