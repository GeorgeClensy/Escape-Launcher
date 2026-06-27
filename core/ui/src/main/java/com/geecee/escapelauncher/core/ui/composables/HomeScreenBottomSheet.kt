package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.theme.ContentColor
import com.geecee.escapelauncher.core.ui.model.AppAction

/**
 * Bottom Sheet home screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenBottomSheet(
    modifier: Modifier = Modifier,
    app: InstalledApp,
    actions: List<AppAction>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState? = null,
    shortcutActions: List<AppAction> = listOf()
) {
    val screenHeight = LocalWindowInfo.current.containerDpSize.height

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState ?: rememberBottomSheetState(initialValue = SheetValue.Hidden)
    ) {
        Column(
            modifier
                .heightIn(max = screenHeight * 0.8f)
                .fillMaxWidth()
                .padding(25.dp, 25.dp, 25.dp, 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "App Options",
                    tint = ContentColor,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    app.displayName,
                    color = ContentColor,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 15.dp))

            // Actions
            Column(Modifier.padding(start = 47.dp, bottom = 50.dp)) {
                if (shortcutActions.isNotEmpty()) {
                    shortcutActions.filter { it.isVisible(app) }.forEach { action ->
                        AppActionItem(action, app)
                    }

                    HorizontalDivider(Modifier.padding(vertical = 15.dp))
                }

                actions.filter { it.isVisible(app) }.forEach { action ->
                    AppActionItem(action, app)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppActionItem(action: AppAction, app: InstalledApp) {
    val label = action.label ?: action.labelRes?.let { stringResource(it) } ?: ""
    Text(
        text = label,
        modifier = Modifier
            .padding(vertical = 10.dp)
            .combinedClickable(onClick = { action.onClick(app) }),
        color = ContentColor,
        style = MaterialTheme.typography.bodyMedium
    )
}