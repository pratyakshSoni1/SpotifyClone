package com.example.spotifyclone.exoplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.spotifyclone.R
import com.example.spotifyclone.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.spotifyclone.other.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager (
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener:PlayerNotificationManager.NotificationListener,
    private val newSongCallbacks: () -> Unit,
){
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(
                NotificationChannel(
                    context.resources.getString(R.string.notification_channel_name),
                    "Music Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        notificationManager = PlayerNotificationManager
            .Builder(
                context,
                NOTIFICATION_ID ,
                context.resources.getString(R.string.notification_channel_name)
            ).setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .setSmallIconResourceId(R.drawable.ic_music)
            .build()

        notificationManager.apply {
            setSmallIcon(R.drawable.ic_music)
            setMediaSessionToken(sessionToken)

        }

    }

    fun showNotification(player: Player){
        notificationManager.setPlayer(player)
    }


    private inner class DescriptionAdapter(
        private val mediaController:MediaControllerCompat,

    ):PlayerNotificationManager.MediaDescriptionAdapter{
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object: CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)

                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
        })
            return null
    }

}



}