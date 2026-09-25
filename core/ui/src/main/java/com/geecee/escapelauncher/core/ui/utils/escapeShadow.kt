package com.geecee.escapelauncher.core.ui.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow


fun escapeShadow(
    color: androidx.compose.ui.graphics.Color, enabled: Boolean
): Shadow {
    return Shadow(
        color = color,
        offset = if (enabled) Offset(x = 2f, y = 2f) else Offset(x = 0f, y = 0f),
        blurRadius = if (enabled) 8f else 0f
    )
}