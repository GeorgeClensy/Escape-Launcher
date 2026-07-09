package com.geecee.escapelauncher.feature.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.onboarding.accessibility.AccessibilityPage
import com.geecee.escapelauncher.feature.onboarding.analytics.AnalyticsPage
import com.geecee.escapelauncher.feature.onboarding.favorites.FavoritesPage
import com.geecee.escapelauncher.feature.onboarding.finished.FinishedPage
import com.geecee.escapelauncher.feature.onboarding.launcher.DefaultLauncherPage
import com.geecee.escapelauncher.feature.onboarding.statistics.StatisticsPage
import com.geecee.escapelauncher.feature.onboarding.welcome.WelcomePage
import kotlinx.coroutines.launch

@Composable
fun Onboarding(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val hapticFeedbackEnabled by viewModel.hapticFeedBackEnabled.collectAsState(initial = true)
    val screens = viewModel.screens
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
            viewModel.onPageSettled(pagerState.currentPage)
        }
    }

    val onNext: () -> Unit = {
        if (pagerState.currentPage < screens.lastIndex) {
            doHapticFeedBack(haptics, hapticFeedbackEnabled)

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
            doHapticFeedBack(haptics, hapticFeedbackEnabled)

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
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface
            )
            .windowInsetsPadding(
                insets = WindowInsets.displayCutout
            )
            .padding(
                start = 0.dp,
                end = 0.dp,
                top = 30.dp
            ),
        topBar = {
            OnboardingProgressBar(
                currentPage = pagerState.currentPage,
                totalPages = screens.size,
                isScrollInProgress = pagerState.isScrollInProgress,
                targetPage = pagerState.targetPage
            )
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
                OnboardingScreen.FINISHED -> FinishedPage(
                    isShown = pagerState.currentPage == screens.lastIndex
                )
            }
        }
    }
}