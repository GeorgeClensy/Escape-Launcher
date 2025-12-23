package com.geecee.escapelauncher.ui.views

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.geecee.escapelauncher.utils.AppUtils.configureAnalytics
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.isDefaultLauncher
import com.geecee.escapelauncher.utils.setBooleanSetting
import com.geecee.escapelauncher.utils.showLauncherSelector
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

private const val SCREEN_WELCOME = "welcome"
private const val SCREEN_STATISTICS = "statistics"
private const val SCREEN_FAVORITES = "favorites"
private const val SCREEN_DEFAULT_LAUNCHER = "default_launcher"
private const val SCREEN_ANALYTICS = "analytics"
private const val SCREEN_ACCESSIBILITY = "accessibility"

@Composable
fun Onboarding(
    mainNavController: NavController,
    mainAppModel: MainAppViewModel,
    homeScreenModel: HomeScreenModel,
    activity: Activity
) {
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()
        .takeIf { it > 0.dp } ?: 50.dp

    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Define the sequence of pages, skipping analytics if it's a FOSS build
    val onboardingPages = remember {
        listOfNotNull(
            SCREEN_WELCOME,
            SCREEN_STATISTICS,
            SCREEN_FAVORITES,
            SCREEN_DEFAULT_LAUNCHER,
            if (!BuildConfig.IS_FOSS) SCREEN_ANALYTICS else null,
            SCREEN_ACCESSIBILITY
        )
    }

    var startDestination = SCREEN_WELCOME

    if (getBooleanSetting(
            mainAppModel.getContext(),
            stringResource(R.string.WasChangingLauncher),
            false
        )
    ) {
        startDestination = SCREEN_DEFAULT_LAUNCHER
    }

    // Progress bar animation based on current page index
    val progressTarget = remember(currentRoute) {
        val index = onboardingPages.indexOf(currentRoute).coerceAtLeast(0)
        (index + 1).toFloat() / onboardingPages.size
    }

    // Animate progress smoothly
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = statusBarHeight, bottom = 15.dp)
    ) {

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(start = 30.dp, end = 30.dp, top = 15.dp),
            color = primaryContentColor,
            trackColor = CardContainerColor
        )

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                SCREEN_WELCOME,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                WelcomeScreen(navController)
            }
            composable(
                SCREEN_STATISTICS,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                StatisticsScreen(navController)
            }
            composable(
                SCREEN_FAVORITES,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                FavoritesSelectionScreen(navController, mainAppModel, homeScreenModel)
            }
            composable(
                SCREEN_DEFAULT_LAUNCHER,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DefaultLauncherSetupScreen(navController, activity)
            }
            if (!BuildConfig.IS_FOSS) {
                composable(
                    SCREEN_ANALYTICS,
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    AnalyticsConsentScreen(navController, mainAppModel)
                }
            }
            composable(
                SCREEN_ACCESSIBILITY,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                AccessibilitySetupScreen(
                    mainNavController, mainAppModel
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 30.dp)
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

        Button(
            onClick = {
                navController.navigate(SCREEN_STATISTICS)
            }, modifier = Modifier.align(Alignment.BottomEnd), colors = ButtonColors(
                primaryContentColor,
                BackgroundColor,
                primaryContentColor,
                BackgroundColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_str), maxLines = 1, // Prevent overflow
                    overflow = TextOverflow.Ellipsis // Gracefully handle long text
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Continue"
                )
            }
        }
    }
}

@Composable
fun StatisticsScreen(navController: NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 30.dp)
    ) {
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

        Button(
            onClick = {
                navController.navigate(SCREEN_FAVORITES)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd),
            colors = ButtonColors(
                primaryContentColor,
                BackgroundColor,
                primaryContentColor,
                BackgroundColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_str), maxLines = 1, // Prevent overflow
                    overflow = TextOverflow.Ellipsis // Gracefully handle long text
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Continue"
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesSelectionScreen(
    navController: NavController,
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 0.dp)
    ) {
        BulkAppManager(
            apps = homeScreenModel.installedApps,
            preSelectedApps = homeScreenModel.favoriteApps,
            title = stringResource(R.string.choose_your_favourite_apps),
            reorderable = true,
            onAppMoved = { fromIndex, toIndex ->
                mainAppModel.favoriteAppsManager.reorderFavoriteApps(fromIndex, toIndex)
                homeScreenModel.reloadFavouriteApps()
            },
            onBackClicked = { navController.popBackStack() },
            hideTitle = false,
            hideBack = true,
            onAppClicked = { app, selected ->
                if (selected) {
                    mainAppModel.favoriteAppsManager.removeFavoriteApp(app.packageName)
                    homeScreenModel.reloadFavouriteApps()
                } else {
                    mainAppModel.favoriteAppsManager.addFavoriteApp(app.packageName)
                    homeScreenModel.reloadFavouriteApps()
                }
            })

        Button(
            onClick = {
                navController.navigate(SCREEN_DEFAULT_LAUNCHER)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp),
            colors = ButtonColors(
                primaryContentColor,
                BackgroundColor,
                primaryContentColor,
                BackgroundColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_str), maxLines = 1, // Prevent overflow
                    overflow = TextOverflow.Ellipsis // Gracefully handle long text
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Continue"
                )
            }
        }
    }
}

@Composable
fun DefaultLauncherSetupScreen(navController: NavController, activity: Activity) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 120.dp, 30.dp, 30.dp)
    ) {
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
                    },
                    modifier = Modifier,
                    colors = ButtonColors(
                        primaryContentColor,
                        BackgroundColor,
                        primaryContentColor,
                        BackgroundColor
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
                    onClick = {
                    },
                    modifier = Modifier.border(
                        1.dp,
                        primaryContentColor,
                        MaterialTheme.shapes.extraLarge
                    ),
                    colors = ButtonColors(
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

        Button(
            onClick = {
                val nextRoute = if (BuildConfig.IS_FOSS) SCREEN_ACCESSIBILITY else SCREEN_ANALYTICS
                navController.navigate(nextRoute)
            }, modifier = Modifier.align(Alignment.BottomEnd), colors = ButtonColors(
                primaryContentColor,
                BackgroundColor,
                primaryContentColor,
                BackgroundColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_str), maxLines = 1, // Prevent overflow
                    overflow = TextOverflow.Ellipsis // Gracefully handle long text
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Continue"
                )
            }
        }
    }
}

@Composable
fun AnalyticsConsentScreen(
    navController: NavController,
    mainAppModel: MainAppModel
) {
    val showPolicyDialog = remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 30.dp)
    ) {
        LazyColumn(
            state = scrollState
        ) {
            item {
                Spacer(Modifier.height(120.dp))
            }

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
                Spacer(Modifier.height(5.dp))
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
                        primaryContentColor,
                        BackgroundColor,
                        primaryContentColor,
                        BackgroundColor
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

        Row(modifier = Modifier.align(Alignment.BottomEnd)) {
            Button(
                onClick = {
                    navController.navigate(SCREEN_ACCESSIBILITY)
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.Analytics),
                        false
                    )
                    configureAnalytics(mainAppModel.getContext(), false)
                }, modifier = Modifier, colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BackgroundColor,
                    contentColor = primaryContentColor
                ), border = BorderStroke(1.dp, primaryContentColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.deny), maxLines = 1, // Prevent overflow
                        overflow = TextOverflow.Ellipsis // Gracefully handle long text
                    )
                }
            }

            Spacer(Modifier.width(15.dp))

            Button(
                onClick = {
                    navController.navigate(SCREEN_ACCESSIBILITY)
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.Analytics),
                        true
                    )
                    configureAnalytics(mainAppModel.getContext(), true)
                }, modifier = Modifier, colors = ButtonColors(
                    primaryContentColor,
                    BackgroundColor,
                    primaryContentColor,
                    BackgroundColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.allow), maxLines = 1, // Prevent overflow
                        overflow = TextOverflow.Ellipsis // Gracefully handle long text
                    )
                }
            }
        }
    }

    AnimatedVisibility(showPolicyDialog.value, enter = fadeIn(), exit = fadeOut()) {
        PrivacyPolicyDialog(mainAppModel, showPolicyDialog)
    }
}

@Composable
fun AccessibilitySetupScreen(
    mainNavController: NavController,
    mainAppModel: MainAppModel
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 30.dp)
    ) {
        LazyColumn(
            state = scrollState
        ) {
            item {
                Spacer(Modifier.height(120.dp))
            }

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
                Spacer(Modifier.height(5.dp))
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
                        context.startActivity(intent)
                    }, modifier = Modifier, colors = ButtonColors(
                        primaryContentColor,
                        BackgroundColor,
                        primaryContentColor,
                        BackgroundColor
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

        Row(modifier = Modifier.align(Alignment.BottomEnd)) {
            Button(
                onClick = {
                    mainNavController.navigate("home") {
                        popUpTo("onboarding") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.FirstTime),
                        false
                    )
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.WasChangingLauncher),
                        false
                    )
                }, modifier = Modifier, colors = ButtonColors(
                    primaryContentColor,
                    BackgroundColor,
                    primaryContentColor,
                    BackgroundColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.next), maxLines = 1, // Prevent overflow
                        overflow = TextOverflow.Ellipsis // Gracefully handle long text
                    )
                }
            }
        }
    }
}
