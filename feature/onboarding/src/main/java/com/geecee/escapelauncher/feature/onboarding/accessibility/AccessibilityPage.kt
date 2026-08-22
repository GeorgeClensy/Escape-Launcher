package com.geecee.escapelauncher.feature.onboarding.accessibility

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem

@Composable
fun AccessibilityPage(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.accessibility_title),
                Modifier,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(10.dp))

            Text(
                stringResource(R.string.accessibility_description),
                Modifier,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(20.dp))

            SettingsNavigationItem(
                label = stringResource(R.string.grant_accessibility), onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }, diagonalArrow = true, isTopOfGroup = true, isBottomOfGroup = true
            )

            Spacer(Modifier.height(50.dp))
        }
    }
}

@Preview
@Composable
fun PrevAccessibilityPage() {
    EscapeThemePreview {
        Box(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
        ) {
            AccessibilityPage()
        }
    }
}