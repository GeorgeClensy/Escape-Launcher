package com.geecee.escapelauncher.utils

import android.content.Context

interface AnalyticsProxy {
    fun configureAnalytics(context: Context, enabled: Boolean)
}

// Global accessor that will be provided by the flavor-specific implementations
lateinit var analyticsProxy: AnalyticsProxy
