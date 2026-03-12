package com.geecee.escapelauncher.ui.composables

import android.content.ComponentName
import android.graphics.Rect
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.common.InstalledApp
import com.geecee.escapelauncher.core.ui.composables.AppAction
import com.geecee.escapelauncher.core.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.core.ui.theme.CardContainerColor
import com.geecee.escapelauncher.core.ui.theme.CardContainerColorDisabled
import com.geecee.escapelauncher.core.ui.theme.ContentColor
import com.geecee.escapelauncher.core.ui.theme.ContentColorDisabled
import com.geecee.escapelauncher.core.ui.theme.SecondaryCardContainerColor
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.PrivateAppItem
import com.geecee.escapelauncher.utils.getPrivateSpaceApps
import com.geecee.escapelauncher.utils.lockPrivateSpace
import com.geecee.escapelauncher.utils.openPrivateSpaceApp
import com.geecee.escapelauncher.utils.showPrivateSpaceAppInfo
import com.geecee.escapelauncher.utils.uninstallPrivateSpaceApp

/**
 * Android 15+ Private space UI with apps, settings button and lock button
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PrivateSpace(mainAppModel: MainAppViewModel, homeScreenModel: HomeScreenModel) {
    val privateSpaceAppActions = listOf(
        AppAction(
            stringResource(R.string.uninstall)
        ) {
            uninstallPrivateSpaceApp(
                homeScreenModel.currentSelectedPrivateApp.value,
                mainAppModel.getContext()
            )
        },
        AppAction(
            stringResource(R.string.app_info)
        ) {
            showPrivateSpaceAppInfo(
                homeScreenModel.currentSelectedPrivateApp.value,
                mainAppModel.getContext()
            )
        }
    )

    Card(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge),
        colors = CardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor,
            disabledContentColor = CardContainerColorDisabled,
            disabledContainerColor = ContentColorDisabled,
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
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
                        {
                            homeScreenModel.showPrivateSpaceSettings.value = true
                        }, Modifier, colors = IconButtonColors(
                            containerColor = SecondaryCardContainerColor,
                            contentColor = ContentColor,
                            disabledContainerColor = SecondaryCardContainerColor,
                            disabledContentColor = ContentColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Settings, stringResource(R.string.private_space_settings)
                        )
                    }

                    IconButton(
                        {
                            lockPrivateSpace(mainAppModel.getContext())
                            homeScreenModel.searchExpanded.value = false
                            homeScreenModel.searchText.value = ""
                        }, Modifier, colors = IconButtonColors(
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

            getPrivateSpaceApps(mainAppModel.getContext()).sortedBy { it.displayName }.forEach { app ->
                PrivateAppItem(app.displayName, {
                    homeScreenModel.currentSelectedPrivateApp.value = app
                    homeScreenModel.showPrivateBottomSheet.value = true
                }) {
                    openPrivateSpaceApp(
                        installedApp = app, context = mainAppModel.getContext(), Rect()
                    )
                    resetHome(homeScreenModel)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (homeScreenModel.showPrivateBottomSheet.value) {
        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedPrivateApp.value.displayName,
            actions = privateSpaceAppActions,
            onDismissRequest = {
                homeScreenModel.showPrivateBottomSheet.value = false
                homeScreenModel.currentSelectedPrivateApp.value =
                    InstalledApp("", "", ComponentName("", ""))
            },
            sheetState = rememberModalBottomSheetState(),
            modifier = Modifier
        )
    }
}
