package com.geecee.escapelauncher.feature.settings.hiddenapps

import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.EscapeSubhead
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.composables.SettingsSwipeableButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


/**
 * Page that lets you manage hidden apps
 *
 * @param goBack Function run when back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiddenApps(
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    goToManageHiddenApps: () -> Unit,
    goBack: () -> Unit
) {
    val installedApps by hiddenAppsViewModel.installedApps.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val hapticFeedbackEnabled by hiddenAppsViewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val hiddenPackageIds by hiddenAppsViewModel.hiddenPackageIds.collectAsState()
    val showHiddenAppsInSearch by hiddenAppsViewModel.showHiddenAppsInSearch.collectAsState(initial = DefaultSettings.SHOW_HIDDEN_APPS_IN_SEARCH)

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            EscapeHeader(goBack, stringResource(R.string.hidden_apps))
        }

        item {
            SettingsButton(
                label = stringResource(R.string.manage_hidden_apps),
                isTopOfGroup = true,
                onClick = {
                    goToManageHiddenApps()
                }
            )
        }

        item {
            SettingsSwitch(
                label = stringResource(R.string.show_hidden_apps_in_search),
                checked = showHiddenAppsInSearch,
                onCheckedChange = {
                    hiddenAppsViewModel.setShowHiddenAppsInSearch(it)
                },
                isBottomOfGroup = true
            )
        }

        item {
            EscapeSubhead(stringResource(R.string.swipe_to_show_app))
        }

        items(
            items = hiddenPackageIds.toList(),
            key = { it } // use package name as unique key
        ) { appPackageName ->
            // Animate the removal of the item
            var visible by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = visible,
                exit = fadeOut(animationSpec = tween(500))
            ) {
                SettingsSwipeableButton(
                    label = hiddenAppsViewModel.appsRepository.getAppNameFromPackageName(
                        appPackageName
                    ),
                    onClick = {
                        val app =
                            installedApps.find { it.packageName == appPackageName }
                                ?: hiddenAppsViewModel.appsRepository.getInstalledAppFromPackageName(
                                    appPackageName
                                )

                        app?.let {
                            hiddenAppsViewModel.launchApp(app)
                            screenTimeViewModel.onAppOpened(app.packageName)
                        }
                    },
                    onDeleteClick = {
                        // Trigger haptic feedback
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        // Animate item out
                        visible = false
                        // Remove from your list after a short delay to let animation run
                        coroutineScope.launch {
                            delay(500.milliseconds)
                            hiddenAppsViewModel.unhideApp(appPackageName)
                        }
                    },
                    isTopOfGroup = hiddenPackageIds.firstOrNull() == appPackageName,
                    isBottomOfGroup = hiddenPackageIds.lastOrNull() == appPackageName,
                    deleteIconContentDescription = stringResource(R.string.remove),
                )
            }
        }

        item {
            SettingsSpacer()
        }
        item {
            SettingsSpacer()
        }
    }
}
