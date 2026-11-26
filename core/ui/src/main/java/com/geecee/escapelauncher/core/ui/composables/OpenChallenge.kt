package com.geecee.escapelauncher.core.ui.composables

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OpenChallenge(
    enabled: Boolean,
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    goBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    // Haptics are presentation only; the ViewModel owns timing and launch decisions.
    LaunchedEffect(remainingSeconds) {
        if (enabled && remainingSeconds in 1..5) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = modifier
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
        if (remainingSeconds > 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = remainingSeconds.toString(),
                    modifier = Modifier.padding(32.dp),
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center
                )

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

@Preview(showBackground = true)
@Composable
private fun OpenChallengePreview() {
    MaterialTheme {
        OpenChallenge(
            enabled = false,
            remainingSeconds = 20,
            goBack = {}
        )
    }
}
