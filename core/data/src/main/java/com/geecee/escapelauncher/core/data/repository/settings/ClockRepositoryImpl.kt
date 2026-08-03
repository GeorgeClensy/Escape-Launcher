package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.ClockRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClockRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ClockRepository {
    override val twelveHourClock: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.TWELVE_HOUR_CLOCK] ?: false }
    override suspend fun setTwelveHourClock(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.TWELVE_HOUR_CLOCK] = enabled }
    }
    override val showClock: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_CLOCK] ?: true }
    override suspend fun setShowClock(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_CLOCK] = enabled }
    }
    override val bigClock: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.BIG_CLOCK] ?: false }
    override suspend fun setBigClock(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.BIG_CLOCK] = enabled }
    }
    override val showDate: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_DATE] ?: false }
    override suspend fun setShowDate(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_DATE] = enabled }
    }
}
