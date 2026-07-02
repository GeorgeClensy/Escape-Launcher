package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.theme.ContentColor

/**
 * Automatically resizing text that fits to container.
 * Uses TextMeasurer for high-performance measurement in a single pass.
 *
 * @param modifier Modifier
 * @param text Text to be displayed
 * @param style Style of the text
 * @param minFontSize Minimum font size
 * @param maxLines Maximum lines
 * @param color Color of the text
 * @param fontFamily Font family of the text
 * @param textAlign Text alignment
 */
@Composable
fun AutoResizingText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    minFontSize: TextUnit = 12.sp,
    maxLines: Int = 1,
    color: Color = ContentColor,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
    textAlign: TextAlign? = null
) {
    BoxWithConstraints(modifier = modifier) {
        val textMeasurer = rememberTextMeasurer()
        val maxWidthPx = constraints.maxWidth

        val fontSize = remember(text, style, maxWidthPx) {
            var currentSize = if (style.fontSize.isUnspecified) 16.sp else style.fontSize
            
            // Fast path: check if it fits with default size
            val result = textMeasurer.measure(
                text = text,
                style = style.copy(fontSize = currentSize, fontFamily = fontFamily),
                constraints = Constraints(maxWidth = maxWidthPx),
                maxLines = maxLines,
                overflow = TextOverflow.Clip
            )

            if (result.hasVisualOverflow) {
                // Iterative shrink (could use binary search for even more speed, 
                // but usually 1-3 steps is enough)
                while (currentSize > minFontSize) {
                    currentSize = (currentSize.value * 0.9f).sp
                    val stepResult = textMeasurer.measure(
                        text = text,
                        style = style.copy(fontSize = currentSize, fontFamily = fontFamily),
                        constraints = Constraints(maxWidth = maxWidthPx),
                        maxLines = maxLines,
                        overflow = TextOverflow.Clip
                    )
                    if (!stepResult.hasVisualOverflow) break
                }
                if (currentSize < minFontSize) currentSize = minFontSize
            }
            currentSize
        }

        Text(
            text = text,
            style = style.copy(fontSize = fontSize, color = color, fontFamily = fontFamily),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            softWrap = false
        )
    }
}
