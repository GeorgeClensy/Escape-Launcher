package com.geecee.escapelauncher.feature.workapps

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material.icons.rounded.WorkOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.BouncyMorphingFab
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI

/**
 * The main work apps UI
 *
 * @param modifier The modifier to apply to the UI
 * @param viewModel The view model to use
 * @param onAppClick The action to perform when an app is clicked
 * @param onAppLongClick The action to perform when an app is long clicked
 *
 * @author George Clensy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkApps(
    modifier: Modifier = Modifier,
    viewModel: WorkAppsViewModel = hiltViewModel(),
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val workApps by viewModel.workApps.collectAsState()
    val appsListAlignment by viewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)
    val scrollState = rememberScrollState()
    val heightToTopOfTabs = 30.dp + 56.dp

    Box (modifier) {
        AnimatedVisibility(
            visible = isUnlocked, enter = fadeIn(), exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize().padding(horizontal = 30.dp)) {
                Column(
                    horizontalAlignment = appsListAlignment,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    val statusBarHeight =
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    Spacer(
                        modifier = Modifier.height(statusBarHeight + 10.dp)
                    )

                    workApps.forEach { app ->
                        HomeScreenItem(appName = app.displayName, onAppLongClick = {
                            onAppLongClick(app)
                        }, onAppClick = {
                            onAppClick(app)
                        }, alignment = appsListAlignment)
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Spacer(
                        modifier = Modifier.height(
                            WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding() + heightToTopOfTabs
                        )
                    )
                }

                if (viewModel.canToggleProfile) {
                    BouncyMorphingFab(
                        icon = Icons.Default.WorkOff,
                        contentDescription = stringResource(R.string.pause_work_apps),
                        onClick = {
                            viewModel.toggleWorkProfile(onLauncherNotDefault = {
                                //todo: do something here
                            })
                        },
                        modifier = Modifier
                            .align(if (appsListAlignment == Alignment.End) Alignment.BottomStart else Alignment.BottomEnd)
                            .padding(
                                bottom = WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding() + heightToTopOfTabs + 15.dp
                            ), // Pad the bottom now so it looks alright above the tabs
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        radius = 24.dp
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !isUnlocked, enter = fadeIn(), exit = fadeOut()
        ) {
            LockedAppFolderUI(
                text = stringResource(R.string.work_profile),
                icon = Icons.Rounded.WorkOff,
                iconContentDescription = stringResource(R.string.work_apps_are_paused),
                buttonText = stringResource(R.string.unpause),
                subhead = stringResource(R.string.work_apps_are_paused_you_wont_recieve_notifications),
                modifier = modifier
                    .padding(
                        bottom = 86.dp,
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    ) // Pad the bottom now so it looks centered against the tabs
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                    ),
                unlockClick = {
                    if (viewModel.canToggleProfile) {
                        viewModel.toggleWorkProfile(onLauncherNotDefault = {
                            //todo: do something here
                            Toast.makeText(
                                context,
                                resources.getString(R.string.launcher_must_be_default_to_pause_or_unpause_work_apps),
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                })
        }
    }
}