package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.geecee.escapelauncher.core.ui.theme.CardContainerColor
import com.geecee.escapelauncher.core.ui.theme.CardContainerColorDisabled
import com.geecee.escapelauncher.core.ui.theme.ContentColor
import com.geecee.escapelauncher.core.ui.theme.ContentColorDisabled


/**
 * Background card for locked folder UIs. This is to be used with Private Space and Secure folder.
 * It exists so that private space and secur folder can share a similar ui.
 * Private space animates the contents of it so I couldn't just put it on the `LockedAppFolderUI` as that would mean `PrivateSpace` would have two cards so it wouldn't animate nicely.
 *
 * @param modifier Modifier to apply to the UI.
 * @param content Content to display in the UI.
 *
 * @author George Clensy
 */
@Composable
fun LockedFolderCard(
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .animateContentSize(),
        colors = CardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor,
            disabledContentColor = CardContainerColorDisabled,
            disabledContainerColor = ContentColorDisabled,
        )
    ) {
        content()
    }
}