package com.example.spotifyclone.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.spotifyclone.other.Constants.NETWORK_ERROR
import com.example.spotifyclone.other.Event
import com.example.spotifyclone.other.Resource

class MusicServiceConnection(
    val context:Context
) {

    private var _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected get() = _isConnected as LiveData<Event<Resource<Boolean>>>


    private var _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError get() = _networkError as LiveData<Event<Resource<Boolean>>>


    private var _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState get() = _playbackState as LiveData<PlaybackStateCompat?>

    private var _curPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val curPlayingSong get() = _curPlayingSong as LiveData<MediaMetadataCompat?>

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName( context, MusicService::class.java ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }


    val transportControlls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe(parentId:String, callback:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unSubscribe(parentId:String, callback:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId, callback)
    }

    inner class MediaBrowserConnectionCallback(
        private val context:Context
    ): MediaBrowserCompat.ConnectionCallback(){

        override fun onConnected() {
            super.onConnected()
            mediaController = MediaControllerCompat(context,  mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            _isConnected.postValue(Event(Resource.erorr(false, "Media Browser Connection Suspended")))
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            _isConnected.postValue(Event(Resource.erorr(false, "Failed to connect to media browser")))
        }

    }


    inner class MediaControllerCallback: MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            when(event){
                NETWORK_ERROR -> {
                    _networkError.postValue(
                        Event(
                            Resource.erorr( null, "Please check your internet connection", )
                        )
                    )
                }
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }


    }


}