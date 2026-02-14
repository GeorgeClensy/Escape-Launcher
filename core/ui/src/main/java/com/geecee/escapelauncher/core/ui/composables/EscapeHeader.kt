package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.theme.ContentColor

/**
 * Title header with back button
 *
 * @param title The text shown on the header
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EscapeHeader(
    goBack: () -> Unit,
    title: String,
    hideBack: Boolean = false,
    color: Color = ContentColor,
    padding: Boolean = true
) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = { goBack() })
            .padding(
                0.dp,
                if (padding) {
                    120.dp
                } else {
                    0.dp
                },
                0.dp,
                8.dp
            )
            .height(70.dp) // Set a fixed height for the header
    ) {
        if (!hideBack) {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = color,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        AutoResizingText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}


/**
 * @param title The text shown on the subhead
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EscapeSubhead(title: String) {
    Row(
        modifier = Modifier.padding(0.dp, 30.dp, 0.dp, 2.dp)
    ) {
        Text(
            text = title,
            color = ContentColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
