package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.theme.CardContainerColor
import com.geecee.escapelauncher.core.ui.theme.CardContainerColorDisabled
import com.geecee.escapelauncher.core.ui.theme.ContentColor
import com.geecee.escapelauncher.core.ui.theme.ContentColorDisabled
import com.geecee.escapelauncher.core.ui.theme.ErrorContainerColor
import com.geecee.escapelauncher.core.ui.theme.ErrorContentColor

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
 * @param useAutoResize Whether to use the expensive AutoResizingText or a standard Text with ellipsis
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
    isDisabled: Boolean = false,
    useAutoResize: Boolean = true
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
            if (useAutoResize) {
                AutoResizingText(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = fontFamily
                )
            } else {
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = fontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
    deleteIconContentDescription: String
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
                        contentDescription = deleteIconContentDescription,
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
 * Spacer 30.dp height
 */
@Composable
fun SettingsSpacer() {
    Spacer(modifier = Modifier.height(30.dp))
}
