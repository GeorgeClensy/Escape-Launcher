package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.utils.escapeShadow

/**
 * A composable that displays a small information widget with an optional icon and text.
 * Usually used to show "glanceable" information like date or weather. It is to be used at the top of the home screen in the launcher
 *
 * @param text The string content to be displayed in the widget.
 * @param icon An optional [ImageVector] to be displayed to the left of the text.
 * @param iconContentDescription A description of the icon for accessibility.
 * @param homeAlignment The horizontal alignment used to determine the text alignment.
 * @param small Whether to use a smaller typography style for the text.
 * @param onClick The callback to be invoked when the widget is clicked.
 *
 * @author George Clensy
 */
@Composable
fun GlanceWidget(
    text: String,
    icon: ImageVector?,
    iconContentDescription: String,
    homeAlignment: Alignment.Horizontal,
    small: Boolean,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    shadow: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable {
            onClick()
        },
    ) {
        if (icon != null) {
            Box (
                Modifier.align(Alignment.CenterVertically)
            ) {
                if (shadow) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.scrim,
                        modifier = Modifier
                            .offset(x = 0.dp, y = 0.dp)
                            .alpha(0.5f)
                            .blur(4.dp)
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    Modifier
                        .size(22.dp)
                        .padding(end = 2.dp),
                    tint = color
                )
            }
        }

        Text(
            text = text,
            color = color,
            style = if (small) {
                MaterialTheme.typography.bodyMedium.copy(
                    shadow = escapeShadow(
                        MaterialTheme.colorScheme.scrim,
                        shadow
                    )
                )
            } else {
                MaterialTheme.typography.bodyLarge.copy(
                    shadow = escapeShadow(
                        MaterialTheme.colorScheme.scrim,
                        shadow
                    )
                )
            },
            fontWeight = FontWeight.W600,
            textAlign = when (homeAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}

@Preview
@Composable
fun PrevGlanceWidget() {
    GlanceWidget(
        text = "6:30 AM",
        icon = Icons.Default.Alarm,
        iconContentDescription = "Test",
        homeAlignment = Alignment.Start,
        small = false
    ) {

    }
}