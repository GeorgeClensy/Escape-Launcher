package com.geecee.escapelauncher.feature.onboarding.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer

@Composable
fun StatisticsPage() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.most_people_waste))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(" 9 ")
                    }
                    append(stringResource(R.string.hours_every_day))
                },
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )

            SettingsSpacer()

            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.thats))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(" 3 ")
                    }
                    append(stringResource(R.string.every_week))
                },
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.End
            )

            SettingsSpacer()

            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.adds_to))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(" 32 ")
                    }
                    append(stringResource(R.string.years_straight))
                },
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )
        }
    }
}
