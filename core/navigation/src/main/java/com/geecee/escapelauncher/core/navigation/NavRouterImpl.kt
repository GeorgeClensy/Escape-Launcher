package com.geecee.escapelauncher.core.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavRouterImpl @Inject constructor() : NavRouter {
    private val _navigationEvents = MutableSharedFlow<NavEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val navigationEvents: Flow<NavEvent> = _navigationEvents.asSharedFlow()

    override fun navigateTo(key: AppNavKey, clearBackStack: Boolean) {
        _navigationEvents.tryEmit(NavEvent.NavigateTo(key, clearBackStack))
    }

    override fun popBackStack() {
        _navigationEvents.tryEmit(NavEvent.PopBackStack)
    }
}