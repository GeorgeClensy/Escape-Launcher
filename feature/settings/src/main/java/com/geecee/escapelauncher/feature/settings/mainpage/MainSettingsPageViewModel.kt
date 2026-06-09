package com.geecee.escapelauncher.feature.settings.mainpage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class MainSettingsPageViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    appsRepository: AppsRepository,
    private val repository: SettingsRepository,
    val appConfiguration: AppConfiguration
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

    fun setWeatherAppPackage(value: String) {
        viewModelScope.launch {
            repository.setWeatherAppPackage(value)
        }
    }

    val appsAlignment = repository.appsAlignment.map { alignment ->
        when (alignment) {
            "Left" -> 0
            "Center" -> 1
            else -> 2
        }
    }
    fun setAppsAlignment(index: Int) {
        val value = when (index) {
            0 -> "Left"
            1 -> "Center"
            else -> "Right"
        }
        viewModelScope.launch {
            repository.setAppsAlignment(value)
        }
    }

    val doubleTapToLock = repository.doubleTapToLock
    fun setDoubleTapToLock(value: Boolean) {
        viewModelScope.launch {
            repository.setDoubleTapToLock(value)
        }
    }

    val showSearchBox = repository.showSearchBox
    fun setShowSearchBox(value: Boolean) {
        viewModelScope.launch {
            repository.setShowSearchBox(value)
        }
    }

    val searchAutoOpen = repository.searchAutoOpen
    fun setSearchAutoOpen(value: Boolean) {
        viewModelScope.launch {
            repository.setSearchAutoOpen(value)
        }
    }

    val bottomSearch = repository.bottomSearch
    fun setBottomSearch(value: Boolean) {
        viewModelScope.launch {
            repository.setBottomSearch(value)
        }
    }

    val automaticallyOpenAppsInSearch = repository.automaticallyOpenAppsInSearch
    fun setAutomaticallyOpenAppsInSearch(value: Boolean) {
        viewModelScope.launch {
            repository.setAutomaticallyOpenAppsInSearch(value)
        }
    }

    val hideScreenTimePage = repository.hideScreenTimePage
    fun setHideScreenTimePage(value: Boolean) {
        viewModelScope.launch {
            repository.setHideScreenTimePage(value)
        }
    }

    val allowAnalytics = repository.allowAnalyitics
    fun setAllowAnalytics(value: Boolean) {
        viewModelScope.launch {
            repository.setAllowAnalytics(value)
        }
    }

    val installedApps = appsRepository.mainUserApps
}