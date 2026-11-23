package com.geecee.escapelauncher.feature.settings.hiddenapps

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader

@Composable
fun HiddenAppsPasswordGate(
    state: HiddenAppsLockState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onUnlock: (String) -> Unit,
    onStartRecovery: () -> Unit = {},
    onCancelRecovery: () -> Unit = {},
    onFinishRecovery: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (state.canAccess) {
        content()
        return
    }
    // Password input is deliberately not saved across activity/process recreation.
    var password by remember { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EscapeHeader(onBack, stringResource(R.string.hidden_apps))
        if (state.recoverySecondsRemaining != null) {
            HiddenAppsRecovery(state.recoverySecondsRemaining, state.isBusy,
                onCancel = onCancelRecovery, onFinish = onFinishRecovery)
            HiddenAppsPasswordError(state.error)
        } else if (state.isPasswordSet == null && state.error == null) {
            CircularProgressIndicator()
        } else {
            Text(stringResource(R.string.hidden_password_prompt), style = MaterialTheme.typography.bodyMedium)
            HiddenAppsPasswordField(
                value = password,
                label = stringResource(R.string.hidden_password_label),
                enabled = !state.isBusy,
                onValueChange = { password = it }
            )
            HiddenAppsPasswordError(state.error)
            Button(
                enabled = !state.isBusy && state.isPasswordSet == true &&
                    password.length in 1..HiddenAppsPasswordRepository.MAX_PASSWORD_LENGTH,
                onClick = {
                    onUnlock(password)
                    password = ""
                }
            ) { Text(stringResource(R.string.hidden_password_unlock)) }
            if (state.isBusy) CircularProgressIndicator()
            if (state.recoveryMinutes != null && state.isPasswordSet == true) {
                Text(stringResource(R.string.hidden_recovery_offer, state.recoveryMinutes),
                    style = MaterialTheme.typography.bodySmall)
                Button(enabled = !state.isBusy, onClick = onStartRecovery) {
                    Text(stringResource(R.string.hidden_recovery_start))
                }
            }
        }
    }
}

@Composable
internal fun HiddenAppsPasswordError(error: HiddenAppsLockError?, modifier: Modifier = Modifier) {
    if (error != null) {
        Text(
            text = stringResource(if (error == HiddenAppsLockError.IncorrectPassword)
                R.string.hidden_password_incorrect else R.string.hidden_password_storage_error),
            modifier = modifier,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HiddenAppsPasswordGatePreview() {
    MaterialTheme {
        HiddenAppsPasswordGate(
            state = HiddenAppsLockState(isPasswordSet = true),
            onBack = {}, onUnlock = {}, content = {}
        )
    }
}
