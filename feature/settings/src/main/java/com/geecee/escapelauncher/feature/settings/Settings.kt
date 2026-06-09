@file:Suppress("KotlinConstantConditions")

package com.geecee.escapelauncher.feature.settings

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.core.common.loadTextFromAssets
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.BulkManager
import com.geecee.escapelauncher.core.ui.composables.PrivacyPolicyDialog
import com.geecee.escapelauncher.feature.settings.devoptions.DevOptions
import com.geecee.escapelauncher.feature.settings.font.ChooseFont
import com.geecee.escapelauncher.feature.settings.font.FontLicenceDialog
import com.geecee.escapelauncher.feature.settings.hiddenapps.HiddenApps
import com.geecee.escapelauncher.feature.settings.hiddenapps.HiddenAppsViewModel
import com.geecee.escapelauncher.feature.settings.mainpage.MainSettingsPage
import com.geecee.escapelauncher.feature.settings.openchallenges.OpenChallengeViewModel
import com.geecee.escapelauncher.feature.settings.theme.ThemeOptions
import com.geecee.escapelauncher.feature.settings.widget.WidgetOptions
import kotlinx.coroutines.launch

//
// MENUS
//

/**
 * Main Settings window you see when settings is first opened
 *
 * @param goBack When back button is pressed
 * @param activity This is needed for some settings
 */
@Composable
fun Settings(
    goBack: () -> Unit,
    activity: Activity,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel(),
    openChallengeViewModel: OpenChallengeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val installedApps by settingsViewModel.installedApps.collectAsState()
    val favouriteApps by settingsViewModel.favoriteApps.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val showPolicyDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp, 0.dp, 20.dp, 0.dp)
    ) {

        val navController = rememberNavController()

        NavHost(navController = navController, "mainSettingsPage") {
            composable(
                "mainSettingsPage",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                MainSettingsPage(
                    { goBack() },
                    { showPolicyDialog.value = true },
                    navController
                )
            }
            composable(
                "hiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                HiddenApps(
                    goToManageHiddenApps = {
                        navController.navigate("bulkHiddenApps")
                    }) { navController.popBackStack() }
            }
            composable(
                "openChallenges",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                val openChallegeAppIds by openChallengeViewModel.challengeAppIds.collectAsState()

                BulkManager(
                    items = installedApps,
                    id = { it.packageName },
                    label = { it.displayName },
                    selectedIdsOverride = openChallegeAppIds,
                    title = stringResource(R.string.manage_open_challenges),
                    onBackClicked = { navController.popBackStack() },
                    onItemClicked = { app, selected ->
                        if (selected) {
                            openChallengeViewModel.removeChallengeFromApp(app.packageName)
                        } else {
                            openChallengeViewModel.addChallengeToApp(app.packageName)
                        }
                    })
            }
            composable(
                "chooseFont",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ChooseFont(context = context) { navController.popBackStack() }
            }
            composable(
                "devOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DevOptions { navController.popBackStack() }
            }
            composable(
                "theme",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ThemeOptions(goBack = { navController.popBackStack() })
            }
            composable(
                "widget",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                WidgetOptions(onBackClick = { navController.popBackStack() })
            }
            composable(
                "bulkHiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                val hiddenPackageIds by hiddenAppsViewModel.hiddenPackageIds.collectAsState()

                BulkManager(
                    items = installedApps,
                    id = { it.packageName },
                    label = { it.displayName },
                    selectedIdsOverride = hiddenPackageIds,
                    title = stringResource(R.string.manage_hidden_apps),
                    onBackClicked = { navController.popBackStack() },
                    onItemClicked = { app, selected ->
                        if (selected) {
                            hiddenAppsViewModel.unhideApp(app.packageName)
                        } else {
                            hiddenAppsViewModel.hideApp(app.packageName)
                        }
                    })
            }
            composable(
                "bulkFavouriteApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                BulkManager(
                    items = installedApps,
                    id = { it.packageName },
                    label = { it.displayName },
                    preSelectedItems = favouriteApps,
                    title = stringResource(R.string.manage_favourite_apps),
                    reorderable = true,
                    onItemMoved = { fromIndex, toIndex ->
                        val app = favouriteApps[fromIndex]
                        coroutineScope.launch {
                            settingsViewModel.modifiedAppsRepository.reorderFavouriteApp(
                                app.packageName, fromIndex, toIndex
                            )
                        }
                    },
                    onBackClicked = { navController.popBackStack() },
                    onItemClicked = { app, selected ->
                        coroutineScope.launch {
                            if (selected) {
                                settingsViewModel.modifiedAppsRepository.removeFavourite(app.packageName)
                            } else {
                                settingsViewModel.modifiedAppsRepository.addFavourite(app.packageName)
                            }
                        }
                    })
            }
            composable(
                "fontLicences",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                FontLicenceDialog(context = context) {
                    navController.popBackStack()
                }
            }
        }
    }

    AnimatedVisibility(showPolicyDialog.value, enter = fadeIn(), exit = fadeOut()) {

        loadTextFromAssets(context, "Privacy Policy.txt")?.let { text ->

            PrivacyPolicyDialog(text = text, onDismiss = { showPolicyDialog.value = false })
        }
    }
}