package com.geecee.escapelauncher.privatespace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.managedprofiles.GetManagedProfileAppsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.managedprofiles.ObserveManagedProfileUnlockedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCaseOutput
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivateSpaceViewModel @Inject constructor(
    getManagedProfileAppsUseCase: GetManagedProfileAppsUseCase,
    observeManagedProfileUnlockedUseCase: ObserveManagedProfileUnlockedUseCase,
    private val toggleManagedProfileUseCase: ToggleManagedProfileUseCase,
    private val launcherBehaviorRepository: LauncherBehaviorRepository
) : ViewModel() {

    val isUnlocked: StateFlow<Boolean> = observeManagedProfileUnlockedUseCase(ManagedProfileType.PrivateSpace)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val privateSpaceApps: StateFlow<List<InstalledApp>> = getManagedProfileAppsUseCase(ManagedProfileType.PrivateSpace)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()
    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }

    fun togglePrivateSpaceProfile(onLauncherNotDefault: () -> Unit) {
        viewModelScope.launch {
            when (toggleManagedProfileUseCase(ManagedProfileType.PrivateSpace)) {
                ToggleManagedProfileUseCaseOutput.FailedNotDefaultLauncher -> onLauncherNotDefault()
                else -> { /* Success cases are handled by the Flow observers */ }
            }
        }
    }

    val hiddenPrivateSpace = launcherBehaviorRepository.hidePrivateSpace
    fun setHiddenPrivateSpace(enabled: Boolean) {
        viewModelScope.launch {
            launcherBehaviorRepository.setHidePrivateSpace(enabled)
        }
    }
}
