package com.geecee.escapelauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HiddenAppsViewModel @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository
) : ViewModel() {
    private val _hiddenPackageIds = MutableStateFlow<Set<String>>(emptySet())
    val hiddenPackageIds: StateFlow<Set<String>> = _hiddenPackageIds.asStateFlow()

    init {
        refreshHiddenApps()
    }

    fun refreshHiddenApps() {
        viewModelScope.launch {
            _hiddenPackageIds.value = modifiedAppsRepository.getHiddenPackageIds().toSet()
        }
    }

    fun hideApp(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.setHidden(packageId, true)
            _hiddenPackageIds.value = modifiedAppsRepository.getHiddenPackageIds().toSet()
        }
    }

    fun unhideApp(packageId: String) {
        viewModelScope.launch {
            modifiedAppsRepository.setHidden(packageId, false)
            _hiddenPackageIds.value = modifiedAppsRepository.getHiddenPackageIds().toSet()
        }
    }
}
