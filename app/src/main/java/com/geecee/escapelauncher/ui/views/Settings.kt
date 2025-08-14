package com.geecee.escapelauncher.ui.views

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.AppTheme
import com.geecee.escapelauncher.ui.theme.getTypographyFromFontName
import com.geecee.escapelauncher.ui.theme.refreshTheme
import com.geecee.escapelauncher.ui.theme.transparentHalf
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.loadTextFromAssets
import com.geecee.escapelauncher.utils.CustomWidgetPicker
import com.geecee.escapelauncher.utils.changeAppsAlignment
import com.geecee.escapelauncher.utils.changeHomeAlignment
import com.geecee.escapelauncher.utils.changeHomeVAlignment
import com.geecee.escapelauncher.utils.getAppsAlignmentAsInt
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.getHomeAlignmentAsInt
import com.geecee.escapelauncher.utils.getHomeVAlignmentAsInt
import com.geecee.escapelauncher.utils.getIntSetting
import com.geecee.escapelauncher.utils.getSavedWidgetId
import com.geecee.escapelauncher.utils.getWidgetHeight
import com.geecee.escapelauncher.utils.getWidgetOffset
import com.geecee.escapelauncher.utils.getWidgetWidth
import com.geecee.escapelauncher.utils.isDefaultLauncher
import com.geecee.escapelauncher.utils.isWidgetConfigurable
import com.geecee.escapelauncher.utils.launchWidgetConfiguration
import com.geecee.escapelauncher.utils.removeWidget
import com.geecee.escapelauncher.utils.resetActivity
import com.geecee.escapelauncher.utils.saveWidgetId
import com.geecee.escapelauncher.utils.setBooleanSetting
import com.geecee.escapelauncher.utils.setIntSetting
import com.geecee.escapelauncher.utils.setStringSetting
import com.geecee.escapelauncher.utils.setWidgetHeight
import com.geecee.escapelauncher.utils.setWidgetOffset
import com.geecee.escapelauncher.utils.setWidgetWidth
import com.geecee.escapelauncher.utils.showLauncherSelector
import com.geecee.escapelauncher.utils.showLauncherSettingsMenu
import com.geecee.escapelauncher.utils.toggleBooleanSetting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

//
// COMPOSABLE
//

@Composable
fun AutoResizingText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    minFontSize: TextUnit = 10.sp,
    maxLines: Int = 1,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily
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
        )
    }
}

/**
 * Settings title header with back button
 *
 * @param title The text shown on the header
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsHeader(goBack: () -> Unit, title: String) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = { goBack() })
            .padding(0.dp, 120.dp, 0.dp, 8.dp)
            .height(70.dp) // Set a fixed height for the header
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Go Back",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontSize = if (title.length > 11) 35.sp else MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

/**
 * @param title The text shown on the subhead
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsSubheading(title: String) {
    Row(
        modifier = Modifier.padding(0.dp, 30.dp, 0.dp, 2.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

/**
 * Switch for setting with a label on the left
 *
 * @param label The text for the label
 * @param checked Whether the switch is on or not
 * @param onCheckedChange Function with Boolean passed that's executed when the switch is pressed
 */
@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    var isChecked by remember { mutableStateOf(checked) }

    // Define the base corner size
    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val topEndRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomStartRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomEndRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius

    Card(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .clickable {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            }, shape = RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomEnd = bottomEndRadius,
            bottomStart = bottomStartRadius
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .height(48.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            AutoResizingText(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp), // Add space between text and switch
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = isChecked, onCheckedChange = {
                    isChecked = it
                    onCheckedChange(isChecked)
                })
        }
    }
}

/**
 * Settings navigation item with label and arrow
 *
 * @param label The text to be shown
 * @param diagonalArrow Whether the arrow should be pointed upwards to signal that pressing this will take you out of Escape Launcher
 * @param onClick When composable is clicked
 * @param isTopOfGroup Whether this item is at the top of a group of items, for corner rounding
 * @param isBottomOfGroup Whether this item is at the bottom of a group of items, for corner rounding
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsNavigationItem(
    label: String,
    diagonalArrow: Boolean?,
    onClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    // Define the base corner size
    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val topEndRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomStartRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomEndRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius

    Card(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomEnd = bottomEndRadius,
            bottomStart = bottomStartRadius
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AutoResizingText(
                text = label,
                modifier = Modifier
                    .weight(1f) // Allow text to take available space
                    .padding(end = 8.dp), // Add space between text and icon
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            val iconModifier = Modifier.size(24.dp) // Standardized icon size slightly
            if (diagonalArrow == true) { // Explicitly check for true
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null, // Content description can be null for decorative icons
                    modifier = iconModifier.rotate(-45f),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null, // Content description can be null for decorative icons
                    modifier = iconModifier,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Settings navigation item with label and arrow
 *
 * @param label The text to be shown
 * @param onClick When composable is clicked
 * @param isTopOfGroup Whether this item is at the top of a group of items, for corner rounding
 * @param isBottomOfGroup Whether this item is at the bottom of a group of items, for corner rounding
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsButton(
    label: String,
    onClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
) {
    // Define the base corner size
    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val topEndRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomStartRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomEndRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius

    Card(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomEnd = bottomEndRadius,
            bottomStart = bottomStartRadius
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AutoResizingText(
                text = label,
                modifier = Modifier
                    .weight(1f) // Allow text to take available space
                    .padding(end = 8.dp), // Add space between text and icon
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = fontFamily
            )
        }
    }
}

/**
 * Settings button that can be swiped to be dismissed
 *
 * @param label The text to be shown
 * @param onClick When composable is clicked
 * @param onDeleteClick When composable is swiped to be deleted
 * @param isTopOfGroup Whether this item is at the top of a group of items, for corner rounding
 * @param isBottomOfGroup Whether this item is at the bottom of a group of items, for corner rounding
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsSwipeableButton(
    label: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
) {
    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val topEndRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomStartRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomEndRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.padding(vertical = 1.dp),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val revealedWidthDp = dismissState.progress.dp

            Box(
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (revealedWidthDp > 0.dp) {
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(dismissState.progress),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.remove),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick),
            shape = RoundedCornerShape(
                topStart = topStartRadius,
                topEnd = topEndRadius,
                bottomEnd = bottomEndRadius,
                bottomStart = bottomStartRadius
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = fontFamily)
                )
            }
        }
    }
}


/**
 * Theme select card
 *
 * @param theme The theme ID number (see: Theme.kt)
 *
 * @see com.geecee.escapelauncher.ui.theme.EscapeTheme
 */
@Composable
fun ThemeCard(
    theme: Int,
    showLightDarkPicker: MutableState<Boolean>,
    isSelected: MutableState<Boolean>,
    isDSelected: MutableState<Boolean>,
    isLSelected: MutableState<Boolean>,
    updateLTheme: (Int) -> Unit,
    updateDTheme: (Int) -> Unit,
    modifier: Modifier,
    onClick: (Int) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val topEndRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomStartRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomEndRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius

    Box(Modifier.padding(vertical = 1.dp)) {
        Box(
            modifier
                .clip(
                    RoundedCornerShape(
                        topStart = topStartRadius,
                        topEnd = topEndRadius,
                        bottomEnd = bottomEndRadius,
                        bottomStart = bottomStartRadius
                    )
                )
                .clickable {
                    onClick(theme)
                }
                .background(AppTheme.fromId(theme).scheme.background)
                .height(72.dp)) {
            AnimatedVisibility(
                isSelected.value && !showLightDarkPicker.value && !showLightDarkPicker.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            2.dp,
                            AppTheme.fromId(theme).scheme.onPrimaryContainer,
                            RoundedCornerShape(
                                topStart = topStartRadius,
                                topEnd = topEndRadius,
                                bottomEnd = bottomEndRadius,
                                bottomStart = bottomStartRadius
                            )
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "",
                            tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                isSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            2.dp,
                            AppTheme.fromId(theme).scheme.onPrimaryContainer,
                            RoundedCornerShape(
                                topStart = topStartRadius,
                                topEnd = topEndRadius,
                                bottomEnd = bottomEndRadius,
                                bottomStart = bottomStartRadius
                            )
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "",
                            tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                isDSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            2.dp,
                            AppTheme.fromId(theme).scheme.onPrimaryContainer,
                            RoundedCornerShape(
                                topStart = topStartRadius,
                                topEnd = topEndRadius,
                                bottomEnd = bottomEndRadius,
                                bottomStart = bottomStartRadius
                            )
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.dark_mode),
                            "",
                            tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                stringResource(AppTheme.nameResFromId(theme)),
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                AppTheme.fromId(theme).scheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(
                isLSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            2.dp,
                            AppTheme.fromId(theme).scheme.onPrimaryContainer,
                            RoundedCornerShape(
                                topStart = topStartRadius,
                                topEnd = topEndRadius,
                                bottomEnd = bottomEndRadius,
                                bottomStart = bottomStartRadius
                            )
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.light_mode),
                            "",
                            tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .background(transparentHalf)
                ) {
                    Button(
                        onClick = {
                            updateLTheme(theme)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(20.dp, 5.dp, 5.dp, 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.fromId(theme).scheme.primary,
                            contentColor = AppTheme.fromId(theme).scheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.light))
                    }

                    Button(
                        onClick = {
                            updateDTheme(theme)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(5.dp, 5.dp, 20.dp, 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.fromId(theme).scheme.primary,
                            contentColor = AppTheme.fromId(theme).scheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.dark))
                    }
                }
            }
        }
    }
}

/**
 * Spacer 30.dp height
 */
@Composable
fun SettingsSpacer() {
    Spacer(modifier = Modifier.height(30.dp))
}

/**
 * A setting item with a label on the left and a SingleChoiceSegmentedButtonRow on the right.
 *
 * @param label The text for the label.
 * @param options A list of strings representing the choices for the segmented buttons.
 * @param selectedIndex The currently selected index in the options list.
 * @param onSelectedIndexChange Callback that is invoked when the selection changes.
 * @param isTopOfGroup Whether this item is the first in a group of settings.
 * @param isBottomOfGroup Whether this item is the last in a group of settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSingleChoiceSegmentedButtons(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    var currentSelectedIndex by remember { mutableIntStateOf(selectedIndex) }

    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val topEndRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomStartRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomEndRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp), shape = RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomStart = bottomStartRadius,
            bottomEnd = bottomEndRadius
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoResizingText(
                text = label,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(4f)
            ) {
                options.forEachIndexed { index, optionLabel ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = options.size
                        ), onClick = {
                            currentSelectedIndex = index
                            onSelectedIndexChange(index)
                        }, selected = index == currentSelectedIndex
                    ) {
                        Text(text = optionLabel, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                }
            }
        }
    }
}

/**
 * A setting item with a label on the left, a Slider in the middle, and a reset Icon on the right.
 * Visually styled to match SettingsSingleChoiceSegmentedButtons.
 *
 * @param label The text for the label displayed to the left of the slider.
 * @param value The current value of the slider.
 * @param onValueChange Callback that is invoked when the slider's value changes.
 * @param valueRange The range of values that the slider can take.
 * @param steps The number of discrete steps the slider can snap to between the min and max values.
 * @param onReset Callback that is invoked when the reset icon is clicked.
 * @param modifier Optional [Modifier] for this composable.
 * @param isTopOfGroup Whether this item is the first in a group of settings, for corner rounding.
 * @param isBottomOfGroup Whether this item is the last in a group of settings, for corner rounding.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val topEndRadius = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomStartRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    val bottomEndRadius = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomStart = bottomStartRadius,
            bottomEnd = bottomEndRadius
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 16.dp)
            )

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.reset_to_default),
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp)
                    .clickable(onClick = onReset),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun SettingsFullScreenMessage(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Box(modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

//
// MENUS
//

/**
 * Main Settings window you see when settings is first opened
 *
 * @param mainAppModel This is needed to get packageManager, context, ect
 * @param goBack When back button is pressed
 * @param activity This is needed for some settings
 */
@Composable
fun Settings(
    mainAppModel: MainAppModel,
    goBack: () -> Unit,
    activity: Activity,
) {
    val showPolicyDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(20.dp, 0.dp, 20.dp, 0.dp)
    ) {

        val navController = rememberNavController()

        NavHost(navController = navController, "mainSettingsPage") {
            composable(
                "mainSettingsPage",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                MainSettingsPage(
                    { goBack() },
                    { showPolicyDialog.value = true },
                    navController,
                    mainAppModel,
                    activity
                )
            }
            composable(
                "hiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                HiddenApps(
                    mainAppModel
                ) { navController.popBackStack() }
            }
            composable(
                "openChallenges",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                OpenChallenges(
                    mainAppModel
                ) { navController.popBackStack() }
            }
            composable(
                "chooseFont",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ChooseFont(mainAppModel.getContext(), activity) { navController.popBackStack() }
            }
            composable(
                "devOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DevOptions(context = mainAppModel.getContext()) { navController.popBackStack() }
            }
            composable(
                "theme",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ThemeOptions(
                    mainAppModel, mainAppModel.getContext()
                ) { navController.popBackStack() }
            }
            composable(
                "widget",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                WidgetOptions(mainAppModel.getContext()) { navController.popBackStack() }
            }
        }
    }

    AnimatedVisibility(showPolicyDialog.value, enter = fadeIn(), exit = fadeOut()) {
        PrivacyPolicyDialog(mainAppModel, showPolicyDialog)
    }
}

/**
 * Fist page of settings, contains navigation to all the other pages
 *
 * @param goBack When back button is pressed
 * @param showPolicyDialog When the show privacy policy button is pressed
 * @param navController Settings nav controller with "personalization", "hiddenApps", "openChallenges"
 * @param mainAppModel This is required for settings to be changed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    showPolicyDialog: () -> Unit,
    navController: NavController,
    mainAppModel: MainAppModel,
    activity: Activity
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(
            goBack, stringResource(R.string.settings)
        )

        //General
        SettingsSubheading(stringResource(id = R.string.general))

        SettingsNavigationItem(
            label = stringResource(id = R.string.theme),
            false,
            isTopOfGroup = true,
            onClick = { navController.navigate("theme") })

        SettingsNavigationItem(
            label = stringResource(id = R.string.choose_font),
            false,
            onClick = { navController.navigate("chooseFont") })

        SettingsSwitch(
            label = stringResource(id = R.string.haptic_feedback),
            isBottomOfGroup = true,
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.Haptic), true
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.Haptic)
                )
            })

        // Home options
        SettingsSubheading(stringResource(R.string.home_screen_options))

        SettingsSwitch(
            label = stringResource(id = R.string.show_clock), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowClock), true
            ), isTopOfGroup = true, onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ShowClock)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.date), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.show_date), false
            ), onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.show_date)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.big_clock), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.BigClock)
            ), onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.BigClock)
                )
            })

        SettingsNavigationItem(
            label = stringResource(id = R.string.widget),
            false,
            isBottomOfGroup = true,
            onClick = { navController.navigate("widget") })

        //Alignment Options
        SettingsSubheading(stringResource(R.string.alignments))

        val homeHorizontalOptions = listOf(
            stringResource(R.string.left),
            stringResource(R.string.center),
            stringResource(R.string.right)
        )
        var selectedHomeHorizontalIndex by remember {
            mutableIntStateOf(getHomeAlignmentAsInt(mainAppModel.getContext()))
        }
        SettingsSingleChoiceSegmentedButtons(
            label = stringResource(id = R.string.home),
            options = homeHorizontalOptions,
            selectedIndex = selectedHomeHorizontalIndex,
            onSelectedIndexChange = { newIndex ->
                selectedHomeHorizontalIndex = newIndex
                changeHomeAlignment(mainAppModel.getContext(), newIndex)
            },
            isTopOfGroup = true // First item in this section
        )

        val homeVerticalOptions = listOf(
            stringResource(R.string.top),
            stringResource(R.string.center),
            stringResource(R.string.bottom)
        )
        var selectedHomeVerticalIndex by remember {
            mutableIntStateOf(getHomeVAlignmentAsInt(mainAppModel.getContext()))
        }
        SettingsSingleChoiceSegmentedButtons(
            label = "",
            options = homeVerticalOptions,
            selectedIndex = selectedHomeVerticalIndex,
            onSelectedIndexChange = { newIndex ->
                selectedHomeVerticalIndex = newIndex
                changeHomeVAlignment(mainAppModel.getContext(), newIndex)
            })

        val appsAlignmentOptions = listOf(
            stringResource(R.string.left),
            stringResource(R.string.center),
            stringResource(R.string.right)
        )
        var selectedAppsAlignmentIndex by remember {
            mutableIntStateOf(getAppsAlignmentAsInt(mainAppModel.getContext()))
        }
        SettingsSingleChoiceSegmentedButtons(
            label = stringResource(id = R.string.apps),
            options = appsAlignmentOptions,
            selectedIndex = selectedAppsAlignmentIndex,
            onSelectedIndexChange = { newIndex ->
                selectedAppsAlignmentIndex = newIndex
                changeAppsAlignment(mainAppModel.getContext(), newIndex)
            },
            isBottomOfGroup = true // Last item in this section before any potential new sections
        )

        // Search settings
        SettingsSubheading(stringResource(R.string.search))

        SettingsSwitch(
            label = stringResource(id = R.string.search_box), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowSearchBox), true
            ), isTopOfGroup = true, onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ShowSearchBox)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.auto_open), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.SearchAutoOpen)
            ), isBottomOfGroup = true, onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.SearchAutoOpen)
                )
            })

        //Screen time
        SettingsSubheading(stringResource(R.string.screen_time))

        SettingsSwitch(
            label = stringResource(id = R.string.screen_time_on_app), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnApp)
            ), isTopOfGroup = true, onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ScreenTimeOnApp)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.screen_time_on_home_screen),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnHome)
            ),
            isBottomOfGroup = true,
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ScreenTimeOnHome)
                )
            })

        //Apps
        SettingsSubheading(
            stringResource(R.string.apps)
        )

        SettingsNavigationItem(
            label = stringResource(id = R.string.manage_hidden_apps),
            false,
            isTopOfGroup = true,
            onClick = { navController.navigate("hiddenApps") })

        SettingsNavigationItem(
            label = stringResource(id = R.string.manage_open_challenges),
            false,
            isBottomOfGroup = true,
            onClick = { navController.navigate("openChallenges") })

        //Other
        SettingsSubheading(stringResource(id = R.string.other))

        SettingsSwitch(
            label = stringResource(id = R.string.Analytics), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.Analytics), true
            ), isTopOfGroup = true, onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.Analytics)
                )
            })

        SettingsNavigationItem(
            label = stringResource(id = R.string.read_privacy_policy),
            false,
            onClick = { showPolicyDialog() })

        SettingsNavigationItem(
            label = stringResource(id = R.string.make_default_launcher), true, onClick = {
                if (!isDefaultLauncher(activity)) {
                    activity.showLauncherSelector()
                } else {
                    showLauncherSettingsMenu(activity)
                }
            })

        SettingsNavigationItem(
            stringResource(id = R.string.escape_launcher) + " " + stringResource(id = R.string.app_version),
            false,
            isBottomOfGroup = true,
            onClick = {
                navController.navigate("devOptions")
            })

        SettingsSpacer()
    }
}

/**
 * Theme options in settings
 *
 * @param mainAppModel Main app model for theme updates
 * @param context Needed to run some functions used within ThemeOptions
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeOptions(
    mainAppModel: MainAppModel, context: Context, goBack: () -> Unit
) {
    val settingToChange = stringResource(R.string.theme)
    val autoThemeChange = stringResource(R.string.autoThemeSwitch)
    val dSettingToChange = stringResource(R.string.dTheme)
    val lSettingToChange = stringResource(R.string.lTheme)
    val isSystemDark = isSystemInDarkTheme()

    // Current highlighted theme card
    val currentHighlightedThemeCard = remember { mutableIntStateOf(-1) }

    // Current selected themes
    val currentSelectedTheme = remember {
        mutableIntStateOf(getIntSetting(context, settingToChange, -1))
    }
    val currentSelectedDTheme = remember {
        mutableIntStateOf(getIntSetting(context, dSettingToChange, -1))
    }
    val currentSelectedLTheme = remember {
        mutableIntStateOf(getIntSetting(context, lSettingToChange, -1))
    }

    // Initialize selection states based on settings
    if (!getBooleanSetting(context, autoThemeChange, false)) {
        currentSelectedDTheme.intValue = -1
        currentSelectedLTheme.intValue = -1
    } else {
        currentSelectedTheme.intValue = -1
    }

    val backgroundInteractionSource = remember { MutableInteractionSource() }

    val themeIds = listOf(11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {
                    currentHighlightedThemeCard.intValue = -1
                },
                indication = null,
                onLongClick = {},
                interactionSource = backgroundInteractionSource
            )
    ) {
        item {
            SettingsHeader(goBack, stringResource(R.string.theme))
        }
        item {
            SettingsSwitch(
                stringResource(R.string.syncLightDark), getBooleanSetting(
                    context, context.getString(R.string.autoThemeSwitch), false
                ), isTopOfGroup = true, onCheckedChange = { switch ->
                    // Disable normal selection box or set it correctly
                    if (switch) {
                        currentSelectedTheme.intValue = -1
                    } else {
                        currentSelectedTheme.intValue = getIntSetting(context, settingToChange, 11)
                    }

                    if (switch) {
                        currentSelectedDTheme.intValue =
                            getIntSetting(context, dSettingToChange, -1)
                        currentSelectedLTheme.intValue =
                            getIntSetting(context, lSettingToChange, -1)
                    } else {
                        currentSelectedDTheme.intValue = -1
                        currentSelectedLTheme.intValue = -1
                    }

                    // Remove the light dark button
                    currentHighlightedThemeCard.intValue = -1

                    setBooleanSetting(
                        context, context.getString(R.string.autoThemeSwitch), switch
                    )

                    // Reload
                    val newTheme = refreshTheme(
                        context = context,
                        settingToChange = context.getString(R.string.theme),
                        autoThemeChange = context.getString(R.string.autoThemeSwitch),
                        dSettingToChange = context.getString(R.string.dTheme),
                        lSettingToChange = context.getString(R.string.lTheme),
                        isSystemDarkTheme = isSystemDark
                    )
                    mainAppModel.appTheme.value = newTheme
                })
        }
        item {
            SettingsButton(
                stringResource(R.string.match_system_wallpaper), isBottomOfGroup = true, onClick = {
                    AppUtils.setSolidColorWallpaperHomeScreen(
                        mainAppModel.getContext(), mainAppModel.appTheme.value.background
                    )
                })
        }
        item {
            SettingsSpacer()
        }
        itemsIndexed(themeIds) { index, themeId ->
            val isSelected = remember(themeId, currentSelectedTheme.intValue) {
                mutableStateOf(currentSelectedTheme.intValue == themeId)
            }
            val isDSelected = remember(themeId, currentSelectedDTheme.intValue) {
                mutableStateOf(currentSelectedDTheme.intValue == themeId)
            }
            val isLSelected = remember(themeId, currentSelectedLTheme.intValue) {
                mutableStateOf(currentSelectedLTheme.intValue == themeId)
            }
            val showLightDarkPicker = remember(
                themeId,
                currentSelectedDTheme.intValue,
                currentSelectedLTheme.intValue,
                currentHighlightedThemeCard.intValue
            ) {
                mutableStateOf(currentHighlightedThemeCard.intValue == themeId)
            }

            ThemeCard(
                theme = themeId,
                showLightDarkPicker = showLightDarkPicker,
                isSelected = isSelected,
                isDSelected = isDSelected,
                isLSelected = isLSelected,
                updateLTheme = { theme ->
                    setIntSetting(context, context.getString(R.string.lTheme), theme)
                    val newTheme = refreshTheme(
                        context = context,
                        settingToChange = context.getString(R.string.theme),
                        autoThemeChange = context.getString(R.string.autoThemeSwitch),
                        dSettingToChange = context.getString(R.string.dTheme),
                        lSettingToChange = context.getString(R.string.lTheme),
                        isSystemDarkTheme = isSystemDark
                    )
                    mainAppModel.appTheme.value = newTheme
                    currentSelectedLTheme.intValue = theme
                    currentHighlightedThemeCard.intValue = -1
                },
                updateDTheme = { theme ->
                    setIntSetting(context, context.getString(R.string.dTheme), theme)
                    val newTheme = refreshTheme(
                        context = context,
                        settingToChange = context.getString(R.string.theme),
                        autoThemeChange = context.getString(R.string.autoThemeSwitch),
                        dSettingToChange = context.getString(R.string.dTheme),
                        lSettingToChange = context.getString(R.string.lTheme),
                        isSystemDarkTheme = isSystemDark
                    )
                    mainAppModel.appTheme.value = newTheme
                    currentSelectedDTheme.intValue = theme
                    currentHighlightedThemeCard.intValue = -1
                },
                modifier = Modifier.fillMaxWidth(),
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == themeIds.size - 1,
                onClick = { theme ->
                    if (getBooleanSetting(
                            context, context.getString(R.string.autoThemeSwitch), false
                        )
                    ) {
                        // For auto theme mode, show light/dark picker
                        currentHighlightedThemeCard.intValue = theme
                    } else {
                        // For single theme mode, just set the theme
                        setIntSetting(context, context.getString(R.string.theme), theme)
                        val newTheme = refreshTheme(
                            context = context,
                            settingToChange = context.getString(R.string.theme),
                            autoThemeChange = context.getString(R.string.autoThemeSwitch),
                            dSettingToChange = context.getString(R.string.dTheme),
                            lSettingToChange = context.getString(R.string.lTheme),
                            isSystemDarkTheme = isSystemDark
                        )
                        mainAppModel.appTheme.value = newTheme
                        currentSelectedTheme.intValue = theme
                    }
                })
        }
        item {
            SettingsSpacer()
        }
    }
}

/**
 * Widget Setup
 *
 * @param context Context is required for some functions
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@Suppress("AssignedValueIsNeverRead", "VariableNeverRead")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetOptions(context: Context, goBack: () -> Unit) {
    var needsConfiguration by remember { mutableStateOf(false) }
    val appWidgetManager = AppWidgetManager.getInstance(context)
    var appWidgetId by remember { mutableIntStateOf(getSavedWidgetId(context)) }
    var appWidgetHostView by remember { mutableStateOf<AppWidgetHostView?>(null) }
    val appWidgetHost = remember { AppWidgetHost(context, 1) }

    // State for showing the custom picker
    var showCustomPicker by remember { mutableStateOf(false) }

    // Activity result launcher for binding widget permission
    val bindWidgetPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newWidgetId =
                result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (newWidgetId != -1) {
                appWidgetId = newWidgetId
                saveWidgetId(context, appWidgetId)
                val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

                needsConfiguration = isWidgetConfigurable(context, appWidgetId)
                if (needsConfiguration) {
                    launchWidgetConfiguration(context, appWidgetId)
                } else {
                    appWidgetHostView = appWidgetHost.createView(
                        context, appWidgetId, widgetInfo
                    ).apply {
                        setAppWidget(appWidgetId, widgetInfo)
                    }
                }
            }
        }
    }

    if (showCustomPicker) {
        CustomWidgetPicker(onWidgetSelected = { widgetProviderInfo ->
            // Allocate widget ID
            val newWidgetId = appWidgetHost.allocateAppWidgetId()

            // Try to bind widget
            val allocated = appWidgetManager.bindAppWidgetIdIfAllowed(
                newWidgetId, widgetProviderInfo.provider
            )

            if (allocated) {
                // Widget successfully bound
                appWidgetId = newWidgetId
                saveWidgetId(context, appWidgetId)

                // Check if widget needs configuration
                needsConfiguration = isWidgetConfigurable(context, appWidgetId)
                if (needsConfiguration) {
                    launchWidgetConfiguration(context, appWidgetId)
                } else {
                    appWidgetHostView = appWidgetHost.createView(
                        context, appWidgetId, widgetProviderInfo
                    ).apply {
                        setAppWidget(appWidgetId, widgetProviderInfo)
                    }
                }
            } else {
                // Request bind widget permission
                val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newWidgetId)
                    putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, widgetProviderInfo.provider
                    )
                }
                bindWidgetPermissionLauncher.launch(bindIntent)
            }

            // Close the picker
            showCustomPicker = false
        }, onDismiss = { showCustomPicker = false })
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.widget))

        SettingsButton(
            label = stringResource(R.string.remove_widget), isTopOfGroup = true, onClick = {
                removeWidget(context)
                appWidgetHostView = null
                appWidgetId = -1
            })

        SettingsButton(
            label = stringResource(R.string.select_widget),
            isBottomOfGroup = true,
            onClick = { showCustomPicker = true })

        SettingsSpacer()

        // Widget offset slider
        var offsetSliderPosition by remember { mutableFloatStateOf(getWidgetOffset(context)) }
        SettingsSlider(
            label = stringResource(id = R.string.offset),
            value = offsetSliderPosition,
            onValueChange = {
                offsetSliderPosition = it
                setWidgetOffset(context, offsetSliderPosition)
            },
            valueRange = -20f..20f,
            steps = 19,
            onReset = {
                offsetSliderPosition = 0F
                setWidgetOffset(context, offsetSliderPosition)
            },
            isTopOfGroup = true
        )

        // Widget height slider
        var heightSliderPosition by remember { mutableFloatStateOf(getWidgetHeight(context)) }
        SettingsSlider(
            label = stringResource(id = R.string.height),
            value = heightSliderPosition,
            onValueChange = {
                heightSliderPosition = it
                setWidgetHeight(context, heightSliderPosition)
            },
            valueRange = 100f..400f,
            steps = 9,
            onReset = {
                heightSliderPosition = 125F
                setWidgetHeight(context, heightSliderPosition)
            })

        // Widget width slider
        var widthSliderPosition by remember { mutableFloatStateOf(getWidgetWidth(context)) }
        SettingsSlider(
            label = stringResource(id = R.string.width),
            value = widthSliderPosition,
            onValueChange = {
                widthSliderPosition = it
                setWidgetWidth(context, widthSliderPosition)
            },
            valueRange = 100f..400f,
            steps = 9,
            onReset = {
                widthSliderPosition = 250F
                setWidgetWidth(context, widthSliderPosition)
            },
            isBottomOfGroup = true
        )
        SettingsSpacer()
    }
}

/**
 * Page that lets you manage hidden apps
 *
 * @param mainAppModel Needed for context & hidden apps manager
 * @param goBack Function run when back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiddenApps(
    mainAppModel: MainAppModel,
    goBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var hiddenAppsList by remember { mutableStateOf(mainAppModel.hiddenAppsManager.getHiddenApps()) }

    if (!hiddenAppsList.isEmpty()) {
        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsHeader(goBack, stringResource(R.string.hidden_apps))
            }

            items(
                items = hiddenAppsList,
                key = { it } // use package name as unique key
            ) { appPackageName ->
                // Animate the removal of the item
                var visible by remember { mutableStateOf(true) }

                AnimatedVisibility(
                    visible = visible,
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    SettingsSwipeableButton(
                        label = AppUtils.getAppNameFromPackageName(
                            mainAppModel.getContext(),
                            appPackageName
                        ),
                        onClick = {},
                        onDeleteClick = {
                            // Trigger haptic feedback
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Animate item out
                            visible = false
                            // Remove from your list after a short delay to let animation run
                            coroutineScope.launch {
                                delay(500)
                                mainAppModel.hiddenAppsManager.removeHiddenApp(appPackageName)
                                hiddenAppsList = mainAppModel.hiddenAppsManager.getHiddenApps()
                            }
                        },
                        isTopOfGroup = hiddenAppsList.firstOrNull() == appPackageName,
                        isBottomOfGroup = hiddenAppsList.lastOrNull() == appPackageName
                    )
                }
            }

            item {
                SettingsSpacer()
            }
        }
    } else {
        Column {
            SettingsHeader(goBack, stringResource(R.string.hidden_apps))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), // takes all remaining space
                contentAlignment = Alignment.Center // centers content in that space
            ) {
                SettingsFullScreenMessage(
                    text = stringResource(R.string.no_hidden_apps),
                    icon = Icons.Default.Info
                )
            }
        }

    }
}

/**
 * Page that lets you manage apps with open challenge
 *
 * @param mainAppModel Needed for context & open challenge apps manager
 * @param goBack Function run when back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpenChallenges(
    mainAppModel: MainAppModel, goBack: () -> Unit
) {
    val challengeApps =
        remember { mutableStateOf(mainAppModel.challengesManager.getChallengeApps()) }
    val haptics = LocalHapticFeedback.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.open_challenges))

        challengeApps.value.forEachIndexed { index, app ->
            SettingsButton(
                label = AppUtils.getAppNameFromPackageName(mainAppModel.getContext(), app),
                onClick = {
                    mainAppModel.challengesManager.removeChallengeApp(app)
                    haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                    challengeApps.value = mainAppModel.challengesManager.getChallengeApps()
                },
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == challengeApps.value.lastIndex
            )
        }
    }
}

/**
 * Font options in settings
 *
 * @param context Needed to run some functions used within ThemeOptions
 * @param activity Needed to reload app after changing theme
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooseFont(context: Context, activity: Activity, goBack: () -> Unit) {
    val fontNames = listOf(
        "Jost",
        "Inter",
        "Lexend",
        "Work Sans",
        "Poppins",
        "Roboto",
        "Open Sans",
        "Lora",
        "Outfit",
        "IBM Plex Sans",
        "IBM Plex Serif"
    )

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.font))

        fontNames.forEachIndexed { index, fontName ->
            SettingsButton(
                label = fontName,
                onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), fontName)
                    resetActivity(context, activity)
                },
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == fontNames.lastIndex,
                fontFamily = getTypographyFromFontName(fontName).bodyMedium.fontFamily
            )
        }
        SettingsSpacer()
    }
}

/**
 * Developer options in settings
 */
@Composable
fun DevOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, "Developer Options")

        SettingsSwitch(
            "First time",
            getBooleanSetting(context, "FirstTime", false),
            onCheckedChange = { it ->
                setBooleanSetting(context, "FirstTime", it)
            },
            isTopOfGroup = true,
            isBottomOfGroup = true
        )
    }
}

/**
 * Privacy Policy dialog
 *
 * @param mainAppModel Needed for context
 * @param showPolicyDialog Pass the MutableState<Boolean> your using to show and hide this dialog so that it can be hidden from within it
 */
@Composable
fun PrivacyPolicyDialog(mainAppModel: MainAppModel, showPolicyDialog: MutableState<Boolean>) {
    val scrollState = rememberScrollState()
    Column {
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)  // Make the content scrollable
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // Load text from the asset
                loadTextFromAssets(mainAppModel.getContext(), "Privacy Policy.txt")?.let { text ->
                    BasicText(
                        text = text, style = TextStyle(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Normal
                        ), modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "OK" Button
                Button(
                    onClick = { showPolicyDialog.value = false },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp),
                    colors = ButtonColors(
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                ) {
                    Text("OK")
                }
            }
        }
    }
}