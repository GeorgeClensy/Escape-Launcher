package com.geecee.escapelauncher.feature.workapps

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material.icons.rounded.WorkOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.ManagedProfileScreen

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

    ManagedProfileScreen(
        modifier = modifier,
        isUnlocked = isUnlocked,
        apps = workApps,
        appsListAlignment = appsListAlignment,
        onAppClick = onAppClick,
        onAppLongClick = onAppLongClick,
        canToggleProfile = viewModel.canToggleProfile,
        toggleIcon = Icons.Default.WorkOff,
        toggleContentDescription = stringResource(R.string.pause_work_apps),
        onToggleClick = {
            viewModel.toggleWorkProfile(onLauncherNotDefault = {
                //todo: do something here
            })
        },
        lockedIcon = Icons.Rounded.WorkOff,
        lockedText = stringResource(R.string.work_profile),
        lockedSubhead = stringResource(R.string.work_apps_are_paused_you_wont_recieve_notifications),
        lockedButtonText = stringResource(R.string.unpause),
        onUnlockClick = {
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
        }
    )
}
