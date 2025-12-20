package com.geecee.escapelauncher.utils

import android.content.Context

class AnalyticsProxyImpl : AnalyticsProxy {
    override fun configureAnalytics(context: Context, enabled: Boolean) {
        // No-op for FOSS build
    }
}
