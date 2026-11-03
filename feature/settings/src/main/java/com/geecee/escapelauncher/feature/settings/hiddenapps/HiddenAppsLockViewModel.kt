package com.geecee.escapelauncher.feature.settings.hiddenapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HiddenAppsLockViewModel @Inject constructor(
    private val repository: HiddenAppsPasswordRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(HiddenAppsLockState())
    val state = mutableState.asStateFlow()
    private var authenticationJob: Job? = null
    private var recoveryJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                repository.recoveryMinutes.collect { minutes ->
                    mutableState.update { it.copy(recoveryMinutes = minutes) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(error = HiddenAppsLockError.StorageError) }
            }
        }
        viewModelScope.launch {
            try {
                repository.isPasswordSet.collect { configured ->
                    mutableState.update { it.copy(isPasswordSet = configured) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(isPasswordSet = null, error = HiddenAppsLockError.StorageError) }
            }
        }
    }

    fun startRecovery() {
        val minutes = state.value.recoveryMinutes ?: return
        if (state.value.isPasswordSet != true || state.value.isBusy ||
            state.value.recoverySecondsRemaining != null) return
        mutableState.update { it.copy(recoverySecondsRemaining = minutes * 60, error = null) }
        recoveryJob = viewModelScope.launch {
            // Count foreground seconds; changing the wall clock cannot shorten the wait.
            repeat(minutes * 60) {
                delay(1000)
                mutableState.update { state ->
                    state.copy(recoverySecondsRemaining = state.recoverySecondsRemaining?.minus(1))
                }
            }
        }
    }

    fun cancelRecovery() {
        recoveryJob?.cancel()
        recoveryJob = null
        mutableState.update { it.copy(recoverySecondsRemaining = null) }
    }

    fun finishRecovery() {
        if (state.value.recoverySecondsRemaining != 0 || state.value.isBusy) return
        authenticate(onSuccess = { cancelRecovery() }) {
            repository.removePasswordAfterRecovery()
            mutableState.update { it.copy(isPasswordSet = false) }
            true
        }
    }

    fun updateRecoveryMinutes(password: String, minutes: Int, onSuccess: () -> Unit) {
        if (!state.value.canAccess || minutes !in 1..HiddenAppsPasswordRepository.MAX_RECOVERY_MINUTES) return
        authenticate(onSuccess = onSuccess) { repository.updateRecoveryMinutes(password, minutes) }
    }

    fun lock() {
        cancelRecovery()
        authenticationJob?.cancel()
        authenticationJob = null
        mutableState.update { it.copy(isUnlocked = false, isBusy = false, error = null) }
    }

    fun unlock(password: String) = authenticate { repository.verifyPassword(password) }

    fun updatePassword(currentPassword: String, newPassword: String?, onSuccess: () -> Unit) {
        if (!state.value.canAccess) return
        authenticate(onSuccess = { lock(); onSuccess() }) {
            val updated = repository.updatePassword(currentPassword, newPassword)
            if (updated) mutableState.update { it.copy(isPasswordSet = newPassword != null) }
            updated
        }
    }

    private fun authenticate(onSuccess: () -> Unit = {}, action: suspend () -> Boolean) {
        if (state.value.isBusy) return
        mutableState.update { it.copy(isBusy = true, error = null) }
        authenticationJob = viewModelScope.launch {
            try {
                val authenticated = action()
                currentCoroutineContext().ensureActive()
                if (authenticated) {
                    mutableState.update { it.copy(isUnlocked = true, isBusy = false, error = null) }
                    onSuccess()
                } else {
                    // Keep submissions serialized and slow down repeated guesses.
                    delay(1000)
                    mutableState.update { it.copy(isBusy = false, error = HiddenAppsLockError.IncorrectPassword) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(isBusy = false, error = HiddenAppsLockError.StorageError) }
            }
        }
    }
}
