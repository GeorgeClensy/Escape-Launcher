package com.geecee.escapelauncher.feature.widgets

import android.appwidget.AppWidgetProviderInfo

/**
 * The information for an individual widget
 *
 * @author George Clensy
 * @param provider The widget provider
 * @param label The widget label
 * @param minWidth The widgets minimum width
 * @param minHeight The widgets maximum height
 */
data class WidgetInfo(
    val provider: AppWidgetProviderInfo,
    val label: String,
    val minWidth: Int,
    val minHeight: Int
)