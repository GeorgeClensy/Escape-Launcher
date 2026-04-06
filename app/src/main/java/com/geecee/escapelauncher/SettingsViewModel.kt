package com.geecee.escapelauncher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
}

@HiltViewModel
class WidgetOptionsPageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val widgetOffset = repository.widgetOffset
    fun setWidgetOffset(value: Float) {
        viewModelScope.launch {
            repository.setWidgetOffset(value)
        }
    }

    val widgetHeight = repository.widgetHeight
    fun setWidgetHeight(value: Float) {
        viewModelScope.launch {
            repository.setWidgetHeight(value)
        }
    }

    val widgetWidth = repository.widgetWidth
    fun setWidgetWidth(value: Float) {
        viewModelScope.launch {
            repository.setWidgetWidth(value)
        }
    }
}

@HiltViewModel
class MainSettingsPageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val hapticFeedBackEnabled = repository.hapticFeedBackEnabled
    fun setHapticFeedback(value: Boolean) {
        viewModelScope.launch {
            repository.setHapticFeedback(value)
        }
    }

    val twelveHourClock = repository.twelveHourClock
    fun setTwelveHourClock(value: Boolean) {
        viewModelScope.launch {
            repository.setTwelveHourClock(value)
        }
    }

    val showClock = repository.showClock
    fun setShowClock(value: Boolean) {
        viewModelScope.launch {
            repository.setShowClock(value)
        }
    }

    val bigClock = repository.bigClock
    fun setBigClock(value: Boolean) {
        viewModelScope.launch {
            repository.setBigClock(value)
        }
    }

    val showDate = repository.showDate
    fun setShowDate(value: Boolean) {
        viewModelScope.launch {
            repository.setShowDate(value)
        }
    }

    val showScreenTimeHome = repository.showScreenTimeHome
    fun setShowScreenTimeHome(value: Boolean) {
        viewModelScope.launch {
            repository.setShowScreenTimeHome(value)
        }
    }

    val showWeather = repository.showWeather
    fun setShowWeather(value: Boolean) {
        viewModelScope.launch {
            repository.setShowWeather(value)
        }
    }

    val useFahrenheit = repository.useFahrenheit
    fun setUseFahrenheit(value: Boolean) {
        viewModelScope.launch {
            repository.setUseFahrenheit(value)
        }
    }

    val showScreenTimeApp = repository.showScreenTimeApp
    fun setShowScreenTimeApp(value: Boolean) {
        viewModelScope.launch {
            repository.setShowScreenTimeApp(value)
        }
    }

    val homeAlignment = repository.homeAlignment.map { alignment ->
        when (alignment) {
            "Left" -> 0
            "Center" -> 1
            else -> 2
        }
    }
    fun setHomeAlignment(index: Int) {
        val value = when (index) {
            0 -> "Left"
            1 -> "Center"
            else -> "Right"
        }
        viewModelScope.launch {
            repository.setHomeAlignment(value)
        }
    }

    val homeVAlignment = repository.homeVAlignment.map { alignment ->
        when (alignment) {
            "Top" -> 0
            "Center" -> 1
            else -> 2
        }
    }
    fun setHomeVAlignment(index: Int) {
        val value = when (index) {
            0 -> "Top"
            1 -> "Center"
            else -> "Bottom"
        }
        viewModelScope.launch {
            repository.setHomeVAlignment(value)
        }
    }

    val weatherAppPackage = repository.weatherAppPackage
    fun setWeatherAppPackage(value: String) {
        viewModelScope.launch {
            repository.setWeatherAppPackage(value)
        }
    }
}


@HiltViewModel
class DevOptionsPageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val firstTimeHelp = repository.firstTimeHelp
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            repository.setFirstTimeHelp(value)
        }
    }

    val weatherAppPackage = repository.weatherAppPackage
    fun setWeatherAppPackage(value: String) {
        viewModelScope.launch {
            repository.setWeatherAppPackage(value)
        }
    }
}