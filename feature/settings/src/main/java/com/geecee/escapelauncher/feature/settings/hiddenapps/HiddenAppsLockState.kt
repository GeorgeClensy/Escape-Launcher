package com.geecee.escapelauncher.feature.settings.hiddenapps

data class HiddenAppsLockState(
    val isPasswordSet: Boolean? = null,
    val isUnlocked: Boolean = false,
    val isBusy: Boolean = false,
    val recoveryMinutes: Int? = null,
    val recoverySecondsRemaining: Int? = null,
    val error: HiddenAppsLockError? = null
) {
    val canAccess: Boolean get() = isPasswordSet == false || (isPasswordSet == true && isUnlocked)
}

enum class HiddenAppsLockError { IncorrectPassword, StorageError }
