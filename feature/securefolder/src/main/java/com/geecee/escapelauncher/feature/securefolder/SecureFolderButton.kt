package com.geecee.escapelauncher.feature.securefolder

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI

/**
 * Button to launch the secure folder on Samsung phones.
 *
 * @author George Clensy
 */
@Composable
fun SecureFolderButton() {
    val context = LocalContext.current

    LockedAppFolderUI(
        text = stringResource(com.geecee.escapelauncher.core.ui.R.string.launch_secure_folder),
        iconContentDescription = stringResource(com.geecee.escapelauncher.core.ui.R.string.launch_secure_folder)
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