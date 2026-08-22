package com.geecee.escapelauncher.core.domain.repository.android

import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing and interacting with Android managed profiles.
 * This includes features like Work Profiles and Android 15+ Private Space.
 */
interface ManagedProfileRepository {
    /**
     * Returns a [Flow] of apps installed within the specified [ManagedProfileType].
     * The list will be empty if the profile is locked or doesn't exist.
     *
     * @param type The type of managed profile to query.
     * @return A flow emitting the list of apps in the profile.
     */
    fun getApps(type: ManagedProfileType): Flow<List<InstalledApp>>

    /**
     * Observes the unlock status of the specified [ManagedProfileType].
     *
     * @param type The type of managed profile to observe.
     * @return A flow emitting true if the profile is unlocked, false otherwise.
     */
    fun observeUnlocked(type: ManagedProfileType): Flow<Boolean>

    /**
     * Checks if the specified [ManagedProfileType] is currently unlocked.
     *
     * @param type The type of managed profile to check.
     * @return True if the profile is unlocked, false otherwise.
     */
    fun isUnlocked(type: ManagedProfileType): Boolean

    /**
     * Checks if a profile of the specified [ManagedProfileType] exists on the device.
     *
     * @param type The type of managed profile to check for existence.
     * @return True if the profile exists, false otherwise.
     */
    fun exists(type: ManagedProfileType): Boolean

    /**
     * Locks the specified [ManagedProfileType] by enabling "Quiet Mode".
     *
     * @param type The type of managed profile to lock.
     */
    suspend fun lock(type: ManagedProfileType)

    /**
     * Unlocks the specified [ManagedProfileType] by disabling "Quiet Mode".
     *
     * @param type The type of managed profile to unlock.
     */
    suspend fun unlock(type: ManagedProfileType)

    /**
     * Checks if the current app is set as the default launcher.
     * This is often a requirement for certain profile management operations.
     *
     * @return True if this app is the default launcher, false otherwise.
     */
    fun isDefaultLauncher(): Boolean
}
