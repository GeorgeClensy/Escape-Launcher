package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import jakarta.inject.Inject

sealed class TryOpenAppResult {
    object Launch : TryOpenAppResult()
    object ShowChallenge : TryOpenAppResult()
}

class TryOpenAppUseCase @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository
) {
    suspend operator fun invoke(
        packageName: String,
        bypassChallenge: Boolean = false
    ): TryOpenAppResult {
        // If we are bypassing (like when the countdown finishes) always launch
        if (bypassChallenge) return TryOpenAppResult.Launch

        return if (modifiedAppsRepository.isChallenge(packageName)) {
            TryOpenAppResult.ShowChallenge
        } else {
            TryOpenAppResult.Launch
        }
    }
}