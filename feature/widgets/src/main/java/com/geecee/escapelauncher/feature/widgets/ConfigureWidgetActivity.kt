package com.geecee.escapelauncher.feature.widgets

import android.app.Activity
import android.app.ActivityOptions
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log

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
        appWidgetHost = getAppWidgetHost(this)
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
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, NO_WIDGET_ID)
        if (appWidgetId == NO_WIDGET_ID) {
            // Allocate a new widget ID only if none provided
            appWidgetId = appWidgetHost.allocateAppWidgetId()
            
            // Try to bind the widget
            val canBind = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, appWidgetProviderInfo.provider)
            if (!canBind) {
                val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, appWidgetProviderInfo.provider)
                }
                startActivityForResult(bindIntent, REQUEST_CODE_BIND)
                return
            }
        }

        configureAppWidget(appWidgetProviderInfo, appWidgetId)
    }

    override fun onDestroy() {
        super.onDestroy()
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
                    intent.getParcelableExtra(
                        EXTRA_APP_WIDGET_PROVIDER_INFO,
                        AppWidgetProviderInfo::class.java
                    )
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
