package com.geecee.escapelauncher.feature.onboarding

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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

    val progress by remember {
        derivedStateOf {
            if (screens.size <= 1) 0f
            else {
                (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(
                    0f,
                    screens.lastIndex.toFloat()
                ) / screens.lastIndex
            }
        }
    }

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
                    pagerState.currentPage != 0, enter = fadeIn(), exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
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
            OnboardingBottomPanel(
                modifier = Modifier,
                showPrevButton = screens[pagerState.currentPage] != OnboardingScreen.WELCOME,
                onPrevButtonClick = {
                    onPrev()
                },
                onNextButtonClick = {
                    onNext()
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .graphicsLayer(),
            userScrollEnabled = false,
            beyondViewportPageCount = 1
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