package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {
    override val firstTime: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.FIRST_TIME] ?: DefaultSettings.FIRST_TIME }
    override suspend fun setFirstTime(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.FIRST_TIME] = enabled }
    }
    override val firstTimeHelp: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.FIRST_TIME_HELP] ?: DefaultSettings.FIRST_TIME_HELP }
    override suspend fun setFirstTimeHelp(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.FIRST_TIME_HELP] = enabled }
    }
    override val isOnDefaultLauncherOnboarding: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE] ?: DefaultSettings.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE }
    override suspend fun setOnDefaultLauncherOnboarding(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE] = value }
    }
}
