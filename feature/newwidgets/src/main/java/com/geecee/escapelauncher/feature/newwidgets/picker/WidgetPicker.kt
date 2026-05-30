package com.geecee.escapelauncher.feature.newwidgets.picker

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.geecee.escapelauncher.core.analytics.analyticsProxy
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.ContentColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A widget picker
 *
 * @author George Clensy
 * @param onWidgetSelected Unit for when a widget is selected
 * @param onDismiss Unit for when the picker is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWidgetPicker(
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var widgetProviders by remember { mutableStateOf<Map<WidgetAppData, List<WidgetInfo>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load widget providers grouped by app asynchronously
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val providers = loadWidgetsGroupedByApp(context)
            withContext(Dispatchers.Main) {
                widgetProviders = providers
                isLoading = false
            }
        }
    }

    // Sort the widget providers by app name
    val sortedWidgetEntries = remember(widgetProviders) {
        widgetProviders.entries.sortedBy { it.key.appName }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator(
                        color = primaryContentColor
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        sortedWidgetEntries.size,
                        key = { index -> sortedWidgetEntries[index].key.packageName }) { index ->
                        val (appInfo, widgets) = sortedWidgetEntries[index]
                        WidgetAppItem(
                            widgetAppData = appInfo,
                            widgets = widgets,
                            onWidgetSelected = onWidgetSelected
                        )
                    }
                }
            }
        }
    }
}

/**
 * A collapsable section of the Wiget Picker to list widgets from a specific app
 *
 * @author George Clensy
 * @param widgetAppData The app
 * @param widgets A list of the apps widgets
 * @param onWidgetSelected Unit for when a widget is selected
 */
@Composable
fun WidgetAppItem(
    widgetAppData: WidgetAppData,
    widgets: List<WidgetInfo>,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f) // The rotation of the arrow

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // App header with icon, name, count, and expand button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // App Icon
                    val appIcon = remember(widgetAppData.icon) {
                        try {
                            widgetAppData.icon?.toBitmap()?.asImageBitmap()
                        } catch (e: Exception) {
                            analyticsProxy.logCustomKey(
                                "Widget Picker App Icon loading failed: ",
                                widgetAppData.packageName
                            )
                            analyticsProxy.recordException(e)
                            null
                        }
                    }

                    if (appIcon != null) {
                        Image(
                            bitmap = appIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BackgroundColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = ContentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // App name and widget count
                    Column {
                        Text(
                            text = widgetAppData.appName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryContentColor
                        )
                        Text(
                            text = "${widgets.size} ${if (widgets.size == 1) "widget" else "widgets"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ContentColor
                        )
                    }
                }

                // Expand/collapse icon
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationState),
                    tint = ContentColor
                )
            }

            // Widget previews when expanded
            AnimatedVisibility(visible = expanded) {
                if (widgets.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        widgets.forEach { widget ->
                            WidgetPreview(
                                widget = widget,
                                onClick = { onWidgetSelected(widget.provider) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A preview of a widget to be shown in the picker
 *
 * @author George Clensy
 * @param widget Widget information to display
 * @param onClick Unit ran when widget is clicked
 */
@Composable
fun WidgetPreview(
    widget: WidgetInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = BackgroundColor.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Load widget preview image asynchronously
            var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
            var imageLoading by remember { mutableStateOf(true) }

            LaunchedEffect(widget.provider) {
                withContext(Dispatchers.IO) {
                    try {
                        val drawable = widget.provider.loadPreviewImage(context, 0)
                        val bitmap = drawable?.toBitmap()?.asImageBitmap()
                        withContext(Dispatchers.Main) {
                            imageBitmap = bitmap
                            imageLoading = false
                        }
                    } catch (e: Exception) {
                        analyticsProxy.logCustomKey(
                            "Widget Picker Preview failed: ",
                            widget.label + " from app" + widget.provider
                        )
                        analyticsProxy.recordException(e)
                        withContext(Dispatchers.Main) {
                            imageLoading = false
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = ContentColor.copy(alpha = 0.3f)
                    )
                } else if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = ContentColor.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No preview available",
                            style = MaterialTheme.typography.labelSmall,
                            color = ContentColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Widget name
            Text(
                text = widget.label,
                style = MaterialTheme.typography.bodyMedium,
                color = primaryContentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
