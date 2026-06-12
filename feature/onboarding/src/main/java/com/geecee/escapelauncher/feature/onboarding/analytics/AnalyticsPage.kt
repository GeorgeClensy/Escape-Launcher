package com.geecee.escapelauncher.feature.onboarding.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.loadTextFromAssets
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.PrivacyPolicyDialog
import com.geecee.escapelauncher.feature.onboarding.NextButton
import com.geecee.escapelauncher.feature.onboarding.PrevButton

@Composable
fun AnalyticsPage(
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val context = LocalContext.current
    val showPolicyDialog = remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

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
                    stringResource(R.string.analytics_and_data_collection),
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
                    stringResource(R.string.anonymous_data),
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
                        showPolicyDialog.value = true
                    }, modifier = Modifier, colors = ButtonColors(
                        primaryContentColor, BackgroundColor, primaryContentColor, BackgroundColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(R.string.read_privacy_policy))
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
            NextButton(
                text = stringResource(R.string.deny), outline = true
            ) {
                analyticsViewModel.setAllowAnalytics(false)
                onNext()
            }

            Spacer(Modifier.width(15.dp))

            NextButton(text = stringResource(R.string.allow)) {
                analyticsViewModel.setAllowAnalytics(true)
                onNext()
            }
        }
    }

    AnimatedVisibility(showPolicyDialog.value, enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.padding(bottom = 30.dp)) {
            loadTextFromAssets(context, "Privacy Policy.txt")?.let { text ->
                PrivacyPolicyDialog(text = text, onDismiss = { showPolicyDialog.value = false })
            }
        }
    }
}
