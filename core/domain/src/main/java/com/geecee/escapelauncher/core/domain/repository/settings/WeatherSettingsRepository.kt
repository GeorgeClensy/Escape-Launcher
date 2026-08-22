package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface WeatherSettingsRepository {
    val showWeather: Flow<Boolean>
    suspend fun setShowWeather(enabled: Boolean)
    val useFahrenheit: Flow<Boolean>
    suspend fun setUseFahrenheit(enabled: Boolean)
    val weatherAppPackage: Flow<String>
    suspend fun setWeatherAppPackage(value: String)
}
