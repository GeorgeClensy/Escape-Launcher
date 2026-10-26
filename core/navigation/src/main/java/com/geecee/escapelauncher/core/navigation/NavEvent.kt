package com.geecee.escapelauncher.core.navigation

import androidx.navigation3.runtime.NavKey

sealed interface NavEvent {
    data class NavigateTo(val key: NavKey) : NavEvent
    data object PopBackStack : NavEvent
    data object NavigateHome : NavEvent
    data object NavigateOnboarding : NavEvent
}