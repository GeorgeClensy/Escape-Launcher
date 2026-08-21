package com.geecee.escapelauncher.core.domain.launcher

import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import jakarta.inject.Inject

class SetDefaultLauncherUseCase @Inject constructor(
    private val systemActionsRepository: SystemActionsRepository
) {
    operator fun invoke(): android.content.Intent {
        return systemActionsRepository.getPromptDefaultLauncherIntent()
    }
}
