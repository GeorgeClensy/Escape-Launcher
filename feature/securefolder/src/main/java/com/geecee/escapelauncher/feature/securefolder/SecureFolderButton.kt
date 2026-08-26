package com.geecee.escapelauncher.feature.securefolder

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.composables.LockedAppFolderUI
import com.geecee.escapelauncher.core.ui.composables.LockedFolderCard
import com.geecee.escapelauncher.core.ui.vectors.getPrivateSpaceLockedImage

/**
 * Button to launch the secure folder on Samsung phones.
 *
 * @author George Clensy
 */
@Composable
fun SecureFolderButton() {
    val context = LocalContext.current

    LockedFolderCard {
        LockedAppFolderUI(
            text = stringResource(com.geecee.escapelauncher.core.ui.R.string.launch_secure_folder),
            image = getPrivateSpaceLockedImage(),
            iconContentDescription = stringResource(com.geecee.escapelauncher.core.ui.R.string.launch_secure_folder)
        ) {
            launchSecureFolder(context = context)
        }
    }
}

@Preview
@Composable
fun SecureFolderButtonPreview() {
    EscapeThemePreview {
        SecureFolderButton()
    }
}