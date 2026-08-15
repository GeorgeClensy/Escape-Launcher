package com.geecee.escapelauncher.feature.workapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.managedprofiles.CanToggleManagedProfileUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.GetManagedProfileAppsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.managedprofiles.ObserveManagedProfileUnlockedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCaseOutput
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkAppsViewModel @Inject constructor(
    getManagedProfileAppsUseCase: GetManagedProfileAppsUseCase,
    observeManagedProfileUnlockedUseCase: ObserveManagedProfileUnlockedUseCase,
    private val toggleManagedProfileUseCase: ToggleManagedProfileUseCase,
    private val canToggleManagedProfileUseCase: CanToggleManagedProfileUseCase
) : ViewModel() {

    val canToggleProfile: Boolean = canToggleManagedProfileUseCase(ManagedProfileType.WorkApps)

    val isUnlocked: StateFlow<Boolean> = observeManagedProfileUnlockedUseCase(ManagedProfileType.WorkApps)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val workApps: StateFlow<List<InstalledApp>> = getManagedProfileAppsUseCase(ManagedProfileType.WorkApps)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleWorkProfile(onLauncherNotDefault: () -> Unit) {
        viewModelScope.launch {
            when (toggleManagedProfileUseCase(ManagedProfileType.WorkApps)) {
                ToggleManagedProfileUseCaseOutput.FailedNotDefaultLauncher -> onLauncherNotDefault()
                else -> { /* Success cases are handled by the Flow observers */ }
            }
        }
    }
}
