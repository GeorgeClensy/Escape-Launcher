package com.geecee.escapelauncher.privatespace

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.theme.CardContainerColor
import com.geecee.escapelauncher.core.ui.theme.CardContainerColorDisabled
import com.geecee.escapelauncher.core.ui.theme.ContentColor
import com.geecee.escapelauncher.core.ui.theme.ContentColorDisabled
import com.geecee.escapelauncher.core.ui.theme.SecondaryCardContainerColor

/**
 * Android 15+ Private space UI with apps, settings button and lock button
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PrivateSpace(
    modifier: Modifier = Modifier,
    viewModel: PrivateSpaceViewModel = hiltViewModel(),
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onSettingsClick: () -> Unit
) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val privateApps by viewModel.PrivateSpaceApps.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .animateContentSize()
            .clickable(
                onClick = {
                    if (!isUnlocked) {
                        viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {})
                    }
                },
            ), colors = CardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor,
            disabledContentColor = CardContainerColorDisabled,
            disabledContainerColor = ContentColorDisabled,
        )
    ) {
        if (isUnlocked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        stringResource(R.string.private_space),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Row(
                        Modifier.align(Alignment.CenterEnd)
                    ) {
                        IconButton(
                            onClick = {
                                onSettingsClick()
                            }, modifier = Modifier, colors = IconButtonColors(
                                containerColor = SecondaryCardContainerColor,
                                contentColor = ContentColor,
                                disabledContainerColor = SecondaryCardContainerColor,
                                disabledContentColor = ContentColor
                            )
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                stringResource(R.string.private_space_settings)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {})
                            }, modifier = Modifier, colors = IconButtonColors(
                                containerColor = SecondaryCardContainerColor,
                                contentColor = ContentColor,
                                disabledContainerColor = SecondaryCardContainerColor,
                                disabledContentColor = ContentColor
                            )
                        ) {
                            Icon(
                                Icons.Default.Lock, stringResource(R.string.lock_private_space)
                            )
                        }
                    }
                }

                privateApps.forEach { app ->
                    PrivateAppItem(app.displayName, {
                        onAppLongClick(app)
                    }) {
                        onAppClick(app)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Text(
                    stringResource(R.string.private_space),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                IconButton(
                    onClick = {
                        viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = {})
                    }, modifier = Modifier.align(Alignment.CenterEnd), colors = IconButtonColors(
                        containerColor = SecondaryCardContainerColor,
                        contentColor = ContentColor,
                        disabledContainerColor = SecondaryCardContainerColor,
                        disabledContentColor = ContentColor
                    )
                ) {
                    Icon(
                        Icons.Default.Lock, stringResource(R.string.lock_private_space)
                    )
                }
            }
        }
    }
}

//todo: add private space settings again

/**
 * UI component for displaying a single Private Space app item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrivateAppItem(
    appName: String, onLongClick: () -> Unit, onClick: () -> Unit
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