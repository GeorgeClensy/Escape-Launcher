package com.geecee.escapelauncher.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CardContainerColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh // Used for items on the background like the settings boxes
val CardContainerColorDisabled: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
val ContentColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface // Content in containers
val ContentColorDisabled: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val BackgroundColor: Color @Composable get() = MaterialTheme.colorScheme.surface // Main app background
val ErrorContainerColor: Color @Composable get() = MaterialTheme.colorScheme.error
val ErrorContentColor: Color @Composable get() = MaterialTheme.colorScheme.onError
val primaryContentColor: Color @Composable get() = MaterialTheme.colorScheme.primary // Primary content, search bar, home screen items, use sparingly
val SecondaryCardContainerColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer // If there needs to be a box on top of another box, try to avoid
