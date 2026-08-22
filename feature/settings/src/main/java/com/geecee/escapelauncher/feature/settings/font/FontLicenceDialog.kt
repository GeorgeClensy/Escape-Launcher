package com.geecee.escapelauncher.feature.settings.font

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.common.loadTextFromAssets
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer

/**
 * Font licence dialog
 *
 * @param context Context
 */
@Composable
fun FontLicenceDialog(context: Context, onOKClick: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)  // Make the content scrollable
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // Load text from the asset
        loadTextFromAssets(context, "Font Licence.txt")?.let { text ->
            BasicText(
                text = text, style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal
                ), modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "OK" Button
        Button(
            onClick = { onOKClick() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
            colors = ButtonColors(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurface,
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("OK")
        }

        SettingsSpacer()
        SettingsSpacer()
        SettingsSpacer()
    }
}