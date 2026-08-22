package com.geecee.escapelauncher.core.domain.apps

import android.graphics.Rect
import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import jakarta.inject.Inject

class OpenAppDetailsUseCase @Inject constructor(
    private val systemActionsRepository: SystemActionsRepository
) {
    operator fun invoke(app: InstalledApp, sourceBounds: Rect? = null) {
        systemActionsRepository.openAppDetails(app, sourceBounds)
    }
}
