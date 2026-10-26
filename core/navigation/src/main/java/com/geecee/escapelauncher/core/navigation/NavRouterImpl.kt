package com.geecee.escapelauncher.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavRouterImpl @Inject constructor() : NavRouter {
    private val _navigationEvents = Channel<NavEvent>(Channel.BUFFERED)
    override val navigationEvents: Flow<NavEvent> = _navigationEvents.receiveAsFlow()

    override fun navigateTo(key: NavKey) {
        _navigationEvents.trySend(NavEvent.NavigateTo(key))
    }

    override fun popBackStack() {
        _navigationEvents.trySend(NavEvent.PopBackStack)
    }

    override fun navigateHome() {
        _navigationEvents.trySend(NavEvent.NavigateHome)
    }

    override fun navigateOnboarding() {
        _navigationEvents.trySend(NavEvent.NavigateOnboarding)
    }
}