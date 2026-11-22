package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


/**
 * Block with tips for first time users
 *
 * @param swipeForAllAppsText Text of the swipe hint
 * @param holdForSettingsText Text of the hold for settings hint
 * @param swipeUp Whether the gesture that opens the apps list swipes up (the
 * vertical layout) or across (the original horizontal layout); it picks the
 * arrow shown next to the swipe hint
 */
@Composable
fun FirstTimeHelp(
    swipeForAllAppsText: String,
    holdForSettingsText: String,
    swipeUp: Boolean = true
) {
    Box(
        Modifier.clip(
            MaterialTheme.shapes.extraLarge
        )
    ) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                Modifier
                    .padding(25.dp, 25.dp, 25.dp, 15.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    if (swipeUp) Icons.Rounded.ArrowUpward else Icons.AutoMirrored.Rounded.ArrowForward,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    swipeForAllAppsText,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                Modifier
                    .padding(25.dp, 0.dp, 25.dp, 25.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.Settings,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    holdForSettingsText,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
