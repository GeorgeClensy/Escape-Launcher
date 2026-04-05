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

    val showClock: Flow<Boolean>
    suspend fun setShowClock(enabled: Boolean)

    val bigClock: Flow<Boolean>
    suspend fun setBigClock(enabled: Boolean)

    val showDate: Flow<Boolean>
    suspend fun setShowDate(enabled: Boolean)

    val showScreenTimeHome: Flow<Boolean>
    suspend fun setShowScreenTimeHome(enabled: Boolean)

    val showWeather: Flow<Boolean>
    suspend fun setShowWeather(enabled: Boolean)

    val showScreenTimeApp: Flow<Boolean>
    suspend fun setShowScreenTimeApp(enabled: Boolean)

    val firstTimeHelp: Flow<Boolean>
    suspend fun setFirstTimeHelp(enabled: Boolean)
}