package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.BouncyMorphingFab

@Composable
fun PrevButton(
    modifier: Modifier = Modifier,
    onPrev: () -> Unit,
) {
    BouncyMorphingFab (
        modifier = modifier,
        icon = Icons.AutoMirrored.Rounded.ArrowBack,
        contentDescription = stringResource(R.string.back),
        containerColor = primaryContentColor,
        contentColor = BackgroundColor,
        onClick = { onPrev() }
    )
}

@Preview
@Composable
fun PrevPrevButton() {
    EscapeThemePreview {
        PrevButton {
            // Do nothing on click
        }
    }
}