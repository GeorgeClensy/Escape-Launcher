package com.geecee.escapelauncher.feature.homescreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.time.GetCurrentTimePartsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCurrentTimeParts: GetCurrentTimePartsUseCase
) : ViewModel() {

    private val _timeParts = MutableStateFlow(getCurrentTimeParts.invoke(LocalTime.now(), false))
    val timeParts: StateFlow<Triple<Int, Int, Boolean>> = _timeParts.asStateFlow()
    private var tickerJob: Job? = null

    private fun timeTicksFlow(): Flow<Unit> = callbackFlow {
        trySend(Unit)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    fun startTicker(twelveHourDisplay: Boolean) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            timeTicksFlow().collect {
                _timeParts.value = getCurrentTimeParts.invoke(LocalTime.now(), twelveHourDisplay)
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onCleared() {
        stopTicker()
    }
}