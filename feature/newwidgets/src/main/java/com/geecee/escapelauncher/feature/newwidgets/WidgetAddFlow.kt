package com.geecee.escapelauncher.feature.newwidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun WidgetAddFlow(
    providerInfo: AppWidgetProviderInfo,
    onWidgetConfigured: (appWidgetId: Int) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    val widgetConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                onWidgetConfigured(appWidgetId)
            } else {
                onCancel()
            }
        } else {
            onCancel()
        }
    }

    // Trigger the flow when a widget is selected
    LaunchedEffect(providerInfo) {
        val intent = Intent(context, ConfigureWidgetActivity::class.java).apply {
            putExtra(ConfigureWidgetActivity.EXTRA_APP_WIDGET_PROVIDER_INFO, providerInfo)
        }
        widgetConfigLauncher.launch(intent)
    }
}