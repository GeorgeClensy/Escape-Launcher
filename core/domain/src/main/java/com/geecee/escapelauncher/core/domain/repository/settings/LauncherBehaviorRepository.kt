package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface LauncherBehaviorRepository {
    val hapticFeedBackEnabled: Flow<Boolean>
    suspend fun setHapticFeedback(enabld: Boolean)
    val doubleTapToLock: Flow<Boolean>
    suspend fun setDoubleTapToLock(enabled: Boolean)
    val allowAnalyitics: Flow<Boolean>
    suspend fun setAllowAnalytics(value: Boolean)
    val hidePrivateSpace: Flow<Boolean>
    suspend fun setHidePrivateSpace(enabled: Boolean)
}
