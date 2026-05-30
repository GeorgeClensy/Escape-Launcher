package com.geecee.escapelauncher.feature.newwidgets

import android.app.ActivityOptions
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for showing the widget configuration
 *
 * @author George Clensy
 */
@AndroidEntryPoint
class ConfigureWidgetActivity : ComponentActivity() {

    @Inject
    lateinit var widgetHostManager: WidgetHostManager

    /**
     * The ID of the widget being configured
     *
     * @author George Clensy
     */
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private var appWidgetProviderInfo: AppWidgetProviderInfo? = null

    // Register modern Activity Result Launcher for binding
    private val bindWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val widget = widgetHostManager.manager.getAppWidgetInfo(appWidgetId)
            configureAppWidget(widget, appWidgetId)
        } else {
            handleFailure()
        }
    }

    /**
     * Activity entry point
     *
     * @author George Clensy
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetProviderInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                EXTRA_APP_WIDGET_PROVIDER_INFO,
                AppWidgetProviderInfo::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_APP_WIDGET_PROVIDER_INFO)
        }

        val info = appWidgetProviderInfo
        if (info == null) {
            Log.e("Widgets", "No app widget provider info provided, canceling")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Use existing widget ID if provided
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Allocate a new widget ID only if none provided
            appWidgetId = widgetHostManager.allocateWidgetId()

            // Try to bind the widget
            val canBind = widgetHostManager.manager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)
            if (!canBind) {
                val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                }
                bindWidgetLauncher.launch(bindIntent)
                return
            }
        }

        configureAppWidget(info, appWidgetId)
    }

    /**
     * Checks that the widget is configurable and then starts the configuration activity for the ID using the widget host
     *
     * @author George Clensy
     */
    private fun configureAppWidget(widget: AppWidgetProviderInfo, appWidgetId: Int) {
        if (widget.configure != null) {
            // NOTE: AppWidgetHost still internally relies on legacy request codes
            widgetHostManager.host.startAppWidgetConfigureActivityForResult(
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
    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_CONFIGURE -> {
                if (resultCode == RESULT_OK) {
                    finishWithResult(appWidgetId)
                } else {
                    handleFailure()
                }
            }
            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    /**
     * Handles common logic when widget binding or configuration fails
     */
    private fun handleFailure() {
        widgetHostManager.deleteWidgetId(appWidgetId)
        setResult(RESULT_CANCELED)
        finish()
    }

    /**
     * Finishes the config activity with success
     */
    private fun finishWithResult(widgetId: Int) {
        val data = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra(EXTRA_APP_WIDGET_PROVIDER_INFO, appWidgetProviderInfo)
        }

        setResult(RESULT_OK, data)
        finish()
    }

    companion object {
        const val REQUEST_CODE_CONFIGURE = 1
        const val EXTRA_APP_WIDGET_PROVIDER_INFO = "extra_app_widget_provider_info"
    }
}
