package com.geecee.escapelauncher.core.theme.type

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

fun escapeType(fontFamily: FontFamily): Typography {
    val typography = Typography(
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 66.sp,
            lineHeight = 53.sp,
            letterSpacing = 0.6.sp
        ), headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 62.sp,
            lineHeight = 49.sp,
            letterSpacing = 0.6.sp
        ), headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 58.sp,
            lineHeight = 45.sp,
            letterSpacing = 0.6.sp
        ), titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 52.sp,
            lineHeight = 53.sp,
            letterSpacing = 0.6.sp
        ), titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 48.sp,
            lineHeight = 49.sp,
            letterSpacing = 0.6.sp
        ), titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 44.sp,
            lineHeight = 45.sp,
            letterSpacing = 0.6.sp
        ), bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 29.sp,
            letterSpacing = 0.6.sp
        ), bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 25.sp,
            letterSpacing = 0.6.sp
        ), bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.6.sp
        )
    )

    return typography
}