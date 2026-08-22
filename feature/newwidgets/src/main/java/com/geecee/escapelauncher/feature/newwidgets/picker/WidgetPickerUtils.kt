package com.geecee.escapelauncher.feature.newwidgets.picker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.PackageManager
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
