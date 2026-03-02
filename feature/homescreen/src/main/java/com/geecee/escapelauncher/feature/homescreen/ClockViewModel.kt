package com.geecee.escapelauncher.feature.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.domain.time.GetCurrentTimePartsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val getCurrentTimeParts: GetCurrentTimePartsUseCase
) : ViewModel() {

    private val _timeParts = MutableStateFlow(getCurrentTimeParts.invoke(LocalTime.now(),false))
    val timeParts: StateFlow<Triple<Int, Int, Boolean>> = _timeParts.asStateFlow()
    private var tickerJob: Job? = null

    fun startTicker(twelveHourDisplay: Boolean) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val now = LocalTime.now()
                _timeParts.value = getCurrentTimeParts.invoke(now, twelveHourDisplay)
                val millisUntilNextMinute = ((59 - now.second) * 1000L) +
                        ((1_000_000_000L - now.nano) / 1_000_000L)
                delay(millisUntilNextMinute.coerceAtLeast(1L))
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onCleared() {
        stopTicker()
        super.onCleared()
    }
}