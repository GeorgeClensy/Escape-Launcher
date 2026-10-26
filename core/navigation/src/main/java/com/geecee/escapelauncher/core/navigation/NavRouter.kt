package com.geecee.escapelauncher.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow

interface NavRouter {
    val navigationEvents: Flow<NavEvent>

    fun navigateTo(key: NavKey)
    fun popBackStack()
    fun navigateHome()
    fun navigateOnboarding()
}