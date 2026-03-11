package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.geecee.escapelauncher.core.ui.theme.BackgroundColor

@Composable
fun ListGradient(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundColor.copy(alpha = 0f),
                        BackgroundColor
                    )
                )
            )
    )
}