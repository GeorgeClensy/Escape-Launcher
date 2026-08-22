package com.geecee.escapelauncher.feature.onboarding.launcher

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem
import com.geecee.escapelauncher.feature.onboarding.OnboardingViewModel

@Composable
fun DefaultLauncherPage(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

    // To show the default launcher prompt, the activity must be started for a result so this is required instead of Context.startActivity
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    Box(
        modifier
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
            if (!isDefaultLauncher) {
                SettingsNavigationItem(
                    label = stringResource(R.string.set_launcher),
                    onClick = {
                        val intent = viewModel.getPromptDefaultLauncherIntent()
                        roleLauncher.launch(intent)
                    },
                    diagonalArrow = true,
                    isTopOfGroup = true,
                    isBottomOfGroup = true
                )
            } else {
                SettingsNavigationItem(
                    label = stringResource(R.string.already_default),
                    onClick = {
                        val intent = viewModel.getPromptDefaultLauncherIntent()
                        roleLauncher.launch(intent)
                    },
                    diagonalArrow = true,
                    repalceIconWichCheck = true,
                    isTopOfGroup = true,
                    isBottomOfGroup = true
                )
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}

@Preview
@Composable
fun PrevDefaultLauncherPage() {
    EscapeThemePreview {
        Box(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
        ) {
            DefaultLauncherPage()
        }
    }
}
