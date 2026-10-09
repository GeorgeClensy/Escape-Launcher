package com.geecee.escapelauncher.core.theme.colours

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.platform.LocalContext

/**
 * Enumeration of available color schemes in the application.
 * Each entry provides a seed color used for dynamic palette generation.
 */
enum class AppColourScheme(val id: Int, val seedColor: Color? = null) {
    MONOCHROME(0, Color(0xFF676767)),
    RED(3, Color(0xFF8F4C38)),
    ORANGE(11, Color(0xFF8C4F28)),
    YELLOW(9, Color(0xFF6D5E0F)),
    GREEN(5, Color(0xFF4C662B)),
    TEAL(14, Color(0xFF006A6A)),
    BLUE(7, Color(0xFF415F91)),
    PURPLE(15, Color(0xFF6750A4)),
    PINK(16, Color(0xFF984061)),
    SYSTEM(12),
    WALLPAPER(17),
    ESCAPE_THEME(13, Color(0xFFB2D8D8));

    companion object {
        /**
         * Resolves an ID to its corresponding [AppColourScheme].
         * Defaults to [ESCAPE_THEME] if the ID is not found.
         */
        fun fromId(id: Int): AppColourScheme =
            entries.find { it.id == id } ?: ESCAPE_THEME

        /**
         * List of themes displayed in the settings menu, ordered logically.
         */
        val selectableThemes = listOf(
            ESCAPE_THEME,
            SYSTEM,
            WALLPAPER,
            RED,
            ORANGE,
            YELLOW,
            GREEN,
            TEAL,
            BLUE,
            PURPLE,
            PINK,
            MONOCHROME,
        )
    }
}

/**
 * Resolves the [AppColourScheme] to a Material 3 [ColorScheme].
 */
@Composable
fun AppColourScheme.resolveColorScheme(): ColorScheme {
    return resolveColorScheme(LocalContext.current, isSystemInDarkTheme())
}

/**
 * Non-composable variant of [resolveColorScheme] so callers can cache the result with `remember`;
 * generating a scheme from a seed evaluates ~50 HCT colours and shouldn't run every recomposition.
 */
fun AppColourScheme.resolveColorScheme(context: Context, isDark: Boolean): ColorScheme {
    return when (this) {
        //AppColourScheme.ESCAPE_THEME -> darkSchemeEscapeTheme

        AppColourScheme.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) darkColorScheme() else lightColorScheme()
            }
        }

        AppColourScheme.WALLPAPER -> {
            val seed = getWallpaperColorCompat(context) ?: Color(0xFFFF5722)
            Log.d("resolveColorScheme", "Using wallpaper color: ${seed.toColorLong()}")
            DynamicColourSchemeUtils.generateColorSchemeFromSeed(seed, isDark)
        }

        else -> {
            // Dynamic generation from seed if available
            seedColor?.let {
                DynamicColourSchemeUtils.generateColorSchemeFromSeed(
                    seedColor = it,
                    isDark = isDark,
                    isMonochrome = this == AppColourScheme.MONOCHROME
                )
            } ?: darkSchemeEscapeTheme
        }
    }
}

private fun getWallpaperColorCompat(context: Context): Color? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val colors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            colors?.primaryColor?.toArgb()?.let { return Color(it) }
        } catch (e: Exception) {
            Log.e("getWallpaperColorCompat", "Error getting wallpaper color", e)
        }
    }
    return null
}