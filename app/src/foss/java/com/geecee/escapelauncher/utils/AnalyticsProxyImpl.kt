package com.geecee.escapelauncher.utils

import android.content.Context

class AnalyticsProxyImpl : AnalyticsProxy {
    override fun configureAnalytics(context: Context, enabled: Boolean) {
        // No-op for FOSS build
    }

    override fun logCustomKey(key: String, value: String) {
        // No-op for FOSS build
    }

    override fun recordException(exception: Exception) {
        // No-op for FOSS build
    }
}
