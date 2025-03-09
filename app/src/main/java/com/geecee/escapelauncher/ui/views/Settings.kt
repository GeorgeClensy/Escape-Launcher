package com.geecee.escapelauncher.ui.views

import android.app.Activity
import android.app.ActivityOptions
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.PitchDarkColorScheme
import com.geecee.escapelauncher.ui.theme.darkScheme
import com.geecee.escapelauncher.ui.theme.darkSchemeBlue
import com.geecee.escapelauncher.ui.theme.darkSchemeGreen
import com.geecee.escapelauncher.ui.theme.darkSchemeRed
import com.geecee.escapelauncher.ui.theme.darkSchemeYellow
import com.geecee.escapelauncher.ui.theme.lightScheme
import com.geecee.escapelauncher.ui.theme.lightSchemeBlue
import com.geecee.escapelauncher.ui.theme.lightSchemeGreen
import com.geecee.escapelauncher.ui.theme.lightSchemeRed
import com.geecee.escapelauncher.ui.theme.lightSchemeYellow
import com.geecee.escapelauncher.ui.theme.offLightScheme
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.loadTextFromAssets
import com.geecee.escapelauncher.utils.changeAppsAlignment
import com.geecee.escapelauncher.utils.changeHomeAlignment
import com.geecee.escapelauncher.utils.changeHomeVAlignment
import com.geecee.escapelauncher.utils.changeLauncher
import com.geecee.escapelauncher.utils.changeTheme
import com.geecee.escapelauncher.utils.getAppsAlignmentAsInt
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.getHomeAlignmentAsInt
import com.geecee.escapelauncher.utils.getHomeVAlignmentAsInt
import com.geecee.escapelauncher.utils.getSavedWidgetId
import com.geecee.escapelauncher.utils.getWidgetHeight
import com.geecee.escapelauncher.utils.getWidgetOffset
import com.geecee.escapelauncher.utils.getWidgetWidth
import com.geecee.escapelauncher.utils.isWidgetConfigurable
import com.geecee.escapelauncher.utils.launchWidgetConfiguration
import com.geecee.escapelauncher.utils.openWidgetPicker
import com.geecee.escapelauncher.utils.removeWidget
import com.geecee.escapelauncher.utils.resetActivity
import com.geecee.escapelauncher.utils.saveWidgetId
import com.geecee.escapelauncher.utils.setBooleanSetting
import com.geecee.escapelauncher.utils.setStringSetting
import com.geecee.escapelauncher.utils.setWidgetHeight
import com.geecee.escapelauncher.utils.setWidgetOffset
import com.geecee.escapelauncher.utils.setWidgetWidth
import com.geecee.escapelauncher.utils.toggleBooleanSetting
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

/**
 * Settings title header with back button
 *
 * @param title The text shown on the header
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsHeader(goBack: () -> Unit, title: String) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = { goBack() })
            .padding(0.dp, 120.dp, 0.dp, 0.dp)
            .height(70.dp) // Set a fixed height for the header
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Go Back",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontSize = if (title.length > 11) 35.sp else MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

/**
 * Switch for setting with a label on the left
 *
 * @param label The text for the label
 * @param checked Whether the switch is on or not
 * @param onCheckedChange Function with Boolean passed thats executed when the switch is pressed
 */
@Composable
fun SettingsSwitch(
    label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(checked) }

    Box(Modifier.fillMaxWidth()) {
        Text(
            label,
            Modifier.padding(0.dp, 15.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Switch(
            checked = isChecked, onCheckedChange = {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            }, Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * Settings navigation item with label and arrow
 *
 * @param label The text to be shown
 * @param diagonalArrow Whether the arrow should be pointed upwards to signal that pressing this will take you out of Escape Launcher
 * @param onClick When composable is clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsNavigationItem(
    label: String, diagonalArrow: Boolean?, onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            Modifier.padding(0.dp, 15.dp).fillMaxWidth(0.9f).fillMaxHeight(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Left,
        )
        if (!diagonalArrow!!) {
            Icon(
                Icons.AutoMirrored.Default.KeyboardArrowRight,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxSize(0.1f)
                    .fillMaxHeight(),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            Icon(
                Icons.AutoMirrored.Default.KeyboardArrowRight,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxWidth(0.1f)
                    .fillMaxHeight()
                    .rotate(-45f),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

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
    goBack: () -> Unit,
    activity: Activity,
) {
    val showPolicyDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(20.dp, 0.dp, 20.dp, 0.dp)
    ) {

        val navController = rememberNavController()

        NavHost(navController = navController, "mainSettingsPage") {
            composable("mainSettingsPage",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                MainSettingsPage(
                    { goBack() },
                    { showPolicyDialog.value = true },
                    navController,
                    mainAppModel
                )
            }
            composable("alignmentOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                AlignmentOptions(mainAppModel.getContext()) { navController.popBackStack() }
            }
            composable("hiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                HiddenApps(
                    mainAppModel
                ) { navController.popBackStack() }
            }
            composable("openChallenges",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                OpenChallenges(
                    mainAppModel
                ) { navController.popBackStack() }
            }
            composable("chooseFont",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ChooseFont(mainAppModel.getContext(), activity) { navController.popBackStack() }
            }
            composable("devOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DevOptions(context = mainAppModel.getContext()) { navController.popBackStack() }
            }
            composable("theme",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ThemeOptions(mainAppModel.getContext(), activity) { navController.popBackStack() }
            }
            composable("personalization",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                PersonalizationOptions(mainAppModel, navController) { navController.popBackStack() }
            }
            composable("widget",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                WidgetOptions(mainAppModel.getContext()) { navController.popBackStack() }
            }
        }
    }

    if (showPolicyDialog.value) {
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    showPolicyDialog: () -> Unit,
    navController: NavController,
    mainAppModel: MainAppModel
) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.settings))

        SettingsNavigationItem(label = stringResource(id = R.string.personalization),
            false,
            onClick = { navController.navigate("personalization") })

        SettingsNavigationItem(label = stringResource(id = R.string.manage_hidden_apps),
            false,
            onClick = { navController.navigate("hiddenApps") })

        SettingsNavigationItem(label = stringResource(id = R.string.manage_open_challenges),
            false,
            onClick = { navController.navigate("openChallenges") })

        SettingsSwitch(label = stringResource(id = R.string.Analytics), checked = getBooleanSetting(
            mainAppModel.getContext(), stringResource(R.string.Analytics), true
        ), onCheckedChange = {
            toggleBooleanSetting(
                mainAppModel.getContext(),
                it,
                mainAppModel.getContext().resources.getString(R.string.Analytics)
            )
        })

        SettingsNavigationItem(label = stringResource(id = R.string.read_privacy_policy),
            false,
            onClick = { showPolicyDialog() })

        SettingsNavigationItem(label = stringResource(id = R.string.make_default_launcher),
            true,
            onClick = { changeLauncher(mainAppModel.getContext()) })

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Text(
            stringResource(id = R.string.escape_launcher) + " " + stringResource(id = R.string.app_version),
            Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {}, onLongClick = {
                    navController.navigate("devOptions")
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(25.dp))
    }
}

/**
 * Personalization options in settings
 *
 * @param mainAppModel This is required for settings to be changed
 * @param navController Settings nav controller with "alignmentOptions", "chooseFont", "theme", "widget"
 * @param goBack When back button is pressed
 *
 * @see Settings
 * @see MainSettingsPage
 */
@Composable
fun PersonalizationOptions(
    mainAppModel: MainAppViewModel,
    navController: NavController,
    goBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.personalization))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        SettingsSwitch(label = stringResource(id = R.string.search_box),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowSearchBox), true
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ShowSearchBox)
                )
            })

        SettingsSwitch(label = stringResource(id = R.string.auto_open), checked = getBooleanSetting(
            mainAppModel.getContext(), stringResource(R.string.SearchAutoOpen)
        ), onCheckedChange = {
            toggleBooleanSetting(
                mainAppModel.getContext(),
                it,
                mainAppModel.getContext().resources.getString(R.string.SearchAutoOpen)
            )
        })

        SettingsSwitch(label = stringResource(id = R.string.show_clock),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowClock), true
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ShowClock)
                )
            })

        SettingsSwitch(label = stringResource(id = R.string.big_clock), checked = getBooleanSetting(
            mainAppModel.getContext(), stringResource(R.string.BigClock)
        ), onCheckedChange = {
            toggleBooleanSetting(
                mainAppModel.getContext(),
                it,
                mainAppModel.getContext().resources.getString(R.string.BigClock)
            )
        })

        SettingsSwitch(label = stringResource(id = R.string.haptic_feedback),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.Haptic), true
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.Haptic)
                )
            })

        SettingsSwitch(label = stringResource(id = R.string.screen_time_on_app),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnApp)
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ScreenTimeOnApp)
                )
            })

        SettingsSwitch(label = stringResource(id = R.string.screen_time_on_home_screen),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnHome)
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ScreenTimeOnHome)
                )
            })

        SettingsNavigationItem(label = stringResource(id = R.string.widget),
            false,
            onClick = { navController.navigate("widget") })

        SettingsNavigationItem(label = stringResource(id = R.string.theme),
            false,
            onClick = { navController.navigate("theme") })

        SettingsNavigationItem(label = stringResource(id = R.string.alignments),
            false,
            onClick = { navController.navigate("alignmentOptions") })

        SettingsNavigationItem(label = stringResource(id = R.string.choose_font),
            false,
            onClick = { navController.navigate("chooseFont") })

        Spacer(Modifier.height(120.dp))
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetOptions(context: Context, goBack: () -> Unit) {
    var needsConfiguration by remember { mutableStateOf(false) }
    val appWidgetManager = AppWidgetManager.getInstance(context)
    var appWidgetId by remember { mutableIntStateOf(getSavedWidgetId(context)) }
    var appWidgetHostView by remember { mutableStateOf<AppWidgetHostView?>(null) }
    val appWidgetHost = remember { AppWidgetHost(context, 1) }
    val widgetPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                ?: return@rememberLauncherForActivityResult
            saveWidgetId(context, appWidgetId)
            val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
            needsConfiguration = isWidgetConfigurable(context, appWidgetId)

            if (needsConfiguration) {
                launchWidgetConfiguration(context, appWidgetId)
            } else {
                appWidgetHostView =
                    appWidgetHost.createView(context, appWidgetId, widgetInfo).apply {
                        setAppWidget(appWidgetId, widgetInfo)
                    }
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
    ) {
        SettingsHeader(goBack, stringResource(R.string.widget))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Button(modifier = Modifier.fillMaxWidth(), onClick = { removeWidget(context) }) {
            Text(stringResource(R.string.remove_widget))
        }

        Spacer(Modifier.height(10.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { openWidgetPicker(appWidgetHost, widgetPickerLauncher) }) {
            Text(stringResource(R.string.select_widget))
        }

        Spacer(Modifier.height(20.dp))

        Box(
            Modifier.fillMaxWidth()
        )
        {
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            Row {
                Text(
                    stringResource(id = R.string.offset),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                sliderPosition = getWidgetOffset(context)

                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        setWidgetOffset(context, sliderPosition)
                    },
                    valueRange = -20f..20f,
                    steps = 40,
                    modifier = Modifier
                        .fillMaxWidth(0.85F)
                        .align(Alignment.CenterVertically)
                        .padding(20.dp, 0.dp, 20.dp, 0.dp)
                )
            }
            Icon(
                Icons.Default.Refresh,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterEnd)
                    .combinedClickable {
                        sliderPosition = 0F
                        setWidgetOffset(context, sliderPosition)
                    }
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

        }

        Box(
            Modifier.fillMaxWidth()
        )
        {
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            Row {
                Text(
                    stringResource(id = R.string.height),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                sliderPosition = getWidgetHeight(context)

                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        setWidgetHeight(context, sliderPosition)
                    },
                    valueRange = 100f..500f,
                    steps = 10,
                    modifier = Modifier
                        .fillMaxWidth(0.85F)
                        .align(Alignment.CenterVertically)
                        .padding(20.dp, 0.dp, 20.dp, 0.dp)
                )
            }
            Icon(
                Icons.Default.Refresh,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterEnd)
                    .combinedClickable {
                        sliderPosition = 125F
                        setWidgetHeight(context, sliderPosition)
                    }
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

        }

        Box(
            Modifier.fillMaxWidth()
        )
        {
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            Row {
                Text(
                    stringResource(id = R.string.width),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                sliderPosition = getWidgetWidth(context)

                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        setWidgetWidth(context, sliderPosition)
                    },
                    valueRange = 150f..350f,
                    steps = 10,
                    modifier = Modifier
                        .fillMaxWidth(0.85F)
                        .align(Alignment.CenterVertically)
                        .padding(20.dp, 0.dp, 20.dp, 0.dp)
                )
            }
            Icon(
                Icons.Default.Refresh,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterEnd)
                    .combinedClickable {
                        sliderPosition = 150F
                        setWidgetWidth(context, sliderPosition)
                    }
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

        }
    }
}

/**
 * Alignment Options in Settings
 *
 * @param context Context is required by some functions used withing AlignmentOptions
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@Composable
fun AlignmentOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.alignments))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                stringResource(id = R.string.home),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedIndex by remember {
                mutableIntStateOf(
                    getHomeAlignmentAsInt(context)
                )
            }
            val options = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = options.size
                        ), onClick = {
                            selectedIndex = index
                            changeHomeAlignment(context, selectedIndex)
                        }, selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }

            var selectedVerticalIndex by remember {
                mutableIntStateOf(
                    getHomeVAlignmentAsInt(context)
                )
            }
            val optionsVertical = listOf(
                stringResource(R.string.top),
                stringResource(R.string.center),
                stringResource(R.string.bottom)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                optionsVertical.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = optionsVertical.size
                        ), onClick = {
                            selectedVerticalIndex = index
                            changeHomeVAlignment(context, selectedVerticalIndex)
                        }, selected = index == selectedVerticalIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                stringResource(id = R.string.apps),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedIndex by remember {
                mutableIntStateOf(
                    getAppsAlignmentAsInt(context)
                )
            }
            val options = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = options.size
                        ), onClick = {
                            selectedIndex = index
                            changeAppsAlignment(context, selectedIndex)
                        }, selected = index == selectedIndex
                    ) {
                        Text(label)
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
 * @see com.geecee.escapelauncher.ui.theme.EscapeTheme
 */
@Composable
fun ThemeCard(
    theme: Int, context: Context, activity: Activity
) {
    Box(
        Modifier
            .size(120.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                changeTheme(theme, context, activity)
            }
            .background(
                when (theme) {
                    0 -> darkScheme.background

                    1 -> lightScheme.background

                    2 -> PitchDarkColorScheme.background

                    3 -> {
                        lightSchemeRed.background
                    }

                    4 -> {
                        darkSchemeRed.background
                    }

                    5 -> {
                        lightSchemeGreen.background
                    }

                    6 -> {
                        darkSchemeGreen.background
                    }

                    7 -> {
                        lightSchemeBlue.background
                    }

                    8 -> {
                        darkSchemeBlue.background
                    }

                    9 -> {
                        lightSchemeYellow.background
                    }

                    10 -> {
                        darkSchemeYellow.background
                    }

                    11 -> {
                        offLightScheme.background
                    }

                    else -> darkScheme.background
                }
            )
    ) {
        Text(
            when (theme) {
                0 -> stringResource(R.string.dark)

                1 -> stringResource(R.string.light)

                2 -> stringResource(R.string.pitch_black)

                3 -> stringResource(R.string.light_red)

                4 -> stringResource(R.string.dark_red)

                5 -> stringResource(R.string.light_green)

                6 -> stringResource(R.string.dark_green)

                7 -> stringResource(R.string.light_blue)

                8 -> stringResource(R.string.dark_blue)

                9 -> stringResource(R.string.light_yellow)

                10 -> stringResource(R.string.dark_yellow)

                11 -> stringResource(R.string.off_white)

                else -> stringResource(R.string.theme)
            },
            Modifier
                .align(Alignment.Center)
                .padding(5.dp), when (theme) {
                0 -> darkScheme.onPrimaryContainer

                1 -> lightScheme.onPrimaryContainer

                2 -> PitchDarkColorScheme.onPrimaryContainer

                3 -> {
                    lightSchemeRed.onPrimaryContainer
                }

                4 -> {
                    darkSchemeRed.onPrimaryContainer
                }

                5 -> {
                    lightSchemeGreen.onPrimaryContainer
                }

                6 -> {
                    darkSchemeGreen.onPrimaryContainer
                }

                7 -> {
                    lightSchemeBlue.onPrimaryContainer
                }

                8 -> {
                    darkSchemeBlue.onPrimaryContainer
                }

                9 -> {
                    lightSchemeYellow.onPrimaryContainer
                }

                10 -> {
                    darkSchemeYellow.onPrimaryContainer
                }

                11 -> {
                    offLightScheme.onPrimaryContainer
                }

                else -> darkScheme.onPrimaryContainer
            }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center
        )
    }
}

/**
 * Theme options in settings
 *
 * @param context Needed to run some functions used within ThemeOptions
 * @param activity Needed to reload app after changing theme
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@Composable
fun ThemeOptions(
    context: Context, activity: Activity, goBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsHeader(goBack, stringResource(R.string.theme))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp, 0.dp, 0.dp))

        LazyVerticalGrid(
            GridCells.Adaptive(minSize = 128.dp)
        ) {
            item {
                ThemeCard(1, context, activity)
            }
            item {
                ThemeCard(0, context, activity)
            }
            item {
                ThemeCard(11, context, activity)
            }
            item {
                ThemeCard(2, context, activity)
            }
            item {
                ThemeCard(3, context, activity)
            }
            item {
                ThemeCard(4, context, activity)
            }
            item {
                ThemeCard(5, context, activity)
            }
            item {
                ThemeCard(6, context, activity)
            }
            item {
                ThemeCard(7, context, activity)
            }
            item {
                ThemeCard(8, context, activity)
            }
            item {
                ThemeCard(9, context, activity)
            }
            item {
                ThemeCard(10, context, activity)
            }
        }
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
    mainAppModel: MainAppModel, goBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.hidden_apps))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        val haptics = LocalHapticFeedback.current
        val hiddenApps = remember { mutableStateOf(mainAppModel.hiddenAppsManager.getHiddenApps()) }

        for (app in hiddenApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    AppUtils.getAppNameFromPackageName(mainAppModel.getContext(), app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent =
                                mainAppModel.packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeBasic()
                                mainAppModel
                                    .getContext()
                                    .startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )

                Icon(
                    Icons.Sharp.Close,
                    "",
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .fillMaxSize()
                        .combinedClickable(onClick = {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Page that lets you manage apps with open challenge
 *
 * @param mainAppModel Needed for context & open challenge apps manager
 * @param goBack Function run when back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpenChallenges(
    mainAppModel: MainAppModel, goBack: () -> Unit
) {
    val challengeApps =
        remember { mutableStateOf(mainAppModel.challengesManager.getChallengeApps()) }
    val haptics = LocalHapticFeedback.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.open_challenges))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        for (app in challengeApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    AppUtils.getAppNameFromPackageName(mainAppModel.getContext(), app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent =
                                mainAppModel.packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeBasic()
                                mainAppModel
                                    .getContext()
                                    .startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            mainAppModel.challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = mainAppModel.challengesManager.getChallengeApps()
                        }),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )

                Icon(
                    Icons.Sharp.Close,
                    "",
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .fillMaxSize()
                        .combinedClickable(onClick = {
                            mainAppModel.challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = mainAppModel.challengesManager.getChallengeApps()
                        }),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
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
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.font))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Text(
            "Jost",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Jost")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Inter",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Inter")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Lexend",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Lexend")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Work Sans",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "Work Sans"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Developer options in settings
 */
@Composable
fun DevOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, "Developer Options")

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Box(Modifier.fillMaxWidth()) {
            Text(
                "First time",
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getBooleanSetting(context, stringResource(R.string.FirstTime), false)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    setBooleanSetting(
                        context, context.resources.getString(R.string.FirstTime), true
                    )
                    setBooleanSetting(
                        context, context.resources.getString(R.string.FirstTimeAppDrawHelp), true
                    )
                }, Modifier.align(Alignment.CenterEnd)
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                ) {
                    Text("OK")
                }
            }
        }
    }
}