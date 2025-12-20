package com.geecee.escapelauncher

import android.app.Application
import com.geecee.escapelauncher.utils.AnalyticsProxyImpl
import com.geecee.escapelauncher.utils.MessagingInitializerImpl
import com.geecee.escapelauncher.utils.analyticsProxy
import com.geecee.escapelauncher.utils.messagingInitializer

class EscapeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize flavor-specific proxies
        analyticsProxy = AnalyticsProxyImpl()
        messagingInitializer = MessagingInitializerImpl()
    }
}
