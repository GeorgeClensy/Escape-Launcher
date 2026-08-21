package com.geecee.escapelauncher.core.cloudmessaging

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import jakarta.inject.Inject

class MessagingInitializerImpl @Inject constructor(): MessagingInitializer {
    override fun initialize(context: Context) {
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TOKEN", "FCM Registration Token: $token")
        }

        Firebase.messaging.subscribeToTopic("updates")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("INFO", "Subscribed to FCM topic: updates")
                }
            }

        // ONLY UNCOMMENT TEMPORARILY DO NOT PUSH WITH THIS UNCOMMENTED
        // Subscribe to a test topic for safe bulk testing
//        Firebase.messaging.subscribeToTopic("updates_test")
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.i("INFO", "Subscribed to FCM topic: updates_test")
//                }
//            }
    }
}
