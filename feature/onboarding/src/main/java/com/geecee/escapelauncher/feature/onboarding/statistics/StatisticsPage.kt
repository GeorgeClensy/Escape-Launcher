package com.geecee.escapelauncher.feature.onboarding.statistics

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R

@Composable
fun StatisticsPage(
    modifier: Modifier = Modifier
) {
    val black = Color.BLACK
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest.toArgb()
    val primary = MaterialTheme.colorScheme.primary.toArgb()
    val secondary = MaterialTheme.colorScheme.secondary.toArgb()
    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.good_phone_bad_phone) // They are freinds, the escape launcher fun never ends
    )
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = surface,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primary,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "SearchBar", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = onSurface,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "Indicator", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = onSurface,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "Ellipse 1", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = onSurface,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "Ellipse 2", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = onSurface,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "Ellipse 3", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = secondary,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "Notification", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = black,
            keyPath = arrayOf("Bad phone container", "Bad Phone", "Camera Cutout", "**")
        ),

        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = surface,
            keyPath = arrayOf("Good phone container", "Good phone", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primary,
            keyPath = arrayOf("Good phone container", "Good phone", "App 1", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primary,
            keyPath = arrayOf("Good phone container", "Good phone", "App 2", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primary,
            keyPath = arrayOf("Good phone container", "Good phone", "App 3", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primary,
            keyPath = arrayOf("Good phone container", "Good phone", "App 4", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primary,
            keyPath = arrayOf("Good phone container", "Good phone", "Clock", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = primary,
            keyPath = arrayOf("Good phone container", "Good phone", "Widgets", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = black,
            keyPath = arrayOf("Good phone container", "Good phone", "Camera Cutout", "**")
        ),
    )

    Box(
        modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .align(Alignment.Center)
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                dynamicProperties = dynamicProperties,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                stringResource(R.string.you_spend_too_long_scrolling),
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.escaoe_launcher_replaces_your_homescreen),
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(50.dp))
        }
    }
}

@Preview
@Composable
fun PrevStatisticsPage() {
    EscapeThemePreview {
        Box(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
        ) {
            StatisticsPage()
        }
    }
}