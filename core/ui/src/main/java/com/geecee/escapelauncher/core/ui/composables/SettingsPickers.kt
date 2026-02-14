package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.theme.CardContainerColor
import com.geecee.escapelauncher.core.ui.theme.ContentColor


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
    isBottomOfGroup: Boolean = false,
    resetButtonContentDescription: String
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
                contentDescription = resetButtonContentDescription,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp)
                    .clickable(onClick = onReset),
                tint = ContentColor,
            )
        }
    }
}
