package com.geecee.escapelauncher.utils.managers

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.CardContainerColor
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay

const val DEFAULT_COUNTDOWN_TIME = 3f
const val DEFAULT_COUNTDOWN_IN_BETWEEN_TIME = 1f
const val DEFAULT_COUNTDOWN_WRAP_UP_TIME = 0.5f

enum class CountdownMode(
    @StringRes val labelRes: Int,
    val value: Float
) {
    SHORT(R.string.set_app_countdown_time_short, 2f),
    NORMAL(R.string.set_app_countdown_time_normal, 3f),
    LONG(R.string.set_app_countdown_time_long, 4f),
}

class ChallengesManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = Migration.UNIFIED_PREFS_NAME
        private const val FAVORITE_APPS_KEY = "ChallengeApps"
    }

    private fun saveChallengeApps(challengeApps: List<String>) {
        val json = gson.toJson(challengeApps)
        sharedPreferences.edit {
            putString(FAVORITE_APPS_KEY, json)
        }
    }

    fun getChallengeApps(): List<String> {
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addChallengeApp(packageName: String) {
        val challengeApps = getChallengeApps().toMutableList()
        if (packageName !in challengeApps) {
            challengeApps.add(packageName)
            saveChallengeApps(challengeApps)
        }
    }

    fun removeChallengeApp(packageName: String) {
        val challengeApps = getChallengeApps().toMutableList()
        if (challengeApps.remove(packageName)) {
            saveChallengeApps(challengeApps)
        }
    }

    fun doesAppHaveChallenge(packageName: String): Boolean {
        val challengeApps = getChallengeApps()
        return packageName in challengeApps
    }
}

@Composable
fun OpenChallenge(haptics: HapticFeedback, openApp: () -> Unit, goBack: () -> Unit) {
    val steps = listOf("5", "4", "3", "2", "1")
    val countdownTime = getCountdownTimeInLong(LocalContext.current)
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var showText by rememberSaveable { mutableStateOf(true) }
    var nextScreen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (nextScreen) return@LaunchedEffect

        while (stepIndex < steps.size) {
            if (showText) {
                delay(countdownTime)
                showText = false
            }

            delay((DEFAULT_COUNTDOWN_IN_BETWEEN_TIME * 1000L).toLong())
            stepIndex++

            if (stepIndex < steps.size) {
                showText = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                nextScreen = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                delay((DEFAULT_COUNTDOWN_WRAP_UP_TIME * 1000L).toLong())
                openApp()
            }
        }
    }

    val currentText = if (stepIndex < steps.size) steps[stepIndex] else ""

    if (!nextScreen) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFB2D8D8),
                            Color(0xFF004C4C)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            Column {
                AnimatedVisibility(
                    visible = showText,
                    enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 1000))
                ) {
                    Text(
                        currentText,
                        Modifier.padding(32.dp),
                        Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,

                        )
                }

                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        goBack()
                    },
                    Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonColors(
                        ContentColor,
                        CardContainerColor,
                        ContentColor,
                        CardContainerColor
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        "Go back",
                        tint = CardContainerColor
                    )
                }
            }
        }
    } else {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFB2D8D8),
                            Color(0xFF004C4C)
                        ),
                        start = Offset(0f, 0f),  // Starting point (top-left corner)
                        end = Offset(0f, Float.POSITIVE_INFINITY) // Ending point (bottom-center)
                    )
                )
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {


            // Second Box with custom animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 1000)
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 1000)
                )
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFB2D8D8), // Peachy-pink color
                                    Color(0xFF004C4C)  // Soft lavender color
                                ),
                                start = Offset(0f, 0f),  // Starting point (top-left corner)
                                end = Offset(
                                    0f,
                                    Float.POSITIVE_INFINITY
                                ) // Ending point (bottom-center)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {}
            }
        }
    }
}

fun resetAndGetCountdownTime(context: Context): Float {
    setCountdownTime(context, DEFAULT_COUNTDOWN_TIME)
    return DEFAULT_COUNTDOWN_TIME
}

fun getCountdownTimeInLong(context: Context): Long {
    return getCountdownTime(context).toLong() * 1000L
}

fun getCountdownTime(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        Migration.UNIFIED_PREFS_NAME, Context.MODE_PRIVATE
    )
    return sharedPreferences.getFloat("CountdownTime", DEFAULT_COUNTDOWN_TIME)
}

fun setCountdownTime(context: Context, value: Float) {
    val sharedPreferences = context.getSharedPreferences(
        Migration.UNIFIED_PREFS_NAME, Context.MODE_PRIVATE
    )
    sharedPreferences.edit {
        putFloat("CountdownTime", value)
    }
}
