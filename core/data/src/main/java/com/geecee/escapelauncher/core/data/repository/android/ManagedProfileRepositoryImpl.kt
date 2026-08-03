package com.geecee.escapelauncher.core.data.repository.android

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagedProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appsRepository: AppsRepository
) : ManagedProfileRepository {

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    private val _profileUpdateTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        _profileUpdateTrigger.tryEmit(Unit)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    _profileUpdateTrigger.tryEmit(Unit)
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
            context.registerReceiver(receiver, filter)
        }
    }

    override fun getApps(type: ManagedProfileType): Flow<List<InstalledApp>> {
        return combine(
            appsRepository.installedApps,
            _profileUpdateTrigger.onStart { emit(Unit) }
        ) { allApps, _ ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return@combine emptyList()

            val userHandle = getProfileUserHandle(type) ?: return@combine emptyList()
            if (userManager.isQuietModeEnabled(userHandle)) return@combine emptyList()

            allApps.filter { it.user == userHandle }
                .sortedBy { it.displayName.lowercase() }
        }
    }

    override fun observeUnlocked(type: ManagedProfileType): Flow<Boolean> {
        return _profileUpdateTrigger.onStart { emit(Unit) }.map {
            isUnlocked(type)
        }
    }

    override fun isUnlocked(type: ManagedProfileType): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return false
        val userHandle = getProfileUserHandle(type) ?: return false
        return !userManager.isQuietModeEnabled(userHandle)
    }

    override fun exists(type: ManagedProfileType): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return false
        return getProfileUserHandle(type) != null
    }

    override suspend fun lock(type: ManagedProfileType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val userHandle = getProfileUserHandle(type) ?: return
        userManager.requestQuietModeEnabled(true, userHandle)
    }

    override suspend fun unlock(type: ManagedProfileType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val userHandle = getProfileUserHandle(type) ?: return
        userManager.requestQuietModeEnabled(false, userHandle)
    }

    override fun isDefaultLauncher(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
        } else {
            val packageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        }
    }

    private fun getProfileUserHandle(type: ManagedProfileType): UserHandle? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return null

        val userType = when (type) {
            ManagedProfileType.PrivateSpace -> "android.os.usertype.profile.PRIVATE"
            ManagedProfileType.WorkApps -> UserManager.USER_TYPE_PROFILE_MANAGED
        }

        return userManager.userProfiles.find {
            launcherApps.getLauncherUserInfo(it)?.userType == userType
        }
    }
}
