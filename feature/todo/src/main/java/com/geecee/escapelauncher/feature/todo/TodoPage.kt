package com.geecee.escapelauncher.feature.todo

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.model.TodoSyncStatus
import com.geecee.escapelauncher.core.model.TodoTask
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val PageHorizontalPadding = 30.dp
private val StepIndent = 36.dp
private val TaskBoxSize = 20.dp
private val StepBoxSize = 15.dp
private const val DoneAlpha = 0.4f
private const val MutedAlpha = 0.5f

/**
 * The tasks page of the home pager: a Todoist project rendered in the launcher's own typography.
 *
 * Type into the line at the top to add a task. Tap a task to complete it, tap × to delete it,
 * long-press for steps and renaming. Sync runs whenever the page is shown and once a minute
 * while it stays on screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoPage(
    isBeingShown: Boolean,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hapticFeedbackEnabled by viewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var addingStepUnder by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<TodoTask?>(null) }
    var sheetTask by remember { mutableStateOf<TodoTask?>(null) }

    LaunchedEffect(isBeingShown) {
        if (!isBeingShown) {
            viewModel.onPageHidden()
            addingStepUnder = null
            editing = null
            focusManager.clearFocus()
            return@LaunchedEffect
        }
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                viewModel.onPageShown()
                delay(60.seconds)
            }
        }
    }

    val hasDone = uiState.items.any { it.task.checked }
    val primary = MaterialTheme.colorScheme.primary
    val statusText = statusText(uiState.syncStatus)

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(PageHorizontalPadding, 0.dp)
        ) {
            item(key = "top") { Spacer(Modifier.height(90.dp)) }

            item(key = "title") {
                Text(
                    text = stringResource(R.string.tasks),
                    color = primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            statusText?.let { status ->
                item(key = "status") {
                    Text(
                        text = status,
                        color = primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .alpha(MutedAlpha)
                    )
                }
            }

            item(key = "gap") { Spacer(Modifier.height(40.dp)) }

            item(key = "input") {
                FocusLine(onAdd = { viewModel.add(it) })
                Spacer(Modifier.height(14.dp))
            }

            uiState.items.forEach { item ->
                item(key = item.task.id) {
                    if (editing?.id == item.task.id) {
                        InlineTaskField(
                            initial = item.task.content,
                            placeholder = "",
                            textStyle = MaterialTheme.typography.bodyMedium,
                            boxSize = TaskBoxSize,
                            indent = 0.dp,
                            onDone = { text ->
                                viewModel.rename(item.task, text)
                                editing = null
                            },
                            onDismiss = { editing = null }
                        )
                    } else {
                        TaskRow(
                            task = item.task,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            boxSize = TaskBoxSize,
                            indent = 0.dp,
                            onClick = {
                                viewModel.toggle(item.task)
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            },
                            onLongClick = {
                                sheetTask = item.task
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            },
                            onDelete = { viewModel.delete(item.task) }
                        )
                    }
                }

                item.steps.forEach { step ->
                    item(key = step.id) {
                        if (editing?.id == step.id) {
                            InlineTaskField(
                                initial = step.content,
                                placeholder = "",
                                textStyle = MaterialTheme.typography.bodySmall,
                                boxSize = StepBoxSize,
                                indent = StepIndent,
                                onDone = { text ->
                                    viewModel.rename(step, text)
                                    editing = null
                                },
                                onDismiss = { editing = null }
                            )
                        } else {
                            TaskRow(
                                task = step,
                                textStyle = MaterialTheme.typography.bodySmall,
                                boxSize = StepBoxSize,
                                indent = StepIndent,
                                onClick = {
                                    viewModel.toggle(step)
                                    doHapticFeedBack(haptics, hapticFeedbackEnabled)
                                },
                                onLongClick = {
                                    sheetTask = step
                                    doHapticFeedBack(haptics, hapticFeedbackEnabled)
                                },
                                onDelete = { viewModel.delete(step) }
                            )
                        }
                    }
                }

                if (addingStepUnder == item.task.id) {
                    item(key = "add-step-${item.task.id}") {
                        InlineTaskField(
                            initial = "",
                            placeholder = stringResource(R.string.new_step),
                            textStyle = MaterialTheme.typography.bodySmall,
                            boxSize = StepBoxSize,
                            indent = StepIndent,
                            onDone = { text -> viewModel.add(text, parentId = item.task.id) },
                            onDismiss = { addingStepUnder = null }
                        )
                    }
                }
            }

            item(key = "bottom") { Spacer(Modifier.height(30.dp)) }
        }

        // Footer, pinned bottom-left like the reference design.
        Box(
            Modifier
                .fillMaxWidth()
                .padding(PageHorizontalPadding, 0.dp, PageHorizontalPadding, 60.dp)
                .height(30.dp)
        ) {
            if (hasDone) {
                Text(
                    text = stringResource(R.string.clear_done),
                    color = primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { viewModel.clearDone() }
                        .alpha(MutedAlpha)
                        .padding(vertical = 4.dp)
                )
            }
        }
    }

    sheetTask?.let { task ->
        TaskActionsSheet(
            task = task,
            connected = uiState.connected,
            onDismiss = { sheetTask = null },
            onAddStep = {
                addingStepUnder = task.id
                sheetTask = null
            },
            onRename = {
                editing = task
                sheetTask = null
            },
            onOpenInTodoist = {
                val intent = Intent(Intent.ACTION_VIEW, "https://app.todoist.com/app/task/${task.id}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(intent) }
                sheetTask = null
            }
        )
    }
}

/** The always-present entry line at the top: type, press done, and the task is added. */
@Composable
private fun FocusLine(onAdd: (String) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    var text by remember { mutableStateOf("") }
    val style = MaterialTheme.typography.bodySmall

    Column(Modifier.fillMaxWidth()) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = style.copy(color = primary),
            cursorBrush = SolidColor(primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                val value = text.trim()
                if (value.isNotEmpty()) {
                    onAdd(value)
                    text = ""
                }
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.main_focus_prompt),
                            color = primary,
                            style = style,
                            modifier = Modifier.alpha(MutedAlpha)
                        )
                    }
                    inner()
                }
            }
        )
        HorizontalDivider(thickness = 1.dp, color = primary.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskRow(
    task: TodoTask,
    textStyle: TextStyle,
    boxSize: Dp,
    indent: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(vertical = 10.dp)
                .alpha(if (task.checked) DoneAlpha else 1f)
        ) {
            CheckBox(checked = task.checked, size = boxSize, color = primary)
            Spacer(Modifier.width(16.dp))
            Text(
                text = task.content,
                color = primary,
                style = textStyle,
                textDecoration = if (task.checked) TextDecoration.LineThrough else null
            )
        }
        Text(
            text = "×",
            color = primary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .clickable(onClick = onDelete)
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .alpha(MutedAlpha)
        )
    }
}

/** A thin square that fills in when the task is done. */
@Composable
private fun CheckBox(checked: Boolean, size: Dp, color: Color, modifier: Modifier = Modifier) {
    val fill by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "boxFill"
    )
    Canvas(modifier.size(size)) {
        val stroke = 1.5.dp.toPx()
        val radius = CornerRadius(2.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(this.size.width - stroke, this.size.height - stroke),
            cornerRadius = radius,
            style = Stroke(width = stroke)
        )
        if (fill > 0f) {
            val inset = stroke * 2.5f
            val inner = (this.size.width - inset * 2f) * fill
            val offset = (this.size.width - inner) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(offset, offset),
                size = Size(inner, inner),
                cornerRadius = CornerRadius(1.dp.toPx())
            )
        }
    }
}

/**
 * A text field drawn exactly like a task row so renaming and adding steps happen in place.
 * Done submits; an empty submit, back, or losing focus while empty dismisses it.
 */
@Composable
private fun InlineTaskField(
    initial: String,
    placeholder: String,
    textStyle: TextStyle,
    boxSize: Dp,
    indent: Dp,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    var text by remember { mutableStateOf(initial) }
    var hadFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BackHandler { onDismiss() }

    fun submit() {
        val value = text.trim()
        if (value.isEmpty()) {
            onDismiss()
        } else {
            onDone(value)
            text = ""
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent, top = 10.dp, bottom = 10.dp)
    ) {
        CheckBox(checked = false, size = boxSize, color = primary, modifier = Modifier.alpha(MutedAlpha))
        Spacer(Modifier.width(16.dp))
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = textStyle.copy(color = primary),
            cursorBrush = SolidColor(primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) hadFocus = true
                    else if (hadFocus && text.isBlank()) onDismiss()
                },
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = primary,
                            style = textStyle,
                            modifier = Modifier.alpha(MutedAlpha)
                        )
                    }
                    inner()
                }
            }
        )
    }
}

/**
 * Nothing is shown while everything works, and nothing at all for a local-only list. The line
 * only appears when a connected list is offline with unsent edits, or Todoist refused the token.
 */
@Composable
private fun statusText(status: TodoSyncStatus): String? {
    val pendingText = if (status.pendingCount > 0) stringResource(R.string.sync_pending, status.pendingCount) else null
    return when (status.state) {
        TodoSyncStatus.State.LOCAL -> null
        TodoSyncStatus.State.OFFLINE -> listOfNotNull(stringResource(R.string.sync_offline), pendingText).joinToString(" · ")
        TodoSyncStatus.State.AUTH_ERROR -> stringResource(R.string.sync_auth_error)
        TodoSyncStatus.State.ERROR -> listOfNotNull(stringResource(R.string.sync_error), pendingText).joinToString(" · ")
        TodoSyncStatus.State.SYNCING, TodoSyncStatus.State.IDLE -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskActionsSheet(
    task: TodoTask,
    connected: Boolean,
    onDismiss: () -> Unit,
    onAddStep: () -> Unit,
    onRename: () -> Unit,
    onOpenInTodoist: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(25.dp, 10.dp, 25.dp, 30.dp)
        ) {
            Text(
                text = task.content,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider(Modifier.padding(vertical = 15.dp))

            if (task.parentId == null) {
                SheetAction(stringResource(R.string.add_step), onAddStep)
            }
            SheetAction(stringResource(R.string.rename), onRename)
            // Tasks created offline still carry a local UUID; Todoist can't open those yet.
            if (connected && '-' !in task.id) {
                SheetAction(stringResource(R.string.open_in_todoist), onOpenInTodoist)
            }
        }
    }
}

@Composable
private fun SheetAction(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}
