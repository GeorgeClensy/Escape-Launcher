package com.geecee.escapelauncher.feature.securefolder

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI
import com.geecee.escapelauncher.core.ui.composables.LockedFolderCard
import com.geecee.escapelauncher.core.ui.theme.EscapePreviewTheme

/**
 * Button to launch the secure folder on samsung phones.
 *
 * @author George Clensy
 */
@Composable
fun SecureFolderButton() {
    val context = LocalContext.current

    LockedFolderCard {
        LockedAppFolderUI(
            text = stringResource(R.string.launch_secure_folder),
            iconContentDescription = stringResource(R.string.launch_secure_folder)
        ) {
            launchSecureFolder(context = context)
        }
    }
}

@Preview
@Composable
fun SecureFolderButtonPreview() {
    EscapePreviewTheme {
        SecureFolderButton()
    }
}