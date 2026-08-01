package com.geecee.escapelauncher.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    ESCAPE_THEME(13);

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
    val isDark = isSystemInDarkTheme()

    return when (this) {
        AppColourScheme.ESCAPE_THEME -> darkSchemeEscapeTheme
        
        AppColourScheme.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) darkColorScheme() else lightColorScheme()
            }
        }

        else -> {
            // Dynamic generation from seed if available
            seedColor?.let {
                DynamicThemeUtils.generateColorSchemeFromSeed(
                    seedColor = it,
                    isDark = isDark,
                    isMonochrome = this == AppColourScheme.MONOCHROME
                )
            } ?: darkSchemeEscapeTheme
        }
    }
}
