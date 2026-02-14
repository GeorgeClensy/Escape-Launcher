package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.theme.BackgroundColor
import com.geecee.escapelauncher.core.ui.theme.primaryContentColor

/**
 * Search Bar for apps list that collapses into a pill
 */
@Composable
fun AnimatedPillSearchBar(
    closedText: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialText: String = "",
    autoFocus: Boolean = false
) {
    var searchText by remember { mutableStateOf(TextFieldValue(initialText)) }

    // Animation Specs
    val width by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else 150.dp,
        label = "widthAnimation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        label = "alphaAnimation"
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
        color = primaryContentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = BackgroundColor,
                modifier = Modifier.size(24.dp)
            )

            if (!isExpanded) {
                Text(
                    text = closedText,
                    color = BackgroundColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        onSearchTextChanged(it.text)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .alpha(alpha)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        onSearchDone(searchText.text.trim())
                    }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = BackgroundColor
                    ),
                    cursorBrush = SolidColor(BackgroundColor)
                )
            }
        }
    }
}