package com.geecee.escapelauncher.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.ui.theme.EscapeTheme
import com.geecee.escapelauncher.ui.theme.offLightScheme
import com.geecee.escapelauncher.ui.theme.primaryContentColor
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime

// Home Screen Item

/**
 * An item displayed on the HomeScreen or Apps list
 *
 * If [showScreenTime] is enabled and [screenTime] is not null the screen time is written next to the app name.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenItem(
    modifier: Modifier = Modifier,
    appName: String,
    screenTime: Long? = null,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    showScreenTime: Boolean = false,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = when (alignment) {
            Alignment.Start -> Arrangement.Start
            Alignment.CenterHorizontally -> Arrangement.Center
            Alignment.End -> Arrangement.End
            else -> Arrangement.Center
        },
        modifier = modifier
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = onAppLongClick
            )
            .fillMaxWidth()
    ) {
        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier.padding(vertical = 15.dp),
            color = primaryContentColor,
            style = MaterialTheme.typography.bodyMedium
        )

        // Optional screen time
        if (showScreenTime && screenTime != null) {
            Text(
                formatScreenTime(screenTime),
                modifier = Modifier
                    .padding(vertical = 15.dp, horizontal = 5.dp)
                    .alpha(0.5f),
                color = primaryContentColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Preview
@Composable
fun HomeScreeItemPrev() {
    EscapeTheme(remember { mutableStateOf(offLightScheme) }) {
        HomeScreenItem(
            modifier = Modifier,
            appName = "App Name",
            screenTime = 1000,
            onAppClick = {},
            onAppLongClick = {},
            showScreenTime = false
        )
    }
}

// Bottom Sheet

/**
 * Action that can be shown in the bottom sheet
 * */
data class AppAction(
    val label: String,
    val onClick: () -> Unit
)

/**
 * Bottom Sheet home screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenBottomSheet(
    title: String,
    actions: List<AppAction>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
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
                    title,
                    color = ContentColor,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 15.dp))

            // Actions
            Column(Modifier.padding(start = 47.dp)) {
                actions.forEach { action ->
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .combinedClickable(onClick = action.onClick),
                        color = ContentColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}