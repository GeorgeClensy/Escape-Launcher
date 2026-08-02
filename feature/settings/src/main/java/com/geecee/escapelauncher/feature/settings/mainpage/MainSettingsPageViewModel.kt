package com.geecee.escapelauncher.feature.settings.mainpage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainSettingsUiState(
    val hapticFeedBackEnabled: Boolean = true,
    val twelveHourClock: Boolean = false,
    val showClock: Boolean = true,
    val bigClock: Boolean = false,
    val showDate: Boolean = false,
    val showStatusBar: Boolean = false,
    val showScreenTimeHome: Boolean = false,
    val showWeather: Boolean = false,
    val useFahrenheit: Boolean = false,
    val showScreenTimeApp: Boolean = false,
    val homeAlignment: Int = 1,
    val homeVAlignment: Int = 1,
    val appsAlignment: Int = 1,
    val doubleTapToLock: Boolean = false,
    val showSearchBox: Boolean = true,
    val searchAutoOpen: Boolean = false,
    val bottomSearch: Boolean = false,
    val automaticallyOpenAppsInSearch: Boolean = false,
    val hideScreenTimePage: Boolean = false,
    val allowAnalytics: Boolean = false
)

@HiltViewModel
class MainSettingsPageViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    appsRepository: AppsRepository,
    private val repository: SettingsRepository,
    val appConfiguration: AppConfiguration
) : ViewModel() {

    val uiState: StateFlow<MainSettingsUiState> = combine(
        listOf(
            repository.hapticFeedBackEnabled,
            repository.twelveHourClock,
            repository.showClock,
            repository.bigClock,
            repository.showDate,
            repository.showStatusBar,
            repository.showScreenTimeHome,
            repository.showWeather,
            repository.useFahrenheit,
            repository.showScreenTimeApp,
            repository.homeAlignment,
            repository.homeVAlignment,
            repository.appsAlignment,
            repository.doubleTapToLock,
            repository.showSearchBox,
            repository.searchAutoOpen,
            repository.bottomSearch,
            repository.automaticallyOpenAppsInSearch,
            repository.hideScreenTimePage,
            repository.allowAnalyitics
        )
    ) { args: Array<Any?> ->
        MainSettingsUiState(
            hapticFeedBackEnabled = args[0] as Boolean,
            twelveHourClock = args[1] as Boolean,
            showClock = args[2] as Boolean,
            bigClock = args[3] as Boolean,
            showDate = args[4] as Boolean,
            showStatusBar = args[5] as Boolean,
            showScreenTimeHome = args[6] as Boolean,
            showWeather = args[7] as Boolean,
            useFahrenheit = args[8] as Boolean,
            showScreenTimeApp = args[9] as Boolean,
            homeAlignment = when (args[10] as String) {
                "Left" -> 0
                "Center" -> 1
                else -> 2
            },
            homeVAlignment = when (args[11] as String) {
                "Top" -> 0
                "Center" -> 1
                else -> 2
            },
            appsAlignment = when (args[12] as String) {
                "Left" -> 0
                "Center" -> 1
                else -> 2
            },
            doubleTapToLock = args[13] as Boolean,
            showSearchBox = args[14] as Boolean,
            searchAutoOpen = args[15] as Boolean,
            bottomSearch = args[16] as Boolean,
            automaticallyOpenAppsInSearch = args[17] as Boolean,
            hideScreenTimePage = args[18] as Boolean,
            allowAnalytics = args[19] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainSettingsUiState()
    )

    fun setHapticFeedback(value: Boolean) {
        viewModelScope.launch {
            repository.setHapticFeedback(value)
        }
    }

    fun setTwelveHourClock(value: Boolean) {
        viewModelScope.launch {
            repository.setTwelveHourClock(value)
        }
    }

    fun setShowClock(value: Boolean) {
        viewModelScope.launch {
            repository.setShowClock(value)
        }
    }

    fun setBigClock(value: Boolean) {
        viewModelScope.launch {
            repository.setBigClock(value)
        }
    }

    fun setShowDate(value: Boolean) {
        viewModelScope.launch {
            repository.setShowDate(value)
        }
    }

    fun setShowStatusBar(value: Boolean) {
        viewModelScope.launch {
            repository.setShowStatusBar(value)
        }
    }

    fun setShowScreenTimeHome(value: Boolean) {
        viewModelScope.launch {
            repository.setShowScreenTimeHome(value)
        }
    }

    fun setShowWeather(value: Boolean) {
        viewModelScope.launch {
            repository.setShowWeather(value)
        }
    }

    fun setUseFahrenheit(value: Boolean) {
        viewModelScope.launch {
            repository.setUseFahrenheit(value)
        }
    }

    fun setShowScreenTimeApp(value: Boolean) {
        viewModelScope.launch {
            repository.setShowScreenTimeApp(value)
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

    fun setDoubleTapToLock(value: Boolean) {
        viewModelScope.launch {
            repository.setDoubleTapToLock(value)
        }
    }

    fun setShowSearchBox(value: Boolean) {
        viewModelScope.launch {
            repository.setShowSearchBox(value)
        }
    }

    fun setSearchAutoOpen(value: Boolean) {
        viewModelScope.launch {
            repository.setSearchAutoOpen(value)
        }
    }

    fun setBottomSearch(value: Boolean) {
        viewModelScope.launch {
            repository.setBottomSearch(value)
        }
    }

    fun setAutomaticallyOpenAppsInSearch(value: Boolean) {
        viewModelScope.launch {
            repository.setAutomaticallyOpenAppsInSearch(value)
        }
    }

    fun setHideScreenTimePage(value: Boolean) {
        viewModelScope.launch {
            repository.setHideScreenTimePage(value)
        }
    }

    fun setAllowAnalytics(value: Boolean) {
        viewModelScope.launch {
            repository.setAllowAnalytics(value)
        }
    }

    val installedApps = appsRepository.mainUserApps
}
