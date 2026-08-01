package com.geecee.escapelauncher.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class AppColourScheme(val id: Int) {
    DARK(0),
    LIGHT(1),
    PITCH_DARK(2),

    LIGHT_RED(3),
    DARK_RED(4),

    LIGHT_GREEN(5),
    DARK_GREEN(6),

    LIGHT_BLUE(7),
    DARK_BLUE(8),

    LIGHT_YELLOW(9),
    DARK_YELLOW(10),

    OFF_LIGHT(11),
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

    return when (this) {
        AppColourScheme.DARK -> darkScheme
        AppColourScheme.LIGHT -> lightScheme
        AppColourScheme.PITCH_DARK -> PitchDarkColorScheme

        AppColourScheme.LIGHT_RED -> lightSchemeRed
        AppColourScheme.DARK_RED -> darkSchemeRed

        AppColourScheme.LIGHT_GREEN -> lightSchemeGreen
        AppColourScheme.DARK_GREEN -> darkSchemeGreen

        AppColourScheme.LIGHT_BLUE -> lightSchemeBlue
        AppColourScheme.DARK_BLUE -> darkSchemeBlue

        AppColourScheme.LIGHT_YELLOW -> lightSchemeYellow
        AppColourScheme.DARK_YELLOW -> darkSchemeYellow

        AppColourScheme.OFF_LIGHT -> offLightScheme

        AppColourScheme.ESCAPE_THEME -> darkSchemeEscapeTheme

        AppColourScheme.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) {
                    dynamicDarkColorScheme(LocalContext.current)
                } else {
                    dynamicLightColorScheme(LocalContext.current)
                }
            } else {
                if (isDark) darkColorScheme() else lightColorScheme()
            }
        }
    }
}