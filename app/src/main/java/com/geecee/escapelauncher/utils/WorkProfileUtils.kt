/**
 * @author George Clensy
 * Utility functions and UI components for managing and interacting with Work Profile in Escape Launcher.
 */

package com.geecee.escapelauncher.utils

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.os.UserManager.USER_TYPE_PROFILE_MANAGED
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.composables.SettingsSwitch
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.ui.theme.transparentHalf

/**
 * BroadcastReceiver that listens for Work Profile state changes (locked/unlocked).
 */
class WorkProfileStateReceiver(private val onStateChange: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PROFILE_AVAILABLE -> onStateChange(true) // Work profile is unlocked
            Intent.ACTION_PROFILE_UNAVAILABLE -> onStateChange(false) // Work profile is locked
        }
    }
}

/**
 * Checks if a Work Profile exists on the device.
 */
fun doesWorkProfileExist(context: Context): Boolean {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return false
    val userManager = getSystemService(context, UserManager::class.java) ?: return false

    for (userInfo in userManager.userProfiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == USER_TYPE_PROFILE_MANAGED) {
            return true
        }
    }

    return false
}

/**
 * Retrieves the UserHandle for the Work Profile.
 */
fun getWorkProfile(context: Context): UserHandle? {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return null
    val userManager = getSystemService(context, UserManager::class.java) ?: return null

    return userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == USER_TYPE_PROFILE_MANAGED
    }
}

/**
 * Determines whether Work Profile is currently unlocked.
 */
fun isWorkProfileUnlocked(context: Context): Boolean {
    val userManager = getSystemService(context, UserManager::class.java) ?: return false
    val workProfile = getWorkProfile(context) ?: return false
    return !userManager.isQuietModeEnabled(workProfile)
}

/**
 * Locks Work Profile by enabling Quiet Mode.
 */
fun lockWorkProfile(context: Context) {
    val userManager = getSystemService(context, UserManager::class.java) ?: return
    getWorkProfile(context)?.let { userHandle ->
        userManager.requestQuietModeEnabled(true, userHandle)
    }
}

/**
 * Unlocks Work Profile by disabling Quiet Mode.
 */
fun unlockWorkProfile(context: Context) {
    val userManager = getSystemService(context, UserManager::class.java) ?: return
    getWorkProfile(context)?.let { userHandle ->
        userManager.requestQuietModeEnabled(false, userHandle)
    }
}

/**
 * Retrieves a list of installed apps in Work Profile.
 */
fun getWorkApps(context: Context): List<InstalledApp> {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return emptyList()
    val workProfile = getWorkProfile(context) ?: return emptyList()

    return launcherApps.getActivityList(null, workProfile).map {
        InstalledApp(
            displayName = it.label?.toString() ?: "Unknown App",
            packageName = it.applicationInfo.packageName,
            componentName = it.componentName
        )
    }
}

/**
 * Shows the system app info page for an app in Work Profile.
 */
fun goToWorkAppAppInfo(
    installedApp: InstalledApp,
    context: Context,
    sourceBounds: Rect? = null
) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val workProfile = getWorkProfile(context) ?: return

    val options = ActivityOptions.makeBasic()
    if (sourceBounds != null) {
        options.launchBounds = sourceBounds
    }
    launcherApps.startAppDetailsActivity(
        installedApp.componentName,
        workProfile,
        sourceBounds,
        options.toBundle()
    )
}

/**
 * Uninstalls an app from Work Profile.
 */
fun uninstallWorkApp(installedApp: InstalledApp, context: Context) {
    val workProfile = getWorkProfile(context) ?: return

    val uninstallIntent = Intent(Intent.ACTION_DELETE)
    uninstallIntent.data = "package:${installedApp.packageName}".toUri()
    uninstallIntent.putExtra(Intent.EXTRA_USER, workProfile)
    uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
    uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(uninstallIntent)
}

/**
 * Opens app in Work Profile.
 */
fun openWorkApp(installedApp: InstalledApp, context: Context, sourceBounds: Rect? = null) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val workProfile = getWorkProfile(context) ?: return

    val options = ActivityOptions.makeBasic()
    launcherApps.startMainActivity(
        installedApp.componentName,
        workProfile,
        sourceBounds,
        options.toBundle()
    )
}

/**
 * UI component for displaying a single Work Profile app item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkAppItem(
    appName: String,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val modifier = Modifier
        .padding(vertical = 15.dp)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)

    Text(
        appName,
        modifier = modifier,
        color = ContentColor,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * UI component for Work Profile settings dialog.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkProfileSettings(
    context: Context,
    backgroundInteractionSource: MutableInteractionSource,
    onDismiss: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = { onDismiss() },
                onLongClick = {},
                indication = null,
                interactionSource = backgroundInteractionSource
            )
            .background(transparentHalf)
    )
    Box(
        Modifier.fillMaxSize()
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(20.dp, 0.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .shadow(
                    5.dp,
                    MaterialTheme.shapes.extraLarge,
                    ambientColor = MaterialTheme.colorScheme.scrim
                )
                .combinedClickable(
                    onClick = {},
                    indication = null,
                    interactionSource = backgroundInteractionSource
                ),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = spacedBy(15.dp)
            ) {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                val settingKey = context.resources.getString(R.string.SearchHiddenWorkProfile)
                SettingsSwitch(
                    stringResource(R.string.hide_work_profile_in_search),
                    getBooleanSetting(context, settingKey, false),
                    onCheckedChange = { value ->
                        setBooleanSetting(context, settingKey, value)
                    },
                    isTopOfGroup = true,
                    isBottomOfGroup = true
                )
                Button(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        }
    }
}
