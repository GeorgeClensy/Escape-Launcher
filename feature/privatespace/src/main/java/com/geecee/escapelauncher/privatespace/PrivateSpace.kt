package com.geecee.escapelauncher.privatespace

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.BouncyMorphingFab
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI
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
    val appsListAlignment by viewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)

    AnimatedVisibility(
        visible = isUnlocked, enter = fadeIn(), exit = fadeOut()
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = appsListAlignment,
                modifier = Modifier.fillMaxWidth()
            ) {
                privateApps.forEach { app ->
                    PrivateAppItem(app.displayName, {
                        onAppLongClick(app)
                    }) {
                        onAppClick(app)
                    }
                }

                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 30.dp + 56.dp))
            }

            if (viewModel.canToggleProfile) {
                BouncyMorphingFab(
                    icon = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.lock_private_space),
                    onClick = {
                        viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {})
                    },
                    modifier = Modifier
                        .align(if (appsListAlignment == Alignment.End) Alignment.BottomStart else Alignment.BottomEnd)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 30.dp + 56.dp + 15.dp), // Pad the bottom now so it looks alright above the tabs
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    AnimatedVisibility(
        visible = !isUnlocked, enter = fadeIn(), exit = fadeOut()
    ) {
        LockedAppFolderUI(
            text = stringResource(R.string.private_space),
            image = getPrivateSpaceLockedImage(),
            iconContentDescription = stringResource(R.string.unlock_private_space),
            subhead = stringResource(R.string.private_space_is_locked),
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
                    viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {
                        //todo: do something here
                    })
                }
            })
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