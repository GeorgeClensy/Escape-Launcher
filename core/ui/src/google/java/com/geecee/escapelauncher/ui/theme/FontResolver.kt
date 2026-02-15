package com.geecee.escapelauncher.ui.theme

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.geecee.escapelauncher.core.ui.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@Suppress("DiscouragedApi")
fun getFontFamily(context: Context, fontName: String): FontFamily {
    val resourceId = context.resources.getIdentifier(
        fontName.lowercase().replace(" ", "_"),
        "font",
        context.packageName
    )

    return if (resourceId != 0) {
        FontFamily(androidx.compose.ui.text.font.Font(resourceId))
    } else FontFamily(
        Font(
            googleFont = GoogleFont(fontName),
            fontProvider = provider
        )
    )
}
