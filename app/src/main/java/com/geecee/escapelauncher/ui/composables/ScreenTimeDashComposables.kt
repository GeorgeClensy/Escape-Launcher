package com.geecee.escapelauncher.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.CardContainerColor
import com.geecee.escapelauncher.ui.theme.escapeGreen
import com.geecee.escapelauncher.ui.theme.escapeRed
import com.geecee.escapelauncher.ui.theme.primaryContentColor
import com.geecee.escapelauncher.ui.views.calculateOveragePercentage

/**
 * Screen time with an arrow indicating whether it's increased or decreased
 */
@Composable
fun ScreenTime(time: String, increased: Boolean, modifier: Modifier) {
    Row {
        Icon(
            Icons.Default.KeyboardArrowUp, contentDescription = "Arrow", tint = if (increased) {
                escapeRed
            } else {
                escapeGreen
            }, modifier = Modifier
                .size(45.dp)
                .align(Alignment.CenterVertically)
                .rotate(
                    if (increased) {
                        0f
                    } else {
                        180f
                    }
                )
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier,
            color = primaryContentColor,
            fontWeight = FontWeight.SemiBold
        )

    }
}

/**
 * Square shaped composable showing how many percent higher screen time is than recommended using [calculateOveragePercentage] function
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HigherRec(percent: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(48.dp))
            .aspectRatio(1f)
            .background(CardContainerColor)
    ) {
        val boxWithConstraintsScope = this
        val padding = boxWithConstraintsScope.maxWidth * 0.1f
        val titleFontSize = boxWithConstraintsScope.maxWidth * 0.3f
        val bodyFontSize = boxWithConstraintsScope.maxWidth * 0.1f

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Percent Text
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = with(LocalDensity.current) { titleFontSize.toSp() },
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (percent < 1) escapeGreen else escapeRed
            )

            // Description Text
            Text(
                text = stringResource(R.string.higher_we_rec),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = with(LocalDensity.current) { bodyFontSize.toSp() },
                    lineHeight = with(LocalDensity.current) { (bodyFontSize + 5.dp).toSp() },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryContentColor
            )
        }
    }
}

/**
 * Circular composable showing what percent of your day was spent on your phone
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DaySpent(percent: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(48.dp))
            .background(CardContainerColor)
    ) {
        val boxWithConstraintsScope = this
        val padding = boxWithConstraintsScope.maxWidth * 0.1f
        val titleFontSize = boxWithConstraintsScope.maxWidth * 0.3f
        val bodyFontSize = boxWithConstraintsScope.maxWidth * 0.08f

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Percent Text
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = with(LocalDensity.current) { titleFontSize.toSp() },
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (percent < 10) escapeGreen else escapeRed
            )

            // Description Text
            Text(
                text = stringResource(R.string.of_your_day_spent_on_your_phone),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = with(LocalDensity.current) { bodyFontSize.toSp() },
                    lineHeight = with(LocalDensity.current) { (bodyFontSize + 5.dp).toSp() },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryContentColor
            )
        }
    }
}

/**
 * Shows usage for a specific app with an arrow to whether its increased or decreased
 */
@Composable
fun AppUsage(appName: String, increased: Boolean, time: String, modifier: Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(5.dp, 5.dp)
    ) {
        Text(
            text = if (appName.length > 12) appName.take(12) + "..." else appName,
            modifier = Modifier.align(Alignment.CenterStart),
            style = MaterialTheme.typography.bodyMedium,
            color = primaryContentColor
        )

        Row(
            Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp, contentDescription = "Arrow", tint = if (increased) {
                    escapeRed
                } else {
                    escapeGreen
                }, modifier = Modifier
                    .size(45.dp)
                    .align(Alignment.CenterVertically)
                    .rotate(
                        if (increased) {
                            0f
                        } else {
                            180f
                        }
                    )
            )

            Spacer(Modifier.width(5.dp))


            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
                color = primaryContentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Box with [AppUsage]s in it
 */
@Composable
fun AppUsages(modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(48.dp))
            .background(CardContainerColor)
    ) {
        Column(
            Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}