package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OpenChallenge(
    enabled: Boolean,
    openApp: () -> Unit,
    goBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    val steps = listOf("5", "4", "3", "2", "1")
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var showText by rememberSaveable { mutableStateOf(true) }
    var nextScreen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (nextScreen) return@LaunchedEffect

        while (stepIndex < steps.size) {
            if (showText) {
                delay(3000.milliseconds)
                showText = false
            }

            delay(1000.milliseconds)
            stepIndex++

            if (stepIndex < steps.size) {
                showText = true
                if (enabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            } else {
                nextScreen = true
                if (enabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                delay(500.milliseconds)
                openApp()
            }
        }
    }

    val currentText = if (stepIndex < steps.size) steps[stepIndex] else ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryFixed,
                        MaterialTheme.colorScheme.secondaryFixed
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        if (!nextScreen) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = showText,
                    enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 1000))
                ) {
                    Text(
                        text = currentText,
                        modifier = Modifier.padding(32.dp),
                        color = MaterialTheme.colorScheme.onPrimaryFixed,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        if (enabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        goBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryFixed,
                        contentColor = MaterialTheme.colorScheme.primaryFixed
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            }
        }
    }
}
