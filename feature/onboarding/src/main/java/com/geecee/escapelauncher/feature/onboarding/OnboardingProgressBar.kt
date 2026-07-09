package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.CardContainerColor
import com.geecee.escapelauncher.core.theme.primaryContentColor

@Composable
fun OnboardingProgressBar(
    currentPage: Int,
    totalPages: Int,
    isScrollInProgress: Boolean,
    targetPage: Int,
    modifier: Modifier = Modifier
) {
    // Progress Bar animations
    val targetProgress by remember(currentPage, totalPages, isScrollInProgress, targetPage) {
        derivedStateOf {
            if (totalPages <= 1) 0f
            else {
                val page = if (isScrollInProgress) targetPage else currentPage
                page.toFloat() / (totalPages - 1)
            }
        }
    }

    val progressAnimationSpec = remember(targetPage, totalPages) {
        if (targetPage == 0 || targetPage == totalPages - 1) {
            // Smooth, non-bouncy transition for the start and end bits
            tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
        } else {
            // Elastic bounce for the middle pages
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = progressAnimationSpec,
        label = "BouncyProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .padding(start = 30.dp, end = 30.dp)
    ) {
        AnimatedVisibility(
            visible = currentPage != 0 && currentPage != totalPages - 1,
            enter = fadeIn(),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 100, easing = LinearEasing)
            )
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                color = primaryContentColor,
                trackColor = CardContainerColor
            )
        }
    }
}