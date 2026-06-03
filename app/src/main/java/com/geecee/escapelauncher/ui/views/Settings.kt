@file:Suppress("KotlinConstantConditions")

package com.geecee.escapelauncher.ui.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.BuildConfig
import com.geecee.escapelauncher.GlobalViewModel
import com.geecee.escapelauncher.feature.settings.hiddenapps.HiddenAppsViewModel
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.feature.settings.openchallenges.OpenChallengeViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.showLauncherSelector
import com.geecee.escapelauncher.core.common.showLauncherSettingsMenu
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.theme.AppColourScheme
import com.geecee.escapelauncher.core.theme.CardContainerColor
import com.geecee.escapelauncher.core.theme.ContentColor
import com.geecee.escapelauncher.core.theme.ThemeViewModel
import com.geecee.escapelauncher.core.theme.getFontFamily
import com.geecee.escapelauncher.core.theme.primaryContentColor
import com.geecee.escapelauncher.core.theme.resolveColorScheme
import com.geecee.escapelauncher.core.theme.transparentHalf
import com.geecee.escapelauncher.core.ui.composables.BulkManager
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.EscapeSubhead
import com.geecee.escapelauncher.core.ui.composables.FooterBox
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem
import com.geecee.escapelauncher.core.ui.composables.SettingsSingleChoiceSegmentedButtons
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.composables.SettingsSwipeableButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import com.geecee.escapelauncher.core.ui.composables.nameResFromId
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.settings.DevOptionsPageViewModel
import com.geecee.escapelauncher.feature.settings.MainSettingsPageViewModel
import com.geecee.escapelauncher.feature.settings.SettingsViewModel
import com.geecee.escapelauncher.feature.settings.widget.WidgetOptions
import com.geecee.escapelauncher.feature.weather.WeatherViewModel
import com.geecee.escapelauncher.core.common.loadTextFromAssets
import com.geecee.escapelauncher.core.common.setSolidColorWallpaperHomeScreen
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor
import com.geecee.escapelauncher.core.common.EscapeAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

//
// MENUS
//

/**
 * Main Settings window you see when settings is first opened
 *
 * @param mainAppModel This is needed to get packageManager, context, ect
 * @param goBack When back button is pressed
 * @param activity This is needed for some settings
 */
@Composable
fun Settings(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    goBack: () -> Unit,
    activity: Activity,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel(),
    openChallengeViewModel: OpenChallengeViewModel = hiltViewModel()
) {
    val installedApps by settingsViewModel.installedApps.collectAsState()
    val showPolicyDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp, 0.dp, 20.dp, 0.dp)
    ) {

        val navController = rememberNavController()

        NavHost(navController = navController, "mainSettingsPage") {
            composable(
                "mainSettingsPage",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                MainSettingsPage(
                    { goBack() },
                    { showPolicyDialog.value = true },
                    navController,
                    mainAppModel,
                    activity
                )
            }
            composable(
                "hiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                HiddenApps(
                    mainAppModel = mainAppModel,
                    homeScreenModel = homeScreenModel,
                    goToManageHiddenApps = {
                        navController.navigate("bulkHiddenApps")
                    }
                ) { navController.popBackStack() }
            }
            composable(
                "openChallenges",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                val openChallegeAppIds by openChallengeViewModel.challengeAppIds.collectAsState()

                BulkManager(
                    items = installedApps,
                    id = { it.packageName },
                    label = { it.displayName },
                    selectedIdsOverride = openChallegeAppIds,
                    title = stringResource(R.string.manage_open_challenges),
                    onBackClicked = { navController.popBackStack() },
                    onItemClicked = { app, selected ->
                        if (selected) {
                            openChallengeViewModel.removeChallengeFromApp(app.packageName)
                        } else {
                            openChallengeViewModel.addChallengeToApp(app.packageName)
                        }
                    })
            }
            composable(
                "chooseFont",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ChooseFont(mainAppModel.getContext(), activity) { navController.popBackStack() }
            }
            composable(
                "devOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DevOptions(
                    mainAppModel = mainAppModel,
                ) { navController.popBackStack() }
            }
            composable(
                "theme",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ThemeOptions(goBack = { navController.popBackStack() })
            }
            composable(
                "widget",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                WidgetOptions(onBackClick = { navController.popBackStack() })
            }
            composable(
                "bulkHiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                val hiddenPackageIds by hiddenAppsViewModel.hiddenPackageIds.collectAsState()

                BulkManager(
                    items = installedApps,
                    id = { it.packageName },
                    label = { it.displayName },
                    selectedIdsOverride = hiddenPackageIds,
                    title = stringResource(R.string.manage_hidden_apps),
                    onBackClicked = { navController.popBackStack() },
                    onItemClicked = { app, selected ->
                        if (selected) {
                            hiddenAppsViewModel.unhideApp(app.packageName)
                        } else {
                            hiddenAppsViewModel.hideApp(app.packageName)
                        }
                    })
            }
            composable(
                "bulkFavouriteApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                BulkManager(
                    items = installedApps,
                    id = { it.packageName },
                    label = { it.displayName },
                    preSelectedItems = homeScreenModel.favoriteApps,
                    title = stringResource(R.string.manage_favourite_apps),
                    reorderable = true,
                    onItemMoved = { fromIndex, toIndex ->
                        val app = homeScreenModel.favoriteApps[fromIndex]
                        homeScreenModel.coroutineScope.launch {
                            mainAppModel.modifiedAppsRepository.reorderFavouriteApp(
                                app.packageName,
                                fromIndex,
                                toIndex
                            )
                        }
                    },
                    onBackClicked = { navController.popBackStack() },
                    onItemClicked = { app, selected ->
                        homeScreenModel.coroutineScope.launch {
                            if (selected) {
                                mainAppModel.modifiedAppsRepository.removeFavourite(app.packageName)
                            } else {
                                mainAppModel.modifiedAppsRepository.addFavourite(app.packageName)
                            }
                        }
                    })
            }
            composable(
                "fontLicences",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                FontLicenceDialog(mainAppModel.getContext()) {
                    navController.popBackStack()
                }
            }
        }
    }

    AnimatedVisibility(showPolicyDialog.value, enter = fadeIn(), exit = fadeOut()) {
        PrivacyPolicyDialog(mainAppModel, showPolicyDialog)
    }
}

/**
 * Fist page of settings, contains navigation to all the other pages
 *
 * @param goBack When back button is pressed
 * @param showPolicyDialog When the show privacy policy button is pressed
 * @param navController Settings nav controller with "personalisation", "hiddenApps", "openChallenges"
 * @param mainAppModel This is required for settings to be changed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    showPolicyDialog: () -> Unit,
    navController: NavController,
    mainAppModel: MainAppModel,
    activity: Activity,
    mainSettingsPageViewModel: MainSettingsPageViewModel = hiltViewModel(),
    globalViewModel: GlobalViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val installedApps by mainSettingsPageViewModel.installedApps.collectAsState()

    var showWeatherAppPicker by remember { mutableStateOf(false) }
    val view = LocalView.current

    val twelveHourClock by mainSettingsPageViewModel.twelveHourClock.collectAsState(initial = false)
    val showClock by mainSettingsPageViewModel.showClock.collectAsState(initial = true)
    val bigClock by mainSettingsPageViewModel.bigClock.collectAsState(initial = false)
    val showDate by mainSettingsPageViewModel.showDate.collectAsState(initial = false)
    val showWeather by mainSettingsPageViewModel.showWeather.collectAsState(initial = false)
    val useFahrenheit by mainSettingsPageViewModel.useFahrenheit.collectAsState(initial = false)
    val showScreenTimeApp by mainSettingsPageViewModel.showScreenTimeApp.collectAsState(initial = false)
    val showScreenTimeHome by mainSettingsPageViewModel.showScreenTimeHome.collectAsState(initial = false)
    val hapticFeedbackEnabled by mainSettingsPageViewModel.hapticFeedBackEnabled.collectAsState(
        initial = true
    )
    val homeHorizontalIndex by mainSettingsPageViewModel.homeAlignment.collectAsState(initial = 1)
    val appsHorizontalIndex by mainSettingsPageViewModel.appsAlignment.collectAsState(initial = 1)
    val homeVerticalIndex by mainSettingsPageViewModel.homeVAlignment.collectAsState(initial = 1)
    val allowAnalytics by globalViewModel.allowAnalytics.collectAsState(initial = false)

    val doubleTapToLock by mainSettingsPageViewModel.doubleTapToLock.collectAsState(initial = false)
    val showSearchBox by mainSettingsPageViewModel.showSearchBox.collectAsState(initial = true)
    val searchAutoOpen by mainSettingsPageViewModel.searchAutoOpen.collectAsState(initial = false)
    val bottomSearch by mainSettingsPageViewModel.bottomSearch.collectAsState(initial = false)
    val automaticallyOpenAppsInSearch by mainSettingsPageViewModel.automaticallyOpenAppsInSearch.collectAsState(initial = false)
    val hideScreenTimePage by mainSettingsPageViewModel.hideScreenTimePage.collectAsState(initial = false)

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            EscapeHeader(
                goBack, stringResource(R.string.settings)
            )
        }

        //General
        item { EscapeSubhead(stringResource(id = R.string.general)) }

        item {
            SettingsNavigationItem(
                label = stringResource(id = R.string.theme),
                false,
                isTopOfGroup = true,
                onClick = { navController.navigate("theme") })
        }

        item {
            SettingsNavigationItem(
                label = stringResource(id = R.string.choose_font),
                false,
                onClick = { navController.navigate("chooseFont") })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.haptic_feedback),
                isBottomOfGroup = true,
                checked = hapticFeedbackEnabled,
                onCheckedChange = {
                    mainSettingsPageViewModel.setHapticFeedback(it)
                    view.isHapticFeedbackEnabled = it
                })
        }

        // Home options
        item { EscapeSubhead(stringResource(R.string.home_screen_options)) }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.show_clock),
                checked = showClock,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowClock(it)
                },
                isTopOfGroup = true
            )
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.twelve_hour_clock_setting),
                checked = twelveHourClock,
                onCheckedChange = {
                    mainSettingsPageViewModel.setTwelveHourClock(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.big_clock),
                checked = bigClock,
                onCheckedChange = {
                    mainSettingsPageViewModel.setBigClock(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.date), checked = showDate, onCheckedChange = {
                    mainSettingsPageViewModel.setShowDate(it)
                })
        }

        item {
            if (!BuildConfig.IS_FOSS) {
                SettingsSwitch(
                    label = stringResource(id = R.string.show_weather),
                    checked = showWeather,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setShowWeather(it)
                        if (it) {
                            weatherViewModel.forceUpdate()
                        }
                    })
            }
        }

        item {
            if (!BuildConfig.IS_FOSS) {
                SettingsSwitch(
                    label = stringResource(id = R.string.use_farenhight),
                    checked = useFahrenheit,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setUseFahrenheit(it)
                        weatherViewModel.forceUpdate()
                    })
            }
        }

        item {
            if (!BuildConfig.IS_FOSS) {
                SettingsNavigationItem(
                    label = stringResource(id = R.string.choose_weather_app),
                    false,
                    onClick = { showWeatherAppPicker = true }
                )
            }
        }

        item {
            SettingsNavigationItem(
                label = stringResource(id = R.string.widget),
                false,
                onClick = { navController.navigate("widget") })
        }

        item {
            SettingsNavigationItem(
                stringResource(R.string.manage_favourite_apps),
                diagonalArrow = false,
                isBottomOfGroup = Build.VERSION.SDK_INT < Build.VERSION_CODES.P,
                onClick = {
                    navController.navigate("bulkFavouriteApps")
                })
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            item {
                SettingsSwitch(
                    label = stringResource(id = R.string.double_tap_to_lock),
                    checked = doubleTapToLock,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setDoubleTapToLock(it)
                    },
                    isBottomOfGroup = EscapeAccessibilityService.instance != null
                )
            }

            if (EscapeAccessibilityService.instance == null) {
                item {
                    SettingsButton(
                        label = stringResource(R.string.enable_accessibility),
                        isBottomOfGroup = true,
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                            mainAppModel.getContext().startActivity(intent)
                        }
                    )
                }
            }
        }


        //Alignment Options
        item { EscapeSubhead(stringResource(R.string.alignments)) }

        item {
            val homeHorizontalOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )

            SettingsSingleChoiceSegmentedButtons(
                label = stringResource(id = R.string.home),
                options = homeHorizontalOptions,
                selectedIndex = homeHorizontalIndex,
                onSelectedIndexChange = { newIndex ->
                    mainSettingsPageViewModel.setHomeAlignment(newIndex)
                },
                isTopOfGroup = true // First item in this section
            )
        }

        item {
            val homeVerticalOptions = listOf(
                stringResource(R.string.top),
                stringResource(R.string.center),
                stringResource(R.string.bottom)
            )

            SettingsSingleChoiceSegmentedButtons(
                label = "",
                options = homeVerticalOptions,
                selectedIndex = homeVerticalIndex,
                onSelectedIndexChange = { newIndex ->
                    mainSettingsPageViewModel.setHomeVAlignment(newIndex)
                })
        }

        item {
            val appsAlignmentOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )

            SettingsSingleChoiceSegmentedButtons(
                label = stringResource(id = R.string.apps),
                options = appsAlignmentOptions,
                selectedIndex = appsHorizontalIndex,
                onSelectedIndexChange = { newIndex ->
                    mainSettingsPageViewModel.setAppsAlignment(newIndex)
                },
                isBottomOfGroup = true // Last item in this section before any potential new sections
            )
        }

        // Search settings
        item { EscapeSubhead(stringResource(R.string.search)) }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.search_box), checked = showSearchBox, isTopOfGroup = true, onCheckedChange = {
                    mainSettingsPageViewModel.setShowSearchBox(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.auto_open), checked = automaticallyOpenAppsInSearch, isBottomOfGroup = false, onCheckedChange = {
                    mainSettingsPageViewModel.setAutomaticallyOpenAppsInSearch(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.search_at_bottom), checked = bottomSearch, isBottomOfGroup = false, onCheckedChange = {
                    mainSettingsPageViewModel.setBottomSearch(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.apps_list_auto_search),
                checked = searchAutoOpen,
                isBottomOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setSearchAutoOpen(it)
                })
        }

        //Screen time
        item { EscapeSubhead(stringResource(R.string.screen_time)) }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.screen_time_on_app),
                checked = showScreenTimeApp,
                isTopOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowScreenTimeApp(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.hide_screen_time_page),
                checked = hideScreenTimePage,
                onCheckedChange = {
                    mainSettingsPageViewModel.setHideScreenTimePage(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.screen_time_on_home_screen),
                checked = showScreenTimeHome,
                isBottomOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowScreenTimeHome(it)
                })
        }

        //Apps
        item {
            EscapeSubhead(
                stringResource(R.string.apps)
            )
        }

        item {
            SettingsNavigationItem(
                label = stringResource(id = R.string.manage_hidden_apps),
                false,
                isTopOfGroup = true,
                onClick = { navController.navigate("hiddenApps") })
        }

        item {
            SettingsNavigationItem(
                label = stringResource(id = R.string.manage_open_challenges),
                false,
                isBottomOfGroup = true,
                onClick = { navController.navigate("openChallenges") })
        }

        //Other
        item { EscapeSubhead(stringResource(id = R.string.other)) }

        item {
            SettingsNavigationItem(
                label = stringResource(id = R.string.make_default_launcher),
                true,
                isTopOfGroup = true,
                onClick = {
                    if (!isDefaultLauncher(activity)) {
                        activity.showLauncherSelector()
                    } else {
                        showLauncherSettingsMenu(activity)
                    }
                })
        }

        if (!BuildConfig.IS_FOSS) {
            item {
                SettingsSwitch(
                    label = stringResource(id = R.string.Analytics),
                    checked = allowAnalytics,
                    onCheckedChange = {
                        globalViewModel.setAllowAnalytics(it)
                    }
                )
            }
        }

        item {
            SettingsNavigationItem(
                label = stringResource(R.string.font_licences),
                diagonalArrow = false,
                isBottomOfGroup = BuildConfig.IS_FOSS,
                onClick = { navController.navigate("fontLicences") }
            )
        }

        if (!BuildConfig.IS_FOSS) {
            item {
                SettingsNavigationItem(
                    label = stringResource(id = R.string.read_privacy_policy),
                    false,
                    isBottomOfGroup = true,
                    onClick = { showPolicyDialog() })
            }
        }

        item { SettingsSpacer() }

        item {
            FooterBox(
                stringResource(id = R.string.app_name) + " " + stringResource(id = R.string.app_version),
                secondText = stringResource(R.string.app_flavour),
                onSponsorClick = {
                    val url = "https://github.com/sponsors/GeorgeClensy"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.setData(url.toUri())
                    i.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    mainAppModel.getContext().startActivity(i)
                },
                icon = painterResource(R.drawable.outlineicon),
                sponsorButtonText = stringResource(R.string.sponsor),
                onBackgroundClick = {
                    navController.navigate("devOptions")
                })
        }

        item { SettingsSpacer() }
        item { SettingsSpacer() }
    }

    if (showWeatherAppPicker) {
        WeatherAppPicker(
            apps = installedApps,
            onAppSelected = { app ->
                mainSettingsPageViewModel.setWeatherAppPackage(app.packageName)
                showWeatherAppPicker = false
            },
            onDismiss = { showWeatherAppPicker = false }
        )
    }
}

/**
 * Theme options in settings
 *
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeOptions(
    goBack: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scheme by themeViewModel.theme.collectAsState()
    val lScheme by themeViewModel.ltheme.collectAsState()
    val dScheme by themeViewModel.dtheme.collectAsState()
    val syncTheme by themeViewModel.syncTheme.collectAsState(false)

    val isDark = isSystemInDarkTheme()
    var highlightedThemeId by remember { mutableIntStateOf(-1) }

    val themeIds = listOf(11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12)

    LazyColumn(Modifier.fillMaxSize()) {

        item {
            EscapeHeader(goBack, stringResource(R.string.theme))
        }

        item {
            SettingsSwitch(
                stringResource(R.string.syncLightDark),
                syncTheme,
                isTopOfGroup = true,
                onCheckedChange = {
                    highlightedThemeId = -1
                    themeViewModel.setSyncTheme(it)
                }
            )
        }

        item {
            val activeTheme = if (syncTheme) {
                if (isDark) dScheme else lScheme
            } else scheme

            val colour = activeTheme.resolveColorScheme().background

            SettingsButton(
                label = stringResource(R.string.match_system_wallpaper),
                isBottomOfGroup = true,
                onClick = {
                    setSolidColorWallpaperHomeScreen(
                        context,
                        colour.toAndroidColor()
                    )
                }
            )
        }

        item { SettingsSpacer() }

        itemsIndexed(themeIds, key = { _, id -> id }) { index, themeId ->

            val isSelected = !syncTheme && scheme.id == themeId
            val isLight = syncTheme && lScheme.id == themeId
            val isDarkSel = syncTheme && dScheme.id == themeId
            val showPicker = highlightedThemeId == themeId

            ThemeCard(
                theme = themeId,

                showLightDarkPicker = showPicker,
                isSelected = isSelected,
                isLSelected = isLight,
                isDSelected = isDarkSel,

                updateLTheme = {
                    themeViewModel.setLTheme(AppColourScheme.fromId(themeId))
                    highlightedThemeId = -1
                },

                updateDTheme = {
                    themeViewModel.setDTheme(AppColourScheme.fromId(themeId))
                    highlightedThemeId = -1
                },

                modifier = Modifier.fillMaxWidth(),
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == themeIds.size - 1,

                onClick = {
                    if (syncTheme) {
                        highlightedThemeId = themeId
                    } else {
                        themeViewModel.setTheme(AppColourScheme.fromId(themeId))
                    }
                }
            )
        }

        item { SettingsSpacer() }
        item { SettingsSpacer() }
    }
}


/**
 * Page that lets you manage hidden apps
 *
 * @param mainAppModel Needed for context & hidden apps manager
 * @param goBack Function run when back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiddenApps(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel(),
    goToManageHiddenApps: () -> Unit,
    goBack: () -> Unit
) {
    val installedApps by hiddenAppsViewModel.installedApps.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val hapticFeedbackEnabled by hiddenAppsViewModel.hapticFeedBackEnabled.collectAsState(initial = true)
    val hiddenPackageIds by hiddenAppsViewModel.hiddenPackageIds.collectAsState()
    val showHiddenAppsInSearch by hiddenAppsViewModel.showHiddenAppsInSearch.collectAsState(initial = false)

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            EscapeHeader(goBack, stringResource(R.string.hidden_apps))
        }

        item {
            SettingsButton(
                label = stringResource(R.string.manage_hidden_apps),
                isTopOfGroup = true,
                onClick = {
                    goToManageHiddenApps()
                }
            )
        }

        item {
            SettingsSwitch(
                label = stringResource(R.string.show_hidden_apps_in_search),
                checked = showHiddenAppsInSearch,
                onCheckedChange = {
                    hiddenAppsViewModel.setShowHiddenAppsInSearch(it)
                },
                isBottomOfGroup = true
            )
        }

        item {
            EscapeSubhead(stringResource(R.string.swipe_to_show_app))
        }

        items(
            items = hiddenPackageIds.toList(),
            key = { it } // use package name as unique key
        ) { appPackageName ->
            // Animate the removal of the item
            var visible by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = visible,
                exit = fadeOut(animationSpec = tween(500))
            ) {
                SettingsSwipeableButton(
                    label = mainAppModel.appsRepository.getAppNameFromPackageName(
                        appPackageName
                    ),
                    onClick = {
                        val app =
                            installedApps.find { it.packageName == appPackageName }
                                ?: mainAppModel.appsRepository.getInstalledAppFromPackageName(
                                    appPackageName
                                )

                        app?.let {
                            homeScreenModel.openApp(
                                app = it,
                                overrideChallenge = false
                            )
                        }
                    },
                    onDeleteClick = {
                        // Trigger haptic feedback
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        // Animate item out
                        visible = false
                        // Remove from your list after a short delay to let animation run
                        coroutineScope.launch {
                            delay(500)
                            hiddenAppsViewModel.unhideApp(appPackageName)
                        }
                    },
                    isTopOfGroup = hiddenPackageIds.firstOrNull() == appPackageName,
                    isBottomOfGroup = hiddenPackageIds.lastOrNull() == appPackageName,
                    deleteIconContentDescription = stringResource(R.string.remove),
                )
            }
        }

        item {
            SettingsSpacer()
        }
        item {
            SettingsSpacer()
        }
    }
}

/**
 * Font options in settings
 *
 * @param context Needed to run some functions used within ThemeOptions
 * @param activity Needed to reload app after changing theme
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooseFont(
    context: Context,
    activity: Activity,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    val selectedFont by themeViewModel.font.collectAsState(initial = "Jost")

    val fontNames = listOf(
        "Jost",
        "Inter",
        "Lexend",
        "Work Sans",
        "Poppins",
        "Roboto",
        "Open Sans",
        "Lora",
        "Outfit",
        "IBM Plex Sans",
        "IBM Plex Serif"
    )

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item { EscapeHeader(goBack, stringResource(R.string.font)) }

        itemsIndexed(fontNames) { index, fontName ->
            SettingsButton(
                label = fontName,
                onClick = {
                    themeViewModel.setFont(fontName)
                },
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == fontNames.lastIndex,
                fontFamily = getFontFamily(context, fontName),
                isSelected = fontName == selectedFont
            )
        }

        item { SettingsSpacer() }
        item { SettingsSpacer() }
    }
}

/**
 * Developer options in settings
 */
@Composable
fun DevOptions(
    mainAppModel: MainAppModel,
    viewModel: DevOptionsPageViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    val context = LocalContext.current
    val firstTimeHelp by viewModel.firstTimeHelp.collectAsState(initial = true)
    val firstTime by viewModel.firstTime.collectAsState(initial = true)
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsState(initial = false)

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item { EscapeHeader(goBack, "Developer Options") }

        item {
            SettingsSwitch(
                "First time",
                firstTimeHelp && firstTime,
                onCheckedChange = {
                    viewModel.setFirstTimeHelp(it)
                    viewModel.setFirstTime(it)
                },
                isTopOfGroup = true
            )
        }

        item {
            SettingsButton(
                label = "Force Stop",
                onClick = {
                    exitProcess(0)
                }
            )
        }

        item {
            SettingsButton(
                label = "Clear weather app",
                onClick = {
                    viewModel.setWeatherAppPackage("")
                    Toast.makeText(context, "Weather app cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            SettingsButton(
                label = "Force crash",
                onClick = {
                    throw RuntimeException("Test Crash")
                }
            )
        }

        item {
            SettingsButton(
                label = "Test Screen Off",
                isBottomOfGroup = true,
                onClick = {
                    val context = mainAppModel.getContext()
                    if (doubleTapToLock) {
                        val service = EscapeAccessibilityService.instance
                        if (service != null) {
                            service.lockScreen()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.accessibility_not_granted_msg),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        }
    }
}

/**
 * Privacy Policy dialog
 *
 * @param mainAppModel Needed for context
 * @param showPolicyDialog Pass the MutableState<Boolean> your using to show and hide this dialogue so that it can be hidden from within it
 */
@Composable
fun PrivacyPolicyDialog(mainAppModel: MainAppModel, showPolicyDialog: MutableState<Boolean>) {
    val scrollState = rememberScrollState()
    Column {
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)  // Make the content scrollable
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // Load text from the asset
                loadTextFromAssets(mainAppModel.getContext(), "Privacy Policy.txt")?.let { text ->
                    BasicText(
                        text = text, style = TextStyle(
                            color = ContentColor,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Normal
                        ), modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "OK" Button
                Button(
                    onClick = { showPolicyDialog.value = false },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp),
                    colors = ButtonColors(
                        CardContainerColor,
                        ContentColor,
                        CardContainerColor,
                        ContentColor
                    )
                ) {
                    Text("OK")
                }

                SettingsSpacer()
                SettingsSpacer()
                SettingsSpacer()
            }
        }
    }
}

/**
 * Font licence dialog
 *
 * @param context Context
 */
@Composable
fun FontLicenceDialog(context: Context, onOKClick: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)  // Make the content scrollable
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // Load text from the asset
        loadTextFromAssets(context, "Font Licence.txt")?.let { text ->
            BasicText(
                text = text, style = TextStyle(
                    color = ContentColor,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal
                ), modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "OK" Button
        Button(
            onClick = { onOKClick() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
            colors = ButtonColors(
                CardContainerColor,
                ContentColor,
                CardContainerColor,
                ContentColor
            )
        ) {
            Text("OK")
        }

        SettingsSpacer()
        SettingsSpacer()
        SettingsSpacer()
    }
}

/**
 * Weather app picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppPicker(
    apps: List<InstalledApp>,
    onAppSelected: (InstalledApp) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(apps.sortedBy { it.displayName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { onAppSelected(app) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = app.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryContentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Theme select card
 *
 * @param theme The theme ID number (see: Theme.kt)
 *
 * @see com.geecee.escapelauncher.core.theme.EscapeTheme
 */
@Composable
fun ThemeCard(
    theme: Int,
    showLightDarkPicker: Boolean,
    isSelected: Boolean,
    isDSelected: Boolean,
    isLSelected: Boolean,
    updateLTheme: (Int) -> Unit,
    updateDTheme: (Int) -> Unit,
    modifier: Modifier,
    onClick: (Int) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val scheme = AppColourScheme.fromId(theme)
    val colors = scheme.resolveColorScheme()

    val groupEdgeCornerRadius = 24.dp
    val defaultCornerRadius = 8.dp

    val shape = RoundedCornerShape(
        topStart = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        topEnd = if (isTopOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        bottomStart = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius,
        bottomEnd = if (isBottomOfGroup) groupEdgeCornerRadius else defaultCornerRadius
    )

    Box(Modifier.padding(vertical = 1.dp)) {
        Box(
            modifier
                .clip(shape)
                .clickable { onClick(theme) }
                .background(colors.background)
                .height(72.dp)
        ) {
            val showCheck = isSelected && !showLightDarkPicker
            val showMoon = isDSelected && !showLightDarkPicker
            val showSun = isLSelected && !showLightDarkPicker

            AnimatedVisibility(
                visible = showCheck || showMoon || showSun,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(2.dp, colors.onPrimaryContainer, shape)
                ) {

                    when {
                        showCheck -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            )
                        }

                        showMoon -> {
                            Icon(
                                painterResource(R.drawable.dark_mode),
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            )
                        }

                        showSun -> {
                            Icon(
                                painterResource(R.drawable.light_mode),
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(AppColourScheme.nameResFromId(theme)),
                color = colors.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            AnimatedVisibility(
                visible = showLightDarkPicker,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .background(transparentHalf)
                ) {
                    Button(
                        onClick = { updateLTheme(theme) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(20.dp, 5.dp, 5.dp, 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.light))
                    }

                    Button(
                        onClick = { updateDTheme(theme) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(5.dp, 5.dp, 20.dp, 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.dark))
                    }
                }
            }
        }
    }
}