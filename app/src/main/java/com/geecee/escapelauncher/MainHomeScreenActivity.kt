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
import com.geecee.escapelauncher.ui.theme.BackgroundColor
import com.geecee.escapelauncher.ui.views.HomeScreenPageManager
import com.geecee.escapelauncher.ui.views.Onboarding
import com.geecee.escapelauncher.ui.views.Settings
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.configureAnalytics
import com.geecee.escapelauncher.utils.InstalledApp
import com.geecee.escapelauncher.utils.PrivateSpaceStateReceiver
import com.geecee.escapelauncher.utils.ScreenOffReceiver
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.managers.ScreenTimeManager
import com.geecee.escapelauncher.utils.managers.scheduleDailyCleanup
import com.geecee.escapelauncher.utils.messagingInitializer
import com.geecee.escapelauncher.utils.setStatusBarImmersive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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
        splashScreen.setKeepOnScreenCondition {
            !viewModel.isReady
        }

        // Make full screen
        enableEdgeToEdge()
        AppUtils.configureFullScreenMode(window)

        // Set up the screen time tracking
        ScreenTimeManager.initialize(this)
        scheduleDailyCleanup(this)

        // Efficient bulk load of screen time
        viewModel.reloadScreenTimeCache()

        // Set up the application content
        setContent {
            AppUtils.SetUpTheme(viewModel = viewModel, content = {
                SetupNavHost(determineStartDestination(LocalContext.current))
            })
        }

        // Assign window
        viewModel.setWindow(window)

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

        val isSticky = getBooleanSetting(this, this.getString(R.string.ScreenTimeOnHome))
        this.setStatusBarImmersive(isSticky)

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

    override fun onPause() {
        super.onPause()

        try {
            AppUtils.resetHome(homeScreenModel, true)
        } catch (ex: Exception) {
            Log.e("ERROR", ex.toString())
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

                    HomeScreenPageManager(
                        viewModel,
                        homeScreenModel
                    ) { navController.navigate("settings") }

                    AppUtils.configureFullScreenMode(window)
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

                    AppUtils.configureFullScreenMode(window)
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
                        activity = this@MainHomeScreenActivity
                    )
                }
            }
        }
    }
}
