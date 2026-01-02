package com.geecee.escapelauncher.utils

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics

class AnalyticsProxyImpl : AnalyticsProxy {
    val crashlytics = Firebase.crashlytics

    override fun configureAnalytics(context: Context, enabled: Boolean) {
        val analytics = Firebase.analytics
        analytics.setConsent(
            mapOf(
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED,
            )
        )
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun logCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun recordException(exception: Exception) {
        crashlytics.recordException(exception)
    }
}
