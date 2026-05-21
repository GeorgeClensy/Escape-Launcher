package com.geecee.escapelauncher.feature.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager

const val WIDGET_HOST_ID = 44203
const val NO_WIDGET_ID = -1

private val lock = Any()
private var sharedAppWidgetHost: AppWidgetHost? = null

/**
 * Returns the shared app widget host for the application
 *
 * @param context The application context
 * @return The shared AppWidgetHost
 */
fun getAppWidgetHost(context: Context): AppWidgetHost {
    return sharedAppWidgetHost ?: synchronized(lock) {
        sharedAppWidgetHost ?: AppWidgetHost(context.applicationContext, WIDGET_HOST_ID).also {
            sharedAppWidgetHost = it
        }
    }
}

/**
 * Returns all available widget grouped by app in a map
 *
 * @author George Clensy
 * @param context The application context
 * @return A map of a widget app data to a list of widget information
 */
fun loadWidgetsGroupedByApp(context: Context): Map<WidgetAppData, List<WidgetInfo>> {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val packageManager = context.packageManager

    // Get all installed widget providers
    val providers = appWidgetManager.installedProviders

    // Group them by package name
    return providers
        .groupBy { it.provider.packageName }
        .mapKeys { (packageName, _) ->
            // Get app info for each package
            val widgetAppData = try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                WidgetAppData(
                    packageName = packageName,
                    appName = packageManager.getApplicationLabel(applicationInfo).toString(),
                    icon = packageManager.getApplicationIcon(packageName)
                )
            } catch (_: PackageManager.NameNotFoundException) {
                WidgetAppData(
                    packageName = packageName,
                    appName = packageName.split(".").last(),
                    icon = null
                )
            }
            widgetAppData
        }
        .mapValues { (_, providers) ->
            providers.map { providerInfo ->
                WidgetInfo(
                    provider = providerInfo,
                    label = providerInfo.loadLabel(packageManager),
                    minWidth = providerInfo.minWidth,
                    minHeight = providerInfo.minHeight
                )
            }
        }
}
