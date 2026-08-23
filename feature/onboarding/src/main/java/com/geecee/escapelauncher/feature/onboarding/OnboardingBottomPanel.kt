package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview

@Composable
fun OnboardingBottomPanel(
    modifier: Modifier = Modifier,
    showPrevButton: Boolean = true,
    useTickNextButton: Boolean = false,
    onPrevButtonClick: () -> Unit = {},
    onNextButtonClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(
        topEnd = 28.dp,
        topStart = 28.dp,
        bottomEnd = 0.dp,
        bottomStart = 0.dp
    )

    Surface(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = shape, clip = true),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            AnimatedVisibility(
                visible = showPrevButton,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BackButton(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    onPrevButtonClick()
                }
            }

            NextButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                showTick = useTickNextButton
            ) {
                onNextButtonClick()
            }
        }
    }
}

@Preview
@Composable
fun PrevOnboardingBottomPanel() {
    EscapeThemePreview {
        OnboardingBottomPanel()
    }
}