package com.geecee.escapelauncher.core.ui.utils
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import kotlin.math.abs

class MorphShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {
    private val composePath = Path()
    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        composePath.reset()

        // Handle Spring Overshoot (< 0f or > 1f) using reflective inversion
        val safeProgress = when {
            progress > 1f -> {
                // If progress is 1.05, this becomes 1 - 0.05 = 0.95
                // It creates a rubber-band distortion back toward the start shape
                1f - (progress - 1f)
            }
            progress < 0f -> {
                // If progress is -0.05, this becomes abs(-0.05) = 0.05
                // It distorts forward toward the target shape
                abs(progress)
            }
            else -> progress
        }.coerceIn(0f, 1f) // Extra safety check to guarantee it never leaves bounds

        // Retrieve the geometry path using the safely inverted progress
        val androidPath = morph.toPath(safeProgress)
        composePath.addPath(androidPath.asComposePath())

        matrix.reset()
        matrix.scale(size.width, size.height)
        composePath.transform(matrix)

        return Outline.Generic(composePath)
    }
}
