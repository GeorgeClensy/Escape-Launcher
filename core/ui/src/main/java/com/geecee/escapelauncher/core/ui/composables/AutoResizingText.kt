package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.ui.theme.ContentColor

/**
 * Automatically resizing text that fits to container.
 *
 * @param modifier Modifier
 * @param text Text to be displayed
 * @param style Style of the text
 * @param minFontSize Minimum font size
 * @param maxLines Maximum lines
 * @param color Color of the text
 * @param fontFamily Font family of the text
 * @param textAlign Text alignment
 *
 * @author George Clensy
 */
@Composable
fun AutoResizingText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    minFontSize: TextUnit = 10.sp,
    maxLines: Int = 1,
    color: Color = ContentColor,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
    textAlign: TextAlign? = null
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()

        var currentFontSize by remember(text, style, minFontSize) {
            mutableStateOf(style.fontSize)
        }

        LaunchedEffect(text, style, minFontSize, maxWidth, currentFontSize) {
            val availableWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }

            if (availableWidthPx <= 0) return@LaunchedEffect

            var tempFontSize = style.fontSize
            if (tempFontSize.isUnspecified || tempFontSize.value <= 0) {
                tempFontSize = 16.sp
            }

            val textLayoutResult = textMeasurer.measure(
                text = text,
                style = style.copy(fontSize = tempFontSize),
                overflow = TextOverflow.Clip,
                softWrap = false,
                maxLines = maxLines,
                constraints = Constraints(maxWidth = availableWidthPx.toInt())
            )

            if (textLayoutResult.didOverflowWidth) {
                var shrunkFontSize = tempFontSize
                while (shrunkFontSize > minFontSize) {
                    shrunkFontSize = (shrunkFontSize.value * 0.9f).sp
                    if (shrunkFontSize < minFontSize) {
                        shrunkFontSize = minFontSize
                    }

                    val shrunkLayoutResult = textMeasurer.measure(
                        text = text,
                        style = style.copy(fontSize = shrunkFontSize),
                        overflow = TextOverflow.Clip,
                        softWrap = false,
                        maxLines = maxLines,
                        constraints = Constraints(maxWidth = availableWidthPx.toInt())
                    )

                    if (!shrunkLayoutResult.didOverflowWidth) {
                        tempFontSize = shrunkFontSize
                        break
                    }

                    if (shrunkFontSize == minFontSize) {
                        tempFontSize = minFontSize
                        break
                    }
                }
            }

            if (currentFontSize != tempFontSize) {
                currentFontSize = tempFontSize
            }
        }

        Text(
            text = text,
            style = style.copy(fontSize = currentFontSize),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            color = color,
            fontFamily = fontFamily,
            textAlign = textAlign
        )
    }
}