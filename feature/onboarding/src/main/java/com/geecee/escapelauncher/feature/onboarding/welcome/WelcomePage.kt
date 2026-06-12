package com.geecee.escapelauncher.feature.onboarding.welcome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.AutoResizingText
import com.geecee.escapelauncher.feature.onboarding.NextButton

@Composable
fun WelcomePage(
    onNext: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y = (-62).dp)
        ) {
            Icon(
                painterResource(R.drawable.outlineicon),
                "Escape Launcher Icon",
                Modifier
                    .padding(3.dp)
                    .align(Alignment.CenterHorizontally),
                tint = primaryContentColor
            )
            Spacer(Modifier.height(15.dp))
            AutoResizingText(
                text = stringResource(R.string.escape_launcher),
                modifier = Modifier,
                color = primaryContentColor,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }

        NextButton(Modifier.align(Alignment.BottomEnd)) {
            onNext()
        }
    }
}
