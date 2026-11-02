package com.geecee.escapelauncher.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.data.repository.settings.LauncherBehaviorRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LauncherBehaviorRepositoryImplTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun defaultsClampsAndPersistsCountdown() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.root.resolve("settings.preferences_pb")
        }
        val repository = LauncherBehaviorRepositoryImpl(store)
        assertEquals(20, repository.countdownSeconds.first())
        repository.setCountdownSeconds(0)
        assertEquals(1, repository.countdownSeconds.first())
        repository.setCountdownSeconds(9999)
        assertEquals(600, repository.countdownSeconds.first())
        repository.setCountdownSeconds(37)
        assertEquals(37, LauncherBehaviorRepositoryImpl(store).countdownSeconds.first())
        store.edit { it[PreferencesKeys.COUNTDOWN_SECONDS] = -10 }
        assertEquals(1, repository.countdownSeconds.first())
        store.edit { it[PreferencesKeys.COUNTDOWN_SECONDS] = 9999 }
        assertEquals(600, repository.countdownSeconds.first())
    }
}
