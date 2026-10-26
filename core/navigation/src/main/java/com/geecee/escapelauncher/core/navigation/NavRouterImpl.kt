package com.geecee.escapelauncher.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavRouterImpl @Inject constructor() : NavRouter {
    private val _navigationEvents = MutableSharedFlow<NavEvent>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val navigationEvents: Flow<NavEvent> = _navigationEvents.asSharedFlow()

    override fun navigateTo(key: NavKey) {
        _navigationEvents.tryEmit(NavEvent.NavigateTo(key))
    }

    override fun popBackStack() {
        _navigationEvents.tryEmit(NavEvent.PopBackStack)
    }

    override fun navigateHome() {
        _navigationEvents.tryEmit(NavEvent.NavigateHome)
    }

    override fun navigateOnboarding() {
        _navigationEvents.tryEmit(NavEvent.NavigateOnboarding)
    }
}