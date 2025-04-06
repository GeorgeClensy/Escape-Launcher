package com.geecee.escapelauncher.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.utils.getStringSetting

val PitchDarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onBackgroundDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = pitchBlackBackground,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val offLightScheme = lightColorScheme(
    primary = primaryLightOffWhite,
    onPrimary = onPrimaryLightOffWhite,
    primaryContainer = primaryContainerLightOffWhite,
    onPrimaryContainer = onBackgroundLightOffWhite,
    secondary = secondaryLightOffWhite,
    onSecondary = onSecondaryLightOffWhite,
    secondaryContainer = secondaryContainerLightOffWhite,
    onSecondaryContainer = onSecondaryContainerLightOffWhite,
    tertiary = tertiaryLightOffWhite,
    onTertiary = onTertiaryLightOffWhite,
    tertiaryContainer = tertiaryContainerLightOffWhite,
    onTertiaryContainer = onTertiaryContainerLightOffWhite,
    error = errorLightOffWhite,
    onError = onErrorLightOffWhite,
    errorContainer = errorContainerLightOffWhite,
    onErrorContainer = onErrorContainerLightOffWhite,
    background = backgroundLightOffWhite,
    onBackground = onBackgroundLightOffWhite,
    surface = surfaceLightOffWhite,
    onSurface = onSurfaceLightOffWhite,
    surfaceVariant = surfaceVariantLightOffWhite,
    onSurfaceVariant = onSurfaceVariantLightOffWhite,
    outline = outlineLightOffWhite,
    outlineVariant = outlineVariantLightOffWhite,
    scrim = scrimLightOffWhite,
    inverseSurface = inverseSurfaceLightOffWhite,
    inverseOnSurface = inverseOnSurfaceLightOffWhite,
    inversePrimary = inversePrimaryLightOffWhite,
    surfaceDim = surfaceDimLightOffWhite,
    surfaceBright = surfaceBrightLightOffWhite,
    surfaceContainerLowest = surfaceContainerLowestLightOffWhite,
    surfaceContainerLow = surfaceContainerLowLightOffWhite,
    surfaceContainer = surfaceContainerLightOffWhite,
    surfaceContainerHigh = surfaceContainerHighLightOffWhite,
    surfaceContainerHighest = surfaceContainerHighestLightOffWhite,
)

val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onBackgroundLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onBackgroundDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val lightSchemeRed = lightColorScheme(
    primary = primaryLightRed,
    onPrimary = onPrimaryLightRed,
    primaryContainer = primaryContainerLightRed,
    onPrimaryContainer = onPrimaryContainerLightRed,
    secondary = secondaryLightRed,
    onSecondary = onSecondaryLightRed,
    secondaryContainer = secondaryContainerLightRed,
    onSecondaryContainer = onSecondaryContainerLightRed,
    tertiary = tertiaryLightRed,
    onTertiary = onTertiaryLightRed,
    tertiaryContainer = tertiaryContainerLightRed,
    onTertiaryContainer = onTertiaryContainerLightRed,
    error = errorLightRed,
    onError = onErrorLightRed,
    errorContainer = errorContainerLightRed,
    onErrorContainer = onErrorContainerLightRed,
    background = backgroundLightRed,
    onBackground = onBackgroundLightRed,
    surface = surfaceLightRed,
    onSurface = onSurfaceLightRed,
    surfaceVariant = surfaceVariantLightRed,
    onSurfaceVariant = onSurfaceVariantLightRed,
    outline = outlineLightRed,
    outlineVariant = outlineVariantLightRed,
    scrim = scrimLightRed,
    inverseSurface = inverseSurfaceLightRed,
    inverseOnSurface = inverseOnSurfaceLightRed,
    inversePrimary = inversePrimaryLightRed,
    surfaceDim = surfaceDimLightRed,
    surfaceBright = surfaceBrightLightRed,
    surfaceContainerLowest = surfaceContainerLowestLightRed,
    surfaceContainerLow = surfaceContainerLowLightRed,
    surfaceContainer = surfaceContainerLightRed,
    surfaceContainerHigh = surfaceContainerHighLightRed,
    surfaceContainerHighest = surfaceContainerHighestLightRed,
)

val darkSchemeRed = darkColorScheme(
    primary = primaryDarkRed,
    onPrimary = onPrimaryDarkRed,
    primaryContainer = primaryContainerDarkRed,
    onPrimaryContainer = onPrimaryContainerDarkRed,
    secondary = secondaryDarkRed,
    onSecondary = onSecondaryDarkRed,
    secondaryContainer = secondaryContainerDarkRed,
    onSecondaryContainer = onSecondaryContainerDarkRed,
    tertiary = tertiaryDarkRed,
    onTertiary = onTertiaryDarkRed,
    tertiaryContainer = tertiaryContainerDarkRed,
    onTertiaryContainer = onTertiaryContainerDarkRed,
    error = errorDarkRed,
    onError = onErrorDarkRed,
    errorContainer = errorContainerDarkRed,
    onErrorContainer = onErrorContainerDarkRed,
    background = backgroundDarkRed,
    onBackground = onBackgroundDarkRed,
    surface = surfaceDarkRed,
    onSurface = onSurfaceDarkRed,
    surfaceVariant = surfaceVariantDarkRed,
    onSurfaceVariant = onSurfaceVariantDarkRed,
    outline = outlineDarkRed,
    outlineVariant = outlineVariantDarkRed,
    scrim = scrimDarkRed,
    inverseSurface = inverseSurfaceDarkRed,
    inverseOnSurface = inverseOnSurfaceDarkRed,
    inversePrimary = inversePrimaryDarkRed,
    surfaceDim = surfaceDimDarkRed,
    surfaceBright = surfaceBrightDarkRed,
    surfaceContainerLowest = surfaceContainerLowestDarkRed,
    surfaceContainerLow = surfaceContainerLowDarkRed,
    surfaceContainer = surfaceContainerDarkRed,
    surfaceContainerHigh = surfaceContainerHighDarkRed,
    surfaceContainerHighest = surfaceContainerHighestDarkRed,
)

val lightSchemeGreen = lightColorScheme(
    primary = primaryLightGreen,
    onPrimary = onPrimaryLightGreen,
    primaryContainer = primaryContainerLightGreen,
    onPrimaryContainer = onPrimaryContainerLightGreen,
    secondary = secondaryLightGreen,
    onSecondary = onSecondaryLightGreen,
    secondaryContainer = secondaryContainerLightGreen,
    onSecondaryContainer = onSecondaryContainerLightGreen,
    tertiary = tertiaryLightGreen,
    onTertiary = onTertiaryLightGreen,
    tertiaryContainer = tertiaryContainerLightGreen,
    onTertiaryContainer = onTertiaryContainerLightGreen,
    error = errorLightGreen,
    onError = onErrorLightGreen,
    errorContainer = errorContainerLightGreen,
    onErrorContainer = onErrorContainerLightGreen,
    background = backgroundLightGreen,
    onBackground = onBackgroundLightGreen,
    surface = surfaceLightGreen,
    onSurface = onSurfaceLightGreen,
    surfaceVariant = surfaceVariantLightGreen,
    onSurfaceVariant = onSurfaceVariantLightGreen,
    outline = outlineLightGreen,
    outlineVariant = outlineVariantLightGreen,
    scrim = scrimLightGreen,
    inverseSurface = inverseSurfaceLightGreen,
    inverseOnSurface = inverseOnSurfaceLightGreen,
    inversePrimary = inversePrimaryLightGreen,
    surfaceDim = surfaceDimLightGreen,
    surfaceBright = surfaceBrightLightGreen,
    surfaceContainerLowest = surfaceContainerLowestLightGreen,
    surfaceContainerLow = surfaceContainerLowLightGreen,
    surfaceContainer = surfaceContainerLightGreen,
    surfaceContainerHigh = surfaceContainerHighLightGreen,
    surfaceContainerHighest = surfaceContainerHighestLightGreen,
)

val darkSchemeGreen = darkColorScheme(
    primary = primaryDarkGreen,
    onPrimary = onPrimaryDarkGreen,
    primaryContainer = primaryContainerDarkGreen,
    onPrimaryContainer = onPrimaryContainerDarkGreen,
    secondary = secondaryDarkGreen,
    onSecondary = onSecondaryDarkGreen,
    secondaryContainer = secondaryContainerDarkGreen,
    onSecondaryContainer = onSecondaryContainerDarkGreen,
    tertiary = tertiaryDarkGreen,
    onTertiary = onTertiaryDarkGreen,
    tertiaryContainer = tertiaryContainerDarkGreen,
    onTertiaryContainer = onTertiaryContainerDarkGreen,
    error = errorDarkGreen,
    onError = onErrorDarkGreen,
    errorContainer = errorContainerDarkGreen,
    onErrorContainer = onErrorContainerDarkGreen,
    background = backgroundDarkGreen,
    onBackground = onBackgroundDarkGreen,
    surface = surfaceDarkGreen,
    onSurface = onSurfaceDarkGreen,
    surfaceVariant = surfaceVariantDarkGreen,
    onSurfaceVariant = onSurfaceVariantDarkGreen,
    outline = outlineDarkGreen,
    outlineVariant = outlineVariantDarkGreen,
    scrim = scrimDarkGreen,
    inverseSurface = inverseSurfaceDarkGreen,
    inverseOnSurface = inverseOnSurfaceDarkGreen,
    inversePrimary = inversePrimaryDarkGreen,
    surfaceDim = surfaceDimDarkGreen,
    surfaceBright = surfaceBrightDarkGreen,
    surfaceContainerLowest = surfaceContainerLowestDarkGreen,
    surfaceContainerLow = surfaceContainerLowDarkGreen,
    surfaceContainer = surfaceContainerDarkGreen,
    surfaceContainerHigh = surfaceContainerHighDarkGreen,
    surfaceContainerHighest = surfaceContainerHighestDarkGreen,
)

val lightSchemeBlue = lightColorScheme(
    primary = primaryLightBlue,
    onPrimary = onPrimaryLightBlue,
    primaryContainer = primaryContainerLightBlue,
    onPrimaryContainer = onPrimaryContainerLightBlue,
    secondary = secondaryLightBlue,
    onSecondary = onSecondaryLightBlue,
    secondaryContainer = secondaryContainerLightBlue,
    onSecondaryContainer = onSecondaryContainerLightBlue,
    tertiary = tertiaryLightBlue,
    onTertiary = onTertiaryLightBlue,
    tertiaryContainer = tertiaryContainerLightBlue,
    onTertiaryContainer = onTertiaryContainerLightBlue,
    error = errorLightBlue,
    onError = onErrorLightBlue,
    errorContainer = errorContainerLightBlue,
    onErrorContainer = onErrorContainerLightBlue,
    background = backgroundLightBlue,
    onBackground = onBackgroundLightBlue,
    surface = surfaceLightBlue,
    onSurface = onSurfaceLightBlue,
    surfaceVariant = surfaceVariantLightBlue,
    onSurfaceVariant = onSurfaceVariantLightBlue,
    outline = outlineLightBlue,
    outlineVariant = outlineVariantLightBlue,
    scrim = scrimLightBlue,
    inverseSurface = inverseSurfaceLightBlue,
    inverseOnSurface = inverseOnSurfaceLightBlue,
    inversePrimary = inversePrimaryLightBlue,
    surfaceDim = surfaceDimLightBlue,
    surfaceBright = surfaceBrightLightBlue,
    surfaceContainerLowest = surfaceContainerLowestLightBlue,
    surfaceContainerLow = surfaceContainerLowLightBlue,
    surfaceContainer = surfaceContainerLightBlue,
    surfaceContainerHigh = surfaceContainerHighLightBlue,
    surfaceContainerHighest = surfaceContainerHighestLightBlue,
)

val darkSchemeBlue = darkColorScheme(
    primary = primaryDarkBlue,
    onPrimary = onPrimaryDarkBlue,
    primaryContainer = primaryContainerDarkBlue,
    onPrimaryContainer = onPrimaryContainerDarkBlue,
    secondary = secondaryDarkBlue,
    onSecondary = onSecondaryDarkBlue,
    secondaryContainer = secondaryContainerDarkBlue,
    onSecondaryContainer = onSecondaryContainerDarkBlue,
    tertiary = tertiaryDarkBlue,
    onTertiary = onTertiaryDarkBlue,
    tertiaryContainer = tertiaryContainerDarkBlue,
    onTertiaryContainer = onTertiaryContainerDarkBlue,
    error = errorDarkBlue,
    onError = onErrorDarkBlue,
    errorContainer = errorContainerDarkBlue,
    onErrorContainer = onErrorContainerDarkBlue,
    background = backgroundDarkBlue,
    onBackground = onBackgroundDarkBlue,
    surface = surfaceDarkBlue,
    onSurface = onSurfaceDarkBlue,
    surfaceVariant = surfaceVariantDarkBlue,
    onSurfaceVariant = onSurfaceVariantDarkBlue,
    outline = outlineDarkBlue,
    outlineVariant = outlineVariantDarkBlue,
    scrim = scrimDarkBlue,
    inverseSurface = inverseSurfaceDarkBlue,
    inverseOnSurface = inverseOnSurfaceDarkBlue,
    inversePrimary = inversePrimaryDarkBlue,
    surfaceDim = surfaceDimDarkBlue,
    surfaceBright = surfaceBrightDarkBlue,
    surfaceContainerLowest = surfaceContainerLowestDarkBlue,
    surfaceContainerLow = surfaceContainerLowDarkBlue,
    surfaceContainer = surfaceContainerDarkBlue,
    surfaceContainerHigh = surfaceContainerHighDarkBlue,
    surfaceContainerHighest = surfaceContainerHighestDarkBlue,
)

val lightSchemeYellow = lightColorScheme(
    primary = primaryLightYellow,
    onPrimary = onPrimaryLightYellow,
    primaryContainer = primaryContainerLightYellow,
    onPrimaryContainer = onPrimaryContainerLightYellow,
    secondary = secondaryLightYellow,
    onSecondary = onSecondaryLightYellow,
    secondaryContainer = secondaryContainerLightYellow,
    onSecondaryContainer = onSecondaryContainerLightYellow,
    tertiary = tertiaryLightYellow,
    onTertiary = onTertiaryLightYellow,
    tertiaryContainer = tertiaryContainerLightYellow,
    onTertiaryContainer = onTertiaryContainerLightYellow,
    error = errorLightYellow,
    onError = onErrorLightYellow,
    errorContainer = errorContainerLightYellow,
    onErrorContainer = onErrorContainerLightYellow,
    background = backgroundLightYellow,
    onBackground = onBackgroundLightYellow,
    surface = surfaceLightYellow,
    onSurface = onSurfaceLightYellow,
    surfaceVariant = surfaceVariantLightYellow,
    onSurfaceVariant = onSurfaceVariantLightYellow,
    outline = outlineLightYellow,
    outlineVariant = outlineVariantLightYellow,
    scrim = scrimLightYellow,
    inverseSurface = inverseSurfaceLightYellow,
    inverseOnSurface = inverseOnSurfaceLightYellow,
    inversePrimary = inversePrimaryLightYellow,
    surfaceDim = surfaceDimLightYellow,
    surfaceBright = surfaceBrightLightYellow,
    surfaceContainerLowest = surfaceContainerLowestLightYellow,
    surfaceContainerLow = surfaceContainerLowLightYellow,
    surfaceContainer = surfaceContainerLightYellow,
    surfaceContainerHigh = surfaceContainerHighLightYellow,
    surfaceContainerHighest = surfaceContainerHighestLightYellow,
)

val darkSchemeYellow = darkColorScheme(
    primary = primaryDarkYellow,
    onPrimary = onPrimaryDarkYellow,
    primaryContainer = primaryContainerDarkYellow,
    onPrimaryContainer = onPrimaryContainerDarkYellow,
    secondary = secondaryDarkYellow,
    onSecondary = onSecondaryDarkYellow,
    secondaryContainer = secondaryContainerDarkYellow,
    onSecondaryContainer = onSecondaryContainerDarkYellow,
    tertiary = tertiaryDarkYellow,
    onTertiary = onTertiaryDarkYellow,
    tertiaryContainer = tertiaryContainerDarkYellow,
    onTertiaryContainer = onTertiaryContainerDarkYellow,
    error = errorDarkYellow,
    onError = onErrorDarkYellow,
    errorContainer = errorContainerDarkYellow,
    onErrorContainer = onErrorContainerDarkYellow,
    background = backgroundDarkYellow,
    onBackground = onBackgroundDarkYellow,
    surface = surfaceDarkYellow,
    onSurface = onSurfaceDarkYellow,
    surfaceVariant = surfaceVariantDarkYellow,
    onSurfaceVariant = onSurfaceVariantDarkYellow,
    outline = outlineDarkYellow,
    outlineVariant = outlineVariantDarkYellow,
    scrim = scrimDarkYellow,
    inverseSurface = inverseSurfaceDarkYellow,
    inverseOnSurface = inverseOnSurfaceDarkYellow,
    inversePrimary = inversePrimaryDarkYellow,
    surfaceDim = surfaceDimDarkYellow,
    surfaceBright = surfaceBrightDarkYellow,
    surfaceContainerLowest = surfaceContainerLowestDarkYellow,
    surfaceContainerLow = surfaceContainerLowDarkYellow,
    surfaceContainer = surfaceContainerDarkYellow,
    surfaceContainerHigh = surfaceContainerHighDarkYellow,
    surfaceContainerHighest = surfaceContainerHighestDarkYellow,
)

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@Composable
fun EscapeTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    // Set theme
    when (sharedPreferencesSettings.getInt(stringResource(R.string.Theme), 11)) {
        0 -> {
            colorScheme = darkScheme
        }

        1 -> {
            colorScheme = lightScheme
        }

        2 -> {
            colorScheme = PitchDarkColorScheme
        }

        3 -> {
            colorScheme = lightSchemeRed
        }

        4 -> {
            colorScheme = darkSchemeRed
        }

        5 -> {
            colorScheme = lightSchemeGreen
        }

        6 -> {
            colorScheme = darkSchemeGreen
        }

        7 -> {
            colorScheme = lightSchemeBlue
        }

        8 -> {
            colorScheme = darkSchemeBlue
        }

        9 -> {
            colorScheme = lightSchemeYellow
        }

        10 -> {
            colorScheme = darkSchemeYellow
        }

        11 -> {
            colorScheme = offLightScheme
        }

        else -> {
            colorScheme = darkScheme
        }
    }

    // Make the typography
    val fontFamily = remember {
        mutableStateOf(
            FontFamily(
                Font(
                    googleFont = GoogleFont(
                        getStringSetting(
                            context,
                            context.resources.getString(R.string.Font),
                            "Jost"
                        ), true
                    ),
                    fontProvider = provider
                )
            )
        )
    }

    val typography = Typography(
        headlineLarge = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 66.sp,
            lineHeight = 53.sp,
            letterSpacing = 0.6.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 62.sp,
            lineHeight = 49.sp,
            letterSpacing = 0.6.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 58.sp,
            lineHeight = 45.sp,
            letterSpacing = 0.6.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 52.sp,
            lineHeight = 53.sp,
            letterSpacing = 0.6.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 48.sp,
            lineHeight = 49.sp,
            letterSpacing = 0.6.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 44.sp,
            lineHeight = 45.sp,
            letterSpacing = 0.6.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 29.sp,
            letterSpacing = 0.6.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 25.sp,
            letterSpacing = 0.6.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.6.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}