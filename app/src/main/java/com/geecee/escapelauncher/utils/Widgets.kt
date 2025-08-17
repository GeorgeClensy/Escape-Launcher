package com.geecee.escapelauncher.utils

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.graphics.drawable.toBitmap
import com.geecee.escapelauncher.R

@Composable
fun WidgetsScreen(
    context: Context,
    modifier: Modifier
) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = remember { AppWidgetHost(context, 1) }
    val appWidgetId by remember { mutableIntStateOf(getSavedWidgetId(context)) }
    var appWidgetHostView by remember { mutableStateOf<AppWidgetHostView?>(null) }


    // On appWidgetId change, re-setup the widget view
    LaunchedEffect(appWidgetId) {
        try {
            if (appWidgetId != -1) {
                val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                if (widgetInfo != null) {
                    appWidgetHostView =
                        appWidgetHost.createView(context, appWidgetId, widgetInfo).apply {
                            setAppWidget(appWidgetId, widgetInfo)
                        }
                } else {
                    Log.e("WidgetsScreen", "Widget info not found for ID $appWidgetId")
                }
            }
        } catch (e: Exception) {
            Log.e("Widget error", e.message.toString())
        }
    }

    appWidgetHostView?.let { hostView ->
        AndroidView(
            factory = { hostView },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWidgetPicker(
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Load widget providers grouped by app
    val widgetProviders = remember { loadWidgetsGroupedByApp(context) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(widgetProviders.size) { index ->
                    val (appInfo, widgets) = widgetProviders.entries.elementAt(index)
                    WidgetAppItem(
                        appInfo = appInfo,
                        widgets = widgets,
                        onWidgetSelected = onWidgetSelected
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetAppItem(
    appInfo: AppInfo,
    widgets: List<WidgetInfo>,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // App header with icon, name, count, and expand button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    appInfo.icon?.let {
                        Image(
                            bitmap = it.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // App name and widget count
                Column {
                    Text(
                        text = appInfo.appName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${widgets.size} ${if (widgets.size == 1) "widget" else "widgets"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expand/collapse icon
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotationState)
            )
        }

        // Widget previews when expanded
        AnimatedVisibility(visible = expanded) {
            if (widgets.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    widgets.forEach { widget ->
                        WidgetPreviewItem(
                            widget = widget,
                            onClick = { onWidgetSelected(widget.provider) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WidgetPreviewItem(
    widget: WidgetInfo,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
    ) {
        // Widget preview
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            widget.previewImage?.let {
                Image(
                    bitmap = it.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                // Fallback if no preview image
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Widget name
        Text(
            text = widget.label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// Data classes for widgets
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

data class WidgetInfo(
    val provider: AppWidgetProviderInfo,
    val label: String,
    val previewImage: Drawable?,
    val minWidth: Int,
    val minHeight: Int
)

// Load all available widgets and group them by app
fun loadWidgetsGroupedByApp(context: Context): Map<AppInfo, List<WidgetInfo>> {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val packageManager = context.packageManager

    // Get all installed widget providers
    val providers = appWidgetManager.installedProviders

    // Group them by package name
    return providers
        .groupBy { it.provider.packageName }
        .mapKeys { (packageName, _) ->
            // Get app info for each package
            val appInfo = try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                AppInfo(
                    packageName = packageName,
                    appName = packageManager.getApplicationLabel(applicationInfo).toString(),
                    icon = packageManager.getApplicationIcon(packageName)
                )
            } catch (_: PackageManager.NameNotFoundException) {
                AppInfo(
                    packageName = packageName,
                    appName = packageName.split(".").last(),
                    icon = null
                )
            }
            appInfo
        }
        .mapValues { (_, providers) ->
            providers.map { providerInfo ->
                WidgetInfo(
                    provider = providerInfo,
                    label = providerInfo.loadLabel(packageManager),
                    previewImage = providerInfo.loadPreviewImage(context, 0),
                    minWidth = providerInfo.minWidth,
                    minHeight = providerInfo.minHeight
                )
            }
        }
}

fun launchWidgetConfiguration(context: Context, appWidgetId: Int) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
    val configureComponent = appWidgetInfo?.configure

    configureComponent?.let {
        val resolveInfo = context.packageManager.resolveActivity(
            Intent().setComponent(configureComponent),
            PackageManager.MATCH_DEFAULT_ONLY
        )

        if (resolveInfo?.activityInfo?.exported == true) {
            val configureIntent = Intent().apply {
                component = it
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK // Add this flag
            }
            context.startActivity(configureIntent)
        } else {
            Log.w("WidgetsScreen", "Configuration activity is not exported and cannot be started.")
        }
    }
}

fun isWidgetConfigurable(context: Context, appWidgetId: Int): Boolean {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return false
    val configureComponent = appWidgetInfo.configure ?: return false

    // Check if the configuration activity is exported
    val resolveInfo = context.packageManager.resolveActivity(
        Intent().setComponent(configureComponent),
        PackageManager.MATCH_DEFAULT_ONLY
    )
    return resolveInfo != null && resolveInfo.activityInfo.exported
}

fun saveWidgetId(context: Context, widgetId: Int) {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        putInt("widget_id", widgetId)
    }
}

fun getSavedWidgetId(context: Context): Int {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("widget_id", -1)
}

fun removeWidget(context: Context) {
    // Save the updated widget ID to shared preferences
    saveWidgetId(context, -1)
}

fun setWidgetOffset(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putFloat("WidgetOffset", sliderPosition)

    }
}

fun getWidgetOffset(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getFloat("WidgetOffset", 0f)
}

fun setWidgetHeight(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putFloat("WidgetHeight", sliderPosition)

    }
}

fun getWidgetWidth(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getFloat("WidgetWidth", 150f)
}

fun setWidgetWidth(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putFloat("WidgetWidth", sliderPosition)

    }
}

fun getWidgetHeight(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getFloat("WidgetHeight", 125f)
}