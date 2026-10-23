package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

/**
 * Automatically resizing text that fits to container.
 * Uses TextMeasurer for high-performance measurement in a single pass.
 *
 * @param modifier Modifier
 * @param text Text to be displayed
 * @param style Style of the text
 * @param minFontSize Minimum font size
 * @param maxLines Maximum lines
 * @param color Colour of the text
 * @param fontFamily Font family of the text
 * @param textAlign Text alignment
 * @param singleLine Whether the text should be restricted to a single line with ellipsis or allowed to wrap
 */
@Composable
fun AutoResizingText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    minFontSize: TextUnit = 12.sp,
    maxLines: Int = 1,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
    textAlign: TextAlign? = null,
    singleLine: Boolean = true
) {
    BasicText(
        text = text,
        style = style.copy(color = color, fontFamily = fontFamily),
        autoSize = TextAutoSize.StepBased(
            minFontSize = minFontSize,
            maxFontSize = if (style.fontSize.isUnspecified) 16.sp else style.fontSize
        ),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        softWrap = !singleLine,
        modifier = modifier
    )
}

@Preview
@Composable
fun PrevAutoResizingText() {
    AutoResizingText(
        text = "Test Text",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Preview(device = "id:Galaxy Nexus")
@Composable
fun PrevAutoResizingTextSmallBox() {
    Box(modifier = Modifier.fillMaxSize()) {
        AutoResizingText(
            text = "A very long piece of text to test what happens when there is not enough space on the screen.",
            style = MaterialTheme.typography.displayLarge
        )
    }
}