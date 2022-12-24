package com.example.spotifyclone.exoplayer.callbacks

import android.app.Service
import android.media.session.PlaybackState
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.spotifyclone.exoplayer.MusicService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
    private val musicService: MusicService
): Player.Listener {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if(playbackState == Player.STATE_READY){
            musicService.stopForeground(Service.STOP_FOREGROUND_DETACH)
        }
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        super.onPlayerErrorChanged(error)
        Toast.makeText(musicService, "An unknown eroor occured", Toast.LENGTH_LONG).show()
    }
}