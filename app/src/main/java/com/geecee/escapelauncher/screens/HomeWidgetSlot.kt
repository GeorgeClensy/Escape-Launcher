package com.geecee.escapelauncher.screens

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import com.geecee.escapelauncher.feature.newwidgets.WidgetRenderer

@Composable
fun HomeWidgetSlot(
    homeAlignment: Alignment.Horizontal,
    widgetOffsetPref: Float,
    widgetId: Int,
    widgetHostManager: WidgetHostManager,
    widgetWidth: Float,
    widgetHeight: Float
) {
    val widgetOffset = remember(homeAlignment, widgetOffsetPref) {
        val alignmentOffset = when (homeAlignment) {
            Alignment.Start -> -8
            Alignment.End -> 8
            else -> 0
        }
        alignmentOffset + widgetOffsetPref.toInt()
    }

    WidgetRenderer(
        appWidgetId = widgetId,
        widgetHostManager = widgetHostManager,
        modifier = Modifier
            .offset {
                IntOffset(
                    (widgetOffset.dp).toPx().toInt(), 0
                )
            }
            .size(
                widgetWidth.dp, widgetHeight.dp
            )
            .padding(0.dp, 0.dp)
    )
}