package com.geecee.escapelauncher.feature.onboarding.launcher

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.showLauncherSelector
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R

@Composable
fun DefaultLauncherPage() {
    val activity = LocalActivity.current

    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column {
            Text(
                stringResource(R.string.set_escape),
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(5.dp))
            Text(
                stringResource(R.string.stop_going_back),
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(10.dp))
            activity?.let {
                if (!isDefaultLauncher(activity)) {
                    Button(
                        onClick = {
                            activity.showLauncherSelector()
                        }, modifier = Modifier, colors = ButtonColors(
                            primaryContentColor,
                            BackgroundColor,
                            primaryContentColor,
                            BackgroundColor
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = stringResource(R.string.set_launcher))
                        }
                    }
                } else {
                    Button(
                        onClick = {}, modifier = Modifier.border(
                            1.dp, primaryContentColor, MaterialTheme.shapes.extraLarge
                        ), colors = ButtonColors(
                            Color.Transparent,
                            primaryContentColor,
                            Color.Transparent,
                            primaryContentColor
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Check, "")
                            Text(text = stringResource(R.string.already_default))
                        }
                    }
                }
            }
            Spacer(Modifier.height(240.dp))
        }
    }
}
