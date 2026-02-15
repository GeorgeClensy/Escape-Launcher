package com.geecee.escapelauncher.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

/**
 * FOSS Implementation: Tries to find the font in res/font. 
 * If it doesn't exist, falls back to default.
 * Note: You must add your .ttf files to res/font for them to work.
 */
@SuppressLint("DiscouragedApi")
fun getFontFamily(context: Context, fontName: String): FontFamily {
    val resourceId = context.resources.getIdentifier(
        fontName.lowercase().replace(" ", "_"),
        "font", 
        context.packageName
    )
    
    return if (resourceId != 0) {
        FontFamily(Font(resourceId))
    } else {
        FontFamily.Default
    }
}
