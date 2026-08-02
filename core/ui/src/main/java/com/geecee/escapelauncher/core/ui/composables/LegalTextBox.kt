package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview

@Composable
fun LegalTextBox(
    modifier: Modifier = Modifier, text: String = ""
) {
    Column(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(
                    size = 24.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        BasicText(
            text = text, style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Justify, fontWeight = FontWeight.Normal
            ), modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp)
        )
    }
}

@Preview
@Composable
fun PrevLegalTextBox() {
    EscapeThemePreview {
        LegalTextBox(
            text = "Text text text text text blah"
        )
    }
}