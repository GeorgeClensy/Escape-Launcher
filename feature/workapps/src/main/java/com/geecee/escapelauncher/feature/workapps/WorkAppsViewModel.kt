package com.geecee.escapelauncher.feature.workapps

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.common.getWorkApps
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.isWorkProfileUnlocked
import com.geecee.escapelauncher.core.common.lockWorkProfile
import com.geecee.escapelauncher.core.common.unlockWorkProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WorkAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _workApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val workApps: StateFlow<List<InstalledApp>> = _workApps.asStateFlow()

    init {
        refreshWorkApps()
    }

    fun refreshWorkApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val unlocked = isWorkProfileUnlocked(context)
            _isUnlocked.value = unlocked
            if (unlocked) {
                _workApps.value = getWorkApps(context).sortedBy { it.displayName.lowercase() }
            } else {
                _workApps.value = emptyList()
            }
        }
    }

    fun toggleWorkProfile(onLauncherNotDefault: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (isDefaultLauncher(context)) {
                if (isWorkProfileUnlocked(context)) {
                    lockWorkProfile(context)
                } else {
                    unlockWorkProfile(context)
                }
                refreshWorkApps()
            } else {
                onLauncherNotDefault()
            }
        }
    }
}