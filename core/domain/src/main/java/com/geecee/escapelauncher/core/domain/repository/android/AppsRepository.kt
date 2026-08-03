package com.geecee.escapelauncher.core.domain.repository.android

import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.StateFlow

interface AppsRepository {
    val installedApps: StateFlow<List<InstalledApp>>
    val mainUserApps: StateFlow<List<InstalledApp>>
    fun reloadApps()
    fun getAppNameFromPackageName(packageName: String): String
    fun getInstalledAppFromPackageName(packageName: String): InstalledApp?
}