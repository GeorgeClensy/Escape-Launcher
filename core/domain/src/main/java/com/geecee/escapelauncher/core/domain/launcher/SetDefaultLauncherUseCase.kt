package com.geecee.escapelauncher.core.domain.launcher

import android.content.Intent
import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import jakarta.inject.Inject

class SetDefaultLauncherUseCase @Inject constructor(
    private val systemActionsRepository: SystemActionsRepository
) {
    operator fun invoke(): Intent {
        return systemActionsRepository.getPromptDefaultLauncherIntent()
    }
}
