package com.geecee.escapelauncher.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val hapticFeedBackEnabled: Flow<Boolean>
    suspend fun setHapticFeedback(enabld: Boolean)

    val hidePrivateSpace: Flow<Boolean>
    suspend fun setHidePrivateSpace(enabled: Boolean)
}