package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.WeatherSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeatherSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : WeatherSettingsRepository {
    override val showWeather: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_WEATHER] ?: DefaultSettings.SHOW_WEATHER }
    override suspend fun setShowWeather(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_WEATHER] = enabled }
    }
    override val useFahrenheit: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.USE_FAHRENHEIT] ?: DefaultSettings.USE_FAHRENHEIT }
    override suspend fun setUseFahrenheit(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_FAHRENHEIT] = enabled }
    }
    override val weatherAppPackage: Flow<String> = dataStore.data.map { it[PreferencesKeys.WEATHER_APP_PACKAGE] ?: DefaultSettings.WEATHER_APP_PACKAGE }
    override suspend fun setWeatherAppPackage(value: String) {
        dataStore.edit { it[PreferencesKeys.WEATHER_APP_PACKAGE] = value }
    }
}
