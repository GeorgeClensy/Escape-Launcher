package com.geecee.escapelauncher.feature.workapps

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI

/**
 * UI component for displaying a single Work Profile app item. Just a `Text()` with `bodyMedium`
 *
 * @param appName The name of the app
 * @param onLongClick The action to perform when the app is long clicked
 * @param onClick The action to perform when the app is clicked
 *
 * @author George Clensy
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkAppItem(
    appName: String,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val modifier = Modifier
        .padding(vertical = 15.dp)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)

    Text(
        appName,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium
    )
}

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

    Box (modifier) {
        // Work apps unlocked
        if (isUnlocked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.work_profile),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (viewModel.canToggleProfile) {
                        IconButton(
                            onClick = {
                                viewModel.toggleWorkProfile {
                                    Toast.makeText(
                                        context,
                                        resources.getString(R.string.launcher_must_be_default_to_pause_or_unpause_work_apps),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                Icons.Rounded.WorkOff,
                                contentDescription = stringResource(R.string.lock_work_profile)
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    workApps.forEach { app ->
                        WorkAppItem(app.displayName, {
                            onAppLongClick(app)
                        }) {
                            onAppClick(app)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
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