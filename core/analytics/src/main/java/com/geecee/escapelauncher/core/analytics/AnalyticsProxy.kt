package com.geecee.escapelauncher.core.analytics

import android.content.Context

interface AnalyticsProxy {
    fun configureAnalytics(@Suppress("unused") context: Context, enabled: Boolean)

    fun logCustomKey(key: String, value: String)

    fun recordException(exception: Exception)
}
