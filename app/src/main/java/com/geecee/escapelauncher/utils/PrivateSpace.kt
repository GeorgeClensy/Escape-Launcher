/**
 * @author George Clensy
 * Utility functions and UI components for managing and interacting with Private Space in Escape Launcher.
 */

package com.geecee.escapelauncher.utils

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Build
import android.os.UserManager
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
import com.geecee.escapelauncher.ui.theme.transparentHalf

private const val PRIVATE_SPACE_USER_TYPE = "android.os.usertype.profile.PRIVATE"

/**
 * BroadcastReceiver that listens for Private Space state changes (locked/unlocked).
 */
class PrivateSpaceStateReceiver(private val onStateChange: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PROFILE_AVAILABLE -> onStateChange(true) // Private space is unlocked
            Intent.ACTION_PROFILE_UNAVAILABLE -> onStateChange(false) // Private space is locked
        }
    }
}

/**
 * Determines whether Private Space is currently unlocked.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun isPrivateSpaceUnlocked(context: Context): Boolean {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return false
    val userManager = getSystemService(context, UserManager::class.java) ?: return false

    val privateUser = userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    } ?: return false

    return !userManager.isQuietModeEnabled(privateUser)
}

/**
 * Locks Private Space by enabling Quiet Mode.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun lockPrivateSpace(context: Context) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val userManager = getSystemService(context, UserManager::class.java) ?: return

    userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    }?.let { userManager.requestQuietModeEnabled(true, it) }
}

/**
 * Unlocks Private Space by disabling Quiet Mode.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun unlockPrivateSpace(context: Context) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val userManager = getSystemService(context, UserManager::class.java) ?: return

    userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    }?.let { userManager.requestQuietModeEnabled(false, it) }
}

/**
 * Retrieves a list of installed apps in Private Space.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun getPrivateSpaceApps(context: Context): List<InstalledApp> {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
        ?: return emptyList()
    val userManager = getSystemService(context, UserManager::class.java) ?: return emptyList()
    val privateUser = userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    } ?: return emptyList()

    return launcherApps.getActivityList(null, privateUser).map {
        InstalledApp(
            displayName = it.label?.toString() ?: "Unknown App",
            packageName = it.applicationInfo.packageName,
            componentName = it.componentName
        )
    }
}

/**
 * Shows the system app info page for an app in Private Space.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun showPrivateSpaceAppInfo(
    installedApp: InstalledApp,
    context: Context,
    sourceBounds: Rect? = null
) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val userManager = getSystemService(context, UserManager::class.java) ?: return

    userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    }?.let { userHandle ->
        val options = ActivityOptions.makeBasic()
        if (sourceBounds != null) {
            options.launchBounds = sourceBounds
        }
        launcherApps.startAppDetailsActivity(
            installedApp.componentName,
            userHandle,
            sourceBounds,
            options.toBundle()
        )
    }
}

/**
 * Uninstalls an app from Private Space.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun uninstallPrivateSpaceApp(installedApp: InstalledApp, context: Context) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val userManager = getSystemService(context, UserManager::class.java) ?: return

    userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    }?.let { userHandle ->
        // Create an intent to uninstall the app
        val uninstallIntent = Intent(Intent.ACTION_DELETE)
        uninstallIntent.data = "package:${installedApp.packageName}".toUri()
        uninstallIntent.putExtra(Intent.EXTRA_USER, userHandle)
        uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Launch the system uninstaller
        context.startActivity(uninstallIntent)
    }
}

/**
 * UI component for displaying a single Private Space app item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrivateAppItem(
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
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * UI component for Private Space settings dialog.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrivateSpaceSettings(
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
                val settingKey = context.resources.getString(R.string.SearchHiddenPrivateSpace)
                SettingsSwitch(
                    stringResource(R.string.hide_private_space_in_search),
                    getBooleanSetting(context, settingKey, false),
                    onCheckedChange = { value ->
                        setBooleanSetting(context, settingKey, value)
                    })
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

/**
 * Opens app in Private space
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun openPrivateSpaceApp(installedApp: InstalledApp, context: Context, sourceBounds: Rect) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            val options = ActivityOptions.makeBasic()
            launcherApps.startMainActivity(
                installedApp.componentName,
                userInfo,
                sourceBounds,
                options.toBundle()
            )
        }
    }
}

/**
 * Checks if a Private Space profile exists on the device.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun doesPrivateSpaceExist(context: Context): Boolean {
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            return true
        }
    }
    return false
}
