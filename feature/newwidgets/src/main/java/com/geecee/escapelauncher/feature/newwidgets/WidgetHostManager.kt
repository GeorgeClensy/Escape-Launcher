package com.geecee.escapelauncher.feature.newwidgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetHostManager @Inject constructor(
    @ApplicationContext context: Context
) {
    // 1024 is an arbitrary host ID unique to your launcher
    val host = AppWidgetHost(context, 1024)
    val manager: AppWidgetManager = AppWidgetManager.getInstance(context)

    fun startListening() = host.startListening()
    fun stopListening() = host.stopListening()

    fun allocateWidgetId(): Int = host.allocateAppWidgetId()

    fun deleteWidgetId(appWidgetId: Int) {
        host.deleteAppWidgetId(appWidgetId)
    }
}