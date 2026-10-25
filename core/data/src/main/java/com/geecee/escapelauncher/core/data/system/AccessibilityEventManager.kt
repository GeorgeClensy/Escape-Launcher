package com.geecee.escapelauncher.core.data.system

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessibilityEventManager {
    // Tracks whether the service is running (replaces the polling check!)
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    // Triggers lock screen actions safely from the repository to the service
    private val _lockScreenEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val lockScreenEvents: SharedFlow<Unit> = _lockScreenEvents.asSharedFlow()

    fun setConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    fun triggerLockScreen() {
        _lockScreenEvents.tryEmit(Unit)
    }
}
