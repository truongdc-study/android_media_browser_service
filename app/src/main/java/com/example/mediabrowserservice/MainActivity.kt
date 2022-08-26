package com.example.mediabrowserservice

import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.mediabrowserservice.service.MediaService

class MainActivity : AppCompatActivity() {

    private lateinit var mediaBrowser : MediaBrowserCompat
    private lateinit var  mediaController : MediaControllerCompat

    private var btnMedia : Button? = null

    private val transportControls : MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initMediaBrowser()
        subscribeMediaBrowser()
        initView()

        btnMedia?.setOnClickListener {
            transportControls.play()
        }
    }

    /**
     * Initialize Media Browser
     */
    private fun initMediaBrowser() {
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaService::class.java),
            MediaConnectionCallback(),
            null
        ).apply {
            connect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowser.disconnect()
        unSubscribeMediaBrowser()
    }

    /**
     * Initialize View
     */
    private fun initView() {
        btnMedia = findViewById(R.id.btnMedia)
    }

    /**
     * Execute subscribe Media Browser Service Compat
     */
    private fun subscribeMediaBrowser() {
        mediaBrowser.subscribe(
            resources.getString(R.string.app_name),
            object : MediaBrowserCompat.SubscriptionCallback(){})
    }

    /**
     * Execute unSubscribe Media Browser Service Compat
     */
    private fun unSubscribeMediaBrowser() {
        mediaBrowser.unsubscribe(getString(R.string.app_name),
            object : MediaBrowserCompat.SubscriptionCallback(){})
    }

    private inner class MediaConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

        /**
         * Called when connection Success
         * Y can connection with MediaControllerCompat
         */
        override fun onConnected() {
            super.onConnected()
            mediaController = MediaControllerCompat(
                this@MainActivity, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
        }

        /**
         * Called when connection Suspended
         */
        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        /**
         * Called when connection Failed
         */
        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    private inner class MediaControllerCallback(
    ): MediaControllerCompat.Callback() {

        /**
         * Return state of Media
         */
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }

        /**
         * Return a metadata of Media
         * Y can get information of song playing form metadata
         */
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }

        /**
         * Return event of Media
         * Y can get event and handle (Example : check Error Network)
         */
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
        }

        /**
         * Called when Media Session Destroy
         */
        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
        }

    }
}
