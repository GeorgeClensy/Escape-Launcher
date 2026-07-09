package com.geecee.escapelauncher.feature.onboarding.launcher

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.showLauncherSelector
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem

@Composable
fun DefaultLauncherPage() {
    val activity = LocalActivity.current

    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.set_escape),
                Modifier,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.stop_going_back),
                Modifier,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(20.dp))
            activity?.let {
                if (!isDefaultLauncher(activity)) {
                    SettingsNavigationItem(
                        label = stringResource(R.string.set_launcher),
                        onClick = {
                            activity.showLauncherSelector()
                        },
                        diagonalArrow = true,
                        isTopOfGroup = true,
                        isBottomOfGroup = true
                    )
                } else {
                    SettingsNavigationItem(
                        label = stringResource(R.string.already_default),
                        onClick = {
                            activity.showLauncherSelector()
                        },
                        diagonalArrow = true,
                        repalceIconWichCheck = true,
                        isTopOfGroup = true,
                        isBottomOfGroup = true
                    )
                }
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}
