package com.geecee.escapelauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GlobalViewModel @Inject constructor(
    repository: SettingsRepository
) : ViewModel() {
    val allowAnalytics = repository.allowAnalyitics
    val firstTime = repository.firstTime
    val showStatusBar = repository.showStatusBar

    private val _navigateHomeEvent = MutableSharedFlow<Unit>(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    val navigateHomeEvent = _navigateHomeEvent.asSharedFlow()

    fun requestToGoHome() {
        viewModelScope.launch {
            _navigateHomeEvent.emit(Unit)
        }
    }
}