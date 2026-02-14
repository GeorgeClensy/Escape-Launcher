package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.ui.theme.CardContainerColor
import com.geecee.escapelauncher.core.ui.theme.ContentColor
import com.geecee.escapelauncher.core.ui.theme.primaryContentColor


@Composable
fun FooterBox(
    text: String,
    secondText: String,
    icon: Painter,
    sponsorButtonText: String,
    onSponsorClick: () -> Unit = {},
    onBackgroundClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .fillMaxWidth()
            .clickable(onClick = {
                onBackgroundClick()
            }),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor
        )
    ) {
        Column(
            Modifier
                .padding(vertical = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                "Escape Launcher Icon",
                Modifier
                    .padding(3.dp),
                tint = ContentColor
            )

            Spacer(
                Modifier.height(10.dp)
            )

            AutoResizingText(
                text = text,
                modifier = Modifier,
                color = ContentColor,
            )

            Spacer(Modifier.height(10.dp))

            AutoResizingText(
                text = secondText,
                modifier = Modifier,
                color = ContentColor,
            )

            Spacer(Modifier.height(15.dp))

            Button(
                onClick = {
                    onSponsorClick()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContentColor,
                    contentColor = CardContainerColor
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, "", tint = CardContainerColor)
                    Spacer(Modifier.width(5.dp))
                    AutoResizingText(
                        text = sponsorButtonText,
                        color = CardContainerColor
                    )
                }
            }

        }
    }
}