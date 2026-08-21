package com.geecee.escapelauncher.feature.newwidgets.picker

import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class WidgetPickerViewModel @Inject constructor(
    private val analyticsProxy: AnalyticsProxy
) : ViewModel() {
    fun logIconError(packageName: String, e: Exception) {
        analyticsProxy.logCustomKey("Widget Picker App Icon loading failed: ", packageName)
        analyticsProxy.recordException(e)
    }
}
