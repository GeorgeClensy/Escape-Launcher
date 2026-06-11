package com.geecee.escapelauncher.feature.settings.mainpage

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.geecee.escapelauncher.core.common.EscapeAccessibilityService
import com.geecee.escapelauncher.core.common.isDefaultLauncher
import com.geecee.escapelauncher.core.common.showLauncherSelector
import com.geecee.escapelauncher.core.common.showLauncherSettingsMenu
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.EscapeSubhead
import com.geecee.escapelauncher.core.ui.composables.FooterBox
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem
import com.geecee.escapelauncher.core.ui.composables.SettingsSingleChoiceSegmentedButtons
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import com.geecee.escapelauncher.feature.settings.weather.WeatherAppPicker
import com.geecee.escapelauncher.feature.weather.WeatherViewModel

/**
 * Fist page of settings, contains navigation to all the other pages
 *
 * @param goBack When back button is pressed
 * @param showPolicyDialog When the show privacy policy button is pressed
 * @param navController Settings nav controller with "personalisation", "hiddenApps", "openChallenges"
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    showPolicyDialog: () -> Unit,
    navController: NavController,
    mainSettingsPageViewModel: MainSettingsPageViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val installedApps by mainSettingsPageViewModel.installedApps.collectAsState()
    var showWeatherAppPicker by remember { mutableStateOf(false) }
    val view = LocalView.current
    val twelveHourClock by mainSettingsPageViewModel.twelveHourClock.collectAsState(initial = false)
    val showClock by mainSettingsPageViewModel.showClock.collectAsState(initial = true)
    val bigClock by mainSettingsPageViewModel.bigClock.collectAsState(initial = false)
    val showDate by mainSettingsPageViewModel.showDate.collectAsState(initial = false)
    val showStatusBar by mainSettingsPageViewModel.showStatusBar.collectAsState(initial = false)
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
    val allowAnalytics by mainSettingsPageViewModel.allowAnalytics.collectAsState(initial = false)
    val doubleTapToLock by mainSettingsPageViewModel.doubleTapToLock.collectAsState(initial = false)
    val showSearchBox by mainSettingsPageViewModel.showSearchBox.collectAsState(initial = true)
    val searchAutoOpen by mainSettingsPageViewModel.searchAutoOpen.collectAsState(initial = false)
    val bottomSearch by mainSettingsPageViewModel.bottomSearch.collectAsState(initial = false)
    val automaticallyOpenAppsInSearch by mainSettingsPageViewModel.automaticallyOpenAppsInSearch.collectAsState(
        initial = false
    )
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
            SettingsSwitch(
                label = stringResource(id = R.string.show_status_bar),
                checked = showStatusBar,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowStatusBar(it)
                })
        }

        item {
            if (!mainSettingsPageViewModel.appConfiguration.isFoss) {
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
            if (!mainSettingsPageViewModel.appConfiguration.isFoss) {
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
            if (!mainSettingsPageViewModel.appConfiguration.isFoss) {
                SettingsNavigationItem(
                    label = stringResource(id = R.string.choose_weather_app),
                    false,
                    onClick = { showWeatherAppPicker = true })
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
                            context.startActivity(intent)
                        })
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
                label = stringResource(id = R.string.search_box),
                checked = showSearchBox,
                isTopOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowSearchBox(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.auto_open),
                checked = automaticallyOpenAppsInSearch,
                isBottomOfGroup = false,
                onCheckedChange = {
                    mainSettingsPageViewModel.setAutomaticallyOpenAppsInSearch(it)
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.search_at_bottom),
                checked = bottomSearch,
                isBottomOfGroup = false,
                onCheckedChange = {
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
                    activity?.let {
                        if (!isDefaultLauncher(it)) {
                            it.showLauncherSelector()
                        } else {
                            showLauncherSettingsMenu(it)
                        }
                    }
                })
        }

        if (!mainSettingsPageViewModel.appConfiguration.isFoss) {
            item {
                SettingsSwitch(
                    label = stringResource(id = R.string.Analytics),
                    checked = allowAnalytics,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setAllowAnalytics(it)
                    })
            }
        }

        item {
            SettingsNavigationItem(
                label = stringResource(R.string.font_licences),
                diagonalArrow = false,
                isBottomOfGroup = mainSettingsPageViewModel.appConfiguration.isFoss,
                onClick = { navController.navigate("fontLicences") })
        }

        if (!mainSettingsPageViewModel.appConfiguration.isFoss) {
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
                mainSettingsPageViewModel.appConfiguration.appName + " " + mainSettingsPageViewModel.appConfiguration.appVersion,
                secondText = mainSettingsPageViewModel.appConfiguration.appFlavour,
                onSponsorClick = {
                    val url = "https://github.com/sponsors/GeorgeClensy"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.setData(url.toUri())
                    i.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(i)
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
        WeatherAppPicker(apps = installedApps, onAppSelected = { app ->
            mainSettingsPageViewModel.setWeatherAppPackage(app.packageName)
            showWeatherAppPicker = false
        }, onDismiss = { showWeatherAppPicker = false })
    }
}