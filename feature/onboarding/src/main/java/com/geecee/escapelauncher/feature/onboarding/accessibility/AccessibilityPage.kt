package com.geecee.escapelauncher.feature.onboarding.accessibility

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.feature.onboarding.NextButton
import com.geecee.escapelauncher.feature.onboarding.PrevButton

@Composable
fun AccessibilityPage(onNext: () -> Unit, onPrev: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        LazyColumn(
            state = scrollState
        ) {
            item {
                Text(
                    stringResource(R.string.accessibility_title),
                    Modifier,
                    primaryContentColor,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Text(
                    stringResource(R.string.accessibility_description),
                    Modifier,
                    primaryContentColor,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    lineHeight = 32.sp
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }, modifier = Modifier, colors = ButtonColors(
                        primaryContentColor, BackgroundColor, primaryContentColor, BackgroundColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(R.string.grant_accessibility))
                    }
                }
            }

            item {
                Spacer(Modifier.height(240.dp))
            }
        }

        PrevButton(
            Modifier.align(Alignment.BottomStart)
        ) {
            onPrev()
        }

        Row(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            NextButton {
                onNext()
            }
        }
    }
}
