package com.geecee.escapelauncher.utils

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

class AnalyticsProxyImpl : AnalyticsProxy {
    override fun configureAnalytics(context: Context, enabled: Boolean) {
        val analytics = Firebase.analytics
        analytics.setConsent(
            mapOf(
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED,
            )
        )
        analytics.setAnalyticsCollectionEnabled(enabled)
    }
}
