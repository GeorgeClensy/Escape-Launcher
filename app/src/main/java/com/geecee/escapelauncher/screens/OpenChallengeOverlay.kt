package com.geecee.escapelauncher.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.geecee.escapelauncher.MainPagerScreenViewModel
import com.geecee.escapelauncher.core.ui.composables.OpenChallenge

@Composable
fun OpenChallengeOverlay(
    viewModel: MainPagerScreenViewModel,
    hapticFeedbackEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val remainingSeconds by viewModel.countdownRemaining.collectAsState()
    remainingSeconds?.let { remaining ->
        OpenChallenge(
            enabled = hapticFeedbackEnabled,
            remainingSeconds = remaining,
            modifier = modifier,
            goBack = viewModel::cancelCountdown
        )
    }
}
