package com.geecee.escapelauncher.feature.settings.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSlider
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.feature.widgets.CustomWidgetPicker
import com.geecee.escapelauncher.feature.widgets.NO_WIDGET_ID
import com.geecee.escapelauncher.feature.widgets.getAppWidgetHost
import com.geecee.escapelauncher.feature.widgets.WidgetsScreen
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
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = remember { getAppWidgetHost(context) }

    // Ensure the host is listening for widget updates while on this screen
    DisposableEffect(appWidgetHost) {
        try {
            appWidgetHost.startListening()
        } catch (e: Exception) {
            Log.e("WidgetOptions", "Error starting AppWidgetHost", e)
        }
        onDispose {
            try {
                appWidgetHost.stopListening()
            } catch (e: Exception) {
                Log.e("WidgetOptions", "Error stopping AppWidgetHost", e)
            }
        }
    }
    
    var showCustomPicker by remember { mutableStateOf(false) }
    var pendingWidgetInfo by remember { mutableStateOf<Pair<Int, AppWidgetProviderInfo>?>(null) }
    var tempWidgetId by remember { mutableStateOf<Int?>(null) }

    val widgetOffset by viewModel.widgetOffset.collectAsState(initial = 0f)
    val widgetHeight by viewModel.widgetHeight.collectAsState(initial = 125f)
    val widgetWidth by viewModel.widgetWidth.collectAsState(initial = 250f)
    val widgetId by viewModel.widgetId.collectAsState(initial = -1)

    // Sync tempWidgetId with saved widgetId
    LaunchedEffect(widgetId) {
        if (widgetId == tempWidgetId) {
            tempWidgetId = null
        }
    }

    val configureWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (id != -1) {
                if (widgetId != NO_WIDGET_ID && widgetId != id) {
                    appWidgetHost.deleteAppWidgetId(widgetId)
                }
                tempWidgetId = id
                viewModel.setWidgetId(id)
            }
        }
    }

    val bindWidgetPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingWidgetInfo?.let { (id, info) ->
                if (info.configure != null) {
                    val intent = Intent(context, com.geecee.escapelauncher.feature.widgets.ConfigureAppWidgetActivity::class.java).apply {
                        putExtra(com.geecee.escapelauncher.feature.widgets.ConfigureAppWidgetActivity.EXTRA_APP_WIDGET_PROVIDER_INFO, info)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    }
                    configureWidgetLauncher.launch(intent)
                } else {
                    if (widgetId != NO_WIDGET_ID && widgetId != id) {
                        appWidgetHost.deleteAppWidgetId(widgetId)
                    }
                    tempWidgetId = id
                    viewModel.setWidgetId(id)
                }
            }
        } else {
            Log.e("WidgetOptions", "User denied widget bind permission")
        }
        pendingWidgetInfo = null
    }

    // Function to handle widget selection and configuration
    fun onWidgetSelected(info: AppWidgetProviderInfo) {
        val newId = appWidgetHost.allocateAppWidgetId()
        val canBind = appWidgetManager.bindAppWidgetIdIfAllowed(newId, info.provider)

        if (canBind) {
            if (info.configure != null) {
                val intent = Intent(context, com.geecee.escapelauncher.feature.widgets.ConfigureAppWidgetActivity::class.java).apply {
                    putExtra(com.geecee.escapelauncher.feature.widgets.ConfigureAppWidgetActivity.EXTRA_APP_WIDGET_PROVIDER_INFO, info)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
                }
                configureWidgetLauncher.launch(intent)
            } else {
                if (widgetId != NO_WIDGET_ID) {
                    appWidgetHost.deleteAppWidgetId(widgetId)
                }
                tempWidgetId = newId
                viewModel.setWidgetId(newId)
            }
        } else {
            // Need to request bind permission
            pendingWidgetInfo = Pair(newId, info)
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            }
            bindWidgetPermissionLauncher.launch(intent)
        }
    }

    if (showCustomPicker) {
        CustomWidgetPicker(
            onWidgetSelected = { info ->
                onWidgetSelected(info)
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
                    if (widgetId != NO_WIDGET_ID) {
                        appWidgetHost.deleteAppWidgetId(widgetId)
                        viewModel.setWidgetId(NO_WIDGET_ID)
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

        val displayWidgetId = tempWidgetId ?: widgetId
        if (displayWidgetId != NO_WIDGET_ID) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WidgetsScreen(
                        widgetId = displayWidgetId,
                        context = context,
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
