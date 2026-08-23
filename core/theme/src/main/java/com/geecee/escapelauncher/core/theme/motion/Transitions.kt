package com.geecee.escapelauncher.core.theme.motion

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

const val TRANSITION_DURATION = 500

fun enterTransition() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(TRANSITION_DURATION)
) + scaleIn(
    tween(TRANSITION_DURATION),
    initialScale = 0.9f
) togetherWith slideOutHorizontally(
    targetOffsetX = { -it / 4 },
    animationSpec = tween(TRANSITION_DURATION),
) + scaleOut(
    tween(TRANSITION_DURATION),
    targetScale = 0.9f
) + fadeOut(
    tween(TRANSITION_DURATION),
    targetAlpha = 0.1f
)

fun exitTransition() = slideInHorizontally(
    initialOffsetX = { -it / 4 },
    animationSpec = tween(TRANSITION_DURATION),
) + scaleIn(
    tween(TRANSITION_DURATION),
    initialScale = 0.9f
) togetherWith slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(TRANSITION_DURATION)
) + scaleOut(
    tween(TRANSITION_DURATION),
    targetScale = 0.9f
) + fadeOut(
    tween(TRANSITION_DURATION),
    targetAlpha = 0.1f
)