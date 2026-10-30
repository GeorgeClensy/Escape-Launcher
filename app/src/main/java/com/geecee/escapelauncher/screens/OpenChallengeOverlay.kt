package com.geecee.escapelauncher.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.geecee.escapelauncher.MainPagerScreenViewModel
import com.geecee.escapelauncher.core.ui.composables.OpenChallenge
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OpenChallengeOverlay(
    viewModel: MainPagerScreenViewModel,
    screenTimeViewModel: ScreenTimeViewModel,
    hapticFeedbackEnabled: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = viewModel.showOpenChallenge.value, enter = fadeIn(), exit = fadeOut()
    ) {
        OpenChallenge(
            enabled = hapticFeedbackEnabled,
            openApp = {
                viewModel.openApp(
                    app = viewModel.currentSelectedApp.value,
                    overrideChallenge = true,
                    onAppOpened = {
                        screenTimeViewModel.onAppOpened(it)
                    })
                coroutineScope.launch {
                    delay(1000.milliseconds)
                    viewModel.showOpenChallenge.value = false
                }
            },
            goBack = {
                viewModel.showOpenChallenge.value = false
            })
    }
}