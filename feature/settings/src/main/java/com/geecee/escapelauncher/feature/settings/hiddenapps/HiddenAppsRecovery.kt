package com.geecee.escapelauncher.feature.settings.hiddenapps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.R

@Composable
fun HiddenAppsRecovery(
    secondsRemaining: Int,
    isBusy: Boolean,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onFinish: () -> Unit
) {
    DisposableEffect(Unit) { onDispose { onCancel() } }
    BackHandler(onBack = onCancel)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.hidden_recovery_warning), style = MaterialTheme.typography.bodySmall)
        Text("%d:%02d:%02d".format(secondsRemaining / 3600, (secondsRemaining / 60) % 60, secondsRemaining % 60),
            style = MaterialTheme.typography.bodyLarge)
        Button(enabled = secondsRemaining == 0 && !isBusy, onClick = onFinish) {
            Text(stringResource(R.string.hidden_recovery_finish))
        }
        Button(enabled = !isBusy, onClick = onCancel) {
            Text(stringResource(R.string.hidden_password_cancel))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HiddenAppsRecoveryPreview() {
    MaterialTheme { HiddenAppsRecovery(3600, false, onCancel = {}, onFinish = {}) }
}
