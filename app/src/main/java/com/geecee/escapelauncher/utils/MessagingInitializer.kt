package com.geecee.escapelauncher.utils

import android.content.Context

interface MessagingInitializer {
    fun initialize(context: Context)
}

// Provided by flavor implementation
lateinit var messagingInitializer: MessagingInitializer
