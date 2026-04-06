package com.geecee.escapelauncher.feature.weather

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @ApplicationContext private val context: Context, private val repository: SettingsRepository
) : ViewModel() {
    val weatherAppPackage = repository.weatherAppPackage

    val weatherText = mutableStateOf("")
    private val delayTime = 30 * 60 * 1000L // 30 Mins

    init {
        startWeatherRefreshLoop()
    }

    private fun startWeatherRefreshLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.useFahrenheit.collect { useFahrenheit ->
                // Restart the fetch loop whenever the setting changes
                fetchWeather(useFahrenheit)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(timeMillis = delayTime)
                val useFahrenheit = repository.useFahrenheit.first()
                fetchWeather(useFahrenheit)
            }
        }
    }

    private suspend fun fetchWeather(useFahrenheit: Boolean) {
        weatherProxy.getWeather(context, useFahrenheit) { result ->
            viewModelScope.launch(Dispatchers.Main) {
                if (!result.contains("error", ignoreCase = true) && !result.contains(
                        "unavailable",
                        ignoreCase = true
                    )
                ) {
                    weatherText.value = result
                }
            }
        }
    }

    fun forceUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            val useFahrenheit = repository.useFahrenheit.first()
            fetchWeather(useFahrenheit)
        }
    }
}