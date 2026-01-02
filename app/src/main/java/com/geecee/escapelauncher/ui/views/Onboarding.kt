package com.geecee.escapelauncher.ui.views

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.geecee.escapelauncher.BuildConfig
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.composables.AutoResizingText
import com.geecee.escapelauncher.ui.composables.BulkAppManager
import com.geecee.escapelauncher.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.ui.theme.BackgroundColor
import com.geecee.escapelauncher.ui.theme.CardContainerColor
import com.geecee.escapelauncher.ui.theme.primaryContentColor
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.configureAnalytics
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.isDefaultLauncher
import com.geecee.escapelauncher.utils.setBooleanSetting
import com.geecee.escapelauncher.utils.showLauncherSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val route: String, val content: @Composable (onNext: () -> Unit, onPrev: () -> Unit) -> Unit
)

@Composable
fun Onboarding(
    mainAppNavController: NavHostController,
    mainAppViewModel: MainAppViewModel,
    homeScreenModel: HomeScreenModel,
    activity: Activity
) {
    @Suppress("KotlinConstantConditions") val pages =
        listOfNotNull(
            OnboardingPage("welcome") { onNext, onPrev ->
                WelcomeScreen(
                    onPrev = {
                        onPrev()
                    },
                    onNext = {
                        onNext()
                    }
                )
            },
            OnboardingPage("stats") { onNext, onPrev ->
                StatisticsScreen(
                    onPrev = {
                        onPrev()
                    },
                    onNext = {
                        onNext()
                    }
                )
            },
            OnboardingPage("favorites") { onNext, onPrev ->
                FavoritesSelectionScreen(
                    mainAppViewModel,
                    homeScreenModel,
                    onPrev = {
                        onPrev()
                    },
                    onNext = {
                        onNext()
                    }
                )
            },
            OnboardingPage("default_launcher") { onNext, onPrev ->
                DefaultLauncherScreen(
                    activity,
                    onPrev = {
                        onPrev()
                    },
                    onNext = {
                        onNext()
                    }
                )
            },
            if (!BuildConfig.IS_FOSS) {
                OnboardingPage("analytics") { onNext, onPrev ->
                    AnalyticsConsentScreen(
                        mainAppViewModel,
                        onPrev = {
                            onPrev()
                        },
                        onNext = {
                            onNext()
                        }
                    )
                }
            } else {
                null
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                OnboardingPage("accessibility") { onNext, onPrev ->
                    AccessibilitySetupScreen(
                        mainAppViewModel,
                        onPrev = {
                            onPrev()
                        },
                        onNext = {
                            onNext()
                        }
                    )
                }
            } else {
                null
            }
        )

    val coroutineScope = rememberCoroutineScope()

    // So you go back to the right bit after changing launcher
    val startIndex = remember {
        if (
            getBooleanSetting(
                mainAppViewModel.getContext(),
                mainAppViewModel.getContext().getString(R.string.WasChangingLauncher),
                false
            )
        ) {
            pages.indexOfFirst { it.route == "default_launcher" }.coerceAtLeast(0)
        } else 0
    }

    val pagerState = rememberPagerState (
        initialPage = startIndex,
        pageCount = { pages.size }
    )

    // Progress follows pager motion
    val progress by remember {
        derivedStateOf {
            if (pages.size <= 1) 0f
            else {
                (
                        pagerState.currentPage +
                                pagerState.currentPageOffsetFraction
                        ).coerceIn(0f, pages.lastIndex.toFloat()) /
                        pages.lastIndex
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout)
            .padding(start = 0.dp, end = 0.dp, top = 30.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(start = 30.dp, end = 30.dp)
        ) {
            @Suppress("RemoveRedundantQualifierName")
            androidx.compose.animation.AnimatedVisibility (
                pagerState.currentPage != 0,
                enter = fadeIn(),
                exit = fadeOut()
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

        // Pager content
        HorizontalPager (
            state = pagerState,
            modifier = Modifier.weight(1f).graphicsLayer(),
            userScrollEnabled = false,
            beyondViewportPageCount = 1
        ) { pageIndex ->

            val page = pages[pageIndex]

            page.content(
                {
                    if (pageIndex < pages.lastIndex) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                pageIndex + 1,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    } else {
                        // Finished onboarding
                        mainAppNavController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                            launchSingleTop = true
                        }

                        setBooleanSetting(
                            mainAppViewModel.getContext(),
                            mainAppViewModel.getContext()
                                .resources.getString(R.string.FirstTime),
                            false
                        )
                        setBooleanSetting(
                            mainAppViewModel.getContext(),
                            mainAppViewModel.getContext()
                                .resources.getString(R.string.WasChangingLauncher),
                            false
                        )

                        mainAppViewModel.getWindow()?.let {
                            AppUtils.configureFullScreenMode(it)
                        }
                    }
                },
                {
                    if (pageIndex > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                pageIndex - 1,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PrevButton(
    modifier: Modifier = Modifier,
    onPrev: () -> Unit,
) {
    IconButton(
        onClick = {
            onPrev()
        }, modifier = modifier.padding(bottom = 30.dp).offset(x = (-4).dp), colors = IconButtonColors(
            containerColor = primaryContentColor,
            contentColor = BackgroundColor,
            disabledContainerColor = primaryContentColor,
            disabledContentColor = BackgroundColor
        )
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)
        )
    }
}

@Composable
fun NextButton(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.continue_str),
    outline: Boolean = false,
    onNext: () -> Unit
) {
    Button(
        onClick = {
            onNext()
        },
        modifier = modifier.padding(bottom = 30.dp),
        border = if (outline) BorderStroke(1.dp, primaryContentColor) else null,
        colors = ButtonColors(
            containerColor = if (outline) Color.Transparent else primaryContentColor,
            contentColor = if (outline) primaryContentColor else BackgroundColor,
            disabledContainerColor = if (outline) Color.Transparent else primaryContentColor,
            disabledContentColor = if (outline) primaryContentColor else BackgroundColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text, maxLines = 1, // Prevent overflow
                overflow = TextOverflow.Ellipsis // Gracefully handle long text
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Continue"
            )
        }
    }
}

@Composable
fun WelcomeScreen(onNext: () -> Unit, @Suppress("unused", "RedundantSuppression") onPrev: () -> Unit) {
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

@Composable
fun StatisticsScreen(onNext: () -> Unit, onPrev: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(start = 30.dp, end = 30.dp)) {
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

        PrevButton(
            Modifier
                .align(Alignment.BottomStart)
        ) {
            onPrev()
        }

        NextButton(
            Modifier
                .align(Alignment.BottomEnd)
        ) {
            onNext()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesSelectionScreen(
    mainAppModel: MainAppViewModel,
    homeScreenModel: HomeScreenModel,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    // Add a small delay before rendering the full list to prevent jank during page transition
    var showList by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        showList = true
    }

    Box(Modifier.fillMaxSize().padding(start = 30.dp, end = 30.dp)) {
        if (showList) {
            BulkAppManager(
                apps = homeScreenModel.installedApps,
                preSelectedApps = homeScreenModel.favoriteApps,
                title = stringResource(R.string.choose_your_favourite_apps),
                reorderable = true,
                onAppMoved = { fromIndex, toIndex ->
                    mainAppModel.favoriteAppsManager.reorderFavoriteApps(fromIndex, toIndex)
                    homeScreenModel.reloadFavouriteApps()
                },
                onBackClicked = { },
                hideTitle = false,
                hideBack = true,
                topPadding = false,
                titleColor = primaryContentColor,
                onAppClicked = { app, selected ->
                    if (selected) {
                        mainAppModel.favoriteAppsManager.removeFavoriteApp(app.packageName)
                        homeScreenModel.reloadFavouriteApps()
                    } else {
                        mainAppModel.favoriteAppsManager.addFavoriteApp(app.packageName)
                        homeScreenModel.reloadFavouriteApps()
                    }
                })
        }

        PrevButton(
            Modifier
                .align(Alignment.BottomStart)
        ) {
            onPrev()
        }

        NextButton(
            Modifier
                .align(Alignment.BottomEnd)
        ) {
            onNext()
        }
    }
}

@Composable
fun DefaultLauncherScreen(
    activity: Activity, onNext: () -> Unit, onPrev: () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(start = 30.dp, end = 30.dp)) {
        Column {
            Text(
                stringResource(R.string.set_escape),
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(5.dp))
            Text(
                stringResource(R.string.stop_going_back),
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(10.dp))
            if (!isDefaultLauncher(activity)) {
                Button(
                    onClick = {
                        setBooleanSetting(
                            activity,
                            activity.resources.getString(R.string.WasChangingLauncher),
                            true
                        )
                        activity.showLauncherSelector()
                    }, modifier = Modifier, colors = ButtonColors(
                        primaryContentColor, BackgroundColor, primaryContentColor, BackgroundColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(R.string.set_launcher))
                    }
                }
            } else {
                Button(
                    onClick = {}, modifier = Modifier.border(
                        1.dp, primaryContentColor, MaterialTheme.shapes.extraLarge
                    ), colors = ButtonColors(
                        Color.Transparent,
                        primaryContentColor,
                        Color.Transparent,
                        primaryContentColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, "")
                        Text(text = stringResource(R.string.already_default))
                    }
                }
            }
            Spacer(Modifier.height(240.dp))
        }

        PrevButton(
            Modifier
                .align(Alignment.BottomStart)
        ) {
            onPrev()
        }

        NextButton(
            Modifier
                .align(Alignment.BottomEnd)
        ) {
            onNext()
        }
    }
}

@Composable
fun AnalyticsConsentScreen(
    mainAppModel: MainAppViewModel, onNext: () -> Unit, onPrev: () -> Unit
) {
    val showPolicyDialog = remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    Box(Modifier.fillMaxSize().padding(start = 30.dp, end = 30.dp)) {
        LazyColumn(
            state = scrollState
        ) {
            item {
                Text(
                    stringResource(R.string.analytics_and_data_collection),
                    Modifier,
                    primaryContentColor,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Text(
                    stringResource(R.string.anonymous_data),
                    Modifier,
                    primaryContentColor,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    lineHeight = 32.sp
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Button(
                    onClick = {
                        showPolicyDialog.value = true
                    }, modifier = Modifier, colors = ButtonColors(
                        primaryContentColor, BackgroundColor, primaryContentColor, BackgroundColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(R.string.read_privacy_policy))
                    }
                }
            }

            item {
                Spacer(Modifier.height(240.dp))
            }
        }

        PrevButton(
            Modifier
                .align(Alignment.BottomStart)
        ) {
            onPrev()
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            NextButton(
                text = stringResource(R.string.deny), outline = true
            ) {
                setBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().resources.getString(R.string.Analytics),
                    false
                )
                configureAnalytics(mainAppModel.getContext(), false)
                onNext()
            }

            Spacer(Modifier.width(15.dp))

            NextButton(text = stringResource(R.string.allow)) {
                setBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().resources.getString(R.string.Analytics),
                    true
                )
                configureAnalytics(mainAppModel.getContext(), true)
                onNext()
            }
        }
    }

    AnimatedVisibility(showPolicyDialog.value, enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.padding(bottom = 30.dp)) {
            PrivacyPolicyDialog(mainAppModel, showPolicyDialog)
        }
    }
}

@Composable
fun AccessibilitySetupScreen(
    mainAppModel: MainAppViewModel, onNext: () -> Unit, onPrev: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val context = mainAppModel.getContext()

    Box(Modifier.fillMaxSize().padding(start = 30.dp, end = 30.dp)) {
        LazyColumn(
            state = scrollState
        ) {
            item {
                Text(
                    stringResource(R.string.accessibility_title),
                    Modifier,
                    primaryContentColor,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Text(
                    stringResource(R.string.accessibility_description),
                    Modifier,
                    primaryContentColor,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    lineHeight = 32.sp
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }, modifier = Modifier, colors = ButtonColors(
                        primaryContentColor, BackgroundColor, primaryContentColor, BackgroundColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(R.string.grant_accessibility))
                    }
                }
            }

            item {
                Spacer(Modifier.height(240.dp))
            }
        }

        PrevButton(
            Modifier
                .align(Alignment.BottomStart)
        ) {
            onPrev()
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            NextButton {
                onNext()
            }
        }
    }
}
