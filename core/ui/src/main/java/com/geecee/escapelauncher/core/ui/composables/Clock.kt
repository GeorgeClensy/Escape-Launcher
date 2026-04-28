package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.primaryContentColor

private const val BIG_TIME_FORMAT = "%02d\n%02d"
private const val SMALL_TIME_FORMAT = "%02d:%02d"

private object ClockDefaults {
    val SmallClockOffsetX = (-2).dp
    val SmallClockOffsetY = 5.dp
}

@Composable
fun Clock(
    hour: Int,
    minute: Int,
    bigClock: Boolean,
    onClockClick: () -> Unit,
    homeAlignment: Alignment.Horizontal
) {
    val text = if (bigClock) BIG_TIME_FORMAT.format(hour, minute)
    else SMALL_TIME_FORMAT.format(hour, minute)

    Text(
        text = text,
        modifier = Modifier
            .clickable { onClockClick() }
            .offset(
                x = if (bigClock) 0.dp else ClockDefaults.SmallClockOffsetX,
                y = if (bigClock) 0.dp else ClockDefaults.SmallClockOffsetY
            ),
        color = primaryContentColor,
        fontWeight = FontWeight.SemiBold,
        style =
            if (bigClock)
                MaterialTheme.typography.headlineLarge.copy(
                    fontFeatureSettings = "tnum"
                )
            else
                MaterialTheme.typography.titleLarge,
        textAlign = when (homeAlignment) {
            Alignment.Start -> TextAlign.Start
            Alignment.End -> TextAlign.End
            else -> TextAlign.Center
        }
    )
}