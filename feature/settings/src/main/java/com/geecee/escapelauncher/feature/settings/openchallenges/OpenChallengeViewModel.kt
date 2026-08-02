package com.geecee.escapelauncher.feature.settings.openchallenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.challenges.GetOpenChallengeAppsUseCase
import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OpenChallengeViewModel @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository,
    getOpenChallengeAppsUseCase: GetOpenChallengeAppsUseCase
) : ViewModel() {
    val challengeAppIds: StateFlow<List<InstalledApp>> = getOpenChallengeAppsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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