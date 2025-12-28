package com.geecee.escapelauncher.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.AppTheme
import com.geecee.escapelauncher.ui.theme.CardContainerColor
import com.geecee.escapelauncher.ui.theme.CardContainerColorDisabled
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.ui.theme.ContentColorDisabled
import com.geecee.escapelauncher.ui.theme.ErrorContainerColor
import com.geecee.escapelauncher.ui.theme.ErrorContentColor
import com.geecee.escapelauncher.ui.theme.primaryContentColor
import com.geecee.escapelauncher.ui.theme.resolveColorScheme
import com.geecee.escapelauncher.ui.theme.transparentHalf
import com.geecee.escapelauncher.utils.InstalledApp

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

/**
 * Settings title header with back button
 *
 * @param title The text shown on the header
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsHeader(
    goBack: () -> Unit,
    title: String,
    hideBack: Boolean = false,
    color: Color = ContentColor,
    padding: Boolean = true
) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = { goBack() })
            .padding(
                0.dp,
                if (padding) {
                    120.dp
                } else {
                    0.dp
                },
                0.dp,
                8.dp
            )
            .height(70.dp) // Set a fixed height for the header
    ) {
        if (!hideBack) {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = color,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        AutoResizingText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color,
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
            color = ContentColor,
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
        ), colors = CardDefaults.cardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor
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
        ), colors = CardDefaults.cardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor
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
                style = MaterialTheme.typography.bodyMedium,
            )
            val iconModifier = Modifier.size(24.dp) // Standardized icon size slightly
            if (diagonalArrow == true) { // Explicitly check for true
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null, // Content description can be null for decorative icons
                    modifier = iconModifier.rotate(-45f),
                    tint = ContentColor,
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null, // Content description can be null for decorative icons
                    modifier = iconModifier,
                    tint = ContentColor,
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
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
    isDisabled: Boolean = false
) {
    // Define the base corner size
    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val topStartRadius by animateDpAsState(
        targetValue = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        label = "topStartRadius"
    )
    val topEndRadius by animateDpAsState(
        targetValue = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        label = "topEndRadius"
    )
    val bottomStartRadius by animateDpAsState(
        targetValue = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        label = "bottomStartRadius"
    )
    val bottomEndRadius by animateDpAsState(
        targetValue = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        label = "bottomEndRadius"
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (!isDisabled) CardContainerColor else CardContainerColorDisabled,
        animationSpec = tween(durationMillis = 300),
        label = "containerColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (!isDisabled) ContentColor else ContentColorDisabled,
        animationSpec = tween(durationMillis = 300),
        label = "contentColor"
    )

    Card(
        modifier = modifier
            .padding(vertical = 1.dp)
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomEnd = bottomEndRadius,
            bottomStart = bottomStartRadius
        ),
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor,
            contentColor = animatedContentColor
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
        positionalThreshold = { it * 0.5f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteClick()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.padding(vertical = 1.dp),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Background while swiping
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = topStartRadius,
                    topEnd = topEndRadius,
                    bottomEnd = bottomEndRadius,
                    bottomStart = bottomStartRadius
                ),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorContainerColor,
                    contentColor = ErrorContentColor
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = ErrorContentColor
                    )
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
            ), colors = CardDefaults.cardColors(
                containerColor = CardContainerColor,
                contentColor = ContentColor
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
                        .weight(1f)
                        .padding(end = 8.dp),
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
                .background(AppTheme.fromId(theme).resolveColorScheme().background)
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
                            AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer,
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
                            tint = AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer
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
                            AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer,
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
                            tint = AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer
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
                            AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer,
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
                            tint = AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                stringResource(AppTheme.nameResFromId(theme)),
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer,
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
                            AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer,
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
                            tint = AppTheme.fromId(theme).resolveColorScheme().onPrimaryContainer
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
                            containerColor = AppTheme.fromId(theme).resolveColorScheme().primary,
                            contentColor = AppTheme.fromId(theme).resolveColorScheme().onPrimary
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
                            containerColor = AppTheme.fromId(theme).resolveColorScheme().primary,
                            contentColor = AppTheme.fromId(theme).resolveColorScheme().onPrimary
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
        ), colors = CardDefaults.cardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor
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
        ), colors = CardDefaults.cardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor
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
                tint = ContentColor,
            )
        }
    }
}

@Composable
fun SponsorBox(
    text: String,
    secondText: String,
    onSponsorClick: () -> Unit = {},
    onBackgroundClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .fillMaxWidth()
            .clickable(onClick = {
                onBackgroundClick()
            }),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor
        )
    ) {
        Column(
            Modifier
                .padding(vertical = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(R.drawable.outlineicon),
                "Escape Launcher Icon",
                Modifier
                    .padding(3.dp),
                tint = ContentColor
            )

            Spacer(
                Modifier.height(10.dp)
            )

            AutoResizingText(
                text = text,
                modifier = Modifier,
                color = ContentColor,
            )

            Spacer(Modifier.height(10.dp))

            AutoResizingText(
                text = secondText,
                modifier = Modifier,
                color = ContentColor,
            )

            Spacer(Modifier.height(15.dp))

            Button(
                onClick = {
                    onSponsorClick()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContentColor,
                    contentColor = CardContainerColor
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, "", tint = CardContainerColor)
                    Spacer(Modifier.width(5.dp))
                    AutoResizingText(
                        text = stringResource(R.string.sponsor),
                        color = CardContainerColor
                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun SponsorBoxPreview() {
    SponsorBox("Escape Launcher", "Testing")
}

/**
 * Weather app picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppPicker(
    apps: List<InstalledApp>,
    onAppSelected: (InstalledApp) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(apps.sortedBy { it.displayName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { onAppSelected(app) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = app.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryContentColor
                        )
                    }
                }
            }
        }
    }
}