package com.geecee.escapelauncher.utils.managers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.geecee.escapelauncher.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract data payload
        val data = remoteMessage.data
        Log.d("FCM", "Received data message: $data")

        // Get title and message from data payload
        val title = data["title"] ?: "Escape Launcher Update"
        val message = data["message"] ?: "New update available!"
        val url = data["url"] ?: "https://github.com/georgeclensy/escape"

        // Intent to open the URL
        val notificationIntent = Intent(Intent.ACTION_VIEW, url.toUri())

        // Send notification
        sendNotification(this, title, message, "updates", "Updates", notificationIntent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
    }
}

fun sendNotification(context: Context, title: String, message: String, channelID: String, channelName: String, intent: Intent) {
    val notificationId = 1

    // Create notification channel
    val channel = NotificationChannel(
        channelID,
        channelName,
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = channelName
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)

    // Build the notification
    val notification = NotificationCompat.Builder(context, channelID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notify(notificationId, notification)
    }
}