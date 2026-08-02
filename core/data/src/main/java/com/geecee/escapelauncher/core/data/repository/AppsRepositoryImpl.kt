package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.geecee.escapelauncher.core.di.ApplicationScope
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Singleton
class AppsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @ApplicationScope scope: CoroutineScope
) : AppsRepository {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    override val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()
    override val mainUserApps: StateFlow<List<InstalledApp>> = installedApps
        .map { apps ->
            apps.filter { it.user == Process.myUserHandle() }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1) // Using this to debounce rapid updates

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: UserHandle) = reloadApps()
        override fun onPackageRemoved(packageName: String, user: UserHandle) = reloadApps()
        override fun onPackageChanged(packageName: String, user: UserHandle) = reloadApps()
        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = reloadApps()
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = reloadApps()
    }

    init {
        launcherApps.registerCallback(callback)

        // Listen for triggers and perform the actual reload with a debounce
        scope.launch {
            refreshTrigger
                .onStart { emit(Unit) }
                .debounce(500.milliseconds) // Wait 500ms for batch installs/updates to settle
                .collect { performReload() }
        }
    }

    override fun reloadApps() {
        refreshTrigger.tryEmit(Unit)
    }

    private fun performReload() {
        val allApps = mutableListOf<InstalledApp>()

        userManager.userProfiles.forEach { userHandle ->
            try {
                val activities = launcherApps.getActivityList(null, userHandle)
                activities.forEach { info ->
                    if (info.applicationInfo.packageName != context.packageName) {
                        allApps.add(
                            InstalledApp(
                                displayName = info.label.toString(),
                                packageName = info.applicationInfo.packageName,
                                componentName = info.componentName,
                                user = userHandle
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle cases where a profile might be locked or inaccessible
            }
        }

        _installedApps.value = allApps
            .distinctBy { it.packageName + it.user.toString() }
            .sortedBy { it.displayName.lowercase() }
    }

    /**
     * Returns the app name from its package
     *
     * @param packageName Name of the package that's app name will be returned
     * @return String app name
     */
    override fun getAppNameFromPackageName(packageName: String): String {
        // Check current installed apps first
        _installedApps.value.find { it.packageName == packageName }?.let {
            return it.displayName
        }
        return "null"
    }

    /**
     * Returns an InstalledApp object from a package name
     *
     * @param packageName Name of the package
     * @return InstalledApp? or null if not found
     */
    override fun getInstalledAppFromPackageName(packageName: String): InstalledApp? {
        // Check current installed apps first
        _installedApps.value.find { it.packageName == packageName }?.let {
            return it
        }
        return null
    }
}