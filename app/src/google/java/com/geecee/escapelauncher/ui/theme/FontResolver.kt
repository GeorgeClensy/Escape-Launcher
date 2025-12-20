package com.geecee.escapelauncher.ui.theme

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.geecee.escapelauncher.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

fun getFontFamily(context: Context, fontName: String): FontFamily {
    return FontFamily(
        Font(
            googleFont = GoogleFont(fontName),
            fontProvider = provider
        )
    )
}
