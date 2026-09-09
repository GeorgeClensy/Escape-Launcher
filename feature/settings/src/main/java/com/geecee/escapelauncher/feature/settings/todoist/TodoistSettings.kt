package com.geecee.escapelauncher.feature.settings.todoist

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.model.TodoProject
import com.geecee.escapelauncher.core.model.TodoSyncStatus
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.EscapeSubhead
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer

/**
 * Settings page for the Todoist connection: API token, first sync, project choice.
 */
@Composable
fun TodoistSettings(
    goBack: () -> Unit,
    viewModel: TodoistSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val connecting by viewModel.connecting.collectAsState()
    val focusManager = LocalFocusManager.current

    var token by rememberSaveable { mutableStateOf("") }
    var showProjectPicker by remember { mutableStateOf(false) }

    // Prefill with the saved token once it arrives, but never overwrite what the user is typing.
    LaunchedEffect(uiState.savedToken) {
        if (token.isEmpty()) token = uiState.savedToken
    }

    val connect = {
        focusManager.clearFocus()
        viewModel.connect(token)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item { EscapeHeader(goBack, stringResource(R.string.todoist)) }

            item {
                Text(
                    text = stringResource(R.string.todoist_token_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .alpha(0.7f)
                        .padding(bottom = 12.dp)
                )
            }

            item {
                TokenField(
                    value = token,
                    onValueChange = { token = it },
                    onDone = connect
                )
            }

            item {
                SettingsButton(
                    label = if (connecting) stringResource(R.string.sync_syncing) else stringResource(R.string.connect),
                    isBottomOfGroup = true,
                    isDisabled = connecting || token.isBlank(),
                    onClick = { if (!connecting && token.isNotBlank()) connect() }
                )
            }

            item {
                Text(
                    text = statusText(uiState.syncStatus),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .alpha(0.7f)
                        .padding(top = 10.dp)
                )
            }

            if (uiState.savedToken.isNotEmpty()) {
                item { EscapeSubhead(stringResource(R.string.project)) }
                item {
                    SettingsNavigationItem(
                        label = uiState.projectName.ifEmpty { stringResource(R.string.choose_project) },
                        diagonalArrow = false,
                        isTopOfGroup = true,
                        isBottomOfGroup = true,
                        onClick = { showProjectPicker = true }
                    )
                }

                item { SettingsSpacer() }
                item {
                    SettingsButton(
                        label = stringResource(R.string.disconnect),
                        isTopOfGroup = true,
                        isBottomOfGroup = true,
                        onClick = {
                            token = ""
                            viewModel.disconnect()
                        }
                    )
                }
            }

            item { SettingsSpacer() }
            item { SettingsSpacer() }
        }
    }

    if (showProjectPicker) {
        ProjectPicker(
            projects = uiState.projects,
            selectedName = uiState.projectName,
            onProjectSelected = {
                viewModel.selectProject(it)
                showProjectPicker = false
            },
            onDismiss = { showProjectPicker = false }
        )
    }
}

@Composable
private fun TokenField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    Card(
        modifier = Modifier.padding(vertical = 1.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = contentColor
        )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = contentColor),
            cursorBrush = SolidColor(contentColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.todoist_api_token),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor,
                            modifier = Modifier.alpha(0.5f)
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun statusText(status: TodoSyncStatus): String = when (status.state) {
    TodoSyncStatus.State.NOT_CONFIGURED -> ""
    TodoSyncStatus.State.SYNCING -> stringResource(R.string.sync_syncing)
    TodoSyncStatus.State.IDLE -> stringResource(R.string.sync_connected)
    TodoSyncStatus.State.OFFLINE -> stringResource(R.string.sync_offline)
    TodoSyncStatus.State.AUTH_ERROR -> stringResource(R.string.sync_auth_error)
    TodoSyncStatus.State.ERROR -> listOfNotNull(stringResource(R.string.sync_error), status.message).joinToString(": ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectPicker(
    projects: List<TodoProject>,
    selectedName: String,
    onProjectSelected: (TodoProject) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { onProjectSelected(project) })
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.alpha(if (project.name == selectedName) 1f else 0.6f)
                        )
                    }
                }
            }
        }
    }
}
