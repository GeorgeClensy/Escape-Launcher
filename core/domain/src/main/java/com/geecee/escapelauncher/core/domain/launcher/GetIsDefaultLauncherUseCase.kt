package com.geecee.escapelauncher.core.domain.launcher

import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import jakarta.inject.Inject

class GetIsDefaultLauncherUseCase @Inject constructor(
    private val systemActionsRepository: SystemActionsRepository
) {
    operator fun invoke(): Boolean {
        return systemActionsRepository.isDefaultLauncher()
    }
}
