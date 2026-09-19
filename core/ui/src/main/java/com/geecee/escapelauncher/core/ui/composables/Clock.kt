package com.geecee.escapelauncher.core.ui.composables

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview

private const val SMALL_TIME_FORMAT = "%02d:%02d"

private object ClockDefaults {
    val SmallClockOffsetX = (-2).dp
    val SmallClockOffsetY = 5.dp
}

@Composable
fun Clock(
    hour: Int,
    minute: Int,
    bigClock: Boolean,
    onClockClick: () -> Unit,
    homeAlignment: Alignment.Horizontal
) {
    val color = MaterialTheme.colorScheme.primary

    if (bigClock) {
        BigClock(
            hour = hour,
            minute = minute,
            color = color,
            baseStyle = MaterialTheme.typography.headlineLarge,
            homeAlignment = homeAlignment,
            onClockClick = onClockClick
        )
    } else {
        Text(
            text = SMALL_TIME_FORMAT.format(hour, minute),
            modifier = Modifier
                .clickable { onClockClick() }
                .offset(
                    x = ClockDefaults.SmallClockOffsetX,
                    y = ClockDefaults.SmallClockOffsetY
                ),
            color = color,
            fontWeight = FontWeight.W600,
            style = MaterialTheme.typography.titleLarge,
            textAlign = when (homeAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}

/** Exact ink bounds of the widest/tallest possible digit, for a given typeface + size. */
private data class DigitMetrics(
    val slotWidth: Float,      // px, box width per digit
    val boxHeight: Float,      // px, box height for a row (tight to ink, no font padding)
    val baselineY: Float,      // px, distance from top of box down to the text baseline
    val inkLefts: List<Float>,  // per-digit left bearing, for horizontal centering
    val inkWidths: List<Float>  // per-digit ink width, for horizontal centering
)

private fun computeDigitMetrics(typeface: Typeface, fontSizePx: Float): DigitMetrics {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = fontSizePx
    }
    val rect = Rect()
    val lefts = mutableListOf<Float>()
    val widths = mutableListOf<Float>()
    var minTop = 0
    var maxBottom = 0
    var maxWidth = 0f

    for (d in 0..9) {
        val s = d.toString()
        paint.getTextBounds(s, 0, 1, rect)
        lefts.add(rect.left.toFloat())
        widths.add(rect.width().toFloat())
        if (rect.top < minTop) minTop = rect.top
        if (rect.bottom > maxBottom) maxBottom = rect.bottom
        if (rect.width() > maxWidth) maxWidth = rect.width().toFloat()
    }

    return DigitMetrics(
        slotWidth = maxWidth,
        boxHeight = (maxBottom - minTop).toFloat(),
        baselineY = -minTop.toFloat(),
        inkLefts = lefts.toList(),
        inkWidths = widths.toList()
    )
}

@Composable
@Suppress("SameParameterValue")
private fun rememberDigitTypeface(fontFamily: FontFamily?, fontWeight: FontWeight): Typeface {
    val resolver = LocalFontFamilyResolver.current
    val typefaceState = remember(fontFamily, fontWeight, resolver) {
        resolver.resolve(fontFamily, fontWeight)
    }
    return typefaceState.value as? Typeface ?: Typeface.DEFAULT
}

@Composable
private fun BigClock(
    hour: Int,
    minute: Int,
    color: Color,
    baseStyle: TextStyle,
    homeAlignment: Alignment.Horizontal,
    onClockClick: () -> Unit
) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { baseStyle.fontSize.toPx() }
    val typeface = rememberDigitTypeface(baseStyle.fontFamily, FontWeight.W600)
    val metrics = remember(typeface, fontSizePx) { computeDigitMetrics(typeface, fontSizePx) }

    val hourDigits = remember(hour) { "%02d".format(hour).map { it - '0' } }
    val minuteDigits = remember(minute) { "%02d".format(minute).map { it - '0' } }

    // Blank space sitting before each row's first digit, purely because that
    // digit is narrower than the widest possible one (e.g. "1" vs "8"). Trim
    // both rows by the SMALLER of the two, so the block shifts left as one
    // unit and the two rows stay column-aligned with each other.
    val leadTrim = remember(metrics, hourDigits, minuteDigits) {
        val hourLead = (metrics.slotWidth - metrics.inkWidths[hourDigits[0]]) / 2f
        val minuteLead = (metrics.slotWidth - metrics.inkWidths[minuteDigits[0]]) / 2f
        minOf(hourLead, minuteLead)
    }

    Column(
        horizontalAlignment = homeAlignment,
        modifier = Modifier.clickable { onClockClick() }
    ) {
        TightDigitRow(hourDigits, typeface, fontSizePx, metrics, leadTrim, color, density)
        Spacer(Modifier.height(7.dp))
        TightDigitRow(minuteDigits, typeface, fontSizePx, metrics, leadTrim, color, density)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TightDigitRow(
    digits: List<Int>,
    typeface: Typeface,
    fontSizePx: Float,
    metrics: DigitMetrics,
    leadTrim: Float,
    color: Color,
    density: androidx.compose.ui.unit.Density,
    spacingPx: Float = 5f
) {
    // Account for the spacing added ONLY between adjacent digits
    val totalSpacingPx = if (digits.size > 1) (digits.size - 1) * spacingPx else 0f
    val widthPx = (metrics.slotWidth * digits.size - leadTrim) + totalSpacingPx
    val widthDp: Dp = with(density) { widthPx.toDp() }
    val heightDp: Dp = with(density) { metrics.boxHeight.toDp() }

    val paint = remember(typeface, fontSizePx, color) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = fontSizePx
            this.color = color.toArgb()
        }
    }

    Canvas(modifier = Modifier.size(widthDp, heightDp)) {
        drawIntoCanvas { canvas ->
            digits.forEachIndexed { index, digit ->
                // Shift each digit by index * spacingPx to accumulate space only between slots
                val slotLeft = (metrics.slotWidth * index - leadTrim) + (index * spacingPx)
                val x = slotLeft + (metrics.slotWidth - metrics.inkWidths[digit]) / 2f - metrics.inkLefts[digit]
                canvas.nativeCanvas.drawText(digit.toString(), x, metrics.baselineY, paint)
            }
        }
    }
}

@Preview
@Composable
private fun ClockPrev() {
    EscapeThemePreview {
        Clock(
            hour = 12,
            minute = 30,
            bigClock = false,
            onClockClick = {},
            homeAlignment = Alignment.Start
        )
    }
}

@Preview
@Composable
private fun BigClockPrev() {
    EscapeThemePreview {
        Clock(
            hour = 12,
            minute = 16,
            bigClock = true,
            onClockClick = {},
            homeAlignment = Alignment.Start
        )
    }
}