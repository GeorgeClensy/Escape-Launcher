package com.geecee.escapelauncher.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val hapticFeedBackEnabled: Flow<Boolean>
    suspend fun setHapticFeedback(enabld: Boolean)
    val hidePrivateSpace: Flow<Boolean>
    suspend fun setHidePrivateSpace(enabled: Boolean)
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
    val useFahrenheit: Flow<Boolean>
    suspend fun setUseFahrenheit(enabled: Boolean)
    val showScreenTimeApp: Flow<Boolean>
    suspend fun setShowScreenTimeApp(enabled: Boolean)
    val firstTimeHelp: Flow<Boolean>
    suspend fun setFirstTimeHelp(enabled: Boolean)
    val homeVAlignment: Flow<String>
    suspend fun setHomeVAlignment(alignment: String)
    val homeAlignment: Flow<String>
    suspend fun setHomeAlignment(alignment: String)
    val widgetOffset: Flow<Float>
    suspend fun setWidgetOffset(offset: Float)
    val widgetHeight: Flow<Float>
    suspend fun setWidgetHeight(height: Float)
    val widgetWidth: Flow<Float>
    suspend fun setWidgetWidth(width: Float)
    val weatherAppPackage: Flow<String>
    suspend fun setWeatherAppPackage(value: String)
    val appsAlignment: Flow<String>
    suspend fun setAppsAlignment(alignment: String)
    val theme: Flow<Int>
    suspend fun setTheme(theme: Int)
    val dtheme: Flow<Int>
    suspend fun setDTheme(theme: Int)
    val ltheme: Flow<Int>
    suspend fun setLTheme(theme: Int)
    val syncTheme: Flow<Boolean>
    suspend fun setSyncTheme(value: Boolean)
    val font: Flow<String>
    suspend fun setFont(value: String)
    val allowAnalyitics: Flow<Boolean>
    suspend fun setAllowAnalytics(value: Boolean)
}