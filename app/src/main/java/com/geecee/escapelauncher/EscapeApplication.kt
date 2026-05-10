package com.geecee.escapelauncher

import android.app.Application
import com.geecee.escapelauncher.core.analytics.AnalyticsProxyImpl
import com.geecee.escapelauncher.utils.MessagingInitializerImpl
import com.geecee.escapelauncher.core.analytics.analyticsProxy
import com.geecee.escapelauncher.utils.managers.Migration
import com.geecee.escapelauncher.utils.messagingInitializer
import com.geecee.escapelauncher.feature.weather.WeatherImpl
import com.geecee.escapelauncher.feature.weather.weatherProxy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EscapeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Migration(this).migrateToUnifiedPrefs()
        // Initialize flavor-specific proxies
        analyticsProxy = AnalyticsProxyImpl()
        messagingInitializer = MessagingInitializerImpl()
        weatherProxy = WeatherImpl()
    }
}
