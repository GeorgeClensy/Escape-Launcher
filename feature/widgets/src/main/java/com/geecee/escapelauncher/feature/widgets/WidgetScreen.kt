package com.geecee.escapelauncher.feature.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * This is the composable that you place on the home screen that actually displays the widget
 *
 * @author George Clensy
 */
@Composable
fun WidgetsScreen(
    widgetId: Int,
    context: Context,
    modifier: Modifier
) {
    if (widgetId == NO_WIDGET_ID) return

    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = remember { getAppWidgetHost(context) }

    // Use remember(widgetId) to create the host view whenever the ID changes
    val hostView = remember(widgetId) {
        try {
            val widgetInfo = appWidgetManager.getAppWidgetInfo(widgetId)
            if (widgetInfo != null) {
                appWidgetHost.createView(context, widgetId, widgetInfo).apply {
                    setAppWidget(widgetId, widgetInfo)
                }
            } else {
                Log.e("Widgets", "Widget info not found for ID $widgetId")
                null
            }
        } catch (e: Exception) {
            Log.e("Widgets", "Error creating widget view: ${e.message}")
            null
        }
    }

    hostView?.let { view ->
        // Use key(widgetId) to force AndroidView to be fully recreated if the widget changes
        key(widgetId) {
            AndroidView(
                factory = { view },
                modifier = modifier,
                update = {
                    // Update if needed
                }
            )
        }
    }
}
