package com.geecee.escapelauncher.feature.onboarding.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.BlurryCircle
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor
import kotlinx.coroutines.launch

@Composable
fun WelcomePage(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Tapping the logo stuff
    val haptics = LocalHapticFeedback.current
    val iconScale = remember { Animatable(initialValue = 1f) }

    // Stuff to make the cirlces move
    val infiniteTransition = rememberInfiniteTransition(label = "FloatingCircles")
    val leftCirlceYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 35f, // Moves down by 35dp
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse // Bounces smoothly back and forth
        ),
        label = "PrimaryCircleY"
    )
    val leftCircleXOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f, // Moves right by 20dp
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = EaseInOutSine
            ), // Different duration so it feels organic
            repeatMode = RepeatMode.Reverse
        ),
        label = "PrimaryCircleX"
    )
    val rightYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -45f, // Moves up by 45dp
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TertiaryCircleY"
    )

    Box(
        modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y = (-62).dp)
        ) {
            Icon(
                painterResource(R.drawable.launcher_logo_icon),
                "Escape Launcher Logo",
                Modifier
                    .padding(3.dp)
                    .align(Alignment.CenterHorizontally)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        doHapticFeedBack(
                            hapticFeedback = haptics,
                            enabled = true
                        )

                        coroutineScope.launch {
                            iconScale.animateTo(0.9f, animationSpec = tween(durationMillis = 50))
                            iconScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }

        BlurryCircle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset {
                    IntOffset(
                        x = -(100).dp.roundToPx() + leftCircleXOffset.dp.roundToPx(),
                        y = 100.dp.roundToPx() + leftCirlceYOffset.dp.roundToPx()
                    )
                },
            circleColor = MaterialTheme.colorScheme.primary.toAndroidColor()
        )

        BlurryCircle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset {
                    IntOffset(
                        x = 100.dp.roundToPx(),
                        y = 200.dp.roundToPx() + rightYOffset.dp.roundToPx()
                    )
                },
            circleColor = MaterialTheme.colorScheme.tertiary.toAndroidColor()
        )
    }
}

@Preview
@Composable
fun PrevWelcomePage() {
    EscapeThemePreview {
        WelcomePage()
    }
}
