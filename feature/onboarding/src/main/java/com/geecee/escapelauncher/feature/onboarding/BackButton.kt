package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R

@Composable
fun PrevButton(
    modifier: Modifier = Modifier,
    onPrev: () -> Unit,
) {
    IconButton(
        onClick = { onPrev() },
        modifier = modifier
            .padding(bottom = 30.dp)
            .offset(x = (-4).dp),
        colors = IconButtonColors(
            containerColor = primaryContentColor,
            contentColor = BackgroundColor,
            disabledContainerColor = primaryContentColor,
            disabledContentColor = BackgroundColor
        )
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)
        )
    }
}