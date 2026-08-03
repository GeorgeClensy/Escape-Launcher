package com.geecee.escapelauncher.privatespace

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.common.PrivateSpaceStateReceiver
import com.geecee.escapelauncher.core.common.getPrivateSpaceApps
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.isPrivateSpaceUnlocked
import com.geecee.escapelauncher.core.common.lockPrivateSpace
import com.geecee.escapelauncher.core.common.unlockPrivateSpace
import com.geecee.escapelauncher.core.domain.repository.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivateSpaceViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _privateSpaceApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val privateSpaceApps: StateFlow<List<InstalledApp>> = _privateSpaceApps.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()
    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }


    private var privateSpaceReceiver: PrivateSpaceStateReceiver? = null

    init {
        refreshPrivateSpaceApps()
        registerReceiver()
    }

    private fun registerReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            privateSpaceReceiver = PrivateSpaceStateReceiver { isUnlocked ->
                _isUnlocked.value = isUnlocked
                if (isUnlocked) {
                    refreshPrivateSpaceApps()
                } else {
                    _privateSpaceApps.value = emptyList()
                }
            }
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
            context.registerReceiver(privateSpaceReceiver, intentFilter)
        }
    }

    fun refreshPrivateSpaceApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val unlocked = isPrivateSpaceUnlocked(context)
            _isUnlocked.value = unlocked
            if (unlocked) {
                _privateSpaceApps.value = getPrivateSpaceApps(context).sortedBy { it.displayName.lowercase() }
            } else {
                _privateSpaceApps.value = emptyList()
            }
        }
    }

    fun togglePrivateSpaceProfile(onLauncherNotDefault: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (isDefaultLauncher(context)) {
                if (isPrivateSpaceUnlocked(context)) {
                    lockPrivateSpace(context)
                } else {
                    unlockPrivateSpace(context)
                }
                refreshPrivateSpaceApps()
            } else {
                onLauncherNotDefault()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        privateSpaceReceiver?.let {
            context.unregisterReceiver(it)
        }
    }

    val hiddenPrivateSpace = repository.hidePrivateSpace
    fun setHiddenPrivateSpace(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHidePrivateSpace(enabled)
        }
    }
}
