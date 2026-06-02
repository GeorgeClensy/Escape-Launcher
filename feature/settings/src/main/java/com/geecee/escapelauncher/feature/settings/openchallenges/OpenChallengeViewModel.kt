package com.geecee.escapelauncher.feature.settings.openchallenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OpenChallengeViewModel @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository
) : ViewModel() {
    val challengeAppIds: StateFlow<Set<String>> = modifiedAppsRepository.getChallengePackageIdsFlow()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun addChallengeToApp(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.setChallenge(packageId, true)
        }
    }

    fun removeChallengeFromApp(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.setChallenge(packageId, false)
        }
    }
}