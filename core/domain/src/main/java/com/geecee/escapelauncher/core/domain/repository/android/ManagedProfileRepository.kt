package com.geecee.escapelauncher.core.domain.repository.android

import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface ManagedProfileRepository {
    fun getApps(type: ManagedProfileType): Flow<List<InstalledApp>>
    fun observeUnlocked(type: ManagedProfileType): Flow<Boolean>
    fun isUnlocked(type: ManagedProfileType): Boolean
    fun exists(type: ManagedProfileType): Boolean
    suspend fun lock(type: ManagedProfileType)
    suspend fun unlock(type: ManagedProfileType)
    fun isDefaultLauncher(): Boolean
}
