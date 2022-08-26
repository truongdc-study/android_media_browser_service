package com.example.mediabrowserservice.service

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.mediabrowserservice.R

const val TAG = "Media-Browser-Service"

class MediaService : MediaBrowserServiceCompat() {

    private lateinit var mediaPlayer: MediaPlayer
    lateinit var mediaSession : MediaSessionCompat

    override fun onCreate() {
        super.onCreate()
        initMediaSession()
        initMediaPlayer()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, TAG).apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }
        sessionToken =  mediaSession.sessionToken
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    private inner class MediaSessionCallback
        : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            /**
             * Called when user press on play
             */
        }

        override fun onPause() {
            super.onPause()
            /**
             * Called when user press on pause
             */
        }

        override fun onStop() {
            super.onStop()
            /**
             * Called when user press on Stop
             */
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            /**
             * Called when user press on Skip next
             */
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            /**
             * Called when user press on Skip previous
             */
        }
    }

}
