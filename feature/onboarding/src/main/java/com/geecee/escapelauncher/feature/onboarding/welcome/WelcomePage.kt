package com.geecee.escapelauncher.feature.onboarding.welcome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor

@Composable
fun WelcomePage() {
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
                painterResource(R.drawable.launcher_logo_icon),
                "Escape Launcher Logo",
                Modifier
                    .padding(3.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        BlurryCircle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -(100).dp, y = 100.dp),
            circleColor = MaterialTheme.colorScheme.primary.toAndroidColor()
        )

        BlurryCircle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 200.dp),
            circleColor = MaterialTheme.colorScheme.tertiary.toAndroidColor()
        )
    }
}

@Preview
@Composable
fun PrevWelcomePage() {
    EscapeThemePreview {
        WelcomePage()
    }
}
