package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LauncherBehaviorRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LauncherBehaviorRepository {
    override val hapticFeedBackEnabled: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.HAPTIC_FEEDBACK] ?: true }
    override suspend fun setHapticFeedback(enabld: Boolean) {
        dataStore.edit { it[PreferencesKeys.HAPTIC_FEEDBACK] = enabld }
    }
    override val doubleTapToLock: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.DOUBLE_TAP_TO_LOCK] ?: false }
    override suspend fun setDoubleTapToLock(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DOUBLE_TAP_TO_LOCK] = enabled }
    }
    override val allowAnalyitics: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.ALLOW_ANALYTICS] ?: false }
    override suspend fun setAllowAnalytics(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.ALLOW_ANALYTICS] = value }
    }
    override val hidePrivateSpace: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.HIDE_PRIVATE_SPACE] ?: true }
    override suspend fun setHidePrivateSpace(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.HIDE_PRIVATE_SPACE] = enabled }
    }
}
