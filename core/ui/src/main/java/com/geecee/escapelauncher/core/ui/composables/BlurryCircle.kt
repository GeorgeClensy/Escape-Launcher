package com.geecee.escapelauncher.core.ui.composables

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BlurryCircle(
    modifier: Modifier = Modifier,
    circleColor: Int
) {
    val shape = MaterialShapes.Cookie12Sided.toShape()

    // Remember the paint and filter outside the draw phase to prevent GC churn
    val paint = remember(circleColor) {
        Paint().nativePaint.apply {
            isAntiAlias = true
            // Increase this value to make it more blurry
            maskFilter = BlurMaskFilter(150f, BlurMaskFilter.Blur.NORMAL)
            color = circleColor
        }
    }

    Box(
        modifier = modifier
            .size(300.dp)
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)

                val nativePath = when (outline) {
                    is Outline.Generic -> outline.path.asAndroidPath()
                    is Outline.Rectangle -> Path().apply { addRect(outline.rect) }.asAndroidPath()
                    is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }.asAndroidPath()
                }

                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawPath(nativePath, paint)
                }
            }
    )
}

@Preview
@Composable
fun PrevBlurryCircle() {
    EscapeThemePreview() {
        BlurryCircle(
            circleColor = MaterialTheme.colorScheme.primary.toAndroidColor()
        )
    }
}