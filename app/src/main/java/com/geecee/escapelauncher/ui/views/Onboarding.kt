package com.geecee.escapelauncher.ui.views

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.utils.changeLauncher
import com.geecee.escapelauncher.utils.configureAnalytics
import com.geecee.escapelauncher.utils.setBooleanSetting
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

@Composable
fun Onboarding(
    mainNavController: NavController,
    mainAppModel: MainAppViewModel,
    pushNotificationPermissionLauncher: ActivityResultLauncher<String>,
    homeScreenModel: HomeScreenModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Page1") {
        composable("Page1",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
            OnboardingPage1(navController)
        }
        composable("Page2",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
            OnboardingPage2(navController)
        }
        composable("Page3",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
            OnboardingPage3(navController, mainAppModel)
        }
        composable("Page4",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
            OnboardingPage4(navController, mainAppModel)
        }
        composable("Page5",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
            OnboardingPage5(navController, mainNavController, mainAppModel)
        }
        composable("Notifications",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
            Notifications(mainNavController, mainAppModel, pushNotificationPermissionLauncher,homeScreenModel)
        }
    }
}

@Composable
fun OnboardingPage1(navController: NavController) {
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
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(15.dp))
            Text(
                stringResource(R.string.escape_launcher),
                Modifier,
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = {
                navController.navigate("Page2")
            }, modifier = Modifier.align(Alignment.BottomEnd), colors = ButtonColors(
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background
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
fun OnboardingPage2(navController: NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 0.dp)
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(120.dp))
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.most_people_waste))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(" 9 ")
                    }
                    append(stringResource(R.string.hours_every_day))
                },
                Modifier,
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(60.dp))

            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.thats))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(" 3 ")
                    }
                    append(stringResource(R.string.every_week))
                },
                Modifier,
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.End
            )

            Spacer(Modifier.height(60.dp))

            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.adds_to))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(" 32 ")
                    }
                    append(stringResource(R.string.years_straight))
                },
                Modifier,
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(120.dp))
        }

        Button(
            onClick = {
                navController.navigate("Page3")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(0.dp, 0.dp, 0.dp, 30.dp),
            colors = ButtonColors(
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background
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
fun OnboardingPage3(navController: NavController, mainAppModel: MainAppModel) {
    fun getInstalledApps(): List<android.content.pm.ResolveInfo> {
        return mainAppModel.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.GET_ACTIVITIES
        )
    }

    val installedApps = remember { mutableStateOf(getInstalledApps()) }
    fun updateFavoriteStatus(packageName: String, isFavorite: Boolean) {
        installedApps.value = getInstalledApps() // Refresh the list
        if (isFavorite) {
            mainAppModel.favoriteAppsManager.addFavoriteApp(packageName)
        } else {
            mainAppModel.favoriteAppsManager.removeFavoriteApp(packageName)
        }
    }

    val favoritedApps = installedApps.value.filter { appInfo ->
        mainAppModel.favoriteAppsManager.isAppFavorite(appInfo.activityInfo.packageName)
    }.sortedBy { appInfo ->
        // Retrieve the index from the favoriteAppsManager
        mainAppModel.favoriteAppsManager.getFavoriteIndex(appInfo.activityInfo.packageName)
    }

    val nonFavoritedApps = installedApps.value.filter { appInfo ->
        !mainAppModel.favoriteAppsManager.isAppFavorite(appInfo.activityInfo.packageName) && !appInfo.activityInfo.packageName.contains("com.geecee.escapelauncher")
    }.sortedBy { it.loadLabel(mainAppModel.packageManager).toString() }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 0.dp)
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(120.dp))

            Text(
                stringResource(R.string.choose_your_favourite_apps),
                Modifier,
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(5.dp))

            Text(
                stringResource(R.string.pinned_for_access),
                Modifier,
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(10.dp))

            Column {
                favoritedApps.forEach { appInfo ->
                    Text(
                        text = appInfo.loadLabel(mainAppModel.packageManager).toString(),
                        modifier = Modifier
                            .padding(vertical = 15.dp)
                            .combinedClickable(onClick = {
                                mainAppModel.favoriteAppsManager.removeFavoriteApp(appInfo.activityInfo.packageName)
                                updateFavoriteStatus(appInfo.activityInfo.packageName, false)
                            }),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider(
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 15.dp)
            )

            Column {
                nonFavoritedApps.forEach { appInfo ->
                    Text(
                        appInfo.loadLabel(mainAppModel.packageManager).toString(),
                        modifier = Modifier
                            .padding(0.dp, 15.dp)
                            .combinedClickable(onClick = {
                                mainAppModel.favoriteAppsManager.addFavoriteApp(appInfo.activityInfo.packageName)
                                updateFavoriteStatus(appInfo.activityInfo.packageName, true)
                            }),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(120.dp))
        }

        Button(
            onClick = {
                navController.navigate("Page4")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp),
            colors = ButtonColors(
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background
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
fun OnboardingPage4(navController: NavController, mainAppModel: MainAppModel) {
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
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(5.dp))
            Text(
                stringResource(R.string.stop_going_back),
                Modifier,
                MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    changeLauncher(mainAppModel.getContext())
                }, modifier = Modifier, colors = ButtonColors(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.background
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = stringResource(R.string.set_launcher))
                }
            }
        }

        Button(
            onClick = {
                navController.navigate("Page5")
            }, modifier = Modifier.align(Alignment.BottomEnd), colors = ButtonColors(
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.background
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
fun OnboardingPage5(navController: NavController, mainNavController: NavController, mainAppModel: MainAppModel) {
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
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start
                )
            }

            item {
                Spacer(Modifier.height(5.dp))
                Text(
                    stringResource(R.string.anonymous_data),
                    Modifier,
                    MaterialTheme.colorScheme.onPrimaryContainer,
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
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        MaterialTheme.colorScheme.background
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
        }

        Row(modifier = Modifier.align(Alignment.BottomEnd)) {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        navController.navigate("Notifications")
                    } else {
                        mainNavController.navigate("home")
                    }
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.Analytics),
                        false
                    )
                    configureAnalytics(false)
                }, modifier = Modifier, colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ), border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        navController.navigate("Notifications")
                    } else {
                        mainNavController.navigate("home")
                    }
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.Analytics),
                        true
                    )
                    configureAnalytics(true)
                }, modifier = Modifier, colors = ButtonColors(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.background
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

    if (showPolicyDialog.value) {
        PrivacyPolicyDialog(mainAppModel, showPolicyDialog)
    }
}

@Composable
fun Notifications(
    navController: NavController,
    mainAppModel: MainAppViewModel,
    pushNotificationPermissionLauncher: ActivityResultLauncher<String>,
    homeScreenModel: HomeScreenModel
) {
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
                    stringResource(R.string.notifications),
                    Modifier,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start
                )
            }

            item {
                Spacer(Modifier.height(5.dp))
                Text(
                    stringResource(R.string.please_allow_notifications),
                    Modifier,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    lineHeight = 32.sp
                )
            }
        }

        Row(modifier = Modifier.align(Alignment.BottomEnd)) {
            Button(
                onClick = {
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.FirstTime),
                        false
                    )
                    homeScreenModel.reloadFavouriteApps()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                    navController.navigate("home")
                }, modifier = Modifier, colors = ButtonColors(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.background
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

}