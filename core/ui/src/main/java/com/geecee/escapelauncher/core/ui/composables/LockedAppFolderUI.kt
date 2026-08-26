package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.vectors.getPrivateSpaceLockedImage

/**
 * UI for a locked folder to be used at the bottom of the apps list.
 * Examples of locked folders are Android 15's private space and Samsung's Secure folder.
 * The UI is based of the private space UI on the pixel launcher.
 *
 * @param modifier Modifier to apply to the UI.
 * @param text Text to display in the UI.
 * @param iconContentDescription Content description for the lock icon.
 * @param unlockClick Action to perform when the clicked.
 *
 * @author George Clensy
 */
@Composable
fun LockedAppFolderUI(
    modifier: Modifier = Modifier,
    image: ImageVector,
    text: String,
    subhead: String = "",
    iconContentDescription: String,
    unlockClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .align(
                    Alignment.Center
                )
                .padding(horizontal = 30.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberVectorPainter(image = image),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )

            Spacer(Modifier.height(15.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
            )

            Text(
                text = subhead,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
            )

            Spacer(Modifier.height(25.dp))

            Button(
                onClick = unlockClick, modifier = Modifier.size(height = 100.dp, width = 200.dp)
            ) {
                Text(
                    text = stringResource(R.string.unlock),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun LockedAppFolderUIPreview() {
    EscapeThemePreview {
        LockedAppFolderUI(
            text = "Private Space",
            image = getPrivateSpaceLockedImage(),
            iconContentDescription = "Lock Private Space",
            subhead = "Private spaced is locked. Unlock to use your private apps.",
            unlockClick = {})
    }
}
