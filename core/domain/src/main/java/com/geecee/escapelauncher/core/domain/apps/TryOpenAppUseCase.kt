package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

sealed class TryOpenAppResult {
    object RequireHiddenAppsUnlock : TryOpenAppResult()
    object Launch : TryOpenAppResult()
    object ShowChallenge : TryOpenAppResult()
}

class TryOpenAppUseCase @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val passwordRepository: HiddenAppsPasswordRepository
) {
    suspend operator fun invoke(
        packageName: String,
        bypassChallenge: Boolean = false
    ): TryOpenAppResult {
        if (modifiedAppsRepository.isHidden(packageName) && passwordRepository.isPasswordSet.first()) {
            return TryOpenAppResult.RequireHiddenAppsUnlock
        }
        // If we are bypassing (like when the countdown finishes) always launch
        if (bypassChallenge) return TryOpenAppResult.Launch

        return if (modifiedAppsRepository.isChallenge(packageName)) {
            TryOpenAppResult.ShowChallenge
        } else {
            TryOpenAppResult.Launch
        }
    }
}