@file:Suppress("KotlinConstantConditions")

package com.geecee.escapelauncher.feature.settings

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.geecee.escapelauncher.core.common.loadTextFromAssets
import com.geecee.escapelauncher.core.theme.motion.enterTransition
import com.geecee.escapelauncher.core.theme.motion.exitTransition
import com.geecee.escapelauncher.core.theme.motion.popEnterTransition
import com.geecee.escapelauncher.core.theme.motion.popExitTransition
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
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation keys for the settings sub-destinations.
 */
sealed interface SettingsNavKey : NavKey {
    @Serializable
    data object MainSettingsPage : SettingsNavKey

    @Serializable
    data object HiddenApps : SettingsNavKey

    @Serializable
    data object OpenChallenges : SettingsNavKey

    @Serializable
    data object ChooseFont : SettingsNavKey

    @Serializable
    data object DevOptions : SettingsNavKey

    @Serializable
    data object Theme : SettingsNavKey

    @Serializable
    data object Widget : SettingsNavKey

    @Serializable
    data object BulkHiddenApps : SettingsNavKey

    @Serializable
    data object BulkFavouriteApps : SettingsNavKey

    @Serializable
    data object FontLicences : SettingsNavKey
}

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
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(20.dp, 0.dp, 20.dp, 0.dp)
    ) {

        val backStack = rememberNavBackStack(SettingsNavKey.MainSettingsPage)

        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                } else {
                    goBack()
                }
            },
            transitionSpec = {
                enterTransition() togetherWith exitTransition()
            },
            popTransitionSpec = {
                popEnterTransition() togetherWith popExitTransition()
            },
            predictivePopTransitionSpec = {
                popEnterTransition() togetherWith popExitTransition()
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<SettingsNavKey.MainSettingsPage> {
                    MainSettingsPage(
                        goBack = { goBack() },
                        showPolicyDialog = { showPolicyDialog.value = true },
                        onNavigate = { key -> backStack.add(key) }
                    )
                }
                entry<SettingsNavKey.HiddenApps> {
                    HiddenApps(
                        goToManageHiddenApps = {
                            backStack.add(SettingsNavKey.BulkHiddenApps)
                        }) { backStack.removeLastOrNull() }
                }
                entry<SettingsNavKey.OpenChallenges> {
                    val openChallengeAppIds by openChallengeViewModel.challengeAppIds.collectAsState()

                    BulkManager(
                        items = installedApps,
                        id = { it.packageName },
                        label = { it.displayName },
                        selectedIdsOverride = openChallengeAppIds.map { it.packageName }.toSet(),
                        title = stringResource(R.string.manage_open_challenges),
                        onBackClicked = { backStack.removeLastOrNull() },
                        onItemClicked = { app, selected ->
                            if (selected) {
                                openChallengeViewModel.removeChallengeFromApp(app.packageName)
                            } else {
                                openChallengeViewModel.addChallengeToApp(app.packageName)
                            }
                        })
                }
                entry<SettingsNavKey.ChooseFont> {
                    ChooseFont(context = context) { backStack.removeLastOrNull() }
                }
                entry<SettingsNavKey.DevOptions> {
                    DevOptions { backStack.removeLastOrNull() }
                }
                entry<SettingsNavKey.Theme> {
                    ThemeOptions(goBack = { backStack.removeLastOrNull() })
                }
                entry<SettingsNavKey.Widget> {
                    WidgetOptions(onBackClick = { backStack.removeLastOrNull() })
                }
                entry<SettingsNavKey.BulkHiddenApps> {
                    val hiddenPackageIds by hiddenAppsViewModel.hiddenPackageIds.collectAsState()

                    BulkManager(
                        items = installedApps,
                        id = { it.packageName },
                        label = { it.displayName },
                        selectedIdsOverride = hiddenPackageIds,
                        title = stringResource(R.string.manage_hidden_apps),
                        onBackClicked = { backStack.removeLastOrNull() },
                        onItemClicked = { app, selected ->
                            if (selected) {
                                hiddenAppsViewModel.unhideApp(app.packageName)
                            } else {
                                hiddenAppsViewModel.hideApp(app.packageName)
                            }
                        })
                }
                entry<SettingsNavKey.BulkFavouriteApps> {
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
                        onBackClicked = { backStack.removeLastOrNull() },
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
                entry<SettingsNavKey.FontLicences> {
                    FontLicenceDialog(context = context) {
                        backStack.removeLastOrNull()
                    }
                }
            }
        )
    }

    AnimatedVisibility(showPolicyDialog.value, enter = fadeIn(), exit = fadeOut()) {

        loadTextFromAssets(context, "Privacy Policy.txt")?.let { text ->

            PrivacyPolicyDialog(text = text, onDismiss = { showPolicyDialog.value = false })
        }
    }
}