package com.example.spotifyclone.di

import android.content.Context
import com.example.spotifyclone.data.remote.MusicDatabase
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

//Interpret search after philipp

@Module
@InstallIn( ServiceComponent::class )
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideMusicdatabase() = MusicDatabase()

    @Provides
    @ServiceScoped
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()


    @Provides
    @ServiceScoped
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes:AudioAttributes
    ) = ExoPlayer.Builder(context).build().apply{
        setAudioAttributes(audioAttributes, true)
        setHandleAudioBecomingNoisy(true)
    }


    @Provides
    @ServiceScoped
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    ) =  DefaultDataSource.Factory(context)

}













