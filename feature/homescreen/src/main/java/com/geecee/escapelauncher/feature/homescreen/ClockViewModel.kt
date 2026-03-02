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
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val getCurrentTimeParts: GetCurrentTimePartsUseCase
) : ViewModel() {

    private val _timeParts = MutableStateFlow(getCurrentTimeParts.invoke(false))
    val timeParts: StateFlow<Triple<Int, Int, Boolean>> = _timeParts.asStateFlow()

    fun startTicker(twelveHourDisplay: Boolean) {
        viewModelScope.launch {
            while (true) {
                _timeParts.value = getCurrentTimeParts.invoke(twelveHourDisplay)

                val now = LocalTime.now()
                val currentMinute = now.minute
                
                // Sleep until the minute changes, with small polls to catch the exact boundary
                while (true) {
                    val millisUntilNextSecond = 1000L - (LocalTime.now().nano / 1_000_000L)
                    delay(millisUntilNextSecond.coerceAtLeast(10L))
                    
                    if (LocalTime.now().minute != currentMinute) {
                        break
                    }
                }
            }
        }
    }
}