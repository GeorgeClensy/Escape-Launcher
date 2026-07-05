package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.theme.primaryContentColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BouncyMorphingFab(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    var isMorphed by remember { mutableStateOf(false) }
    val isPressed by interactionSource.collectIsPressedAsState()

    val radius = if (isMorphed || isPressed) 32.dp else 16.dp

    val animatedRadius by animateDpAsState(
        targetValue = radius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "FabCornerAnimation"
    )

    MediumExtendedFloatingActionButton(
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

            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(animatedRadius),
        interactionSource = interactionSource,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        AnimatedContent(
            targetState = icon,
            label = "InternalIconTransition"
        ) { targetIcon ->
            Icon(
                imageVector = targetIcon,
                contentDescription = contentDescription
            )
        }
    }
}

@Preview
@Composable
fun PrevBouncyMorphingFab() {
    EscapeThemePreview {
        BouncyMorphingFab (
            modifier = Modifier,
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = "Continue",
            containerColor = primaryContentColor,
            contentColor = BackgroundColor,
            onClick = { }
        )
    }
}