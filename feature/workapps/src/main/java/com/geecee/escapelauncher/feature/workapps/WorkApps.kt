package com.geecee.escapelauncher.feature.workapps

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.WorkOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.LockedFolderCard
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.ContentColor
import com.geecee.escapelauncher.core.theme.SecondaryCardContainerColor
import com.geecee.escapelauncher.core.theme.primaryContentColor

// IMPORTANT TODO: Fix that the app doesn't track the work apps state for example if you uninstall an app escape will still show it until it has been restarted which can cause it to crash if you then tap on the app

/**
 * A FAB with a Work Profile icon.
 *
 * @param modifier The modifier to apply to the FAB
 * @param onClick The action to perform when the FAB is clicked
 *
 * @author George Clensy
 */
@Composable
fun WorkAppsFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = {
            onClick()
        },
        modifier = modifier.size(56.dp),
        contentColor = BackgroundColor,
        containerColor = primaryContentColor,
    ) {
        Icon(Icons.Rounded.Work, contentDescription = stringResource(R.string.work_profile))
    }
}

/**
 * UI component for displaying a single Work Profile app item. Just a `Text()` with `bodyMedium`
 *
 * @param appName The name of the app
 * @param onLongClick The action to perform when the app is long clicked
 * @param onClick The action to perform when the app is clicked
 *
 * @author George Clensy
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
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
        color = ContentColor,
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
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
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

    LockedFolderCard(
       modifier = modifier.padding(horizontal = 30.dp, vertical = 120.dp))
    {
        // Work apps unlocked
        if(isUnlocked) {
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
                            containerColor = SecondaryCardContainerColor,
                            contentColor = ContentColor,
                            disabledContainerColor = SecondaryCardContainerColor,
                            disabledContentColor = ContentColor
                        )
                    ) {
                        Icon(
                            Icons.Rounded.WorkOff,
                            contentDescription = stringResource(R.string.lock_work_profile)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
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
        else {
            Column(
                Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.work_apps_are_paused),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        top = 30.dp,
                        start = 30.dp,
                        end = 30.dp,
                        bottom = 5.dp
                    )
                )

                Text(
                    stringResource(R.string.you_wont_receive_notifications_from_work_apps),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        top = 5.dp,
                        start = 30.dp,
                        end = 30.dp,
                        bottom = 10.dp
                    )
                )

                OutlinedButton(
                    onClick = {
                        viewModel.toggleWorkProfile {
                            Toast.makeText(
                                context,
                                resources.getString(R.string.launcher_must_be_default_to_pause_or_unpause_work_apps),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .padding(bottom = 30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryCardContainerColor,
                        contentColor = ContentColor,
                    )
                ) {
                    Text(stringResource(R.string.unpause))
                }
            }
        }
    }
}