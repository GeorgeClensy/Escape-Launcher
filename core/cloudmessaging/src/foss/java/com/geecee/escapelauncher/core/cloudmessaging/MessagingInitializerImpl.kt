package com.geecee.escapelauncher.core.cloudmessaging

import android.content.Context
import jakarta.inject.Inject

class MessagingInitializerImpl @Inject constructor() : MessagingInitializer {
    override fun initialize(context: Context) {
        // No-op for FOSS build
    }
}
