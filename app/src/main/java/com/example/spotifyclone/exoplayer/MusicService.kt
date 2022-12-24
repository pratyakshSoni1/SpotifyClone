package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import androidx.media.MediaSessionManager
import com.example.spotifyclone.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.example.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "MusicServiceTag"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer:ExoPlayer

    @Inject
    lateinit var firebaseMusicSource:FirebaseMusicSource

    private val serviceJob = Job()

    // merge properties of our job and main so our custom scope is formed
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // to cumminicate with service of current music we have music session
    private lateinit var mediaSession:MediaSessionCompat
    private lateinit var mediaSessionConnector:MediaSessionConnector
    private lateinit var musicNotificationManager: MusicNotificationManager

    private lateinit var musicPlayerEventListener:MusicPlayerEventListener

    var isforegroundService = false
    private var curPlayingSong: MediaMetadataCompat? = null

    private var isPlayerPrepared = false

    companion object{
        var curSongDuration = 0L
        private set
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        //to do something on notification click we have this intent
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        /* service extends MediaBrowserServiceCompat and it contains the sessionToken property
        that can be used to get info about specific different music session using different tokens */
        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            context= this,
            sessionToken = mediaSession.sessionToken,
            notificationListener= MusicPlayerNotificationListener(this),
            newSongCallbacks= {
                curSongDuration = exoPlayer.duration
            }
        )

        val musicPlaybackPreparer:MusicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource){
            curPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
            Log.d("MUSICSERVICE", "Player Playback prepared and also player prepared ")
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setPlayer(exoPlayer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs:List<MediaMetadataCompat>,
        itemToPlay:MediaMetadataCompat?,
        playNow: Boolean
    ){
        val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)

        exoPlayer.prepare()
        exoPlayer.addMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex,0L)
        exoPlayer.playWhenReady =  playNow
        Log.d("MUSICSERVICE", "Player prepared ")
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        return BrowserRoot(MEDIA_ROOT_ID, null)

    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){

            MEDIA_ROOT_ID -> {
                val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                    if(isInitialized){
                        result.sendResult(firebaseMusicSource.asMediaItem())
                        if(!isPlayerPrepared && firebaseMusicSource.songs.isEmpty()){
                            preparePlayer( firebaseMusicSource.songs, firebaseMusicSource.songs[0], false )
                            isPlayerPrepared = true
                            Log.d("MUSICSERVICE", "Player prepared during loadChildren ")
                        }
                    }else{
                        result.sendResult(null)
                    }
                }

                if(!resultSent){
                    // wait for some time to get results ready and call the whole
                    // loading thing after some time
                    result.detach()
                }

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.release()
        exoPlayer.removeListener(musicPlayerEventListener)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

}