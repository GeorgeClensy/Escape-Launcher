package com.geecee.escapelauncher.core.navigation

import androidx.navigation3.runtime.NavKey

sealed interface NavEvent {
    data class NavigateTo(
        val key: NavKey,
        val clearBackStack: Boolean = false
    ) : NavEvent

    data object PopBackStack : NavEvent
}