package com.geecee.escapelauncher.utils

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.os.Build
import android.view.accessibility.AccessibilityEvent

@SuppressLint("AccessibilityPolicy")
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
    }
}