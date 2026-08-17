package com.geecee.escapelauncher

import android.Manifest
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.compose.material3.MaterialTheme
import com.geecee.escapelauncher.core.analytics.analyticsProxy
import com.geecee.escapelauncher.core.cloudmessaging.messagingInitializer
import com.geecee.escapelauncher.core.common.configureNavBar
import com.geecee.escapelauncher.core.common.configureStatusBar
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.common.hasPermission
import com.geecee.escapelauncher.core.data.worker.ClearOldDataWorker
import com.geecee.escapelauncher.core.theme.EscapeTheme
import com.geecee.escapelauncher.core.theme.motion.enterTransition
import com.geecee.escapelauncher.core.theme.motion.exitTransition
import com.geecee.escapelauncher.core.theme.motion.popEnterTransition
import com.geecee.escapelauncher.core.theme.motion.popExitTransition
import com.geecee.escapelauncher.core.ui.recievers.ScreenOffReceiver
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import com.geecee.escapelauncher.feature.onboarding.Onboarding
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.feature.settings.Settings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Type-safe navigation keys for the top-level app destinations.
 */
sealed interface AppNavKey : NavKey {
    @Serializable
    data object Home : AppNavKey

    @Serializable
    data object Settings : AppNavKey

    @Serializable
    data object Onboarding : AppNavKey
}

@AndroidEntryPoint
class MainHomeScreenActivity : ComponentActivity() {
    private val globalViewModel: GlobalViewModel by viewModels()
    private val mainPagerViewModel: MainPagerScreenViewModel by viewModels()
    private val screenTimeViewModel: ScreenTimeViewModel by viewModels()

    @Inject
    lateinit var widgetHostManager: WidgetHostManager
    private lateinit var screenOffReceiver: ScreenOffReceiver
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onStart() {
        super.onStart()

        // Start listening for widgets
        try {
            widgetHostManager.startListening()
        } catch (e: Exception) {
            Log.e("Widgets", "Error starting AppWidgetHost in onStart", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup Splashscreen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Setup analytics
        lifecycleScope.launch {
            globalViewModel.allowAnalytics.collect { enabled ->
                analyticsProxy.configureAnalytics(this@MainHomeScreenActivity, enabled)
                Log.d("Analytics", "Analytics are $enabled")
            }
        }

        // Sort out the window
        enableEdgeToEdge()
        lifecycleScope.launch {
            globalViewModel.showStatusBar.collect { show ->
                window.configureStatusBar(hide = !show)
                window.configureNavBar(hide = false)
            }
        }

        // Set up the screen time tracking clean-up
        ClearOldDataWorker.scheduleDailyCleanup(this)

        // Determine start destination before hiding splash screen
        var startDestination by mutableStateOf<AppNavKey?>(null)
        lifecycleScope.launch {
            val isFirstTime = globalViewModel.firstTime.first()
            startDestination = if (isFirstTime) AppNavKey.Onboarding else AppNavKey.Home
        }

        // Keep splash screen visible until we know where to go and essential data is loaded
        splashScreen.setKeepOnScreenCondition {
            startDestination == null || !globalViewModel.initializationState.value.isAllLoaded
        }

        // Register screen off receiver
        screenOffReceiver = ScreenOffReceiver {
            // Screen turned off
            if (screenTimeViewModel.hasActiveSession()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val packageName = screenTimeViewModel.getActiveSessionPackageName() ?: ""
                    if (packageName.isNotEmpty()) {
                        screenTimeViewModel.onAppClosed(packageName)

                        Log.i(
                            "INFO",
                            "Screen turned off with app $packageName open, stopping screen time counting at " + formatScreenTime(
                                screenTimeViewModel.getScreenTime(packageName)
                            )
                        )
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)

        // Subscribe to notifications via flavour-specific initializer
        messagingInitializer.initialize(this)

        // Set up the application content
        setContent {
            val destination = startDestination
            if (destination != null) {
                EscapeTheme {
                    SetupNavDisplay(destination)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Responsible for taking you to the favourites page when you press the home button
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            // Only navigate home if we've finished onboarding
            lifecycleScope.launch {
                val isFirstTime = globalViewModel.firstTime.first()
                if (!isFirstTime) {
                    globalViewModel.requestToGoHome()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if we need to update screen time when coming back from an app
        if (screenTimeViewModel.hasActiveSession()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val packageName = screenTimeViewModel.getActiveSessionPackageName() ?: ""
                if (packageName.isNotEmpty()) {
                    screenTimeViewModel.onAppClosed(packageName)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        // Stop listening for widgets
        try {
            widgetHostManager.stopListening()
        } catch (e: Exception) {
            Log.e("Widgets", "Error stopping AppWidgetHost in onStop", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the screen off receivers
        if (::screenOffReceiver.isInitialized) {
            unregisterReceiver(screenOffReceiver)
        }
    }


    /**
     * Sets up main navigation display for the app
     *
     * @param startDestination Where to start
     */
    @Composable
    private fun SetupNavDisplay(startDestination: AppNavKey) {
        val backStack = rememberNavBackStack(startDestination)

        // Responsible for going home when navigateHomeEvent happens
        LaunchedEffect(globalViewModel.navigateHomeEvent) {
            globalViewModel.navigateHomeEvent.collect {
                // Clear the back stack and set Home as the only entry
                if (backStack.lastOrNull() !is AppNavKey.Home) {
                    backStack.removeAll { it !is AppNavKey.Home }
                    if (backStack.isEmpty()) {
                        backStack.add(AppNavKey.Home)
                    }
                }
                launch {
                    mainPagerViewModel.animatedGoToMainPage()
                }
                launch {
                    delay(300.milliseconds)
                    mainPagerViewModel.appsListScrollState.scrollToItem(0)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .animateContentSize()
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<AppNavKey.Home> {
                        // Ask for permissions as soon as you get to the homepage. Bad.
                        LaunchedEffect(Unit) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }

                            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }

                        MainPagerScreen(
                            viewModel = mainPagerViewModel,
                            onOpenSettings = { backStack.add(AppNavKey.Settings) }
                        )
                    }
                    entry<AppNavKey.Settings> {
                        Settings(
                            goBack = {
                                backStack.removeLastOrNull()
                            },
                            activity = this@MainHomeScreenActivity,
                        )
                    }
                    entry<AppNavKey.Onboarding> {
                        Onboarding(
                            onFinished = {
                                backStack.clear()
                                backStack.add(AppNavKey.Home)
                            }
                        )
                    }
                },
                transitionSpec = {
                    enterTransition() togetherWith exitTransition()
                },
                popTransitionSpec = {
                    popEnterTransition() togetherWith popExitTransition()
                },
                predictivePopTransitionSpec = {
                    popEnterTransition() togetherWith popExitTransition()
                }
            )
        }
    }
}