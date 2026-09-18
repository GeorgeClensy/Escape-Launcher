package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R

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
    val heightToTopOfTabs = 30.dp + 56.dp

    Box(modifier = modifier) {
        if (isUnlocked) {
            UnlockedManagedProfileUI(
                title = lockedText,
                apps = apps,
                appsListAlignment = appsListAlignment,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                canToggleProfile = canToggleProfile,
                toggleIcon = toggleIcon,
                toggleContentDescription = toggleContentDescription,
                onToggleClick = onToggleClick,
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .align(if (appsListAlignment == Alignment.End) Alignment.BottomEnd else if (appsListAlignment == Alignment.CenterHorizontally) Alignment.BottomCenter else Alignment.BottomStart)
                    .padding(horizontal = 30.dp)
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + heightToTopOfTabs + 20.dp,
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 5.dp
                    )
            )
        }

        if (!isUnlocked) {
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
    title: String = stringResource(R.string.private_space),
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

    Card(
        modifier = modifier, shape = MaterialTheme.shapes.extraLarge, colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 5.dp)
        ) {
            if (appsListAlignment != Alignment.End) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterStart),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appsListAlignment == Alignment.End) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onToggleClick()
                    }, modifier = Modifier, colors = IconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        toggleIcon, toggleContentDescription
                    )
                }
            }

        }


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            apps.forEach { app ->
                HomeScreenItem(
                    appName = app.displayName,
                    onAppLongClick = {
                        onAppLongClick(app)
                    },
                    onAppClick = {
                        onAppClick(app)
                    },
                    alignment = appsListAlignment,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
