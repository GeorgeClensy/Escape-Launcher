package com.geecee.escapelauncher.ui.views

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.EscapeTheme
import com.geecee.escapelauncher.ui.theme.offLightScheme
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.managers.OpenChallenge
import com.geecee.escapelauncher.utils.setBooleanSetting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel


/**
 *  Main composable for home screen:
 *  contains a pager with all the pages inside of it, contains bottom sheet, contains open challenge UI
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun HomeScreenPageManager(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    onOpenSettings: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Add effect to hide keyboard on page change
    LaunchedEffect(homeScreenModel.pagerState.currentPage) {
        if (homeScreenModel.pagerState.currentPage != 2) {
            focusManager.clearFocus()
            keyboardController?.hide()
            homeScreenModel.searchText.value = ""
            homeScreenModel.searchExpanded.value = false
        }
    }

    // Home Screen Pages
    HorizontalPager (
        state = homeScreenModel.pagerState,
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {}, onLongClickLabel = {}.toString(),
                onLongClick = {
                    doHapticFeedBack(mainAppModel.getContext(), hapticFeedback)
                    onOpenSettings()
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                        false
                    )
                },
                indication = null, interactionSource = homeScreenModel.interactionSource
            )
    ) { page ->
        if (getBooleanSetting(
                context = mainAppModel.getContext(),
                setting = mainAppModel.getContext().resources.getString(R.string.hideScreenTimePage),
                defaultValue = false
            )
        ) {
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
                0 -> ScreenTimeDashboard(
                    context = mainAppModel.getContext(),
                    mainAppModel = mainAppModel
                )

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
        var actions = listOf(
            AppAction(
                label = stringResource(id = R.string.uninstall),
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_DELETE,
                        "package:${homeScreenModel.currentSelectedApp.value.packageName}".toUri()
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mainAppModel.getContext().startActivity(intent)
                }
            ),
            AppAction(
                label = stringResource(if (homeScreenModel.isCurrentAppFavorite.value) R.string.rem_from_fav else R.string.add_to_fav),
                onClick = {
                    if (homeScreenModel.isCurrentAppFavorite.value) {
                        mainAppModel.favoriteAppsManager.removeFavoriteApp(
                            homeScreenModel.currentSelectedApp.value.packageName
                        )
                        homeScreenModel.isCurrentAppFavorite.value = false
                        homeScreenModel.showBottomSheet.value = false
                    } else {
                        mainAppModel.favoriteAppsManager.addFavoriteApp(
                            homeScreenModel.currentSelectedApp.value.packageName
                        )
                        homeScreenModel.isCurrentAppFavorite.value = true
                        homeScreenModel.showBottomSheet.value = false
                        homeScreenModel.coroutineScope.launch {
                            homeScreenModel.goToMainPage()
                        }
                    }
                    homeScreenModel.reloadFavouriteApps()
                }
            ),
            AppAction(
                label = stringResource(R.string.hide),
                onClick = {
                    mainAppModel.hiddenAppsManager.addHiddenApp(homeScreenModel.currentSelectedApp.value.packageName)
                    homeScreenModel.showBottomSheet.value = false
                    homeScreenModel.installedApps.remove(homeScreenModel.currentSelectedApp.value)
                    resetHome(homeScreenModel, false)
                }
            ),
            AppAction(
                label = stringResource(id = R.string.app_info),
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data =
                            "package:${homeScreenModel.currentSelectedApp.value.packageName}".toUri()
                    }.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    mainAppModel.getContext().startActivity(intent)
                    resetHome(homeScreenModel, false)
                }
            )
        )

        if (!homeScreenModel.isCurrentAppChallenged.value) {
            actions = actions +
                    AppAction(
                        label = stringResource(R.string.add_open_challenge),
                        onClick = {
                            mainAppModel.challengesManager.addChallengeApp(
                                homeScreenModel.currentSelectedApp.value.packageName
                            )
                            homeScreenModel.showBottomSheet.value = false
                            homeScreenModel.isCurrentAppChallenged.value = true
                        }
                    )
        }


        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedApp.value.displayName,
            actions = actions,
            onDismissRequest = { homeScreenModel.showBottomSheet.value = false },
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
                AppUtils.openApp(
                    homeScreenModel.currentSelectedApp.value,
                    mainAppModel,
                    homeScreenModel,
                    true,
                    null
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

/**
 * An item displayed on the HomeScreen or Apps list
 *
 * If [showScreenTime] is enabled and [screenTime] is not null the screen time is written next to the app name.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenItem(
    modifier: Modifier = Modifier,
    appName: String,
    screenTime: Long? = null,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    showScreenTime: Boolean = false,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = when (alignment) {
            Alignment.Start -> Arrangement.Start
            Alignment.CenterHorizontally -> Arrangement.Center
            Alignment.End -> Arrangement.End
            else -> Arrangement.Center
        },
        modifier = modifier
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = onAppLongClick
            )
            .fillMaxWidth()
    ) {
        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier.padding(vertical = 15.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )

        // Optional screen time
        if (showScreenTime && screenTime != null) {
            Text(
                formatScreenTime(screenTime),
                modifier = Modifier
                    .padding(vertical = 15.dp, horizontal = 5.dp)
                    .alpha(0.5f),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
fun HomeScreeItemPrev() {
    EscapeTheme(remember { mutableStateOf(offLightScheme) }) {
        HomeScreenItem(
            modifier = Modifier,
            appName = "App Name",
            screenTime = 1000,
            onAppClick = {},
            onAppLongClick = {},
            showScreenTime = false
        )
    }
}

/**
 * Action that can be shown in the bottom sheet
 * */
data class AppAction(
    val label: String,
    val onClick: () -> Unit
)

/**
 * Bottom Sheet home screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenBottomSheet(
    title: String,
    actions: List<AppAction>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
            // Header
            Row {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "App Options",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 15.dp))

            // Actions
            Column(Modifier.padding(start = 47.dp)) {
                actions.forEach { action ->
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .combinedClickable(onClick = action.onClick),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}