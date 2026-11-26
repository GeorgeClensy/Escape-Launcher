package com.geecee.escapelauncher.feature.settings.openchallenges

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.SettingsButton

@Composable
internal fun CountdownDurationSetting(
    seconds: Int,
    modifier: Modifier = Modifier,
    onDurationChanged: (Int) -> Unit
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }
    Column(modifier) {
        SettingsButton(
            label = pluralStringResource(R.plurals.countdown_duration, seconds, seconds),
            isTopOfGroup = true,
            onClick = {
                input = seconds.toString()
                isEditing = true
            }
        )
        SettingsButton(
            label = stringResource(R.string.reset_countdown_duration),
            isBottomOfGroup = true,
            onClick = { onDurationChanged(DefaultSettings.COUNTDOWN_SECONDS) }
        )
    }
    if (isEditing) {
        val value = input.toIntOrNull()
        val isValid = value != null && value in
            DefaultSettings.MIN_COUNTDOWN_SECONDS..DefaultSettings.MAX_COUNTDOWN_SECONDS
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = {
                Text(
                    text = stringResource(R.string.countdown_duration_title),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(stringResource(R.string.countdown_duration_range)) },
                    isError = !isValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = isValid,
                    onClick = {
                        value?.let(onDurationChanged)
                        isEditing = false
                    }
                ) { Text(stringResource(R.string.countdown_save)) }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text(stringResource(R.string.countdown_cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CountdownDurationSettingPreview() {
    MaterialTheme {
        CountdownDurationSetting(seconds = 20, onDurationChanged = {})
    }
}
