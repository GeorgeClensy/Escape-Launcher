package com.geecee.escapelauncher.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val hapticFeedBackEnabled: Flow<Boolean>
    suspend fun setHapticFeedback(enabld: Boolean)

    //Private Space
    val hidePrivateSpace: Flow<Boolean>
    suspend fun setHidePrivateSpace(enabled: Boolean)

    //Home
    val twelveHourClock: Flow<Boolean>
    suspend fun setTwelveHourClock(enabled: Boolean)
}