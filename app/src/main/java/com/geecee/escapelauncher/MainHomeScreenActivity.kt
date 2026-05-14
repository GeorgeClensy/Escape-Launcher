package com.geecee.escapelauncher

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.ui.views.MainPagerScreen
import com.geecee.escapelauncher.ui.views.Onboarding
import com.geecee.escapelauncher.ui.views.Settings
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.configureAnalytics
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.utils.ScreenOffReceiver
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.core.data.worker.ClearOldDataWorker
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.EscapeTheme
import com.geecee.escapelauncher.core.cloudmessaging.messagingInitializer
import com.geecee.escapelauncher.core.common.configureFullScreenMode
import com.geecee.escapelauncher.core.common.formatScreenTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainHomeScreenActivity : ComponentActivity() {
    private val globalViewModel: GlobalViewModel by viewModels()

    private val screenTimeViewModel: ScreenTimeViewModel by viewModels()
    private lateinit var screenOffReceiver: ScreenOffReceiver
    private lateinit var packageChangeReceiver: BroadcastReceiver
    private val homeScreenModel by viewModels<HomeScreenModel> {
        HomeScreenModelFactory(application, viewModel)
    }
    private val viewModel: MainAppViewModel by viewModels()
    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

    fun requestLocationPermission(context: Context, activity: Activity) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    /**
     * Main Entry point
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup Splashscreen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Setup analytics
        lifecycleScope.launch {
            globalViewModel.allowAnalytics.collect { enabled ->
                configureAnalytics(this@MainHomeScreenActivity, enabled)
            }
        }

        // Make full screen
        enableEdgeToEdge()
        configureFullScreenMode(window)

        // Set up the screen time tracking clean-up
        ClearOldDataWorker.scheduleDailyCleanup(this)

        // Mark screen time as loaded (now handled by ScreenTimeViewModel)
        viewModel.isScreenTimeLoaded.value = true

        // Set up the application content
        setContent {
            EscapeTheme {
                SetupNavHost(determineStartDestination(LocalContext.current))
            }
        }

        // Assign window
        viewModel.setWindow(window)

        // Register screen off receiver
        screenOffReceiver = ScreenOffReceiver {
            // Screen turned off
            if (viewModel.isAppOpened) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val packageName = homeScreenModel.currentSelectedApp.value.packageName
                    screenTimeViewModel.onAppClosed(packageName)

                    Log.i(
                        "INFO",
                        "Screen turned off with app $packageName open, stopping screen time counting at " + formatScreenTime(
                            screenTimeViewModel.getScreenTime(packageName)
                        )
                    )

                    // Reset state
                    homeScreenModel.currentSelectedApp =
                        mutableStateOf(InstalledApp("", "", ComponentName("", "")))
                }
                viewModel.isAppOpened = false
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)


        // Subscribe to notifications via flavor-specific initializer
        messagingInitializer.initialize(this)
    }

    override fun onResume() {
        super.onResume()

        // Check if we need to update screen time when coming back from an app
        if (viewModel.isAppOpened) {
            lifecycleScope.launch(Dispatchers.IO) {
                val packageName = homeScreenModel.currentSelectedApp.value.packageName
                screenTimeViewModel.onAppClosed(packageName)

                // Reset state
                homeScreenModel.currentSelectedApp.value = InstalledApp("", "", ComponentName("", ""))
            }
            viewModel.isAppOpened = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the receivers
        if (::screenOffReceiver.isInitialized) {
            unregisterReceiver(screenOffReceiver)
        }
        if (::packageChangeReceiver.isInitialized) {
            unregisterReceiver(packageChangeReceiver)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            AppUtils.resetHome(homeScreenModel)
            viewModel.requestToGoHome()
        }
    }

    /**
     * Determines the start location for the NavHost
     *
     * @param context The context of the app
     *
     * @author George Clensy
     *
     * @see Settings
     *
     * @return Returns "home" if it is not the first time and "onboarding" if it is
     */
    private fun determineStartDestination(context: Context): String {
        return when {
            getBooleanSetting(
                context,
                context.resources.getString(R.string.FirstTime),
                true
            ) -> "onboarding"

            else -> "home"
        }
    }

    /**
     * Sets up main navigation host for the app
     *
     * @param startDestination Where to start
     */
    @Composable
    private fun SetupNavHost(startDestination: String) {
        val navController = rememberNavController()

        LaunchedEffect(viewModel.navigateHomeEvent) {
            viewModel.navigateHomeEvent.collectLatest {
                if (navController.currentDestination?.route != "home") {
                    homeScreenModel.goToMainPage()
                    homeScreenModel.appsListScrollState.scrollToItem(0)
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
                else {
                    launch {
                        homeScreenModel.animatedGoToMainPage()
                    }
                    launch {
                        delay(550)
                        homeScreenModel.appsListScrollState.scrollToItem(0)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BackgroundColor)
                .animateContentSize()
        ) {
            NavHost(navController, startDestination = startDestination) {
                composable(
                    "home",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pushNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    requestLocationPermission(
                        this@MainHomeScreenActivity,
                        this@MainHomeScreenActivity
                    )

                    MainPagerScreen(
                        viewModel,
                        homeScreenModel
                    ) { navController.navigate("settings") }

                    configureFullScreenMode(window)
                }
                composable(
                    "settings",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Settings(
                        viewModel,
                        homeScreenModel = homeScreenModel,
                        {
                            navController.navigate("home") {
                                popUpTo("settings") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        this@MainHomeScreenActivity,
                    )

                    configureFullScreenMode(window)
                }
                composable(
                    "onboarding",
                    enterTransition = { fadeIn(tween(900)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    AppUtils.configureOnboardingFullScreen(window)

                    Onboarding(
                        mainAppNavController = navController,
                        mainAppViewModel = viewModel,
                        homeScreenModel = homeScreenModel,
                        globalViewModel = globalViewModel,
                        activity = this@MainHomeScreenActivity
                    )
                }
            }
        }
    }
}
