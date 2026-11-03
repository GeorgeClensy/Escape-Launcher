package com.geecee.escapelauncher.feature.settings.hiddenapps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.SettingsButton

@Composable
fun HiddenAppsPasswordSettings(
    state: HiddenAppsLockState,
    modifier: Modifier = Modifier,
    onUpdate: (String, String?, () -> Unit) -> Unit
) {
    var mode by remember { mutableStateOf<String?>(null) }
    Column(modifier) {
        SettingsButton(
            label = stringResource(if (state.isPasswordSet == true)
                R.string.hidden_password_change else R.string.hidden_password_set),
            onClick = { mode = "set" },
            isTopOfGroup = true,
            isBottomOfGroup = state.isPasswordSet != true
        )
        if (state.isPasswordSet == true) {
            SettingsButton(
                label = stringResource(R.string.hidden_password_remove),
                onClick = { mode = "remove" },
                isBottomOfGroup = true
            )
        }
    }
    mode?.let { operation ->
        HiddenAppsPasswordDialog(
            state = state,
            isRemoving = operation == "remove",
            onDismiss = { if (!state.isBusy) mode = null },
            onUpdate = { current, password -> onUpdate(current, password) { mode = null } }
        )
    }
}

@Composable
private fun HiddenAppsPasswordDialog(
    state: HiddenAppsLockState,
    isRemoving: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onUpdate: (String, String?) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val valid = (state.isPasswordSet != true || current.isNotEmpty()) &&
        (isRemoving || (password.length in HiddenAppsPasswordRepository.MIN_PASSWORD_LENGTH..
            HiddenAppsPasswordRepository.MAX_PASSWORD_LENGTH && password == confirmation))
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(when {
                    isRemoving -> R.string.hidden_password_remove
                    state.isPasswordSet == true -> R.string.hidden_password_change
                    else -> R.string.hidden_password_set
                }),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.hidden_password_explanation), style = MaterialTheme.typography.bodySmall)
                if (state.isPasswordSet == true) {
                    HiddenAppsPasswordField(current, stringResource(R.string.hidden_password_current),
                        enabled = !state.isBusy, onValueChange = { current = it })
                }
                if (!isRemoving) {
                    HiddenAppsPasswordField(password, stringResource(R.string.hidden_password_new),
                        enabled = !state.isBusy, onValueChange = { password = it })
                    HiddenAppsPasswordField(confirmation, stringResource(R.string.hidden_password_confirm),
                        enabled = !state.isBusy, onValueChange = { confirmation = it })
                    Text(stringResource(R.string.hidden_password_length), style = MaterialTheme.typography.bodySmall)
                }
                HiddenAppsPasswordError(state.error)
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !state.isBusy, onClick = {
                onUpdate(current, if (isRemoving) null else password)
            }) { Text(stringResource(if (isRemoving) R.string.hidden_password_remove else R.string.hidden_password_save)) }
        },
        dismissButton = {
            TextButton(enabled = !state.isBusy, onClick = onDismiss) {
                Text(stringResource(R.string.hidden_password_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun HiddenAppsPasswordSettingsPreview() {
    MaterialTheme {
        HiddenAppsPasswordSettings(state = HiddenAppsLockState(isPasswordSet = false), onUpdate = { _, _, _ -> })
    }
}
