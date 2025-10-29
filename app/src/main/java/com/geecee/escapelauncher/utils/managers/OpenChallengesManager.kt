package com.geecee.escapelauncher.utils.managers

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.geecee.escapelauncher.ui.theme.CardContainerColor
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChallengesManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "ChallengePrefs"
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
fun OpenChallenge(haptics: HapticFeedback,openApp: () -> Unit, goBack: () -> Unit) {
    var currentText by remember { mutableStateOf("5") }
    var showText by remember { mutableStateOf(true) }
    var nextScreen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val stringOne = "4"
    val stringTwo = "3"
    val stringThree = "2"
    val stringFour = "1"

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                delay(3000)
                showText = false

                delay(1000)
                currentText = stringOne
                showText = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                delay(3000)
                showText = false

                delay(1000)
                currentText = stringTwo
                showText = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                delay(3000)
                showText = false

                delay(1000)
                currentText = stringThree
                showText = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                delay(3000)
                showText = false

                delay(1000)
                currentText = stringFour
                showText = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                delay(3000)
                showText = false

                delay(1000)
                nextScreen = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                delay(500)
                openApp()
            }
        }
    }

    if (!nextScreen) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFAABB),
                            Color(0xFFB19CD9)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                .clickable(enabled = true) {},
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

                Button(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    goBack()
                }, Modifier.align(Alignment.CenterHorizontally), colors = ButtonColors(
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
    }
    else {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFAABB), // Peachy-pink color
                            Color(0xFFB19CD9)  // Soft lavender color
                        ),
                        start = Offset(0f, 0f),  // Starting point (top-left corner)
                        end = Offset(0f, Float.POSITIVE_INFINITY) // Ending point (bottom-center)
                    )
                )
                .clickable(enabled = true) {},
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
                                    Color(0xFFFFAABB), // Peachy-pink color
                                    Color(0xFFB19CD9)  // Soft lavender color
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