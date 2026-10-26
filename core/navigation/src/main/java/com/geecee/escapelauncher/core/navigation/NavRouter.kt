package com.geecee.escapelauncher.core.navigation

import kotlinx.coroutines.flow.Flow

interface NavRouter {
    val navigationEvents: Flow<NavEvent>

    fun navigateTo(key: AppNavKey, clearBackStack: Boolean = false)
    fun popBackStack()
}