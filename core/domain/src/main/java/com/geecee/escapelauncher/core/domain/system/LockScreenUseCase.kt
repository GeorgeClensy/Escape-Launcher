package com.geecee.escapelauncher.core.domain.system

import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import jakarta.inject.Inject

class LockScreenUseCase @Inject constructor(
    private val systemActionsRepository: SystemActionsRepository
) {
    operator fun invoke() {
        systemActionsRepository.lockScreen()
    }
}
