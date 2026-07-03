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
import com.geecee.escapelauncher.feature.settings.SettingsNavKey
import com.geecee.escapelauncher.core.common.EscapeAccessibilityService
import com.geecee.escapelauncher.core.common.configureStatusBar
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
 * @param onNavigate Callback to navigate to a settings sub-page
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    showPolicyDialog: () -> Unit,
    onNavigate: (SettingsNavKey) -> Unit,
    mainSettingsPageViewModel: MainSettingsPageViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val view = LocalView.current

    val installedApps by mainSettingsPageViewModel.installedApps.collectAsState()
    val uiState by mainSettingsPageViewModel.uiState.collectAsState()
    var showWeatherAppPicker by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "header") {
            EscapeHeader(
                goBack, stringResource(R.string.settings)
            )
        }

        //General
        item(key = "general_subhead") { EscapeSubhead(stringResource(id = R.string.general)) }

        item(key = "theme") {
            SettingsNavigationItem(
                label = stringResource(id = R.string.theme),
                false,
                isTopOfGroup = true,
                onClick = { onNavigate(SettingsNavKey.Theme) })
        }

        item(key = "font") {
            SettingsNavigationItem(
                label = stringResource(id = R.string.choose_font),
                false,
                onClick = { onNavigate(SettingsNavKey.ChooseFont) })
        }

        item(key = "haptic") {
            SettingsSwitch(
                label = stringResource(id = R.string.haptic_feedback),
                isBottomOfGroup = true,
                checked = uiState.hapticFeedBackEnabled,
                onCheckedChange = {
                    mainSettingsPageViewModel.setHapticFeedback(it)
                    view.isHapticFeedbackEnabled = it
                })
        }

        // Home options
        item(key = "home_options_subhead") { EscapeSubhead(stringResource(R.string.home_screen_options)) }

        item(key = "show_clock") {
            SettingsSwitch(
                label = stringResource(id = R.string.show_clock),
                checked = uiState.showClock,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowClock(it)
                },
                isTopOfGroup = true
            )
        }

        item(key = "12h_clock") {
            SettingsSwitch(
                label = stringResource(id = R.string.twelve_hour_clock_setting),
                checked = uiState.twelveHourClock,
                onCheckedChange = {
                    mainSettingsPageViewModel.setTwelveHourClock(it)
                })
        }

        item(key = "big_clock") {
            SettingsSwitch(
                label = stringResource(id = R.string.big_clock),
                checked = uiState.bigClock,
                onCheckedChange = {
                    mainSettingsPageViewModel.setBigClock(it)
                })
        }

        item(key = "show_date") {
            SettingsSwitch(
                label = stringResource(id = R.string.date),
                checked = uiState.showDate,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowDate(it)
                })
        }

        item(key = "show_status_bar") {
            SettingsSwitch(
                label = stringResource(id = R.string.show_status_bar),
                checked = uiState.showStatusBar,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowStatusBar(it)
                    activity?.window?.configureStatusBar(hide = !it)
                })
        }

        if (!mainSettingsPageViewModel.appConfiguration.isFoss) {
            item(key = "show_weather") {
                SettingsSwitch(
                    label = stringResource(id = R.string.show_weather),
                    checked = uiState.showWeather,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setShowWeather(it)
                        if (it) {
                            weatherViewModel.forceUpdate()
                        }
                    })
            }

            item(key = "use_fahrenheit") {
                SettingsSwitch(
                    label = stringResource(id = R.string.use_farenhight),
                    checked = uiState.useFahrenheit,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setUseFahrenheit(it)
                        weatherViewModel.forceUpdate()
                    })
            }

            item(key = "choose_weather_app") {
                SettingsNavigationItem(
                    label = stringResource(id = R.string.choose_weather_app),
                    false,
                    onClick = { showWeatherAppPicker = true })
            }
        }

        item(key = "widget") {
            SettingsNavigationItem(
                label = stringResource(id = R.string.widget),
                false,
                onClick = { onNavigate(SettingsNavKey.Widget) })
        }

        item(key = "manage_fav_apps") {
            SettingsNavigationItem(
                stringResource(R.string.manage_favourite_apps),
                diagonalArrow = false,
                isBottomOfGroup = Build.VERSION.SDK_INT < Build.VERSION_CODES.P,
                onClick = {
                    onNavigate(SettingsNavKey.BulkFavouriteApps)
                })
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            item(key = "double_tap_to_lock") {
                SettingsSwitch(
                    label = stringResource(id = R.string.double_tap_to_lock),
                    checked = uiState.doubleTapToLock,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setDoubleTapToLock(it)
                    },
                    isBottomOfGroup = EscapeAccessibilityService.instance != null
                )
            }

            if (EscapeAccessibilityService.instance == null) {
                item(key = "enable_accessibility") {
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
        item(key = "alignment_subhead") { EscapeSubhead(stringResource(R.string.alignments)) }

        item(key = "home_alignment") {
            val homeHorizontalOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )

            SettingsSingleChoiceSegmentedButtons(
                label = stringResource(id = R.string.home),
                options = homeHorizontalOptions,
                selectedIndex = uiState.homeAlignment,
                onSelectedIndexChange = { newIndex ->
                    mainSettingsPageViewModel.setHomeAlignment(newIndex)
                },
                isTopOfGroup = true
            )
        }

        item(key = "home_v_alignment") {
            val homeVerticalOptions = listOf(
                stringResource(R.string.top),
                stringResource(R.string.center),
                stringResource(R.string.bottom)
            )

            SettingsSingleChoiceSegmentedButtons(
                label = "",
                options = homeVerticalOptions,
                selectedIndex = uiState.homeVAlignment,
                onSelectedIndexChange = { newIndex ->
                    mainSettingsPageViewModel.setHomeVAlignment(newIndex)
                })
        }

        item(key = "apps_alignment") {
            val appsAlignmentOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )

            SettingsSingleChoiceSegmentedButtons(
                label = stringResource(id = R.string.apps),
                options = appsAlignmentOptions,
                selectedIndex = uiState.appsAlignment,
                onSelectedIndexChange = { newIndex ->
                    mainSettingsPageViewModel.setAppsAlignment(newIndex)
                },
                isBottomOfGroup = true
            )
        }

        // Search settings
        item(key = "search_subhead") { EscapeSubhead(stringResource(R.string.search)) }

        item(key = "show_search_box") {
            SettingsSwitch(
                label = stringResource(id = R.string.search_box),
                checked = uiState.showSearchBox,
                isTopOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowSearchBox(it)
                })
        }

        item(key = "auto_open_search") {
            SettingsSwitch(
                label = stringResource(id = R.string.auto_open),
                checked = uiState.automaticallyOpenAppsInSearch,
                isBottomOfGroup = false,
                onCheckedChange = {
                    mainSettingsPageViewModel.setAutomaticallyOpenAppsInSearch(it)
                })
        }

        item(key = "bottom_search") {
            SettingsSwitch(
                label = stringResource(id = R.string.search_at_bottom),
                checked = uiState.bottomSearch,
                isBottomOfGroup = false,
                onCheckedChange = {
                    mainSettingsPageViewModel.setBottomSearch(it)
                })
        }

        item(key = "apps_list_auto_search") {
            SettingsSwitch(
                label = stringResource(id = R.string.apps_list_auto_search),
                checked = uiState.searchAutoOpen,
                isBottomOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setSearchAutoOpen(it)
                })
        }

        //Screen time
        item(key = "screen_time_subhead") { EscapeSubhead(stringResource(R.string.screen_time)) }

        item(key = "screen_time_app") {
            SettingsSwitch(
                label = stringResource(id = R.string.screen_time_on_app),
                checked = uiState.showScreenTimeApp,
                isTopOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowScreenTimeApp(it)
                })
        }

        item(key = "hide_screen_time_page") {
            SettingsSwitch(
                label = stringResource(id = R.string.hide_screen_time_page),
                checked = uiState.hideScreenTimePage,
                onCheckedChange = {
                    mainSettingsPageViewModel.setHideScreenTimePage(it)
                })
        }

        item(key = "screen_time_home") {
            SettingsSwitch(
                label = stringResource(id = R.string.screen_time_on_home_screen),
                checked = uiState.showScreenTimeHome,
                isBottomOfGroup = true,
                onCheckedChange = {
                    mainSettingsPageViewModel.setShowScreenTimeHome(it)
                })
        }

        //Apps
        item(key = "apps_subhead") {
            EscapeSubhead(
                stringResource(R.string.apps)
            )
        }

        item(key = "manage_hidden_apps") {
            SettingsNavigationItem(
                label = stringResource(id = R.string.manage_hidden_apps),
                false,
                isTopOfGroup = true,
                onClick = { onNavigate(SettingsNavKey.HiddenApps) })
        }

        item(key = "manage_open_challenges") {
            SettingsNavigationItem(
                label = stringResource(id = R.string.manage_open_challenges),
                false,
                isBottomOfGroup = true,
                onClick = { onNavigate(SettingsNavKey.OpenChallenges) })
        }

        //Other
        item(key = "other_subhead") { EscapeSubhead(stringResource(id = R.string.other)) }

        item(key = "make_default_launcher") {
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
            item(key = "analytics") {
                SettingsSwitch(
                    label = stringResource(id = R.string.Analytics),
                    checked = uiState.allowAnalytics,
                    onCheckedChange = {
                        mainSettingsPageViewModel.setAllowAnalytics(it)
                    })
            }
        }

        item(key = "font_licences") {
            SettingsNavigationItem(
                label = stringResource(R.string.font_licences),
                diagonalArrow = false,
                isBottomOfGroup = mainSettingsPageViewModel.appConfiguration.isFoss,
                onClick = { onNavigate(SettingsNavKey.FontLicences) })
        }

        if (!mainSettingsPageViewModel.appConfiguration.isFoss) {
            item(key = "privacy_policy") {
                SettingsNavigationItem(
                    label = stringResource(id = R.string.read_privacy_policy),
                    false,
                    isBottomOfGroup = true,
                    onClick = { showPolicyDialog() })
            }
        }

        item(key = "spacer1") { SettingsSpacer() }

        item(key = "footer") {
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
                    onNavigate(SettingsNavKey.DevOptions)
                })
        }

        item(key = "spacer2") { SettingsSpacer() }
        item(key = "spacer3") { SettingsSpacer() }
    }

    if (showWeatherAppPicker) {
        WeatherAppPicker(apps = installedApps, onAppSelected = { app ->
            mainSettingsPageViewModel.setWeatherAppPackage(app.packageName)
            showWeatherAppPicker = false
        }, onDismiss = { showWeatherAppPicker = false })
    }
}
