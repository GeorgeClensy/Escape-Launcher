package com.geecee.escapelauncher.core.ui.vectors

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

@Composable
fun getPrivateSpaceLockedImage(): ImageVector {
    val primaryContainer = MaterialTheme.colorScheme.primary
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimary

    val vector = remember(primaryContainer, onPrimaryContainer) {
        ImageVector.Builder(
            defaultWidth = 300.dp,
            defaultHeight = 300.dp,
            viewportWidth = 210f,
            viewportHeight = 210f
        ).apply {
            addGroup(
                name = "scaled_group",
                translationX = -45f,
                translationY = -45f
            )
            addPath(
                pathData = PathParser().parsePathString("M176.66,54.43C219.62,35.78 263.22,79.38 244.57,122.34L241.48,129.44C235.79,142.55 235.79,157.45 241.48,170.56L244.57,177.66C263.22,220.62 219.62,264.22 176.66,245.57L169.56,242.48C156.45,236.79 141.55,236.79 128.44,242.48L121.34,245.57C78.38,264.22 34.78,220.62 53.43,177.66L56.52,170.56C62.21,157.45 62.21,142.55 56.52,129.44L53.43,122.34C34.78,79.38 78.38,35.78 121.34,54.43L128.44,57.52C141.55,63.21 156.45,63.21 169.56,57.52L176.66,54.43Z")
                    .toNodes(),
                fill = SolidColor(primaryContainer)
            )
            addPath(
                pathData = PathParser().parsePathString("M126.6,194.7C123.14,194.7 120.17,193.47 117.7,191C115.23,188.53 114,185.57 114,182.1V144.5C114,141.04 115.23,138.07 117.7,135.6C120.17,133.13 123.14,131.9 126.6,131.9H127.9H140.5H158.3H170.9H172.2C175.66,131.9 178.63,133.13 181.1,135.6C183.57,138.07 184.8,141.04 184.8,144.5V182.1C184.8,185.57 183.57,188.53 181.1,191C178.63,193.47 175.66,194.7 172.2,194.7H126.6ZM172.2,182.1H126.6V144.5H172.2V182.1Z")
                    .toNodes(),
                fill = SolidColor(onPrimaryContainer),
                pathFillType = PathFillType.EvenOdd
            )
            addPath(
                pathData = PathParser().parsePathString("M157.4,163.3C157.4,165.5 156.62,167.38 155.05,168.95C153.48,170.52 151.6,171.3 149.4,171.3C147.2,171.3 145.32,170.52 143.75,168.95C142.18,167.38 141.4,165.5 141.4,163.3C141.4,161.1 142.18,159.22 143.75,157.65C145.32,156.08 147.2,155.3 149.4,155.3C151.6,155.3 153.48,156.08 155.05,157.65C156.62,159.22 157.4,161.1 157.4,163.3Z")
                    .toNodes(),
                fill = SolidColor(onPrimaryContainer)
            )
            addPath(
                pathData = PathParser().parsePathString("M128,131.8V141.7C128,145.18 130.82,148 134.3,148C137.78,148 140.6,145.18 140.6,141.7V131.8C140.6,129.24 141.45,127.07 143.15,125.28C144.85,123.49 146.97,122.6 149.5,122.6C152.03,122.6 154.15,123.49 155.85,125.28C157.55,127.07 158.4,129.24 158.4,131.8V141.7C158.4,145.18 161.22,148 164.7,148C168.18,148 171,145.18 171,141.7V131.8C171,125.73 168.92,120.58 164.75,116.35C160.58,112.12 155.5,110 149.5,110C143.5,110 138.42,112.12 134.25,116.35C130.08,120.58 128,125.73 128,131.8Z")
                    .toNodes(),
                fill = SolidColor(onPrimaryContainer)
            )
            addPath(
                pathData = PathParser().parsePathString("M126.6,182.1H172.2V144.5H126.6V182.1ZM157.4,163.3C157.4,165.5 156.62,167.38 155.05,168.95C153.48,170.52 151.6,171.3 149.4,171.3C147.2,171.3 145.32,170.52 143.75,168.95C142.18,167.38 141.4,165.5 141.4,163.3C141.4,161.1 142.18,159.22 143.75,157.65C145.32,156.08 147.2,155.3 149.4,155.3C151.6,155.3 153.48,156.08 155.05,157.65C156.62,159.22 157.4,161.1 157.4,163.3Z")
                    .toNodes(),
                fill = SolidColor(primaryContainer),
                pathFillType = PathFillType.EvenOdd
            )
        }.build()
    }

    return vector
}