package com.geecee.escapelauncher.core.cloudmessaging

import android.content.Context

interface MessagingInitializer {
    fun initialize(@Suppress("unused") context: Context)
}

// Provided by flavor implementation
lateinit var messagingInitializer: MessagingInitializer
