package com.geecee.escapelauncher.ui.views

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.composables.AppAction
import com.geecee.escapelauncher.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.managers.MyDeviceAdminReceiver
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
                indication = null, interactionSource = homeScreenModel.interactionSource,
                onDoubleClick = {
                    val devicePolicyManager =
                        mainAppModel.getContext().getSystemService(DevicePolicyManager::class.java)
                    val compName = ComponentName(mainAppModel.getContext(), MyDeviceAdminReceiver::class.java)

                    if (devicePolicyManager.isAdminActive(compName)) {
                        mainAppModel.blackOverlay.value = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            devicePolicyManager.lockNow()
                            mainAppModel.blackOverlay.value = false
                        }, 300)
                    } else {
                        Toast.makeText(mainAppModel.getContext(), "Enable Device Admin first", Toast.LENGTH_SHORT).show()
                    }
                }
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
                label = stringResource(if (homeScreenModel.isCurrentAppFavorite) R.string.rem_from_fav else R.string.add_to_fav),
                onClick = {
                    val selectedApp = homeScreenModel.currentSelectedApp.value
                    if (homeScreenModel.isCurrentAppFavorite) {
                        mainAppModel.favoriteAppsManager.removeFavoriteApp(selectedApp.packageName)
                        homeScreenModel.favoriteApps.remove(selectedApp)
                    } else {
                        mainAppModel.favoriteAppsManager.addFavoriteApp(selectedApp.packageName)
                        homeScreenModel.favoriteApps.add(selectedApp)

                        homeScreenModel.coroutineScope.launch {
                            homeScreenModel.goToMainPage()
                        }
                    }
                    homeScreenModel.showBottomSheet.value = false
                }
            ),
            AppAction(
                label = stringResource(R.string.hide),
                onClick = {
                    mainAppModel.hiddenAppsManager.addHiddenApp(homeScreenModel.currentSelectedApp.value.packageName)
                    homeScreenModel.showBottomSheet.value = false
                    mainAppModel.notifyHiddenAppsChanged()
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

        if (!homeScreenModel.isCurrentAppChallenged) {
            actions = actions +
                    AppAction(
                        label = stringResource(R.string.add_open_challenge),
                        onClick = {
                            mainAppModel.challengesManager.addChallengeApp(
                                homeScreenModel.currentSelectedApp.value.packageName
                            )
                            mainAppModel.notifyChallengesChanged()
                            homeScreenModel.showBottomSheet.value = false
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