package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R

@Composable
fun NextButton(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.continue_str),
    outline: Boolean = false,
    onNext: () -> Unit
) {
    Button(
        onClick = { onNext() },
        modifier = modifier.padding(bottom = 30.dp),
        border = if (outline) BorderStroke(1.dp, primaryContentColor) else null,
        colors = ButtonColors(
            containerColor = if (outline) Color.Transparent else primaryContentColor,
            contentColor = if (outline) primaryContentColor else BackgroundColor,
            disabledContainerColor = if (outline) Color.Transparent else primaryContentColor,
            disabledContentColor = if (outline) primaryContentColor else BackgroundColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text, maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Continue"
            )
        }
    }
}
