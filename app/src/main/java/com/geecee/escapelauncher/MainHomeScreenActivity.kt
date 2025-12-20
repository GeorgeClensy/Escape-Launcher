package com.geecee.escapelauncher

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.ui.theme.BackgroundColor
import com.geecee.escapelauncher.ui.views.HomeScreenPageManager
import com.geecee.escapelauncher.ui.views.Onboarding
import com.geecee.escapelauncher.ui.views.Settings
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.animateSplashScreen
import com.geecee.escapelauncher.utils.AppUtils.configureAnalytics
import com.geecee.escapelauncher.utils.InstalledApp
import com.geecee.escapelauncher.utils.PrivateSpaceStateReceiver
import com.geecee.escapelauncher.utils.ScreenOffReceiver
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.managers.ScreenTimeManager
import com.geecee.escapelauncher.utils.managers.getUsageForApp
import com.geecee.escapelauncher.utils.managers.scheduleDailyCleanup
import com.geecee.escapelauncher.utils.messagingInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainHomeScreenActivity : ComponentActivity() {
    private lateinit var privateSpaceReceiver: PrivateSpaceStateReceiver
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

    /**
     * Main Entry point
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup analytics
        configureAnalytics(
            this,
            getBooleanSetting(
                this,
                this.resources.getString(R.string.Analytics),
                false
            )
        )

        // Setup Splashscreen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        animateSplashScreen(splashScreen)

        // Make full screen
        enableEdgeToEdge()
        AppUtils.configureFullScreenMode(window)

        // Set up the screen time tracking
        ScreenTimeManager.initialize(this)
        scheduleDailyCleanup(this)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            homeScreenModel.installedApps.forEach { app ->
                val screenTime = getUsageForApp(app.packageName, viewModel.getToday())
                viewModel.screenTimeCache[app.packageName] = screenTime
            }
            viewModel.shouldReloadScreenTime.value++
        }

        // Set up the application content
        setContent {
            AppUtils.SetUpTheme(viewModel = viewModel, content = {
                SetupNavHost(determineStartDestination(LocalContext.current))
            })

            // Black overlay, to make it seem smoother when turning screen off
            if (viewModel.blackOverlay.value) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
        }

        // Register screen off receiver
        screenOffReceiver = ScreenOffReceiver {
            // Screen turned off
            if (viewModel.isAppOpened) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val packageName = homeScreenModel.currentSelectedApp.value.packageName
                    ScreenTimeManager.onAppClosed(packageName)

                    // Update screen time for just this app in the cache
                    viewModel.updateAppScreenTime(packageName)

                    // Trigger UI refresh
                    viewModel.shouldReloadScreenTime.value++

                    Log.i(
                        "INFO",
                        "Screen turned off with app " + homeScreenModel.currentSelectedApp.value.packageName + " open, stopping screen time counting at " + AppUtils.formatScreenTime(
                            viewModel.getCachedScreenTime(homeScreenModel.currentSelectedApp.value.packageName)
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

        //Private space receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            privateSpaceReceiver = PrivateSpaceStateReceiver { isUnlocked ->
                viewModel.isPrivateSpaceUnlocked.value = isUnlocked
            }
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
            registerReceiver(privateSpaceReceiver, intentFilter)
        }

        // Package change receiver
        packageChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED,
                    Intent.ACTION_PACKAGE_REMOVED,
                    Intent.ACTION_PACKAGE_REPLACED -> {
                        Log.i("INFO", "Package changed: ${intent.action}")
                        lifecycleScope.launch(Dispatchers.Default) {
                            homeScreenModel.loadApps()
                        }
                    }
                }
            }
        }
        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageChangeReceiver, packageFilter)

        // Subscribe to notifications via flavor-specific initializer
        messagingInitializer.initialize(this)
    }

    override fun onResume() {
        super.onResume()

        // Check if we need to update screen time when coming back from an app
        if (viewModel.isAppOpened) {
            lifecycleScope.launch(Dispatchers.IO) {
                val packageName = homeScreenModel.currentSelectedApp.value.packageName
                ScreenTimeManager.onAppClosed(packageName)

                // Update screen time for just this app in the cache
                viewModel.updateAppScreenTime(packageName)

                // Trigger UI refresh
                viewModel.shouldReloadScreenTime.value++

                // Reset state
                homeScreenModel.currentSelectedApp =
                    mutableStateOf(InstalledApp("", "", ComponentName("", "")))
            }
            viewModel.isAppOpened = false
        }

        // Reset home
        try {
            AppUtils.resetHome(homeScreenModel, viewModel.shouldGoHomeOnResume.value)
            viewModel.shouldGoHomeOnResume.value = false
        } catch (ex: Exception) {
            Log.e("ERROR", ex.toString())
        }


    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the receivers
        if (::privateSpaceReceiver.isInitialized) {
            unregisterReceiver(privateSpaceReceiver)
        }
        if (::screenOffReceiver.isInitialized) {
            unregisterReceiver(screenOffReceiver)
        }
        if (::packageChangeReceiver.isInitialized) {
            unregisterReceiver(packageChangeReceiver)
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
                        pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }

                    HomeScreenPageManager(
                        viewModel,
                        homeScreenModel
                    ) { navController.navigate("settings") }
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
                }
                composable(
                    "onboarding",
                    enterTransition = { fadeIn(tween(900)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Onboarding(
                        navController,
                        viewModel,
                        homeScreenModel,
                        this@MainHomeScreenActivity
                    )
                }
            }
        }
    }
}
