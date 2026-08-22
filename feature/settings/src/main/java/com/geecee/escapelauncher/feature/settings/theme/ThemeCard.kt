package com.geecee.escapelauncher.feature.settings.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.colours.AppColourScheme
import com.geecee.escapelauncher.core.theme.colours.resolveColorScheme
import com.geecee.escapelauncher.core.ui.composables.nameRes


/**
 * Theme select card
 *
 * @param scheme The theme scheme
 *
 * @see com.geecee.escapelauncher.core.theme.EscapeTheme
 */
@Composable
fun ThemeCard(
    scheme: AppColourScheme,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: (AppColourScheme) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val colors = scheme.resolveColorScheme()

    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val shape = RoundedCornerShape(
        topStart = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        topEnd = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        bottomStart = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        bottomEnd = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    )

    Box(Modifier.padding(vertical = 1.dp)) {
        Box(
            modifier
                .clip(shape)
                .clickable { onClick(scheme) }
                .background(colors.primaryContainer)
                .height(72.dp)) {

            AnimatedVisibility(
                visible = isSelected, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(2.dp, colors.onPrimaryContainer, shape)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    )
                }
            }

            Text(
                text = stringResource(scheme.nameRes()),
                color = colors.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}
