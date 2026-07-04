package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.theme.primaryContentColor

@Composable
fun NextButton(
    modifier: Modifier = Modifier,
    onNext: () -> Unit
) {
    MediumExtendedFloatingActionButton(
        onClick = { onNext() },
        modifier = modifier,
        containerColor = primaryContentColor,
        contentColor = BackgroundColor,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Continue"
        )
    }
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