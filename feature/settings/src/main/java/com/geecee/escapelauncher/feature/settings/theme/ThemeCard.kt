package com.geecee.escapelauncher.feature.settings.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.geecee.escapelauncher.core.theme.AppColourScheme
import com.geecee.escapelauncher.core.theme.resolveColorScheme
import com.geecee.escapelauncher.core.theme.transparentHalf
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.nameResFromId


/**
 * Theme select card
 *
 * @param theme The theme ID number (see: Theme.kt)
 *
 * @see com.geecee.escapelauncher.core.theme.EscapeTheme
 */
@Composable
fun ThemeCard(
    theme: Int,
    showLightDarkPicker: Boolean,
    isSelected: Boolean,
    isDSelected: Boolean,
    isLSelected: Boolean,
    updateLTheme: (Int) -> Unit,
    updateDTheme: (Int) -> Unit,
    modifier: Modifier,
    onClick: (Int) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val scheme = AppColourScheme.fromId(theme)
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
                .clickable { onClick(theme) }
                .background(colors.background)
                .height(72.dp)) {
            val showCheck = isSelected && !showLightDarkPicker
            val showMoon = isDSelected && !showLightDarkPicker
            val showSun = isLSelected && !showLightDarkPicker

            AnimatedVisibility(
                visible = showCheck || showMoon || showSun, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(2.dp, colors.onPrimaryContainer, shape)
                ) {

                    when {
                        showCheck -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            )
                        }

                        showMoon -> {
                            Icon(
                                Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            )
                        }

                        showSun -> {
                            Icon(
                                Icons.Default.LightMode,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(AppColourScheme.nameResFromId(theme)),
                color = colors.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            AnimatedVisibility(
                visible = showLightDarkPicker, enter = fadeIn(), exit = fadeOut()
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .background(transparentHalf)
                ) {
                    Button(
                        onClick = { updateLTheme(theme) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(20.dp, 5.dp, 5.dp, 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary, contentColor = colors.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.light))
                    }

                    Button(
                        onClick = { updateDTheme(theme) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(5.dp, 5.dp, 20.dp, 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary, contentColor = colors.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.dark))
                    }
                }
            }
        }
    }
}