package com.geecee.escapelauncher.ui.views

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.BuildConfig
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
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
    val navController = rememberNavController()

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

    // Work out progress for progress bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentIndex = pages.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val progress = if (pages.size > 1) currentIndex / (pages.size - 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    // Go to the launcher page if you're coming back from that:
    var startDestination = pages[0].route
    if (getBooleanSetting(
            mainAppViewModel.getContext(), stringResource(R.string.WasChangingLauncher), false
        )
    ) {
        startDestination = "default_launcher"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout)
            .padding(start = 30.dp, end = 30.dp, top = 30.dp)
    ) {
        AnimatedVisibility(navBackStackEntry?.destination?.route != "welcome") {
            Column {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    color = primaryContentColor,
                    trackColor = CardContainerColor
                )

                Spacer(
                    Modifier.height(30.dp)
                )
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f)
        ) {
            pages.forEachIndexed { index, page ->
                composable(page.route) {
                    page.content({ // onNext
                        if (index < pages.lastIndex) {
                            navController.navigate(pages[index + 1].route)
                        } else {
                            // Finished onboarding
                            mainAppNavController.navigate("home") {
                                popUpTo("onboarding") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                            setBooleanSetting(
                                mainAppViewModel.getContext(),
                                mainAppViewModel.getContext().resources.getString(R.string.FirstTime),
                                false
                            )
                            setBooleanSetting(
                                mainAppViewModel.getContext(),
                                mainAppViewModel.getContext().resources.getString(R.string.WasChangingLauncher),
                                false
                            )
                            mainAppViewModel.getWindow()?.let { window ->
                                AppUtils.configureFullScreenMode(window = window)
                            }
                        }
                    }, {
                        // onPrev
                        if (index > 0) {
                            navController.navigate(pages[index - 1].route)
                        }
                    })
                }
            }
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
        }, modifier = modifier, colors = IconButtonColors(
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
        modifier = modifier,
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
fun WelcomeScreen(onNext: () -> Unit, @Suppress("unused") onPrev: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp)
    ) {
        Column(Modifier.align(Alignment.Center)) {
            Icon(
                painterResource(R.drawable.outlineicon),
                "Escape Launcher Icon",
                Modifier
                    .padding(3.dp)
                    .align(Alignment.CenterHorizontally),
                tint = primaryContentColor
            )
            Spacer(Modifier.height(15.dp))
            Text(
                stringResource(R.string.escape_launcher),
                Modifier,
                primaryContentColor,
                style = MaterialTheme.typography.titleSmall,
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
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            SettingsSpacer()

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
                .padding(bottom = 30.dp)
        ) {
            onPrev()
        }

        NextButton(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp)
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
    Box(Modifier.fillMaxSize()) {
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

        PrevButton(
            Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 30.dp)
        ) {
            onPrev()
        }

        NextButton(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp)
        ) {
            onNext()
        }
    }
}

@Composable
fun DefaultLauncherScreen(
    activity: Activity, onNext: () -> Unit, onPrev: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
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
                .padding(bottom = 30.dp)
        ) {
            onPrev()
        }

        NextButton(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp)
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

    Box(Modifier.fillMaxSize()) {
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
                .padding(bottom = 30.dp)
        ) {
            onPrev()
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp)
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

    Box(Modifier.fillMaxSize()) {
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
                .padding(bottom = 30.dp)
        ) {
            onPrev()
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp)
        ) {
            NextButton {
                onNext()
            }
        }
    }
}
