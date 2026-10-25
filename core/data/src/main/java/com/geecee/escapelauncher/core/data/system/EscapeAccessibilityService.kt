package com.geecee.escapelauncher.core.data.system

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@SuppressLint("AccessibilityPolicy")
class EscapeAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityEventManager.setConnected(true)

        // Listen for lock screen requests safely
        serviceScope.launch {
            AccessibilityEventManager.lockScreenEvents.collect {
                performLockScreen()
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        AccessibilityEventManager.setConnected(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        AccessibilityEventManager.setConnected(false)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    private fun performLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
    }
}
