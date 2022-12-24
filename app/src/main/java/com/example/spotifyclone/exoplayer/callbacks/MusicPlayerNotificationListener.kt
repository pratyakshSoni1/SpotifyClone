package com.example.spotifyclone.exoplayer.callbacks

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.spotifyclone.exoplayer.MusicService
import com.example.spotifyclone.other.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener

class MusicPlayerNotificationListener (
    private val musicService:MusicService
) :PlayerNotificationManager.NotificationListener
{
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        musicService.apply {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
            isforegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            if( ongoing && !isforegroundService){
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                startForeground(NOTIFICATION_ID, notification)
                isforegroundService = true
            }
        }
    }
}