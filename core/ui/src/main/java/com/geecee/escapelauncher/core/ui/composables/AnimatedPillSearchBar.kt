package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * Search Bar for apps list that collapses into a pill
 */
@Composable
fun AnimatedPillSearchBar(
    searchText: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: (String, SoftwareKeyboardController?) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(searchText)) }

    // Sync internal state with external searchText
    LaunchedEffect(searchText) {
        if (searchText != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = searchText)
        }
    }

    // Animation Specs
    val width by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else 56.dp, label = "widthAnimation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f, label = "alphaAnimation"
    )

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle Auto-focus and Expansion changes
    LaunchedEffect(isExpanded, autoFocus) {
        if (isExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    Surface(
        modifier = modifier
            .width(width)
            .height(56.dp)
            .clickable { onExpandedChange(!isExpanded) },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.secondary
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
            )

            if (isExpanded) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        onSearchTextChanged(it.text)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 48.dp, end = 16.dp)
                        .alpha(alpha)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onSearchDone(textFieldValue.text.trim(), keyboardController)
                    }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.surface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondary),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}