package com.geecee.escapelauncher.ui.views

import android.content.ComponentName
import android.graphics.Rect
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.InstalledApp
import com.geecee.escapelauncher.utils.PrivateAppItem
import com.geecee.escapelauncher.utils.PrivateSpaceSettings
import com.geecee.escapelauncher.utils.doesPrivateSpaceExist
import com.geecee.escapelauncher.utils.getAppsAlignment
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.getPrivateSpaceApps
import com.geecee.escapelauncher.utils.lockPrivateSpace
import com.geecee.escapelauncher.utils.openPrivateSpaceApp
import com.geecee.escapelauncher.utils.showPrivateSpaceAppInfo
import com.geecee.escapelauncher.utils.uninstallPrivateSpaceApp
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

    Box(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = homeScreenModel.appsListScrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp, 0.dp),
            horizontalAlignment = getAppsAlignment(mainAppModel.getContext())
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
                    )
                ) {
                    Spacer(modifier = Modifier.height(15.dp))

                    AnimatedPillSearchBar(
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

                                var shouldShowHiddenApps = !mainAppModel.hiddenAppsManager.isAppHidden(
                                    appInfo.packageName
                                )

                                if(!homeScreenModel.searchText.value.isBlank() && getBooleanSetting(mainAppModel.getContext(), mainAppModel.getContext().getString(R.string.showHiddenAppsInSearch), false)) {
                                    shouldShowHiddenApps = true
                                }

                                if(shouldShowHiddenApps) {
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

            items(
                // All installed apps filtered with search term
                homeScreenModel.installedApps.filter { appInfo ->
                    val appName = appInfo.displayName
                    if (homeScreenModel.searchExpanded.value) {
                        if (homeScreenModel.searchText.value.isBlank()) {
                            true
                        } else {
                            AppUtils.fuzzyMatch(appName, homeScreenModel.searchText.value)
                        }
                    } else {
                        true
                    }
                }
            )
            { app ->
                var shouldShowHiddenApps = !mainAppModel.hiddenAppsManager.isAppHidden(
                    app.packageName
                )

                if(!homeScreenModel.searchText.value.isBlank() && getBooleanSetting(mainAppModel.getContext(), stringResource(R.string.showHiddenAppsInSearch), false)) {
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
    }
}

/**
 * Apps List title
 */
@Composable
fun AppsListHeader() {
    Spacer(modifier = Modifier.height(140.dp))
    Text(
        text = stringResource(id = R.string.all_apps),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.titleMedium
    )
}

/**
 * Search Bar for apps list that collapses into a pill
 */
@Composable
fun AnimatedPillSearchBar(
    textChange: (searchText: String) -> Unit,
    keyboardDone: (searchText: String) -> Unit,
    expanded: MutableState<Boolean>
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }


    // Animate the width of the search bar
    val width by animateDpAsState(targetValue = if (expanded.value) 280.dp else 150.dp, label = "")

    // Animate the alpha of the text field content
    val alpha by animateFloatAsState(targetValue = if (expanded.value) 1f else 0f, label = "")

    // FocusRequester to request focus on the text field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(expanded.value) {
        if (expanded.value) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
        if (!expanded.value) {
            keyboardController?.hide()
        }
    }

    Surface(
        modifier = Modifier
            .width(width)
            .height(56.dp)
            .clickable {
                expanded.value = !expanded.value
            }
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .animateContentSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .padding(5.dp, 0.dp)
                    .size(25.dp)
            )

            if (!expanded.value) {
                Text(
                    stringResource(id = R.string.search),
                    modifier = Modifier.animateContentSize(),
                    color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (expanded.value) {
                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                        .animateContentSize()
                )

                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        textChange(searchText.text)
                    },
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .animateContentSize(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (alpha > 0) {
                            innerTextField()
                        }
                    },
                    maxLines = 1,
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        keyboardDone(searchText.text)
                    }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    }
}

/**
 * Android 15+ Private space UI with apps, settings button and lock button
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PrivateSpace(mainAppModel: MainAppModel, homeScreenModel: HomeScreenModel) {
    val privateSpaceAppActions = listOf(
        AppAction(
            stringResource(R.string.uninstall)
        ) {
            uninstallPrivateSpaceApp(
                homeScreenModel.currentSelectedPrivateApp.value,
                mainAppModel.getContext()
            )
        },
        AppAction(
            stringResource(R.string.app_info)
        ) {
            showPrivateSpaceAppInfo(
                homeScreenModel.currentSelectedPrivateApp.value,
                mainAppModel.getContext()
            )
        }
    )

    Card(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    stringResource(R.string.private_space),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Row(
                    Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        {
                            homeScreenModel.showPrivateSpaceSettings.value = true
                        }, Modifier, colors = IconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Settings, stringResource(R.string.private_space_settings)
                        )
                    }

                    IconButton(
                        {
                            lockPrivateSpace(mainAppModel.getContext())
                            homeScreenModel.searchExpanded.value = false
                            homeScreenModel.searchText.value = ""
                        }, Modifier, colors = IconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Lock, stringResource(R.string.lock_private_space)
                        )
                    }
                }
            }

            getPrivateSpaceApps(mainAppModel.getContext()).forEach { app ->
                PrivateAppItem(app.displayName, {
                    homeScreenModel.currentSelectedPrivateApp.value = app
                    homeScreenModel.showPrivateBottomSheet.value = true
                }) {
                    openPrivateSpaceApp(
                        installedApp = app, context = mainAppModel.getContext(), Rect()
                    )
                    resetHome(homeScreenModel)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (homeScreenModel.showPrivateBottomSheet.value) {
        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedPrivateApp.value.displayName,
            actions = privateSpaceAppActions,
            onDismissRequest = {
                homeScreenModel.showPrivateBottomSheet.value = false
                homeScreenModel.currentSelectedPrivateApp.value =
                    InstalledApp("", "", ComponentName("", ""))
            },
            sheetState = rememberModalBottomSheetState(),
            modifier = Modifier
        )
    }
}