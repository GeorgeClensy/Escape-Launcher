package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    var isMorphed by remember { mutableStateOf(false) }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonHeight = if (isMorphed || isPressed) 90.dp else 100.dp

    val animatedButtonHeight by animateDpAsState(
        targetValue = buttonHeight, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "UnlockButtonAnimation"
    )


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
                onClick = {
                    if (!isMorphed) {
                        isMorphed = true
                        scope.launch {
                            delay(200.milliseconds)
                            isMorphed = false
                        }
                    } else {
                        isMorphed = false
                    }

                    unlockClick()
                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .height(height = animatedButtonHeight)
                    .aspectRatio(2f)
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
