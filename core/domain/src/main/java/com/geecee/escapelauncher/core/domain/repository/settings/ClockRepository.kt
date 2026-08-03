package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface ClockRepository {
    val twelveHourClock: Flow<Boolean>
    suspend fun setTwelveHourClock(enabled: Boolean)
    val showClock: Flow<Boolean>
    suspend fun setShowClock(enabled: Boolean)
    val bigClock: Flow<Boolean>
    suspend fun setBigClock(enabled: Boolean)
    val showDate: Flow<Boolean>
    suspend fun setShowDate(enabled: Boolean)
}
