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

enum class AppColourScheme(val id: Int, val seedColor: Color? = null) {
    MONOCHROME(0, Color(0xFF676767)),
    RED(3, Color(0xFF8F4C38)),
    DARK_RED(4, Color(0xFF8F4C38)),
    GREEN(5, Color(0xFF4C662B)),
    DARK_GREEN(6, Color(0xFF4C662B)),
    BLUE(7, Color(0xFF415F91)),
    DARK_BLUE(8, Color(0xFF415F91)),
    YELLOW(9, Color(0xFF6D5E0F)),
    DARK_YELLOW(10, Color(0xFF6D5E0F)),
    OFF_LIGHT(11, Color(0xFF8C4F28)),
    SYSTEM(12),
    ESCAPE_THEME(13);

    companion object {
        fun fromId(id: Int): AppColourScheme =
            entries.find { it.id == id } ?: ESCAPE_THEME
    }
}

@Composable
fun AppColourScheme.resolveColorScheme(): ColorScheme {
    val isDark = isSystemInDarkTheme()

    if (this == AppColourScheme.ESCAPE_THEME) return darkSchemeEscapeTheme
    
    if (this == AppColourScheme.SYSTEM) {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) {
                dynamicDarkColorScheme(LocalContext.current)
            } else {
                dynamicLightColorScheme(LocalContext.current)
            }
        } else {
            if (isDark) darkColorScheme() else lightColorScheme()
        }
    }

    // Dynamic generation from seed if available
    seedColor?.let {
        return DynamicThemeUtils.generateColorSchemeFromSeed(
            seedColor = it,
            isDark = isDark,
            isMonochrome = this == AppColourScheme.MONOCHROME
        )
    }

    // Fallback
    return if (isDark) darkColorScheme() else lightColorScheme()
}
