package com.geecee.escapelauncher.feature.securefolder

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI

/**
 * Button to launch the secure folder on Samsung phones.
 *
 * @author George Clensy
 */
@Composable
fun SecureFolderButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LockedAppFolderUI(
        text = stringResource(R.string.launch_secure_folder),
        iconContentDescription = stringResource(R.string.launch_secure_folder),
        subhead = stringResource(R.string.secure_folder_is_locked),
        modifier = modifier
            .padding(
                bottom = 86.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            ) // Pad the bottom now so it looks centered against the tabs
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
            ),
    ) {
        launchSecureFolder(context = context)
    }
}

@Preview
@Composable
fun SecureFolderButtonPreview() {
    EscapeThemePreview {
        SecureFolderButton()
    }
}