package com.example.mediabrowserservice.viewmodel

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediabrowserservice.api.SongAPI
import com.example.mediabrowserservice.connection.MediaServiceConnection
import com.example.mediabrowserservice.model.Song
import com.example.mediabrowserservice.utils.Constant.MEDIA_ID_ROOT
import com.example.mediabrowserservice.utils.Resource
import com.example.mediabrowserservice.utils.asSong
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MediaViewModel(
    private val mediaServiceConnection: MediaServiceConnection
) : ViewModel() {


    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: MutableLiveData<Resource<List<Song>>>
        get() = _mediaItems
    val isConnected = mediaServiceConnection.isConnected
    val netWorkError = mediaServiceConnection.networkError
    val curPlayingSong = mediaServiceConnection.curPlayingSong
    val playbackState = mediaServiceConnection.playbackState
    val durationSong = mediaServiceConnection.durationSong

    init {
        _mediaItems.postValue(Resource.loading(null))
        viewModelScope.launch {
            val deferred = async(Dispatchers.IO) { return@async SongAPI.getAPiSong(SongAPI.URL_SONG) }
            mediaServiceConnection.setList(deferred.await())
            mediaServiceConnection.subscribe(
                MEDIA_ID_ROOT,
                object : MediaBrowserCompat.SubscriptionCallback() {
                    override fun onChildrenLoaded(
                        parentId: String,
                        children: MutableList<MediaBrowserCompat.MediaItem>
                    ) {
                        super.onChildrenLoaded(parentId, children)
                        _mediaItems.postValue(Resource.success(children.asSong()))
                    }
                }
            )
        }
    }

    fun skipToNextSong() = viewModelScope.launch {
        mediaServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() = viewModelScope.launch {
        mediaServiceConnection.transportControls.skipToPrevious()
    }

    fun playOrPauseSong() = viewModelScope.launch {
        val state = mediaServiceConnection.mediaControllerCompat.playbackState.state
        if(state == PlaybackStateCompat.STATE_PLAYING)
            mediaServiceConnection.transportControls.pause()
        else mediaServiceConnection.transportControls.play()
    }

    fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) = viewModelScope.launch {
        mediaServiceConnection.transportControls.playFromMediaId(mediaId, extras)
    }

    fun seekToSong(pos: Long) = viewModelScope.launch {
        mediaServiceConnection.transportControls.seekTo(pos)
    }

    override fun onCleared() {
        super.onCleared()
        mediaServiceConnection.unSubscribe(
            MEDIA_ID_ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
            })
    }
}
