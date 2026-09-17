package com.geecee.escapelauncher.privatespace

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.ManagedProfileScreen

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
    val appsListAlignment by viewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)

    ManagedProfileScreen(
        modifier = modifier,
        isUnlocked = isUnlocked,
        apps = privateApps,
        appsListAlignment = appsListAlignment,
        onAppClick = onAppClick,
        onAppLongClick = onAppLongClick,
        canToggleProfile = viewModel.canToggleProfile,
        toggleIcon = Icons.Default.Lock,
        toggleContentDescription = stringResource(R.string.lock_private_space),
        onToggleClick = {
            viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {})
        },
        lockedIcon = Icons.Default.Lock,
        lockedText = stringResource(R.string.private_space),
        lockedSubhead = stringResource(R.string.private_space_is_locked),
        lockedButtonText = stringResource(R.string.unlock),
        onUnlockClick = {
            if (viewModel.canToggleProfile) {
                viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {
                    //todo: do something here
                })
            }
        }
    )
}
