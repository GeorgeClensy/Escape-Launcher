package com.geecee.escapelauncher.privatespace

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import com.geecee.escapelauncher.core.ui.vectors.getPrivateSpaceLockedImage

/**
 * Android 15+ Private space UI with apps, settings button and lock button
 *
 * @param modifier The modifier to apply to the UI
 * @param viewModel The view model to use
 * @param onAppClick The action to perform when an app is clicked
 * @param onAppLongClick The action to perform when an app is long clicked
 *
 * @author George Clensy
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PrivateSpace(
    modifier: Modifier = Modifier,
    viewModel: PrivateSpaceViewModel = hiltViewModel(),
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit
) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val privateApps by viewModel.privateSpaceApps.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val hiddenPrivateSpaceSetting by viewModel.hiddenPrivateSpace.collectAsState(initial = DefaultSettings.HIDE_PRIVATE_SPACE)

    if (isUnlocked) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val statusBarHeight =
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Spacer(
                modifier = Modifier.height(statusBarHeight + 10.dp)
            )

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
                        onClick = {
                            viewModel.toggleSettings()
                        }, modifier = Modifier, colors = IconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            stringResource(R.string.private_space_settings)
                        )
                    }

                    if (viewModel.canToggleProfile) {
                        IconButton(
                            onClick = {
                                viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {})
                            }, modifier = Modifier, colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                Icons.Default.Lock, stringResource(R.string.lock_private_space)
                            )
                        }
                    }
                }
            }

            if (!showSettings) {
                privateApps.forEach { app ->
                    PrivateAppItem(app.displayName, {
                        onAppLongClick(app)
                    }) {
                        onAppClick(app)
                    }
                }
            } else {
                SettingsSwitch(
                    label = stringResource(R.string.hide_private_space_in_search),
                    checked = hiddenPrivateSpaceSetting,
                    onCheckedChange = { enabled ->
                        viewModel.setHiddenPrivateSpace(enabled)
                    },
                    isTopOfGroup = true,
                    isBottomOfGroup = true,
                )
            }

            Spacer(Modifier.height(120.dp))
        }
    } else {
         LockedAppFolderUI(
            text = stringResource(R.string.private_space),
            image = getPrivateSpaceLockedImage(),
            iconContentDescription = stringResource(R.string.unlock_private_space),
            subhead = stringResource(R.string.private_space_is_locked),
            modifier = modifier.padding(bottom = 86.dp), // Pad the bottom now so it looks centered against the tabs
            unlockClick = {
                if (viewModel.canToggleProfile) {
                    viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {
                        //todo: do something here
                    })
                }
            }
        )
    }
}

/**
 * UI component for displaying a single Private Space app item.
 *
 * @param appName The name of the app
 * @param onLongClick The action to perform when the app is long clicked
 * @param onClick The action to perform when the app is clicked
 *
 * @author George Clensy
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrivateAppItem(
    appName: String, onLongClick: () -> Unit, onClick: () -> Unit
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