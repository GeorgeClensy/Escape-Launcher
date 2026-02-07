@file:Suppress("KotlinConstantConditions")

package com.geecee.escapelauncher.ui.views

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.BuildConfig
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.composables.BulkAppManager
import com.geecee.escapelauncher.ui.composables.SettingsButton
import com.geecee.escapelauncher.ui.composables.SettingsHeader
import com.geecee.escapelauncher.ui.composables.SettingsNavigationItem
import com.geecee.escapelauncher.ui.composables.SettingsSingleChoiceSegmentedButtons
import com.geecee.escapelauncher.ui.composables.SettingsSlider
import com.geecee.escapelauncher.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.ui.composables.SettingsSubheading
import com.geecee.escapelauncher.ui.composables.SettingsSwipeableButton
import com.geecee.escapelauncher.ui.composables.SettingsSwitch
import com.geecee.escapelauncher.ui.composables.SponsorBox
import com.geecee.escapelauncher.ui.composables.ThemeCard
import com.geecee.escapelauncher.ui.composables.WeatherAppPicker
import com.geecee.escapelauncher.ui.theme.AppTheme
import com.geecee.escapelauncher.ui.theme.CardContainerColor
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.ui.theme.getFontFamily
import com.geecee.escapelauncher.ui.theme.resolveColorScheme
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.loadTextFromAssets
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.CustomWidgetPicker
import com.geecee.escapelauncher.utils.EscapeAccessibilityService
import com.geecee.escapelauncher.utils.WIDGET_HOST_ID
import com.geecee.escapelauncher.utils.changeAppsAlignment
import com.geecee.escapelauncher.utils.changeHomeAlignment
import com.geecee.escapelauncher.utils.changeHomeVAlignment
import com.geecee.escapelauncher.utils.getAppsAlignmentAsInt
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.getHomeAlignmentAsInt
import com.geecee.escapelauncher.utils.getHomeVAlignmentAsInt
import com.geecee.escapelauncher.utils.getIntSetting
import com.geecee.escapelauncher.utils.getSavedWidgetId
import com.geecee.escapelauncher.utils.getWidgetHeight
import com.geecee.escapelauncher.utils.getWidgetOffset
import com.geecee.escapelauncher.utils.getWidgetWidth
import com.geecee.escapelauncher.utils.isDefaultLauncher
import com.geecee.escapelauncher.utils.isWidgetConfigurable
import com.geecee.escapelauncher.utils.launchWidgetConfiguration
import com.geecee.escapelauncher.utils.managers.CountdownMode
import com.geecee.escapelauncher.utils.managers.getCountdownTime
import com.geecee.escapelauncher.utils.managers.resetAndGetCountdownTime
import com.geecee.escapelauncher.utils.managers.setCountdownTime
import com.geecee.escapelauncher.utils.removeWidget
import com.geecee.escapelauncher.utils.resetActivity
import com.geecee.escapelauncher.utils.saveWidgetId
import com.geecee.escapelauncher.utils.setBooleanSetting
import com.geecee.escapelauncher.utils.setIntSetting
import com.geecee.escapelauncher.utils.setStringSetting
import com.geecee.escapelauncher.utils.setWidgetHeight
import com.geecee.escapelauncher.utils.setWidgetOffset
import com.geecee.escapelauncher.utils.setWidgetWidth
import com.geecee.escapelauncher.utils.showLauncherSelector
import com.geecee.escapelauncher.utils.showLauncherSettingsMenu
import com.geecee.escapelauncher.utils.toggleBooleanSetting
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
) {
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
                    activity,
                    homeScreenModel
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
                val challengeApps = remember(mainAppModel.challengesTrigger.intValue) {
                    val currentChallenges = mainAppModel.challengesManager.getChallengeApps()
                    homeScreenModel.installedApps.filter { it.packageName in currentChallenges }
                }

                BulkAppManager(
                    apps = homeScreenModel.installedApps,
                    preSelectedApps = challengeApps,
                    title = stringResource(R.string.manage_open_challenges),
                    onBackClicked = { navController.popBackStack() },
                    onAppClicked = { app, selected ->
                        if (selected) {
                            mainAppModel.challengesManager.removeChallengeApp(app.packageName)
                        } else {
                            mainAppModel.challengesManager.addChallengeApp(app.packageName)
                        }
                        mainAppModel.notifyChallengesChanged()
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
                    context = mainAppModel.getContext()
                ) { navController.popBackStack() }
            }
            composable(
                "theme",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ThemeOptions(
                    mainAppModel, mainAppModel.getContext()
                ) { navController.popBackStack() }
            }
            composable(
                "widget",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                WidgetOptions(mainAppModel.getContext()) { navController.popBackStack() }
            }
            composable(
                "bulkHiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                val hiddenAppsList = remember(mainAppModel.hiddenAppsTrigger.intValue) {
                    val currentHidden = mainAppModel.hiddenAppsManager.getHiddenApps()
                    homeScreenModel.installedApps.filter { it.packageName in currentHidden }
                }

                BulkAppManager(
                    apps = homeScreenModel.installedApps,
                    preSelectedApps = hiddenAppsList,
                    title = stringResource(R.string.manage_hidden_apps),
                    onBackClicked = { navController.popBackStack() },
                    onAppClicked = { app, selected ->
                        if (selected) {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app.packageName)
                        } else {
                            mainAppModel.hiddenAppsManager.addHiddenApp(app.packageName)
                            resetHome(homeScreenModel, false)
                        }
                        mainAppModel.notifyHiddenAppsChanged()
                    })
            }
            composable(
                "bulkFavouriteApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                val preSelectedFavoriteApps = remember(homeScreenModel.favoriteApps.size) {
                    val favoritePackages = mainAppModel.favoriteAppsManager.getFavoriteApps()
                    favoritePackages.mapNotNull { pkg -> homeScreenModel.installedApps.find { it.packageName == pkg } }
                }

                BulkAppManager(
                    apps = homeScreenModel.installedApps,
                    preSelectedApps = preSelectedFavoriteApps,
                    title = stringResource(R.string.manage_favourite_apps),
                    reorderable = true,
                    onAppMoved = { fromIndex, toIndex ->
                        mainAppModel.favoriteAppsManager.reorderFavoriteApps(fromIndex, toIndex)
                        homeScreenModel.reloadFavouriteApps()
                    },
                    onBackClicked = { navController.popBackStack() },
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
            composable(
                "fontLicences",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                FontLicenceDialog(mainAppModel.getContext()) {
                    navController.popBackStack()
                }
            }
            composable(
                "newSettingsScreen",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                AppCountdownTime(mainAppModel.getContext()) {
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
 * @param navController Settings nav controller with "personalization", "hiddenApps", "openChallenges"
 * @param mainAppModel This is required for settings to be changed
 *
 * @see Settings
 */
@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    showPolicyDialog: () -> Unit,
    navController: NavController,
    mainAppModel: MainAppModel,
    activity: Activity,
    homeScreenModel: HomeScreenModel
) {
    var showWeatherAppPicker by remember { mutableStateOf(false) }
    val view = LocalView.current


    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsHeader(
                goBack, stringResource(R.string.settings)
            )
        }

        //General
        item { SettingsSubheading(stringResource(id = R.string.general)) }

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
                checked = view.isHapticFeedbackEnabled,
                onCheckedChange = {
                    view.isHapticFeedbackEnabled = it
                })
        }

        // Home options
        item { SettingsSubheading(stringResource(R.string.home_screen_options)) }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.show_clock),
                checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ShowClock), true
                ),
                onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.ShowClock)
                    )
                },
                isTopOfGroup = true
            )
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.twelve_hour_clock_setting),
                checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.twelve_hour_clock), false
                ),
                onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.twelve_hour_clock)
                    )
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.big_clock), checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.BigClock)
                ), onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.BigClock)
                    )
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.date), checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.show_date), false
                ), onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.show_date)
                    )
                })
        }

        item {
            if (!BuildConfig.IS_FOSS) {
                SettingsSwitch(
                    label = stringResource(id = R.string.show_weather), checked = getBooleanSetting(
                        mainAppModel.getContext(), stringResource(R.string.show_weather), false
                    ), onCheckedChange = {
                        toggleBooleanSetting(
                            mainAppModel.getContext(),
                            it,
                            mainAppModel.getContext().resources.getString(R.string.show_weather)
                        )
                    })
            }
        }

        item {
            if (!BuildConfig.IS_FOSS) {
                SettingsSwitch(
                    label = stringResource(id = R.string.use_farenhight), checked = getBooleanSetting(
                        mainAppModel.getContext(), stringResource(R.string.UseFahrenheit), false
                    ), onCheckedChange = {
                        toggleBooleanSetting(
                            mainAppModel.getContext(),
                            it,
                            mainAppModel.getContext().resources.getString(R.string.UseFahrenheit)
                        )
                        mainAppModel.forceUpdateWeather()
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
                    checked = getBooleanSetting(
                        mainAppModel.getContext(), stringResource(R.string.DoubleTapToLock), false
                    ),
                    onCheckedChange = {
                        setBooleanSetting(
                            mainAppModel.getContext(),
                            mainAppModel.getContext().resources.getString(R.string.DoubleTapToLock),
                            it
                        )
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
        item { SettingsSubheading(stringResource(R.string.alignments)) }

        item {
            val homeHorizontalOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            val selectedHomeHorizontalIndex = getHomeAlignmentAsInt(mainAppModel.getContext())

            SettingsSingleChoiceSegmentedButtons(
                label = stringResource(id = R.string.home),
                options = homeHorizontalOptions,
                selectedIndex = selectedHomeHorizontalIndex,
                onSelectedIndexChange = { newIndex ->
                    changeHomeAlignment(mainAppModel.getContext(), newIndex)
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
            val selectedHomeVerticalIndex = getHomeVAlignmentAsInt(mainAppModel.getContext())

            SettingsSingleChoiceSegmentedButtons(
                label = "",
                options = homeVerticalOptions,
                selectedIndex = selectedHomeVerticalIndex,
                onSelectedIndexChange = { newIndex ->
                    changeHomeVAlignment(mainAppModel.getContext(), newIndex)
                })
        }

        item {
            val appsAlignmentOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            val selectedAppsAlignmentIndex = getAppsAlignmentAsInt(mainAppModel.getContext())

            SettingsSingleChoiceSegmentedButtons(
                label = stringResource(id = R.string.apps),
                options = appsAlignmentOptions,
                selectedIndex = selectedAppsAlignmentIndex,
                onSelectedIndexChange = { newIndex ->
                    changeAppsAlignment(mainAppModel.getContext(), newIndex)
                },
                isBottomOfGroup = true // Last item in this section before any potential new sections
            )
        }

        // Search settings
        item { SettingsSubheading(stringResource(R.string.search)) }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.search_box), checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ShowSearchBox), true
                ), isTopOfGroup = true, onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.ShowSearchBox)
                    )
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.auto_open), checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.SearchAutoOpen)
                ), isBottomOfGroup = false, onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.SearchAutoOpen)
                    )
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.search_at_bottom), checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.bottomSearch), false
                ), isBottomOfGroup = false, onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.bottomSearch)
                    )
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.apps_list_auto_search),
                checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.appsListAutoSearch), false
                ),
                isBottomOfGroup = true,
                onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.appsListAutoSearch)
                    )
                })
        }

        //Screen time
        item { SettingsSubheading(stringResource(R.string.screen_time)) }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.screen_time_on_app),
                checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnApp)
                ),
                isTopOfGroup = true,
                onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.ScreenTimeOnApp)
                    )
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.hide_screen_time_page),
                checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.hideScreenTimePage)
                ),
                onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.hideScreenTimePage)
                    )
                })
        }

        item {
            SettingsSwitch(
                label = stringResource(id = R.string.screen_time_on_home_screen),
                checked = getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnHome)
                ),
                isBottomOfGroup = true,
                onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.ScreenTimeOnHome)
                    )
                })
        }

        //Apps
        item {
            SettingsSubheading(
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

        // New Settings
        item { SettingsSubheading(stringResource(id = R.string.new_settings)) }

        item {
            SettingsNavigationItem(
                label = stringResource(id = R.string.set_app_countdown_time),
                false,
                isTopOfGroup = true,
                isBottomOfGroup = true,
                onClick = { navController.navigate("newSettingsScreen") })
        }

        //Other
        item { SettingsSubheading(stringResource(id = R.string.other)) }

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
                    label = stringResource(id = R.string.Analytics), checked = getBooleanSetting(
                        mainAppModel.getContext(), stringResource(R.string.Analytics), true
                    ), onCheckedChange = {
                        toggleBooleanSetting(
                            mainAppModel.getContext(),
                            it,
                            mainAppModel.getContext().resources.getString(R.string.Analytics)
                        )
                    })
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
            SponsorBox(
                stringResource(id = R.string.app_name) + " " + stringResource(id = R.string.app_version),
                secondText = stringResource(R.string.app_flavour),
                onSponsorClick = {
                    val url = "https://github.com/sponsors/GeorgeClensy"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.setData(url.toUri())
                    i.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    mainAppModel.getContext().startActivity(i)
                },
                onBackgroundClick = {
                    navController.navigate("devOptions")
                })
        }

        item { SettingsSpacer() }
        item { SettingsSpacer() }
    }

    if (showWeatherAppPicker) {
        WeatherAppPicker(
            apps = homeScreenModel.installedApps,
            onAppSelected = { app ->
                setStringSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().getString(R.string.weather_app_package),
                    app.packageName
                )
                showWeatherAppPicker = false
            },
            onDismiss = { showWeatherAppPicker = false }
        )
    }
}

/**
 * Theme options in settings
 *
 * @param mainAppModel Main app model for theme updates
 * @param context Needed to run some functions used within ThemeOptions
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("AssignedValueIsNeverRead")
@Composable
fun ThemeOptions(
    mainAppModel: MainAppModel, context: Context, goBack: () -> Unit
) {
    val settingToChange = stringResource(R.string.theme)
    val autoThemeChange = stringResource(R.string.autoThemeSwitch)
    val dSettingToChange = stringResource(R.string.dTheme)
    val lSettingToChange = stringResource(R.string.lTheme)

    // Current highlighted theme card
    var currentHighlightedThemeCard by remember { mutableIntStateOf(-1) }

    // Current selected themes
    var currentSelectedTheme by remember {
        mutableIntStateOf(getIntSetting(context, settingToChange, -1))
    }
    var currentSelectedDTheme by remember {
        mutableIntStateOf(getIntSetting(context, dSettingToChange, -1))
    }
    var currentSelectedLTheme by remember {
        mutableIntStateOf(getIntSetting(context, lSettingToChange, -1))
    }

    val isDark = isSystemInDarkTheme()

    // Initialize selection states based on settings
    LaunchedEffect(Unit) {
        if (!getBooleanSetting(context, autoThemeChange, false)) {
            currentSelectedDTheme = -1
            currentSelectedLTheme = -1
        } else {
            currentSelectedTheme = -1
        }
    }

    val backgroundInteractionSource = remember { MutableInteractionSource() }

    val themeIds = listOf(11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {
                    currentHighlightedThemeCard = -1
                },
                indication = null,
                onLongClick = {},
                interactionSource = backgroundInteractionSource
            )
    ) {
        item {
            SettingsHeader(goBack, stringResource(R.string.theme))
        }
        item {
            SettingsSwitch(
                stringResource(R.string.syncLightDark), getBooleanSetting(
                    context, context.getString(R.string.autoThemeSwitch), false
                ), isTopOfGroup = true, onCheckedChange = { switch ->
                    // Disable normal selection box or set it correctly
                    currentSelectedTheme = if (switch) {
                        -1
                    } else {
                        getIntSetting(context, settingToChange, 11)
                    }

                    if (switch) {
                        currentSelectedDTheme =
                            getIntSetting(context, dSettingToChange, -1)
                        currentSelectedLTheme =
                            getIntSetting(context, lSettingToChange, -1)
                    } else {
                        currentSelectedDTheme = -1
                        currentSelectedLTheme = -1
                    }

                    // Remove the light dark button
                    currentHighlightedThemeCard = -1

                    if (switch) {
                        currentSelectedTheme = -1

                        currentSelectedDTheme =
                            getIntSetting(context, dSettingToChange, -1)
                        currentSelectedLTheme =
                            getIntSetting(context, lSettingToChange, -1)


                        val newThemeId = if (isDark) {
                            currentSelectedDTheme
                        } else {
                            currentSelectedLTheme
                        }

                        if (newThemeId != -1) {
                            mainAppModel.appTheme.value = AppTheme.fromId(newThemeId)
                        }

                    } else {
                        currentSelectedTheme =
                            getIntSetting(context, settingToChange, 11)

                        currentSelectedDTheme = -1
                        currentSelectedLTheme = -1

                        if (currentSelectedTheme != -1) {
                            mainAppModel.appTheme.value =
                                AppTheme.fromId(currentSelectedTheme)
                        }
                    }

                    currentHighlightedThemeCard = -1

                    setBooleanSetting(
                        context,
                        context.getString(R.string.autoThemeSwitch),
                        switch
                    )
                })
        }
        item {
            val backgroundColor = mainAppModel.appTheme.value.resolveColorScheme().background
            SettingsButton(
                label = stringResource(R.string.match_system_wallpaper),
                isBottomOfGroup = true,
                onClick = {
                    AppUtils.setSolidColorWallpaperHomeScreen(
                        mainAppModel.getContext(), backgroundColor
                    )
                })
        }
        item {
            SettingsSpacer()
        }
        itemsIndexed(themeIds, key = { _, themeId -> themeId }) { index, themeId ->
            val isSelected = currentSelectedTheme == themeId
            val isDSelected = currentSelectedDTheme == themeId
            val isLSelected = currentSelectedLTheme == themeId
            val showLightDarkPicker = currentHighlightedThemeCard == themeId

            ThemeCard(
                theme = themeId,
                showLightDarkPicker = remember(showLightDarkPicker) {
                    mutableStateOf(
                        showLightDarkPicker
                    )
                },
                isSelected = remember(isSelected) { mutableStateOf(isSelected) },
                isDSelected = remember(isDSelected) { mutableStateOf(isDSelected) },
                isLSelected = remember(isLSelected) { mutableStateOf(isLSelected) },
                updateLTheme = { theme ->
                    setIntSetting(context, context.getString(R.string.lTheme), theme)
                    mainAppModel.appTheme.value = AppTheme.fromId(themeId)
                    currentSelectedLTheme = theme
                    currentHighlightedThemeCard = -1
                },
                updateDTheme = { theme ->
                    setIntSetting(context, context.getString(R.string.dTheme), theme)
                    mainAppModel.appTheme.value = AppTheme.fromId(themeId)
                    currentSelectedDTheme = theme
                    currentHighlightedThemeCard = -1
                },
                modifier = Modifier.fillMaxWidth(),
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == themeIds.size - 1,
                onClick = { theme ->
                    if (getBooleanSetting(
                            context, context.getString(R.string.autoThemeSwitch), false
                        )
                    ) {
                        // For auto theme mode, show light/dark picker
                        currentHighlightedThemeCard = theme
                    } else {
                        // For single theme mode, just set the theme
                        setIntSetting(context, context.getString(R.string.theme), theme)
                        mainAppModel.appTheme.value = AppTheme.fromId(themeId)
                        currentSelectedTheme = theme
                    }
                })
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
 * Widget Setup
 *
 * @param context Context is required for some functions
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@Suppress("AssignedValueIsNeverRead", "VariableNeverRead")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetOptions(context: Context, goBack: () -> Unit) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = remember { AppWidgetHost(context, WIDGET_HOST_ID) }
    var appWidgetId by remember { mutableIntStateOf(getSavedWidgetId(context)) }
    var appWidgetHostView by remember { mutableStateOf<AppWidgetHostView?>(null) }
    var showCustomPicker by remember { mutableStateOf(false) }

    // Called whenever the widget cannot be loaded or bound
    fun widgetCouldNotBind(message: String) {
        Log.e("Widgets", "Widget could not bind: $message")
        goBack()
    }

    // Shows the widget permission dialog
    val bindWidgetPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            widgetCouldNotBind("User denied widget bind permission")
        } else {
            // Re-attempt to bind the widget after permission is granted
            val tempId = appWidgetHost.allocateAppWidgetId()
            val dummyProviders = appWidgetManager.installedProviders
            val testProvider = dummyProviders.firstOrNull()

            if (testProvider != null) {
                try {
                    if (appWidgetManager.bindAppWidgetIdIfAllowed(tempId, testProvider.provider)) {
                        // Widget bound, now proceed with setup if needed or just acknowledge
                        // For now, we just acknowledge and let the user select a widget from the picker
                    } else {
                        widgetCouldNotBind("Widget binding still not allowed after permission grant.")
                    }
                } catch (e: Exception) {
                    widgetCouldNotBind("Failed to re-bind widget after permission grant: ${e.message}")
                }
            } else {
                widgetCouldNotBind("No available widget providers found after permission grant.")
            }
        }
    }

    LaunchedEffect(Unit) {
        val tempId = appWidgetHost.allocateAppWidgetId()
        val dummyProviders = appWidgetManager.installedProviders
        val testProvider = dummyProviders.firstOrNull()

        if (testProvider != null) {
            val alreadyAllowed =
                appWidgetManager.bindAppWidgetIdIfAllowed(tempId, testProvider.provider)

            if (!alreadyAllowed) {
                val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, tempId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, testProvider.provider)
                }
                try {
                    bindWidgetPermissionLauncher.launch(bindIntent)
                } catch (e: Exception) {
                    widgetCouldNotBind("Failed to launch widget bind request: ${e.message}")
                }
            }
        } else {
            widgetCouldNotBind("No available widget providers found to check permission")
        }
    }

    // Common setup logic for binding/creating widget
    fun setupWidget(widgetId: Int, info: AppWidgetProviderInfo?) {
        if (widgetId == -1) {
            widgetCouldNotBind("Invalid widget ID (-1)")
            return
        }
        if (info == null) {
            widgetCouldNotBind("AppWidgetProviderInfo was null")
            return
        }

        appWidgetId = widgetId
        saveWidgetId(context, widgetId)

        if (isWidgetConfigurable(context, widgetId)) {
            try {
                launchWidgetConfiguration(context, info, widgetId)
            } catch (e: Exception) {
                widgetCouldNotBind("Failed to launch widget configuration: ${e.message}")
            }
        } else {
            try {
                appWidgetHostView = appWidgetHost.createView(context, widgetId, info).apply {
                    setAppWidget(widgetId, info)
                }
            } catch (e: Exception) {
                widgetCouldNotBind("Failed to create widget view: ${e.message}")
            }
        }
    }

    if (showCustomPicker) {
        CustomWidgetPicker(
            onWidgetSelected = { info ->
                val newId = appWidgetHost.allocateAppWidgetId()

                try {
                    if (appWidgetManager.bindAppWidgetIdIfAllowed(newId, info.provider)) {
                        setupWidget(newId, info)
                        showCustomPicker = false
                    } else {
                        widgetCouldNotBind("AppWidgetManager refused to bind the selected widget")
                    }
                } catch (e: Exception) {
                    widgetCouldNotBind("Exception while trying to bind widget: ${e.message}")
                }
            },
            onDismiss = { showCustomPicker = false }
        )
    }


    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    )
    {
        item { SettingsHeader(goBack, stringResource(R.string.widget)) }

        item {
            SettingsButton(
                label = stringResource(R.string.remove_widget),
                isTopOfGroup = true,
                onClick = {
                    removeWidget(context)
                    appWidgetHostView = null
                    appWidgetId = -1
                }
            )
        }

        item {
            SettingsButton(
                label = stringResource(R.string.select_widget),
                isBottomOfGroup = true,
                onClick = { showCustomPicker = true }
            )
        }

        item { SettingsSpacer() }

        // Offset slider
        item {
            var offset by remember { mutableFloatStateOf(getWidgetOffset(context)) }
            SettingsSlider(
                label = stringResource(R.string.offset),
                value = offset,
                onValueChange = {
                    offset = it
                    setWidgetOffset(context, offset)
                },
                valueRange = -20f..20f,
                steps = 19,
                onReset = {
                    offset = 0f
                    setWidgetOffset(context, offset)
                },
                isTopOfGroup = true
            )
        }

        // Height slider
        item {
            var height by remember { mutableFloatStateOf(getWidgetHeight(context)) }
            SettingsSlider(
                label = stringResource(R.string.height),
                value = height,
                onValueChange = {
                    height = it
                    setWidgetHeight(context, height)
                },
                valueRange = 100f..400f,
                steps = 9,
                onReset = {
                    height = 125f
                    setWidgetHeight(context, height)
                }
            )
        }

        // Width slider
        item {
            var width by remember { mutableFloatStateOf(getWidgetWidth(context)) }
            SettingsSlider(
                label = stringResource(R.string.width),
                value = width,
                onValueChange = {
                    width = it
                    setWidgetWidth(context, width)
                },
                valueRange = 100f..400f,
                steps = 9,
                onReset = {
                    width = 250f
                    setWidgetWidth(context, width)
                },
                isBottomOfGroup = true
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
@Suppress("AssignedValueIsNeverRead")
@Composable
fun HiddenApps(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    goToManageHiddenApps: () -> Unit,
    goBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var hiddenAppsList by remember { mutableStateOf(mainAppModel.hiddenAppsManager.getHiddenApps()) }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsHeader(goBack, stringResource(R.string.hidden_apps))
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
                checked = getBooleanSetting(
                    mainAppModel.getContext(),
                    stringResource(R.string.showHiddenAppsInSearch),
                    false
                ),
                onCheckedChange = {
                    toggleBooleanSetting(
                        mainAppModel.getContext(),
                        it,
                        mainAppModel.getContext().resources.getString(R.string.showHiddenAppsInSearch)
                    )
                },
                isBottomOfGroup = true
            )
        }

        item {
            SettingsSubheading(stringResource(R.string.swipe_to_show_app))
        }

        items(
            items = hiddenAppsList,
            key = { it } // use package name as unique key
        ) { appPackageName ->
            // Animate the removal of the item
            var visible by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = visible,
                exit = fadeOut(animationSpec = tween(500))
            ) {
                SettingsSwipeableButton(
                    label = AppUtils.getAppNameFromPackageName(
                        mainAppModel.getContext(),
                        appPackageName
                    ),
                    onClick = {
                        val app =
                            homeScreenModel.installedApps.find { it.packageName == appPackageName }
                                ?: AppUtils.getInstalledAppFromPackageName(
                                    mainAppModel.getContext(),
                                    appPackageName
                                )

                        app?.let {
                            AppUtils.openApp(
                                app = it,
                                overrideOpenChallenge = false,
                                openChallengeShow = homeScreenModel.showOpenChallenge,
                                mainAppModel = mainAppModel,
                                homeScreenModel = homeScreenModel
                            )
                        }

                        resetHome(homeScreenModel)
                    },
                    onDeleteClick = {
                        // Trigger haptic feedback
                        AppUtils.doHapticFeedBack(haptics)
                        // Animate item out
                        visible = false
                        // Remove from your list after a short delay to let animation run
                        coroutineScope.launch {
                            delay(500)
                            mainAppModel.hiddenAppsManager.removeHiddenApp(appPackageName)
                            mainAppModel.notifyHiddenAppsChanged()
                            hiddenAppsList = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }
                    },
                    isTopOfGroup = hiddenAppsList.firstOrNull() == appPackageName,
                    isBottomOfGroup = hiddenAppsList.lastOrNull() == appPackageName
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
fun ChooseFont(context: Context, activity: Activity, goBack: () -> Unit) {
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
        item { SettingsHeader(goBack, stringResource(R.string.font)) }

        itemsIndexed(fontNames) { index, fontName ->
            SettingsButton(
                label = fontName,
                onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), fontName)
                    resetActivity(context, activity)
                },
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == fontNames.lastIndex,
                fontFamily = getFontFamily(context, fontName)
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
fun DevOptions(mainAppModel: MainAppModel, context: Context, goBack: () -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item { SettingsHeader(goBack, "Developer Options") }

        item {
            SettingsSwitch(
                "First time",
                getBooleanSetting(context, "FirstTime", false),
                onCheckedChange = {
                    setBooleanSetting(context, "FirstTime", it)
                    setBooleanSetting(
                        context,
                        mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                        it
                    )
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
                    setStringSetting(context, context.getString(R.string.weather_app_package), "")
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
                    val doubleTapEnabled = getBooleanSetting(
                        context,
                        context.getString(R.string.DoubleTapToLock),
                        false
                    )
                    if (doubleTapEnabled) {
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
 * @param showPolicyDialog Pass the MutableState<Boolean> your using to show and hide this dialog so that it can be hidden from within it
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

@Composable
fun AppCountdownTime(context: Context, goBack: () -> Unit) {
    var countdownTime by remember { mutableFloatStateOf(getCountdownTime(context)) }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item { SettingsHeader(goBack, stringResource(R.string.new_settings)) }

        itemsIndexed(CountdownMode.entries) {index, mode ->
            SettingsButton(
                label = stringResource(mode.labelRes),
                isSelected = countdownTime == mode.value,
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == CountdownMode.entries.size - 1,
                onClick = {
                    countdownTime = mode.value
                    setCountdownTime(context, mode.value)
                }
            )
        }

        item { SettingsSpacer() }

        item {
            SettingsSlider(
                label = stringResource(R.string.set_app_countdown_time_slider),
                value = countdownTime,
                onValueChange = {
                    countdownTime = it
                    setCountdownTime(context, countdownTime)
                },
                valueRange = 1f..5f,
                steps = 3,
                onReset = {
                    countdownTime = resetAndGetCountdownTime(context)
                },
                isTopOfGroup = true,
                isBottomOfGroup = true
            )
        }

        item { SettingsSpacer() }
    }
}
