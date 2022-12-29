package com.example.spotifyclone.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.exoplayer.MusicServiceConnection
import com.example.spotifyclone.exoplayer.isPlayEnabled
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.isPrepared
import com.example.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.example.spotifyclone.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems get() = _mediaItems as LiveData<Resource<List<Song>>>

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {

        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })

    }

    fun skipToNextSong(){
        musicServiceConnection.transportControlls.skipToNext()
    }

    fun skipToPreviousSong(){
        musicServiceConnection.transportControlls.skipToPrevious()
    }

    fun seekTo(pos:Long){
        musicServiceConnection.transportControlls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem:Song, toggle:Boolean = false){
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == curPlayingSong?.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let{ playbackState ->
                when{
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControlls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControlls.play()
                    else -> Unit
                }
            }
        }else{
            musicServiceConnection.transportControlls.playFromMediaId( mediaItem.mediaId, null )
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe( MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {} )
    }


}