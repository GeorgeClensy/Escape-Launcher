package com.geecee.escapelauncher.core.data.repository.settings

import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys.HIDDEN_APPS_PASSWORD_RECORD
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys.HIDDEN_APPS_RECOVERY_MINUTES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val ITERATIONS = 600_000

class HiddenAppsPasswordRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : HiddenAppsPasswordRepository {
    override val isPasswordSet = dataStore.data.map { it[HIDDEN_APPS_PASSWORD_RECORD] != null }
        .distinctUntilChanged()

    override val recoveryMinutes = dataStore.data.map {
        (it[HIDDEN_APPS_RECOVERY_MINUTES] ?: HiddenAppsPasswordRepository.DEFAULT_RECOVERY_MINUTES)
            .coerceIn(1, HiddenAppsPasswordRepository.MAX_RECOVERY_MINUTES)
    }.distinctUntilChanged()

    override suspend fun updateRecoveryMinutes(password: String, minutes: Int): Boolean =
        withContext(Dispatchers.Default) {
            require(minutes in 1..HiddenAppsPasswordRepository.MAX_RECOVERY_MINUTES)
            var updated = false
            dataStore.edit {
                val record = it[HIDDEN_APPS_PASSWORD_RECORD]
                if (record != null && !matches(password, record)) return@edit
                it[HIDDEN_APPS_RECOVERY_MINUTES] = minutes
                updated = true
            }
            updated
        }

    override suspend fun removePasswordAfterRecovery() {
        dataStore.edit { it.remove(HIDDEN_APPS_PASSWORD_RECORD) }
    }

    override suspend fun verifyPassword(password: String): Boolean = withContext(Dispatchers.Default) {
        val record = dataStore.data.first()[HIDDEN_APPS_PASSWORD_RECORD] ?: return@withContext false
        matches(password, record)
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String?): Boolean =
        withContext(Dispatchers.Default) {
            require(newPassword == null || newPassword.length in
                HiddenAppsPasswordRepository.MIN_PASSWORD_LENGTH..HiddenAppsPasswordRepository.MAX_PASSWORD_LENGTH)
            var updated = false
            // DataStore serializes the check and update; two setup requests cannot overwrite each other.
            dataStore.edit { preferences ->
                val record = preferences[HIDDEN_APPS_PASSWORD_RECORD]
                if (record != null && !matches(currentPassword, record)) return@edit
                if (newPassword == null) {
                    preferences.remove(HIDDEN_APPS_PASSWORD_RECORD)
                } else {
                    val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
                    val encodedSalt = Base64.getEncoder().encodeToString(salt)
                    val hash = Base64.getEncoder().encodeToString(derive(newPassword, salt))
                    preferences[HIDDEN_APPS_PASSWORD_RECORD] = "$encodedSalt:$hash"
                }
                updated = true
            }
            updated
        }

    private fun matches(password: String, record: String): Boolean {
        if (password.length !in 1..HiddenAppsPasswordRepository.MAX_PASSWORD_LENGTH) return false
        return try {
            val parts = record.split(':')
            if (parts.size != 2) return false
            val salt = Base64.getDecoder().decode(parts[0])
            val expected = Base64.getDecoder().decode(parts[1])
            if (salt.size != 16 || expected.size != 32) return false
            MessageDigest.isEqual(expected, derive(password, salt))
        } catch (_: IllegalArgumentException) {
            // A corrupt credential must never make the protected area accessible.
            false
        }
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
