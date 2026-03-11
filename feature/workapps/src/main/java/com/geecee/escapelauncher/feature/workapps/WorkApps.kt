package com.geecee.escapelauncher.feature.workapps

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.theme.BackgroundColor
import com.geecee.escapelauncher.core.ui.theme.primaryContentColor

@Composable
fun WorkAppsFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = {
            onClick()
        },
        modifier = modifier.size(56.dp),
        contentColor = BackgroundColor,
        containerColor = primaryContentColor,
    ) {
        Icon(Icons.Rounded.Work, contentDescription = stringResource(R.string.work_profile))
    }
}