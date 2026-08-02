package com.geecee.escapelauncher.feature.onboarding.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.loadTextFromAssets
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.LegalTextBox
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch

@Composable
fun AnalyticsPage(
    modifier: Modifier = Modifier,
    analyticsViewModel: AnalyticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allowAnalytics by analyticsViewModel.allowAnalytics.collectAsState(initial = false)

    Box(
        modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column (
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxHeight()
        ) {
            Text(
                stringResource(R.string.analytics_and_data_collection),
                Modifier,
                MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )


            Spacer(Modifier.height(10.dp))


            SettingsSwitch(
                label = stringResource(id = R.string.Analytics),
                checked = allowAnalytics,
                isBottomOfGroup = true,
                isTopOfGroup = true,
                onCheckedChange = {
                    analyticsViewModel.setAllowAnalytics(it)
                })


            Spacer(Modifier.height(10.dp))


            loadTextFromAssets(context, "Privacy Policy.txt")?.let { text ->
                LegalTextBox(
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    text = text
                )
            }
        }
    }
}

@Preview
@Composable
fun PrevAnalyticsPage() {
    EscapeThemePreview {
        Box(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
        ) {
            AnalyticsPage()
        }
    }
}