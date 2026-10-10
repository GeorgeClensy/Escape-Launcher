package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.utils.MorphShape
import com.geecee.escapelauncher.core.ui.utils.bouncyClickable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LockedAppFolderUI(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Lock,
    buttonText: String = stringResource(R.string.unlock),
    text: String,
    subhead: String = "",
    iconContentDescription: String,
    unlockClick: () -> Unit
) {
    val imageInteractionSource = remember { MutableInteractionSource() }
    val isImagePressed by imageInteractionSource.collectIsPressedAsState()

    val imageMorph = remember {
        Morph(MaterialShapes.Cookie4Sided, MaterialShapes.Bun)
    }

    val imageMorphProgress by animateFloatAsState(
        targetValue = if (isImagePressed) 1f else 0f,
        animationSpec = tween(400),
        label = "polygon_morph_progress"
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 30.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = MorphShape(imageMorph, imageMorphProgress),
                modifier = Modifier
                    .size(250.dp)
                    // The magic happens here:
                    .bouncyClickable(
                        interactionSource = imageInteractionSource, // Pass it so MorphShape knows when it's pressed!
                        onClick = { /* Image click does nothing, just bounces! */ }
                    ),
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
                textAlign = TextAlign.Center
            )

            Text(
                text = subhead,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(25.dp))

            // Replaced the Button with a Box to avoid default Material ripples fighting the bounce
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(100.dp)
                    .aspectRatio(2f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    // The magic happens here again:
                    .bouncyClickable {
                        unlockClick()
                    }
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
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
