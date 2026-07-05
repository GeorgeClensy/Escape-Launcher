package com.geecee.escapelauncher.feature.onboarding

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.CardContainerColor
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.feature.onboarding.accessibility.AccessibilityPage
import com.geecee.escapelauncher.feature.onboarding.analytics.AnalyticsPage
import com.geecee.escapelauncher.feature.onboarding.favorites.FavoritesPage
import com.geecee.escapelauncher.feature.onboarding.launcher.DefaultLauncherPage
import com.geecee.escapelauncher.feature.onboarding.statistics.StatisticsPage
import com.geecee.escapelauncher.feature.onboarding.welcome.WelcomePage
import kotlinx.coroutines.launch

@Composable
fun Onboarding(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val screens = viewModel.screens
    val coroutineScope = rememberCoroutineScope()
    val isOnDefaultLauncherPage by viewModel.startFromLauncherPage.collectAsState(initial = null)
    if (isOnDefaultLauncherPage == null) return

    val pagerState = rememberPagerState(
        pageCount = { screens.size },
        initialPage = if (isOnDefaultLauncherPage == true) {
            screens.indexOf(OnboardingScreen.DEFAULT_LAUNCHER).coerceAtLeast(0)
        } else {
            0
        }
    )

    // Set StartFromLauncherPage
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            if (pagerState.currentPage != screens.indexOf(OnboardingScreen.DEFAULT_LAUNCHER)) {
                viewModel.setStartFromLauncherPage(false)
            } else {
                viewModel.setStartFromLauncherPage(true)
            }
            Log.d("Onboarding", "Page settled on ${pagerState.currentPage}")
        }
    }

    // Progress Bar animations
    val targetProgress by remember {
        derivedStateOf {
            if (screens.size <= 1) 0f
            else {
                val targetPage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
                targetPage.toFloat() / screens.lastIndex
            }
        }
    }

    val progressAnimationSpec = remember(pagerState.targetPage) {
        val targetPage = pagerState.targetPage
        if (targetPage == 0 || targetPage == screens.lastIndex) {
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

    val onNext: () -> Unit = {
        if (pagerState.currentPage < screens.lastIndex) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                    pagerState.currentPage + 1, animationSpec = tween(
                        durationMillis = 500, easing = FastOutSlowInEasing
                    )
                )
            }
        } else {
            viewModel.completeOnboarding()
            onFinished()
        }
    }

    val onPrev: () -> Unit = {
        if (pagerState.currentPage > 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                    pagerState.currentPage - 1, animationSpec = tween(
                        durationMillis = 500, easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundColor)
            .windowInsetsPadding(WindowInsets.displayCutout)
            .padding(start = 0.dp, end = 0.dp, top = 30.dp),
        topBar = {
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(start = 30.dp, end = 30.dp)
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage != 0,
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
        },
        bottomBar = {
            // Bottom Panel
            OnboardingBottomPanel(
                modifier = Modifier,
                showPrevButton = screens[pagerState.currentPage] != OnboardingScreen.WELCOME,
                useTickNextButton = pagerState.currentPage == screens.lastIndex,
                onPrevButtonClick = {
                    onPrev()
                },
                onNextButtonClick = {
                    onNext()
                }
            )
        }
    ) { innerPadding ->
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .graphicsLayer(),
            userScrollEnabled = true,
            beyondViewportPageCount = 2 // This keeps the "orbs" that are offset on the first page visible on the 2nd page cuz the first page is still loaded, I think this makes it look cleaner cuz there's no sharp edges
        ) { page ->
            val screen = screens[page]

            when (screen) {
                OnboardingScreen.WELCOME -> WelcomePage()
                OnboardingScreen.STATISTICS -> StatisticsPage()
                OnboardingScreen.FAVORITES -> FavoritesPage()
                OnboardingScreen.DEFAULT_LAUNCHER -> DefaultLauncherPage()
                OnboardingScreen.ANALYTICS -> AnalyticsPage()
                OnboardingScreen.ACCESSIBILITY -> AccessibilityPage()
            }
        }
    }
}