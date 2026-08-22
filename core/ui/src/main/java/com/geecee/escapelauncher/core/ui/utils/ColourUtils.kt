package com.geecee.escapelauncher.core.ui.utils

import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as AndroidColor

/**
 * Convert a Compose Colour to an Android Colour.
 *
 * @return The Android Colour as an integer.
 */
fun ComposeColor.toAndroidColor(): Int {
    return AndroidColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}