package com.geecee.escapelauncher

import android.app.Application
import com.geecee.escapelauncher.utils.AnalyticsProxyImpl
import com.geecee.escapelauncher.utils.MessagingInitializerImpl
import com.geecee.escapelauncher.utils.WeatherImpl
import com.geecee.escapelauncher.utils.analyticsProxy
import com.geecee.escapelauncher.utils.messagingInitializer
import com.geecee.escapelauncher.utils.weatherProxy

class EscapeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize flavor-specific proxies
        analyticsProxy = AnalyticsProxyImpl()
        messagingInitializer = MessagingInitializerImpl()
        weatherProxy = WeatherImpl()
    }
}
