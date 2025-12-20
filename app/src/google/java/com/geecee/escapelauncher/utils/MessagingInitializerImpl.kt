package com.geecee.escapelauncher.utils

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging

class MessagingInitializerImpl : MessagingInitializer {
    override fun initialize(context: Context) {
        Firebase.messaging.subscribeToTopic("updates")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("INFO", "Subscribed to FCM topic: updates")
                }
            }
    }
}
