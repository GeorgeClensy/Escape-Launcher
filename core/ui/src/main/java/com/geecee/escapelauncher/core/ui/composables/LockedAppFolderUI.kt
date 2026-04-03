package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.theme.ContentColor
import com.geecee.escapelauncher.core.ui.theme.EscapePreviewTheme
import com.geecee.escapelauncher.core.ui.theme.SecondaryCardContainerColor

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
    text: String,
    iconContentDescription: String,
    unlockClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(20.dp)
            .fillMaxWidth()
            .clickable(onClick = unlockClick)
    ) {
        Text(
            text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(
                alignment = Alignment.CenterStart
            )
        )

        IconButton(
            onClick = {
                unlockClick()
            }, modifier = Modifier.align(
                alignment = Alignment.CenterEnd
            ), colors = IconButtonColors(
                containerColor = SecondaryCardContainerColor,
                contentColor = ContentColor,
                disabledContainerColor = SecondaryCardContainerColor,
                disabledContentColor = ContentColor
            )
        ) {
            Icon(
                imageVector = Icons.Default.Lock, contentDescription = iconContentDescription
            )
        }
    }
}

@Preview
@Composable
fun LockedAppFolderUIPreview() {
    EscapePreviewTheme {
        LockedAppFolderUI(
            text = "Private Space",
            iconContentDescription = "Lock Private Space",
            unlockClick = {}
        )
    }
}

@Preview
@Composable
fun LockedAppFolderUIPreviewWithBackgroundCard() {
    EscapePreviewTheme {
        LockedFolderCard  {
            LockedAppFolderUI(
                text = "Private Space",
                iconContentDescription = "Lock Private Space",
                unlockClick = {}
            )
        }
    }
}