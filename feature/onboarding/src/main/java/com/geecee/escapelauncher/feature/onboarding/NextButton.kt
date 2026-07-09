package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.composables.BouncyMorphingFab
import com.geecee.escapelauncher.core.ui.R

@Composable
fun NextButton(
    modifier: Modifier = Modifier,
    showTick: Boolean = false,
    onNext: () -> Unit
) {
    BouncyMorphingFab (
        modifier = modifier,
        icon = if (!showTick) Icons.AutoMirrored.Rounded.ArrowForward else Icons.Rounded.Check,
        contentDescription = stringResource(R.string.continue_str),
        containerColor = primaryContentColor,
        contentColor = BackgroundColor,
        onClick = { onNext() }
    )
}

@Preview
@Composable
fun PrevNextButton() {
    EscapeThemePreview {
        NextButton {
            // Do nothing on click
        }
    }
}

@Preview
@Composable
fun PrevNextButtonTick() {
    EscapeThemePreview {
        NextButton(
            showTick = true
        ) {
            // Do nothing on click
        }
    }
}