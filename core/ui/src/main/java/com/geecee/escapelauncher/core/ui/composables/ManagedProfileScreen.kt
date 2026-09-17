package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.model.InstalledApp

/**
 * A common screen for managed profiles (Work Profile, Private Space)
 *
 * @param modifier The modifier to apply to the UI
 * @param isUnlocked Whether the profile is unlocked
 * @param apps The list of apps in the profile
 * @param appsListAlignment The alignment of the apps list
 * @param onAppClick The action to perform when an app is clicked
 * @param onAppLongClick The action to perform when an app is long clicked
 * @param canToggleProfile Whether the profile can be toggled
 * @param toggleIcon The icon for the toggle FAB
 * @param toggleContentDescription The content description for the toggle FAB
 * @param onToggleClick The action to perform when the toggle FAB is clicked
 * @param lockedIcon The icon for the locked state
 * @param lockedText The text for the locked state
 * @param lockedSubhead The subhead for the locked state
 * @param lockedButtonText The button text for the locked state
 * @param onUnlockClick The action to perform when the unlock button is clicked
 */
@Composable
fun ManagedProfileScreen(
    modifier: Modifier = Modifier,
    isUnlocked: Boolean,
    apps: List<InstalledApp>,
    appsListAlignment: Alignment.Horizontal,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    canToggleProfile: Boolean,
    toggleIcon: ImageVector,
    toggleContentDescription: String,
    onToggleClick: () -> Unit,
    lockedIcon: ImageVector,
    lockedText: String,
    lockedSubhead: String,
    lockedButtonText: String,
    onUnlockClick: () -> Unit
) {
    Box(modifier) {
        UnlockedManagedProfileUI(
            isVisible = isUnlocked,
            apps = apps,
            appsListAlignment = appsListAlignment,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            canToggleProfile = canToggleProfile,
            toggleIcon = toggleIcon,
            toggleContentDescription = toggleContentDescription,
            onToggleClick = onToggleClick
        )

        AnimatedVisibility(
            visible = !isUnlocked, enter = fadeIn(), exit = fadeOut()
        ) {
            LockedAppFolderUI(
                text = lockedText,
                icon = lockedIcon,
                iconContentDescription = toggleContentDescription,
                buttonText = lockedButtonText,
                subhead = lockedSubhead,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = 86.dp,
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                    ),
                unlockClick = onUnlockClick
            )
        }
    }
}

/**
 * UI for an unlocked managed profile (Work Profile, Private Space)
 *
 * @param modifier The modifier to apply to the UI
 * @param isVisible Whether the UI is visible
 * @param apps The list of apps in the profile
 * @param appsListAlignment The alignment of the apps list
 * @param onAppClick The action to perform when an app is clicked
 * @param onAppLongClick The action to perform when an app is long clicked
 * @param canToggleProfile Whether the profile can be toggled
 * @param toggleIcon The icon for the toggle FAB
 * @param toggleContentDescription The content description for the toggle FAB
 * @param onToggleClick The action to perform when the toggle FAB is clicked
 */
@Composable
fun UnlockedManagedProfileUI(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    apps: List<InstalledApp>,
    appsListAlignment: Alignment.Horizontal,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    canToggleProfile: Boolean,
    toggleIcon: ImageVector,
    toggleContentDescription: String,
    onToggleClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val heightToTopOfTabs = 30.dp + 56.dp

    AnimatedVisibility(
        visible = isVisible, enter = fadeIn(), exit = fadeOut(), modifier = modifier
    ) {
        Box(Modifier.fillMaxSize().padding(horizontal = 30.dp)) {
            Column(
                horizontalAlignment = appsListAlignment,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                val statusBarHeight =
                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Spacer(
                    modifier = Modifier.height(statusBarHeight + 10.dp)
                )

                apps.forEach { app ->
                    HomeScreenItem(appName = app.displayName, onAppLongClick = {
                        onAppLongClick(app)
                    }, onAppClick = {
                        onAppClick(app)
                    }, alignment = appsListAlignment)
                }

                Spacer(modifier = Modifier.height(15.dp))

                Spacer(
                    modifier = Modifier.height(
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + heightToTopOfTabs
                    )
                )
            }

            if (canToggleProfile) {
                BouncyMorphingFab(
                    icon = toggleIcon,
                    contentDescription = toggleContentDescription,
                    onClick = onToggleClick,
                    modifier = Modifier
                        .align(if (appsListAlignment == Alignment.End) Alignment.BottomStart else Alignment.BottomEnd)
                        .padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding() + heightToTopOfTabs + 15.dp
                        ),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    radius = 24.dp
                )
            }
        }
    }
}
