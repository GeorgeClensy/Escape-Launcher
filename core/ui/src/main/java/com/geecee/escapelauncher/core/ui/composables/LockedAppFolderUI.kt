package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.utils.MorphShape
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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LockedAppFolderUI(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Lock,
    text: String,
    subhead: String = "",
    iconContentDescription: String,
    unlockClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val buttonInteractionSource = remember { MutableInteractionSource() }
    var isButtonMorphed by remember { mutableStateOf(false) }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonHeight = if (isButtonMorphed || isButtonPressed) 90.dp else 100.dp
    val animatedButtonHeight by animateDpAsState(
        targetValue = buttonHeight, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "UnlockButtonAnimation"
    )

    val imageInteractionSource = remember { MutableInteractionSource() }
    var isImageMorphed by remember { mutableStateOf(false) }
    val isImagePressed by imageInteractionSource.collectIsPressedAsState()
    val imageMorph = remember {
        Morph(MaterialShapes.Cookie4Sided, MaterialShapes.Bun)
    }
    val imageMorphValue = if (isImageMorphed || isImagePressed) 1f else 0f
    val imageMorphProgress by animateFloatAsState(
        targetValue = imageMorphValue,
        animationSpec = tween(400),
        label = "polygon_morph_progress"
    )
    val imageSizeValue = if (isImageMorphed || isImagePressed) 0.9f else 1f
    val imageSizeProgress by animateFloatAsState(
        targetValue = imageSizeValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ),
        label = "polygon_morph_progress"
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
            Card(
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = MorphShape(imageMorph, imageMorphProgress),
                modifier = Modifier.size(250.dp).combinedClickable(
                    indication = null,
                    interactionSource = imageInteractionSource,
                    onClick = {
                        if (!isImageMorphed) {
                            isImageMorphed = true
                            scope.launch {
                                delay(400.milliseconds)
                                isImageMorphed = false
                            }
                        } else {
                            isImageMorphed = false
                        }
                    }
                ).scale(imageSizeProgress),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(100.dp),
                        contentDescription = iconContentDescription
                    )
                }
            }

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
                    if (!isButtonMorphed) {
                        isButtonMorphed = true
                        scope.launch {
                            delay(200.milliseconds)
                            isButtonMorphed = false
                        }
                    } else {
                        isButtonMorphed = false
                    }

                    unlockClick()
                },
                interactionSource = buttonInteractionSource,
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
            iconContentDescription = "Lock Private Space",
            subhead = "Private spaced is locked. Unlock to use your private apps.",
            unlockClick = {})
    }
}
