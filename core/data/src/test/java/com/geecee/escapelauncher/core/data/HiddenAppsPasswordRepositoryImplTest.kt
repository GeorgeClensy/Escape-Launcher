package com.geecee.escapelauncher.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.geecee.escapelauncher.core.data.repository.settings.HiddenAppsPasswordRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HiddenAppsPasswordRepositoryImplTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun setupVerificationChangeRemovalAndCorruptRecordFailClosed() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.root.resolve("password.preferences_pb")
        }
        val repository = HiddenAppsPasswordRepositoryImpl(store)
        assertFalse(repository.isPasswordSet.first())
        assertFalse(repository.verifyPassword("friend-secret"))
        assertTrue(repository.updatePassword("", "friend-secret"))
        assertTrue(repository.isPasswordSet.first())
        assertTrue(repository.verifyPassword("friend-secret"))
        assertFalse(repository.verifyPassword("wrong-secret"))
        assertFalse(store.data.first().asMap().values.any { it.toString().contains("friend-secret") })
        assertFalse(repository.updatePassword("", "replacement"))
        assertFalse(repository.updatePassword("wrong-secret", null))
        assertTrue(repository.updatePassword("friend-secret", "replacement"))
        assertFalse(repository.verifyPassword("friend-secret"))
        assertTrue(HiddenAppsPasswordRepositoryImpl(store).verifyPassword("replacement"))
        assertTrue(repository.updatePassword("replacement", null))
        assertFalse(repository.isPasswordSet.first())
        store.edit { it[stringPreferencesKey("hidden_apps_password_v1")] = "corrupt" }
        assertTrue(repository.isPasswordSet.first())
        assertFalse(repository.verifyPassword("anything"))
        assertFalse(repository.updatePassword("anything", null))
    }

    @Test
    fun passwordLengthIsEnforcedByRepository() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.root.resolve("length.preferences_pb")
        }
        try {
            HiddenAppsPasswordRepositoryImpl(store).updatePassword("", "123")
            fail("Short passwords must be rejected")
        } catch (_: IllegalArgumentException) {
            assertTrue(store.data.first().asMap().isEmpty())
        }
        val repository = HiddenAppsPasswordRepositoryImpl(store)
        assertTrue(repository.updatePassword("", "1234"))
        assertTrue(repository.verifyPassword("1234"))
    }
    @Test
    fun recoveryDurationIsPersistedAndProtectedByCurrentPassword() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.root.resolve("recovery.preferences_pb")
        }
        val repository = HiddenAppsPasswordRepositoryImpl(store)
        assertEquals(60, repository.recoveryMinutes.first())
        assertTrue(repository.updateRecoveryMinutes("", 120))
        assertTrue(repository.updatePassword("", "friend-secret"))
        assertFalse(repository.updateRecoveryMinutes("wrong", 1))
        assertEquals(120, repository.recoveryMinutes.first())
        assertTrue(repository.updateRecoveryMinutes("friend-secret", 90))
        assertEquals(90, HiddenAppsPasswordRepositoryImpl(store).recoveryMinutes.first())
        for (invalid in listOf(0, 1441)) {
            try {
                repository.updateRecoveryMinutes("friend-secret", invalid)
                fail("Out-of-range duration must be rejected")
            } catch (_: IllegalArgumentException) { }
        }
        repository.removePasswordAfterRecovery()
        assertFalse(repository.isPasswordSet.first())
        assertEquals(90, repository.recoveryMinutes.first())
    }
    @Test
    fun generalSettingsResetPreservesPasswordAndRecoveryDuration() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.root.resolve("reset.preferences_pb")
        }
        val repository = HiddenAppsPasswordRepositoryImpl(store)
        repository.updateRecoveryMinutes("", 120)
        repository.updatePassword("", "friend-secret")
        store.edit { it[stringPreferencesKey("unrelated_setting")] = "custom" }
        com.geecee.escapelauncher.core.data.repository.settings.SettingsManagerImpl(store).resetToDefaults()
        assertTrue(repository.verifyPassword("friend-secret"))
        assertEquals(120, repository.recoveryMinutes.first())
        assertNull(store.data.first()[stringPreferencesKey("unrelated_setting")])
    }
}
