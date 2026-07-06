package com.geecee.escapelauncher.feature.onboarding.finished

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.BlurryCircle
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor

@Composable
fun FinishedPage(
    isShown: Boolean
) {
    // Animation in middle stuff
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.finished_animation)
    )
    val lottieAnimatable = rememberLottieAnimatable()
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer.toArgb()
    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer.toArgb()
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primaryContainerColor,
            keyPath = arrayOf("Middle", "shape","**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = onPrimaryContainerColor,
            keyPath = arrayOf("Middle", "check", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = secondaryContainerColor,
            keyPath = arrayOf("Behind 1", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = tertiaryContainerColor,
            keyPath = arrayOf("Behind 3", "**")
        )
    )
    LaunchedEffect(isShown) {
        if (isShown) {
            lottieAnimatable.snapTo(composition = composition, progress = 0f) // Reset to start frame
            lottieAnimatable.animate(
                composition = composition,
                iterations = 1
            )
        }
    }


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
        Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y = (-62).dp)
        ) {
            LottieAnimation(
                composition = composition,
                progress = { lottieAnimatable.progress },
                dynamicProperties = dynamicProperties,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                stringResource(id = R.string.all_setup),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = Color(onPrimaryContainerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
            )
        }

        BlurryCircle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset {
                    IntOffset(
                        x = 100.dp.roundToPx(),
                        y = 200.dp.roundToPx() + rightYOffset.dp.roundToPx()
                    )
                },
            circleColor = MaterialTheme.colorScheme.tertiary.toAndroidColor()
        )

        BlurryCircle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset {
                    IntOffset(
                        x = -(100).dp.roundToPx() + leftCircleXOffset.dp.roundToPx(),
                        y = 100.dp.roundToPx() + leftCirlceYOffset.dp.roundToPx()
                    )
                },
            circleColor = MaterialTheme.colorScheme.primary.toAndroidColor()
        )
    }
}

@Preview
@Composable
fun PrevFinishedPage() {
    EscapeThemePreview {
        FinishedPage(
            isShown = true
        )
    }
}
