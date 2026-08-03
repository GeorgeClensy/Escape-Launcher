package com.geecee.escapelauncher.feature.settings.mainpage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.*
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
    private val appearanceRepository: AppearanceRepository,
    private val clockRepository: ClockRepository,
    private val launcherBehaviorRepository: LauncherBehaviorRepository,
    private val searchSettingsRepository: SearchSettingsRepository,
    private val screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    private val weatherSettingsRepository: WeatherSettingsRepository,
    val appConfiguration: AppConfiguration
) : ViewModel() {

    val uiState: StateFlow<MainSettingsUiState> = combine(
        listOf(
            launcherBehaviorRepository.hapticFeedBackEnabled,
            clockRepository.twelveHourClock,
            clockRepository.showClock,
            clockRepository.bigClock,
            clockRepository.showDate,
            appearanceRepository.showStatusBar,
            screenTimeSettingsRepository.showScreenTimeHome,
            weatherSettingsRepository.showWeather,
            weatherSettingsRepository.useFahrenheit,
            screenTimeSettingsRepository.showScreenTimeApp,
            appearanceRepository.homeAlignment,
            appearanceRepository.homeVAlignment,
            appearanceRepository.appsAlignment,
            launcherBehaviorRepository.doubleTapToLock,
            searchSettingsRepository.showSearchBox,
            searchSettingsRepository.searchAutoOpen,
            searchSettingsRepository.bottomSearch,
            searchSettingsRepository.automaticallyOpenAppsInSearch,
            screenTimeSettingsRepository.hideScreenTimePage,
            launcherBehaviorRepository.allowAnalyitics
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
            launcherBehaviorRepository.setHapticFeedback(value)
        }
    }

    fun setTwelveHourClock(value: Boolean) {
        viewModelScope.launch {
            clockRepository.setTwelveHourClock(value)
        }
    }

    fun setShowClock(value: Boolean) {
        viewModelScope.launch {
            clockRepository.setShowClock(value)
        }
    }

    fun setBigClock(value: Boolean) {
        viewModelScope.launch {
            clockRepository.setBigClock(value)
        }
    }

    fun setShowDate(value: Boolean) {
        viewModelScope.launch {
            clockRepository.setShowDate(value)
        }
    }

    fun setShowStatusBar(value: Boolean) {
        viewModelScope.launch {
            appearanceRepository.setShowStatusBar(value)
        }
    }

    fun setShowScreenTimeHome(value: Boolean) {
        viewModelScope.launch {
            screenTimeSettingsRepository.setShowScreenTimeHome(value)
        }
    }

    fun setShowWeather(value: Boolean) {
        viewModelScope.launch {
            weatherSettingsRepository.setShowWeather(value)
        }
    }

    fun setUseFahrenheit(value: Boolean) {
        viewModelScope.launch {
            weatherSettingsRepository.setUseFahrenheit(value)
        }
    }

    fun setShowScreenTimeApp(value: Boolean) {
        viewModelScope.launch {
            screenTimeSettingsRepository.setShowScreenTimeApp(value)
        }
    }

    fun setHomeAlignment(index: Int) {
        val value = when (index) {
            0 -> "Left"
            1 -> "Center"
            else -> "Right"
        }
        viewModelScope.launch {
            appearanceRepository.setHomeAlignment(value)
        }
    }

    fun setHomeVAlignment(index: Int) {
        val value = when (index) {
            0 -> "Top"
            1 -> "Center"
            else -> "Bottom"
        }
        viewModelScope.launch {
            appearanceRepository.setHomeVAlignment(value)
        }
    }

    fun setWeatherAppPackage(value: String) {
        viewModelScope.launch {
            weatherSettingsRepository.setWeatherAppPackage(value)
        }
    }

    fun setAppsAlignment(index: Int) {
        val value = when (index) {
            0 -> "Left"
            1 -> "Center"
            else -> "Right"
        }
        viewModelScope.launch {
            appearanceRepository.setAppsAlignment(value)
        }
    }

    fun setDoubleTapToLock(value: Boolean) {
        viewModelScope.launch {
            launcherBehaviorRepository.setDoubleTapToLock(value)
        }
    }

    fun setShowSearchBox(value: Boolean) {
        viewModelScope.launch {
            searchSettingsRepository.setShowSearchBox(value)
        }
    }

    fun setSearchAutoOpen(value: Boolean) {
        viewModelScope.launch {
            searchSettingsRepository.setSearchAutoOpen(value)
        }
    }

    fun setBottomSearch(value: Boolean) {
        viewModelScope.launch {
            searchSettingsRepository.setBottomSearch(value)
        }
    }

    fun setAutomaticallyOpenAppsInSearch(value: Boolean) {
        viewModelScope.launch {
            searchSettingsRepository.setAutomaticallyOpenAppsInSearch(value)
        }
    }

    fun setHideScreenTimePage(value: Boolean) {
        viewModelScope.launch {
            screenTimeSettingsRepository.setHideScreenTimePage(value)
        }
    }

    fun setAllowAnalytics(value: Boolean) {
        viewModelScope.launch {
            launcherBehaviorRepository.setAllowAnalytics(value)
        }
    }

    val installedApps = appsRepository.mainUserApps
}
