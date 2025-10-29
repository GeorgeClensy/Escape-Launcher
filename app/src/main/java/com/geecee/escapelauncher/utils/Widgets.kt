package com.geecee.escapelauncher.utils

import android.app.Activity
import android.app.ActivityOptions
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
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
import com.geecee.escapelauncher.ui.theme.BackgroundColor
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.ui.theme.primaryContentColor

// Constants for SharedPreferences used in widget saving/loading
private const val WIDGET_PREFS_NAME = "widget_prefs"
private const val WIDGET_ID_KEY = "widget_id"
private const val INVALID_WIDGET_ID = -1
const val WIDGET_HOST_ID = 44203

/**
 * Activity for showing the widget configuration
 *
 * @author George Clensy
 */
class ConfigureAppWidgetActivity : Activity() {
    /**
     * The app widget host for getting a widget
     *
     * @author George Clensy
     */
    private lateinit var appWidgetHost: AppWidgetHost

    /**
     * The app widget manager for managing widgets
     *
     * @author George Clensy
     */
    private lateinit var appWidgetManager: AppWidgetManager

    /**
     * The ID of the widget being configured
     *
     * @author George Clensy
     */
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    /**
     * Activity entry point
     *
     * @author George Clensy
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetHost = AppWidgetHost(this, WIDGET_HOST_ID)
        appWidgetManager = AppWidgetManager.getInstance(this)

        val appWidgetProviderInfo: AppWidgetProviderInfo? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    EXTRA_APP_WIDGET_PROVIDER_INFO,
                    AppWidgetProviderInfo::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_APP_WIDGET_PROVIDER_INFO)
            }
        if (appWidgetProviderInfo == null) {
            Log.e("Widgets", "No app widget provider info provided, canceling")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Use existing widget ID if provided
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID_WIDGET_ID)
        if (appWidgetId == INVALID_WIDGET_ID) {
            // Allocate a new widget ID only if none provided
            appWidgetId = appWidgetHost.allocateAppWidgetId()
        }

        configureAppWidget(appWidgetProviderInfo, appWidgetId)
    }

    /**
     * Checks that the widget is configurable and then starts the configuration activity for the ID using the widget host
     *
     * @author George Clensy
     */
    private fun configureAppWidget(widget: AppWidgetProviderInfo, appWidgetId: Int) {
        if (widget.configure != null) {
            appWidgetHost.startAppWidgetConfigureActivityForResult(
                this,
                appWidgetId,
                0,
                REQUEST_CODE_CONFIGURE,
                getConfigurationOptions(),
            )
        } else {
            finishWithResult(appWidgetId)
        }
    }

    /**
     * Returns the configurationOptions as a Bundle? for starting the widget configuration activity
     *
     * @author George Clensy
     * @return Bundle? with ActivityOptions.makeBasic and .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE)
     */
    private fun getConfigurationOptions(): Bundle? {
        if (Build.VERSION.SDK_INT < 34) return null
        val mode = if (Build.VERSION.SDK_INT >= 36) {
            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
        } else {
            @Suppress("DEPRECATION")
            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
        }
        return ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(mode)
            .toBundle()
    }



    /**
     * Finishes the activity
     *
     * @author George Clensy
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_BIND -> {
                if (resultCode == RESULT_OK) {
                    val widget = appWidgetManager.getAppWidgetInfo(appWidgetId)
                    configureAppWidget(widget, appWidgetId)
                } else {
                    appWidgetHost.deleteAppWidgetId(appWidgetId)
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }

            REQUEST_CODE_CONFIGURE -> {
                if (resultCode == RESULT_OK) {
                    finishWithResult(appWidgetId)
                } else {
                    appWidgetHost.deleteAppWidgetId(appWidgetId)
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }

            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    /**
     * Finishes the config activity with success
     */
    private fun finishWithResult(widgetId: Int) {
        val data = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            val providerInfo: AppWidgetProviderInfo? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_APP_WIDGET_PROVIDER_INFO, AppWidgetProviderInfo::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_APP_WIDGET_PROVIDER_INFO)
                }
            putExtra(EXTRA_APP_WIDGET_PROVIDER_INFO, providerInfo)
        }

        setResult(RESULT_OK, data)
        finish()
    }

    companion object {
        const val REQUEST_CODE_CONFIGURE = 1
        const val REQUEST_CODE_BIND = 2
        const val EXTRA_APP_WIDGET_PROVIDER_INFO = "extra_app_widget_provider_info"
    }
}

//
// Composables
//

/**
 * This is the composable that you place on the home screen that actually displays the widget
 *
 * @author George Clensy
 */
@Composable
fun WidgetsScreen(
    context: Context,
    modifier: Modifier
) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = remember { AppWidgetHost(context, WIDGET_HOST_ID) }
    val appWidgetId by remember { mutableIntStateOf(getSavedWidgetId(context)) } // The ID of the widget being used. This is set by escape launcher
    var appWidgetHostView by remember { mutableStateOf<AppWidgetHostView?>(null) }


    // On appWidgetId change, re-setup the widget view
    LaunchedEffect(appWidgetId) {
        try {
            if (appWidgetId != INVALID_WIDGET_ID) {
                val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                if (widgetInfo != null) {
                    appWidgetHostView =
                        appWidgetHost.createView(context, appWidgetId, widgetInfo).apply {
                            setAppWidget(appWidgetId, widgetInfo)
                        }
                } else {
                    Log.e("Widgets", "Widget info not found for ID $appWidgetId")
                }
            }
        } catch (e: Exception) {
            Log.e("Widgets", e.message.toString())
        }
    }

    appWidgetHostView?.let { hostView ->
        AndroidView(
            factory = { hostView },
            modifier = modifier
        )
    }
}

/**
 * The widget picker itself
 *
 * @author George Clensy
 * @param onWidgetSelected Unit for when a widget is selected
 * @param onDismiss Unit for when the picker is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWidgetPicker(
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Load widget providers grouped by app
    val widgetProviders = remember { loadWidgetsGroupedByApp(context) }
    // Sort the widget providers by app name
    val sortedWidgetEntries = remember(widgetProviders) {
        widgetProviders.entries.sortedBy { it.key.appName }
    }

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
                items(sortedWidgetEntries.size) { index ->
                    val (appInfo, widgets) = sortedWidgetEntries[index] // Use the sorted list
                    WidgetAppItem(
                        widgetAppData = appInfo,
                        widgets = widgets,
                        onWidgetSelected = onWidgetSelected
                    )
                }
            }
        }
    }
}

/**
 * The app folder in the widget menu
 *
 * @author George Clensy
 * @param widgetAppData The app data
 * @param widgets A list of the apps widgets
 * @param onWidgetSelected Unit for when a widget is selected
 */
@Composable
fun WidgetAppItem(
    widgetAppData: WidgetAppData,
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
                Spacer(modifier = Modifier.width(16.dp))

                // App name and widget count
                Column {
                    Text(
                        text = widgetAppData.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryContentColor
                    )
                    Text(
                        text = "${widgets.size} ${if (widgets.size == 1) "widget" else "widgets"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ContentColor
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

/**
 * The widget preview you click on to select it on the widget picker
 *
 * @author George Clensy
 * @param widget Widget information to display
 * @param onClick Unit ran when widget is clicked
 */
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
                .background(BackgroundColor)
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
                    tint = ContentColor
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
 * @param previewImage The widget preview image
 * @param minWidth The widgets minimum width
 * @param minHeight The widgets maximum height
 */
data class WidgetInfo(
    val provider: AppWidgetProviderInfo,
    val label: String,
    val previewImage: Drawable?,
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
                    previewImage = providerInfo.loadPreviewImage(context, 0),
                    minWidth = providerInfo.minWidth,
                    minHeight = providerInfo.minHeight
                )
            }
        }
}

//
// Config stuff
//

/**
 * This launches the apps widget configuration activity and opens the configuration for a specific widget
 *
 * @return Will return true on success, on failure, will return false and return error message to log with tag "Widgets"
 * @author George Clensy
 */
fun launchWidgetConfiguration(
    context: Context,
    appWidgetProviderInfo: AppWidgetProviderInfo,
    appWidgetId: Int
): Boolean {
    return try {
        val intent = Intent(context, ConfigureAppWidgetActivity::class.java).apply {
            putExtra(ConfigureAppWidgetActivity.EXTRA_APP_WIDGET_PROVIDER_INFO, appWidgetProviderInfo)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        true
    } catch (e: Exception) {
        Log.e("Widgets", "Failed to launch ConfigureAppWidgetActivity: ${e.message}")
        false
    }
}

/**
 * Checks if a specific widget ID has configuration
 *
 * @author George Clensy
 * @param context The application context
 * @param appWidgetId The widget to check
 * @return Returns a Boolean indicating if the widget has configuration
 */
fun isWidgetConfigurable(context: Context, appWidgetId: Int): Boolean {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return false
    return appWidgetInfo.configure != null
}

//
// Saving and loading
//

/**
 * Saves the provided widget ID to SharedPreferences.
 * The widget ID is stored in a file named [WIDGET_PREFS_NAME] with the key [WIDGET_ID_KEY].
 *
 * @author George Clensy
 * @param context The application context, used to access SharedPreferences.
 * @param widgetId The ID of the widget to be saved.
 */
fun saveWidgetId(context: Context, widgetId: Int) {
    val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit {
        putInt(WIDGET_ID_KEY, widgetId)
    }
}

/**
 * Retrieves the saved widget ID from SharedPreferences.
 * The widget ID is loaded from a file named [WIDGET_PREFS_NAME] using the key [WIDGET_ID_KEY].
 * If no widget ID is found, it returns [INVALID_WIDGET_ID] (-1).
 *
 * @author George Clensy
 * @param context The application context, used to access SharedPreferences.
 * @return The saved widget ID, or [INVALID_WIDGET_ID] if no ID is found.
 */
fun getSavedWidgetId(context: Context): Int {
    val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(WIDGET_ID_KEY, INVALID_WIDGET_ID)
}

/**
 * Removes the currently saved widget by setting its ID to [INVALID_WIDGET_ID] (-1) in SharedPreferences.
 * This effectively indicates that no widget is currently selected or displayed.
 * The operation uses the SharedPreferences file [WIDGET_PREFS_NAME] and key [WIDGET_ID_KEY].
 *
 * @author George Clensy
 * @param context The application context, used to access SharedPreferences.
 */
fun removeWidget(context: Context) {
    saveWidgetId(context, INVALID_WIDGET_ID)
}

//
// Widget Settings
//

/**
 * Sets the widget's horizontal offset on the screen.
 * This value is saved in the shared preferences file defined by [R.string.settings_pref_file_name].
 * The specific key used for storing the offset is "WidgetOffset".
 *
 * @author George Clensy
 * @param context The application context.
 * @param sliderPosition The new horizontal offset for the widget.
 */
fun setWidgetOffset(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.settings_pref_file_name), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {
        putFloat("WidgetOffset", sliderPosition)
    }
}

/**
 * Retrieves the widget's horizontal offset from shared preferences.
 * This value is stored in the shared preferences file defined by [R.string.settings_pref_file_name].
 * The specific key used for storing the offset is "WidgetOffset".
 * If no value is found, it defaults to 0f.
 *
 * @author George Clensy
 * @param context The application context.
 * @return The widget's horizontal offset, or 0f if not set.
 */
fun getWidgetOffset(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.settings_pref_file_name), Context.MODE_PRIVATE
    )
    return sharedPreferences.getFloat("WidgetOffset", 0f)
}

/**
 * Sets the widget's height.
 * This value is saved in the shared preferences file defined by [R.string.settings_pref_file_name].
 * The specific key used for storing the height is "WidgetHeight".
 *
 * @author George Clensy
 * @param context The application context.
 * @param sliderPosition The new height for the widget.
 */
fun setWidgetHeight(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.settings_pref_file_name), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {
        putFloat("WidgetHeight", sliderPosition)
    }
}

/**
 * Retrieves the widget's width from shared preferences.
 * This value is stored in the shared preferences file defined by [R.string.settings_pref_file_name].
 * The specific key used for storing the width is "WidgetWidth".
 * If no value is found, it defaults to 150f.
 *
 * @author George Clensy
 * @param context The application context.
 * @return The widget's width, or 150f if not set.
 */
fun getWidgetWidth(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.settings_pref_file_name), Context.MODE_PRIVATE
    )

    return sharedPreferences.getFloat("WidgetWidth", 150f)
}

/**
 * Sets the widget's width.
 * This value is saved in the shared preferences file defined by [R.string.settings_pref_file_name].
 * The specific key used for storing the width is "WidgetWidth".
 *
 * @author George Clensy
 * @param context The application context.
 * @param sliderPosition The new width for the widget.
 */
fun setWidgetWidth(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.settings_pref_file_name), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {
        putFloat("WidgetWidth", sliderPosition)
    }
}

/**
 * Retrieves the widget's height from shared preferences.
 * This value is stored in the shared preferences file defined by [R.string.settings_pref_file_name].
 * The specific key used for storing the height is "WidgetHeight".
 * If no value is found, it defaults to 125f.
 *
 * @author George Clensy
 * @param context The application context.
 * @return The widget's height, or 125f if not set.
 */
fun getWidgetHeight(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.settings_pref_file_name), Context.MODE_PRIVATE
    )
    return sharedPreferences.getFloat("WidgetHeight", 125f)
}

