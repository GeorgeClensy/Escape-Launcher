package com.geecee.escapelauncher.utils

import android.content.Context

interface AnalyticsProxy {
    fun configureAnalytics(@Suppress("unused") context: Context, enabled: Boolean)

    fun logCustomKey(key: String, value: String)
}

// Global accessor that will be provided by the flavor-specific implementations
lateinit var analyticsProxy: AnalyticsProxy
