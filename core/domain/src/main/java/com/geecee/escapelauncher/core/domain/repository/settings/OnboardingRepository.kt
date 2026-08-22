package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val firstTime: Flow<Boolean>
    suspend fun setFirstTime(enabled: Boolean)
    val firstTimeHelp: Flow<Boolean>
    suspend fun setFirstTimeHelp(enabled: Boolean)
}
