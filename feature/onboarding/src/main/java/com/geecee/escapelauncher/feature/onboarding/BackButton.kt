package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R

@Composable
fun PrevButton(
    modifier: Modifier = Modifier,
    onPrev: () -> Unit,
) {
    MediumExtendedFloatingActionButton(
        onClick = { onPrev() },
        modifier = modifier,
        containerColor = primaryContentColor,
        contentColor = BackgroundColor,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)
        )
    }
}