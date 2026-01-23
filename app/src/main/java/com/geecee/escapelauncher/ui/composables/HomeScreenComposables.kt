package com.geecee.escapelauncher.ui.composables

import android.content.ComponentName
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.WorkOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.BackgroundColor
import com.geecee.escapelauncher.ui.theme.CardContainerColor
import com.geecee.escapelauncher.ui.theme.CardContainerColorDisabled
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.ui.theme.ContentColorDisabled
import com.geecee.escapelauncher.ui.theme.SecondaryCardContainerColor
import com.geecee.escapelauncher.ui.theme.primaryContentColor
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime
import com.geecee.escapelauncher.utils.AppUtils.getCurrentTime
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.InstalledApp
import com.geecee.escapelauncher.utils.PrivateAppItem
import com.geecee.escapelauncher.utils.WorkAppItem
import com.geecee.escapelauncher.utils.analyticsProxy
import com.geecee.escapelauncher.utils.getPrivateSpaceApps
import com.geecee.escapelauncher.utils.getStringSetting
import com.geecee.escapelauncher.utils.getWorkApps
import com.geecee.escapelauncher.utils.goToWorkAppAppInfo
import com.geecee.escapelauncher.utils.isDefaultLauncher
import com.geecee.escapelauncher.utils.isWorkProfileUnlocked
import com.geecee.escapelauncher.utils.lockPrivateSpace
import com.geecee.escapelauncher.utils.lockWorkProfile
import com.geecee.escapelauncher.utils.openPrivateSpaceApp
import com.geecee.escapelauncher.utils.openWorkApp
import com.geecee.escapelauncher.utils.showPrivateSpaceAppInfo
import com.geecee.escapelauncher.utils.uninstallPrivateSpaceApp
import com.geecee.escapelauncher.utils.uninstallWorkApp
import com.geecee.escapelauncher.utils.unlockWorkProfile
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Home Screen Item

/**
 * An item displayed on the HomeScreen or Apps list
 *
 * If [showScreenTime] is enabled and [screenTime] is not null the screen time is written next to the app name.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenItem(
    modifier: Modifier = Modifier,
    appName: String,
    screenTime: Long? = null,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    showScreenTime: Boolean = false,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (alignment) {
            Alignment.Start -> Arrangement.Start
            Alignment.CenterHorizontally -> Arrangement.Center
            Alignment.End -> Arrangement.End
            else -> Arrangement.Center
        },
        modifier = modifier
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = onAppLongClick
            )
            .fillMaxWidth()
    ) {
        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier.padding(vertical = 15.dp),
            color = primaryContentColor,
            style = MaterialTheme.typography.bodyMedium
        )

        // Optional screen time
        if (showScreenTime && screenTime != null) {
            Text(
                formatScreenTime(screenTime),
                modifier = Modifier
                    .padding(vertical = 15.dp, horizontal = 5.dp)
                    .alpha(0.5f),
                color = primaryContentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Clock to be shown on home screen
 */
@Composable
fun Clock(
    bigClock: Boolean, homeAlignment: Alignment.Horizontal, twelveHour: Boolean
) {
    var time by remember { mutableStateOf(getCurrentTime(twelveHour)) }
    val parts = time.split(":")
    val hours = parts[0]
    val minutes = parts[1]
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val secondsToNextMinute = 60 - now.get(Calendar.SECOND)
            delay(secondsToNextMinute * 1000L)
            time = getCurrentTime(twelveHour)
        }
    }

    if (bigClock) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(0.dp, 15.dp)
                .clickable {
                    try {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("Error", e.message.toString())
                    }
                }
        ) {
            // Hours row
            Row {
                // Ensure hours has two digits
                val hourDigits = if (hours.length == 1) "0$hours" else hours

                hourDigits.forEachIndexed { _, digit ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(40.dp)
                            .offset(0.dp, 35.dp)
                    ) {
                        Text(
                            text = digit.toString(),
                            color = primaryContentColor,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Minutes row
            Row {
                // Ensure minutes has two digits
                val minuteDigits = if (minutes.length == 1) "0$minutes" else minutes

                minuteDigits.forEachIndexed { _, digit ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Text(
                            text = digit.toString(),
                            color = primaryContentColor,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = time,
            color = primaryContentColor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable {
                    try {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("Error", e.message.toString())
                    }
                }
                .offset((-2).dp, 5.dp),
            textAlign = when (homeAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}

@Composable
fun Date(
    homeAlignment: Alignment.Horizontal,
    small: Boolean
) {
    val context = LocalContext.current

    val dateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormat.format(java.util.Date())
    }

    var date by remember { mutableStateOf(getCurrentDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val delayMillis = calendar.timeInMillis - now
            delay(delayMillis)
            date = getCurrentDate()
        }
    }

    Text(
        text = date,
        color = primaryContentColor,
        style = if (small) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge
        },
        fontWeight = FontWeight.W600,
        modifier = Modifier
            .padding(end = 10.dp)
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALENDAR)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                catch(e: Exception) {
                    analyticsProxy.recordException(e)
                }
            },
        textAlign = when (homeAlignment) {
            Alignment.Start -> TextAlign.Start
            Alignment.End -> TextAlign.End
            else -> TextAlign.Center
        }
    )
}

/**
 * Weather composable to be shown on home screen
 */
@Composable
fun Weather(
    homeAlignment: Alignment.Horizontal, mainAppModel: MainAppViewModel, small: Boolean
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        mainAppModel.updateWeather()
    }

    AnimatedVisibility(mainAppModel.weatherText.value != "", enter = fadeIn(), exit = fadeOut()) {
        Row(Modifier.padding(end = 10.dp)) {
            Icon(
                Icons.Default.WbSunny,
                "",
                Modifier
                    .align(Alignment.CenterVertically)
                    .size(22.dp)
                    .padding(end = 2.dp),
                tint = primaryContentColor
            )

            Text(
                text = mainAppModel.weatherText.value,
                color = primaryContentColor,
                style = if (small) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                fontWeight = FontWeight.W600,
                modifier = Modifier.clickable {
                    val weatherAppPackage = getStringSetting(
                        context,
                        mainAppModel.getContext().getString(R.string.weather_app_package),
                        ""
                    )
                    if (weatherAppPackage.isNotEmpty()) {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(weatherAppPackage)
                        launchIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            mainAppModel.getContext()
                                .getString(R.string.set_weather_app_in_settings),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                textAlign = when (homeAlignment) {
                    Alignment.Start -> TextAlign.Start
                    Alignment.End -> TextAlign.End
                    else -> TextAlign.Center
                }
            )
        }
    }
}

/**
 * Screen time on home screen
 */
@Composable
fun HomeScreenScreenTime(
    homeAlignment: Alignment.Horizontal,
    small: Boolean,
    screenTime: String,
) {
    Row(Modifier.padding(end = 10.dp)) {
        Icon(
            Icons.Default.Timer,
            "",
            Modifier
                .align(Alignment.CenterVertically)
                .size(22.dp)
                .padding(end = 2.dp),
            tint = primaryContentColor
        )

        Text(
            text = screenTime,
            color = primaryContentColor,
            style = if (small) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            fontWeight = FontWeight.W600,
            modifier = Modifier.clickable {
                // Logic to open a weather app if needed
            },
            textAlign = when (homeAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}

/**
 * Block with tips for first time users
 */
@Composable
fun FirstTimeHelp() {
    Box(
        Modifier.clip(
            MaterialTheme.shapes.extraLarge
        )
    ) {
        Column(
            Modifier.background(CardContainerColor)
        ) {
            Row(
                Modifier
                    .padding(25.dp, 25.dp, 25.dp, 15.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = ContentColor
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.swipe_for_all_apps),
                    modifier = Modifier,
                    color = ContentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                Modifier
                    .padding(25.dp, 0.dp, 25.dp, 25.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.Settings,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = ContentColor
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.hold_for_settings),
                    modifier = Modifier,
                    color = ContentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Bottom Sheet

/**
 * Action that can be shown in the bottom sheet
 * */
data class AppAction(
    val label: String,
    val onClick: () -> Unit
)

/**
 * Bottom Sheet home screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenBottomSheet(
    modifier: Modifier = Modifier,
    title: String,
    actions: List<AppAction>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    shortcutActions: List<AppAction> = listOf()
) {
    val screenHeight = LocalWindowInfo.current.containerDpSize.height

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier
                .heightIn(max = screenHeight * 0.8f)
                .fillMaxWidth()
                .padding(25.dp, 25.dp, 25.dp, 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "App Options",
                    tint = ContentColor,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    title,
                    color = ContentColor,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 15.dp))

            // Actions
            Column(Modifier.padding(start = 47.dp, bottom = 50.dp)) {
                if (!shortcutActions.isEmpty()) {
                    shortcutActions.forEach { action ->
                        Text(
                            text = action.label,
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .combinedClickable(onClick = action.onClick),
                            color = ContentColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(Modifier.padding(vertical = 15.dp))
                }

                actions.forEach { action ->
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .combinedClickable(onClick = action.onClick),
                        color = ContentColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Apps list

/**
 * Apps List title
 */
@Composable
fun AppsListHeader() {
    Spacer(modifier = Modifier.height(140.dp))
    Text(
        text = stringResource(id = R.string.all_apps),
        color = primaryContentColor,
        style = MaterialTheme.typography.titleMedium
    )
}

/**
 * Search Bar for apps list that collapses into a pill
 */
@Composable
fun AnimatedPillSearchBar(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialText: String = "",
    autoFocus: Boolean = false
) {
    var searchText by remember { mutableStateOf(TextFieldValue(initialText)) }

    // Animation Specs
    val width by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else 150.dp,
        label = "widthAnimation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        label = "alphaAnimation"
    )

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle Auto-focus and Expansion changes
    LaunchedEffect(isExpanded, autoFocus) {
        if (isExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    Surface(
        modifier = modifier
            .width(width)
            .height(56.dp)
            .clickable { onExpandedChange(!isExpanded) },
        shape = RoundedCornerShape(28.dp),
        color = primaryContentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = BackgroundColor,
                modifier = Modifier.size(24.dp)
            )

            if (!isExpanded) {
                Text(
                    text = stringResource(id = R.string.search),
                    color = BackgroundColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        onSearchTextChanged(it.text)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .alpha(alpha)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        onSearchDone(searchText.text)
                    }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = BackgroundColor
                    ),
                    cursorBrush = SolidColor(BackgroundColor)
                )
            }
        }
    }
}

/**
 * Android 15+ Private space UI with apps, settings button and lock button
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PrivateSpace(mainAppModel: MainAppViewModel, homeScreenModel: HomeScreenModel) {
    val privateSpaceAppActions = listOf(
        AppAction(
            stringResource(R.string.uninstall)
        ) {
            uninstallPrivateSpaceApp(
                homeScreenModel.currentSelectedPrivateApp.value,
                mainAppModel.getContext()
            )
        },
        AppAction(
            stringResource(R.string.app_info)
        ) {
            showPrivateSpaceAppInfo(
                homeScreenModel.currentSelectedPrivateApp.value,
                mainAppModel.getContext()
            )
        }
    )

    Card(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge),
        colors = CardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor,
            disabledContentColor = CardContainerColorDisabled,
            disabledContainerColor = ContentColorDisabled,
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    stringResource(R.string.private_space),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Row(
                    Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        {
                            homeScreenModel.showPrivateSpaceSettings.value = true
                        }, Modifier, colors = IconButtonColors(
                            containerColor = SecondaryCardContainerColor,
                            contentColor = ContentColor,
                            disabledContainerColor = SecondaryCardContainerColor,
                            disabledContentColor = ContentColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Settings, stringResource(R.string.private_space_settings)
                        )
                    }

                    IconButton(
                        {
                            lockPrivateSpace(mainAppModel.getContext())
                            homeScreenModel.searchExpanded.value = false
                            homeScreenModel.searchText.value = ""
                        }, Modifier, colors = IconButtonColors(
                            containerColor = SecondaryCardContainerColor,
                            contentColor = ContentColor,
                            disabledContainerColor = SecondaryCardContainerColor,
                            disabledContentColor = ContentColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Lock, stringResource(R.string.lock_private_space)
                        )
                    }
                }
            }

            getPrivateSpaceApps(mainAppModel.getContext()).forEach { app ->
                PrivateAppItem(app.displayName, {
                    homeScreenModel.currentSelectedPrivateApp.value = app
                    homeScreenModel.showPrivateBottomSheet.value = true
                }) {
                    openPrivateSpaceApp(
                        installedApp = app, context = mainAppModel.getContext(), Rect()
                    )
                    resetHome(homeScreenModel)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (homeScreenModel.showPrivateBottomSheet.value) {
        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedPrivateApp.value.displayName,
            actions = privateSpaceAppActions,
            onDismissRequest = {
                homeScreenModel.showPrivateBottomSheet.value = false
                homeScreenModel.currentSelectedPrivateApp.value =
                    InstalledApp("", "", ComponentName("", ""))
            },
            sheetState = rememberModalBottomSheetState(),
            modifier = Modifier
        )
    }
}

@Composable
fun ListGradient(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundColor.copy(alpha = 0f),
                        BackgroundColor
                    )
                )
            )
    )
}

@Composable
fun WorkAppsFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = {
            onClick()
        },
        modifier = modifier.size(56.dp),
        containerColor = primaryContentColor,
        contentColor = BackgroundColor
    ) {
        Icon(Icons.Rounded.Work, contentDescription = stringResource(R.string.work_profile))
    }
}

@Preview
@Composable
fun PrevWorkAppsFab() {
    WorkAppsFab {}
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun WorkApps(
    mainAppModel: MainAppViewModel,
    homeScreenModel: HomeScreenModel,
    modifier: Modifier = Modifier
) {
    val workAppActions = listOf(
        AppAction(
            stringResource(R.string.uninstall)
        ) {
            uninstallWorkApp(
                homeScreenModel.currentSelectedWorkApp.value,
                mainAppModel.getContext()
            )
        },
        AppAction(
            stringResource(R.string.app_info)
        ) {
            goToWorkAppAppInfo(
                homeScreenModel.currentSelectedWorkApp.value,
                homeScreenModel,
                mainAppModel.getContext()
            )
        }
    )

    val isUnlocked = remember {
        mutableStateOf(isWorkProfileUnlocked(mainAppModel.getContext()))
    }

    Card(
        modifier
            .padding(horizontal = 30.dp, vertical = 120.dp)
            .clip(MaterialTheme.shapes.extraLarge),
        colors = CardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor,
            disabledContentColor = CardContainerColorDisabled,
            disabledContainerColor = ContentColorDisabled,
        )
    ) {
        AnimatedVisibility(isUnlocked.value) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.work_profile),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(
                        onClick = {
                            if (isDefaultLauncher(mainAppModel.getContext())) {
                                lockWorkProfile(mainAppModel.getContext())
                                isUnlocked.value = isWorkProfileUnlocked(mainAppModel.getContext())
                            } else {
                                Toast.makeText(
                                    mainAppModel.getContext(),
                                    mainAppModel.getContext()
                                        .getString(R.string.launcher_must_be_default_to_pause_or_unpause_work_apps),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = IconButtonColors(
                            containerColor = SecondaryCardContainerColor,
                            contentColor = ContentColor,
                            disabledContainerColor = SecondaryCardContainerColor,
                            disabledContentColor = ContentColor
                        )
                    ) {
                        Icon(
                            Icons.Rounded.WorkOff,
                            contentDescription = stringResource(R.string.lock_work_profile)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    getWorkApps(mainAppModel.getContext())
                        .sortedBy { it.displayName.lowercase() }
                        .forEach { app ->
                            WorkAppItem(app.displayName, {
                                homeScreenModel.currentSelectedWorkApp.value = app
                                homeScreenModel.showWorkBottomSheet.value = true
                            }) {
                                openWorkApp(
                                    installedApp = app, context = mainAppModel.getContext(), Rect()
                                )
                                resetHome(homeScreenModel)
                            }
                        }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
        AnimatedVisibility(!isUnlocked.value) {
            Column(
                Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.work_apps_are_paused),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        top = 30.dp,
                        start = 30.dp,
                        end = 30.dp,
                        bottom = 5.dp
                    )
                )

                Text(
                    stringResource(R.string.you_wont_receive_notifications_from_work_apps),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        top = 5.dp,
                        start = 30.dp,
                        end = 30.dp,
                        bottom = 10.dp
                    )
                )

                OutlinedButton(
                    onClick = {
                        if (isDefaultLauncher(mainAppModel.getContext())) {
                            unlockWorkProfile(
                                mainAppModel.getContext()
                            )
                            isUnlocked.value = isWorkProfileUnlocked(mainAppModel.getContext())
                        } else {
                            Toast.makeText(
                                mainAppModel.getContext(),
                                mainAppModel.getContext()
                                    .getString(R.string.launcher_must_be_default_to_pause_or_unpause_work_apps),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .padding(bottom = 30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryCardContainerColor,
                        contentColor = ContentColor,
                    )
                ) {
                    Text(stringResource(R.string.unpause))
                }
            }
        }
    }

    if (homeScreenModel.showWorkBottomSheet.value) {
        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedWorkApp.value.displayName,
            actions = workAppActions,
            onDismissRequest = {
                homeScreenModel.showWorkBottomSheet.value = false
                homeScreenModel.currentSelectedWorkApp.value =
                    InstalledApp("", "", ComponentName("", ""))
            },
            sheetState = rememberModalBottomSheetState(),
            modifier = Modifier
        )
    }
}
