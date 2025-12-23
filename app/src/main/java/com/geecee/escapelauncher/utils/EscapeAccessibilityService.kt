package com.geecee.escapelauncher.utils

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class EscapeAccessibilityService : AccessibilityService() {
    companion object {
        var instance: EscapeAccessibilityService? = null
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun lockScreen() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }
}