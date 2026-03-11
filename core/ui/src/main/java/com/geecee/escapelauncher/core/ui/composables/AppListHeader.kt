package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.theme.primaryContentColor

/**
 * Apps List title
 */
@Composable
fun AppsListHeader(text: String = "") {
    Spacer(modifier = Modifier.height(140.dp))
    Text(
        text = text,
        color = primaryContentColor,
        style = MaterialTheme.typography.titleMedium
    )
}
