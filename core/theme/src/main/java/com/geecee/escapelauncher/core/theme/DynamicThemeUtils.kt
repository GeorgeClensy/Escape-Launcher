package com.geecee.escapelauncher.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.SchemeTonalSpot
import android.annotation.SuppressLint

/**
 * Utility to generate a complete Material 3 [ColorScheme] from a single seed color
 * using Google's official Material Color Utilities algorithm.
 */
object DynamicThemeUtils {
    /**
     * Generates a full Material 3 [ColorScheme] from a single seed color
     *
     * @param seedColor the source color to derive the palette from
     * @param isDark whether to build the dark or light variant of the scheme
     * @param contrastLevel -1.0 (low) to 1.0 (high); 0.0 is the Material 3 default
     */
    @SuppressLint("RestrictedApi")
    fun generateColorSchemeFromSeed(
        seedColor: Color,
        isDark: Boolean,
        contrastLevel: Double = 0.0
    ): ColorScheme {
        val scheme = SchemeTonalSpot(Hct.fromInt(seedColor.toArgb()), isDark, contrastLevel)

        return if (isDark) {
            darkColorScheme(
                primary = Color(scheme.primary),
                onPrimary = Color(scheme.onPrimary),
                primaryContainer = Color(scheme.primaryContainer),
                onPrimaryContainer = Color(scheme.onPrimaryContainer),
                inversePrimary = Color(scheme.inversePrimary),
                secondary = Color(scheme.secondary),
                onSecondary = Color(scheme.onSecondary),
                secondaryContainer = Color(scheme.secondaryContainer),
                onSecondaryContainer = Color(scheme.onSecondaryContainer),
                tertiary = Color(scheme.tertiary),
                onTertiary = Color(scheme.onTertiary),
                tertiaryContainer = Color(scheme.tertiaryContainer),
                onTertiaryContainer = Color(scheme.onTertiaryContainer),
                background = Color(scheme.background),
                onBackground = Color(scheme.onBackground),
                surface = Color(scheme.surface),
                onSurface = Color(scheme.onSurface),
                surfaceVariant = Color(scheme.surfaceVariant),
                onSurfaceVariant = Color(scheme.onSurfaceVariant),
                surfaceTint = Color(scheme.primary),
                inverseSurface = Color(scheme.inverseSurface),
                inverseOnSurface = Color(scheme.inverseOnSurface),
                error = Color(scheme.error),
                onError = Color(scheme.onError),
                errorContainer = Color(scheme.errorContainer),
                onErrorContainer = Color(scheme.onErrorContainer),
                outline = Color(scheme.outline),
                outlineVariant = Color(scheme.outlineVariant),
                scrim = Color(scheme.scrim),
                surfaceBright = Color(scheme.surfaceBright),
                surfaceDim = Color(scheme.surfaceDim),
                surfaceContainer = Color(scheme.surfaceContainer),
                surfaceContainerHigh = Color(scheme.surfaceContainerHigh),
                surfaceContainerHighest = Color(scheme.surfaceContainerHighest),
                surfaceContainerLow = Color(scheme.surfaceContainerLow),
                surfaceContainerLowest = Color(scheme.surfaceContainerLowest),
            )
        } else {
            lightColorScheme(
                primary = Color(scheme.primary),
                onPrimary = Color(scheme.onPrimary),
                primaryContainer = Color(scheme.primaryContainer),
                onPrimaryContainer = Color(scheme.onPrimaryContainer),
                inversePrimary = Color(scheme.inversePrimary),
                secondary = Color(scheme.secondary),
                onSecondary = Color(scheme.onSecondary),
                secondaryContainer = Color(scheme.secondaryContainer),
                onSecondaryContainer = Color(scheme.onSecondaryContainer),
                tertiary = Color(scheme.tertiary),
                onTertiary = Color(scheme.onTertiary),
                tertiaryContainer = Color(scheme.tertiaryContainer),
                onTertiaryContainer = Color(scheme.onTertiaryContainer),
                background = Color(scheme.background),
                onBackground = Color(scheme.onBackground),
                surface = Color(scheme.surface),
                onSurface = Color(scheme.onSurface),
                surfaceVariant = Color(scheme.surfaceVariant),
                onSurfaceVariant = Color(scheme.onSurfaceVariant),
                surfaceTint = Color(scheme.primary),
                inverseSurface = Color(scheme.inverseSurface),
                inverseOnSurface = Color(scheme.inverseOnSurface),
                error = Color(scheme.error),
                onError = Color(scheme.onError),
                errorContainer = Color(scheme.errorContainer),
                onErrorContainer = Color(scheme.onErrorContainer),
                outline = Color(scheme.outline),
                outlineVariant = Color(scheme.outlineVariant),
                scrim = Color(scheme.scrim),
                surfaceBright = Color(scheme.surfaceBright),
                surfaceDim = Color(scheme.surfaceDim),
                surfaceContainer = Color(scheme.surfaceContainer),
                surfaceContainerHigh = Color(scheme.surfaceContainerHigh),
                surfaceContainerHighest = Color(scheme.surfaceContainerHighest),
                surfaceContainerLow = Color(scheme.surfaceContainerLow),
                surfaceContainerLowest = Color(scheme.surfaceContainerLowest),
            )
        }
    }
}
