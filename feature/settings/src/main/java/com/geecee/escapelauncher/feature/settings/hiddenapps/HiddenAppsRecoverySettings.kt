package com.geecee.escapelauncher.feature.settings.hiddenapps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.SettingsButton

@Composable
fun HiddenAppsRecoverySettings(
    state: HiddenAppsLockState,
    modifier: Modifier = Modifier,
    onUpdate: (String, Int, () -> Unit) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val minutes = state.recoveryMinutes ?: return
    SettingsButton(modifier = modifier,
        label = stringResource(R.string.hidden_recovery_duration, minutes),
        onClick = { showDialog = true }, isTopOfGroup = true, isBottomOfGroup = true)
    if (showDialog) {
        RecoveryDurationDialog(state, onDismiss = { showDialog = false },
            onUpdate = { password, duration -> onUpdate(password, duration) { showDialog = false } })
    }
}

@Composable
private fun RecoveryDurationDialog(
    state: HiddenAppsLockState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onUpdate: (String, Int) -> Unit
) {
    var duration by remember { mutableStateOf(state.recoveryMinutes.toString()) }
    var password by remember { mutableStateOf("") }
    val minutes = duration.toIntOrNull()
    AlertDialog(modifier = modifier,
        onDismissRequest = { if (!state.isBusy) onDismiss() },
        title = { Text(stringResource(R.string.hidden_recovery_title), style = MaterialTheme.typography.bodyLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.hidden_recovery_range), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = duration, onValueChange = { duration = it },
                    enabled = !state.isBusy, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(stringResource(R.string.hidden_recovery_minutes), style = MaterialTheme.typography.bodySmall) })
                if (state.isPasswordSet == true) {
                    HiddenAppsPasswordField(password, stringResource(R.string.hidden_password_current),
                        enabled = !state.isBusy, onValueChange = { password = it })
                }
                HiddenAppsPasswordError(state.error)
            }
        },
        confirmButton = {
            TextButton(enabled = !state.isBusy && minutes != null &&
                minutes in 1..HiddenAppsPasswordRepository.MAX_RECOVERY_MINUTES &&
                (state.isPasswordSet != true || password.isNotEmpty()),
                onClick = { minutes?.let { onUpdate(password, it) } }) {
                Text(stringResource(R.string.hidden_password_save))
            }
        },
        dismissButton = {
            TextButton(enabled = !state.isBusy, onClick = onDismiss) { Text(stringResource(R.string.hidden_password_cancel)) }
        })
}

@Preview(showBackground = true)
@Composable
private fun HiddenAppsRecoverySettingsPreview() {
    MaterialTheme { HiddenAppsRecoverySettings(HiddenAppsLockState(recoveryMinutes = 60), onUpdate = { _, _, _ -> }) }
}
