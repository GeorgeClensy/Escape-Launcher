package com.geecee.escapelauncher

import android.app.Application
import com.geecee.escapelauncher.core.analytics.AnalyticsProxyImpl
import com.geecee.escapelauncher.core.cloudmessaging.MessagingInitializerImpl
import com.geecee.escapelauncher.core.analytics.analyticsProxy
import com.geecee.escapelauncher.core.cloudmessaging.messagingInitializer
import com.geecee.escapelauncher.feature.weather.WeatherImpl
import com.geecee.escapelauncher.feature.weather.weatherProxy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EscapeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize flavor-specific proxies
        analyticsProxy = AnalyticsProxyImpl()
        messagingInitializer = MessagingInitializerImpl()
        weatherProxy = WeatherImpl()
    }
}
