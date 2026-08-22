package com.geecee.escapelauncher.feature.newwidgets

import android.appwidget.AppWidgetManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * A modular Composable for rendering an Android App Widget.
 *
 * @param appWidgetId The ID of the widget to render.
 * @param widgetHostManager The manager providing the AppWidgetHost and AppWidgetManager.
 * @param modifier Modifier for sizing and positioning the widget.
 */
@Composable
fun WidgetRenderer(
    appWidgetId: Int,
    widgetHostManager: WidgetHostManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

    // Create or retrieve the host view
    val hostView = remember(appWidgetId) {
        try {
            val widgetInfo = widgetHostManager.manager.getAppWidgetInfo(appWidgetId)
            if (widgetInfo != null) {
                widgetHostManager.host.createView(context, appWidgetId, widgetInfo).apply {
                    setAppWidget(appWidgetId, widgetInfo)
                }
            } else {
                Log.e("WidgetRenderer", "Widget info not found for ID $appWidgetId")
                null
            }
        } catch (e: Exception) {
            Log.e("WidgetRenderer", "Error creating widget view: ${e.message}")
            null
        }
    }

    hostView?.let { view ->
        key(appWidgetId) {
            AndroidView(
                factory = { view },
                modifier = modifier,
                update = {
                    //todo: do the updates
                }
            )
        }
    }
}
