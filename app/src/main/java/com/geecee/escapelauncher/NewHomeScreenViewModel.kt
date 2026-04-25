package com.geecee.escapelauncher

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map

@HiltViewModel
class NewHomeScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val twelveHourClock = repository.twelveHourClock
    val showClock = repository.showClock
    val bigClock = repository.bigClock
    val showDate = repository.showDate
    val showScreenTimeHome = repository.showScreenTimeHome
    val showWeather = repository.showWeather
    val showScreenTimeApp = repository.showScreenTimeApp
    val firstTimeHelp = repository.firstTimeHelp

    val homeAlignment = repository.homeAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }

    val homeVAlignment = repository.homeVAlignment.map { alignment ->
        when (alignment) {
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            else -> Arrangement.Bottom
        }
    }

    val widgetOffset = repository.widgetOffset
    val widgetHeight = repository.widgetHeight
    val widgetWidth = repository.widgetWidth
}