package com.geecee.escapelauncher.feature.widgets

import android.graphics.drawable.Drawable

/**
 * Details of an app that has widgets
 *
 * @author George Clensy
 * @param packageName The package name for the app
 * @param appName The display name of the app
 * @param icon The apps icon
 */
data class WidgetAppData(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)
