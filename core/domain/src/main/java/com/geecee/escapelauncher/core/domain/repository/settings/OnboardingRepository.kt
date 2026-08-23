package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface OnboardingRepository {
    val replayOnboardingEvent: MutableSharedFlow<Unit>
    val firstTime: Flow<Boolean>
    suspend fun setFirstTime(enabled: Boolean)
    val firstTimeHelp: Flow<Boolean>
    suspend fun setFirstTimeHelp(enabled: Boolean)
}
