package com.geecee.escapelauncher.feature.settings.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSlider
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.feature.newwidgets.picker.CustomWidgetPicker
import com.geecee.escapelauncher.feature.newwidgets.WidgetAddFlow
import com.geecee.escapelauncher.feature.newwidgets.WidgetRenderer
import com.geecee.escapelauncher.core.ui.R

/**
 * Widget Setup screen
 *
 * @param onBackClick When back button is pressed
 */
@Composable
fun WidgetOptions(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WidgetOptionsViewModel = hiltViewModel()
) {
    var showCustomPicker by remember { mutableStateOf(false) }
    var selectedProviderInfo by remember { mutableStateOf<AppWidgetProviderInfo?>(null) } // This is the data for the widget, it tells the launcher which actual widget were working with

    val widgetOffset by viewModel.widgetOffset.collectAsState(initial = 0f)
    val widgetHeight by viewModel.widgetHeight.collectAsState(initial = 125f)
    val widgetWidth by viewModel.widgetWidth.collectAsState(initial = 250f)
    val widgetId by viewModel.widgetId.collectAsState(initial = AppWidgetManager.INVALID_APPWIDGET_ID)

    // Modular Widget Addition Flow
    selectedProviderInfo?.let { info ->
        WidgetAddFlow(
            providerInfo = info,
            onWidgetConfigured = { newId ->
                // Delete old widget if it exists and is different
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && widgetId != newId) {
                    viewModel.widgetHostManager.deleteWidgetId(widgetId)
                }
                viewModel.setWidgetId(newId)
                selectedProviderInfo = null
            },
            onCancel = {
                selectedProviderInfo = null
            }
        )
    }

    if (showCustomPicker) {
        CustomWidgetPicker(
            onWidgetSelected = { info ->
                selectedProviderInfo = info
                showCustomPicker = false
            },
            onDismiss = { showCustomPicker = false }
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxSize()
    ) {
        item { EscapeHeader(onBackClick, stringResource(R.string.widget)) }

        item {
            SettingsButton(
                label = stringResource(R.string.remove_widget),
                isTopOfGroup = true,
                onClick = {
                    if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        viewModel.widgetHostManager.deleteWidgetId(widgetId)
                        viewModel.setWidgetId(AppWidgetManager.INVALID_APPWIDGET_ID)
                    }
                }
            )
        }

        item {
            SettingsButton(
                label = stringResource(R.string.select_widget),
                isBottomOfGroup = true,
                onClick = { showCustomPicker = true }
            )
        }

        item { SettingsSpacer() }

        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WidgetRenderer(
                        appWidgetId = widgetId,
                        widgetHostManager = viewModel.widgetHostManager,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (widgetOffset.dp).toPx().toInt(), 0
                                )
                            }
                            .size(widgetWidth.dp, widgetHeight.dp)
                    )
                }
            }
            item { SettingsSpacer() }
        }

        // Offset slider
        item {
            SettingsSlider(
                label = stringResource(R.string.offset),
                value = widgetOffset,
                onValueChange = {
                    viewModel.setWidgetOffset(it)
                },
                valueRange = -20f..20f,
                steps = 19,
                resetButtonContentDescription = stringResource(R.string.reset_to_default),
                onReset = {
                    viewModel.setWidgetOffset(0f)
                },
                isTopOfGroup = true
            )
        }

        // Height slider
        item {
            SettingsSlider(
                label = stringResource(R.string.height),
                value = widgetHeight,
                onValueChange = {
                    viewModel.setWidgetHeight(it)
                },
                valueRange = 100f..400f,
                steps = 9,
                resetButtonContentDescription = stringResource(R.string.reset_to_default),
                onReset = {
                    viewModel.setWidgetHeight(125f)
                }
            )
        }

        // Width slider
        item {
            SettingsSlider(
                label = stringResource(R.string.width),
                value = widgetWidth,
                onValueChange = {
                    viewModel.setWidgetWidth(it)
                },
                valueRange = 100f..400f,
                steps = 9,
                resetButtonContentDescription = stringResource(R.string.reset_to_default),
                onReset = {
                    viewModel.setWidgetWidth(250f)
                },
                isBottomOfGroup = true
            )
        }

        item { SettingsSpacer() }
        item { SettingsSpacer() }
    }
}
