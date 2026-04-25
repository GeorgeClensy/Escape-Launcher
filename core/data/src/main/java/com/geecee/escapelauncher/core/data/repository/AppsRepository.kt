package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import com.geecee.escapelauncher.core.di.ApplicationScope
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: UserHandle) = loadApps()
        override fun onPackageRemoved(packageName: String, user: UserHandle) = loadApps()
        override fun onPackageChanged(packageName: String, user: UserHandle) = loadApps()
        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = loadApps()
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = loadApps()
    }

    init {
        launcherApps.registerCallback(callback)
        loadApps()
    }

    fun loadApps() {
        scope.launch {
            val allApps = mutableListOf<InstalledApp>()

            // Fetch apps for all profiles (Main, Work, etc.)
            userManager.userProfiles.forEach { userHandle ->
                val activities = launcherApps.getActivityList(null, userHandle)
                activities.forEach { resolveInfo ->
                    // Filter out the launcher itself
                    if (resolveInfo.applicationInfo.packageName != context.packageName) {
                        allApps.add(
                            InstalledApp(
                                displayName = resolveInfo.label.toString(),
                                packageName = resolveInfo.applicationInfo.packageName,
                                componentName = resolveInfo.componentName,
                                user = userHandle
                            )
                        )
                    }
                }
            }

            _installedApps.value = allApps.distinctBy { it.packageName + it.user.toString() }
        }
    }
}