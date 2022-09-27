package com.example.mediabrowserservice.service

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.example.mediabrowserservice.R
import com.example.mediabrowserservice.model.Song
import com.example.mediabrowserservice.utils.Constant
import com.example.mediabrowserservice.utils.Constant.MEDIA_ID_ROOT
import com.example.mediabrowserservice.utils.ConstantBundle
import com.example.mediabrowserservice.utils.asMediaItems
import com.example.mediabrowserservice.utils.asMediaMetadataCompat
import kotlinx.coroutines.*


const val TAG = "Media-Browser-Service"

class MediaService : MediaBrowserServiceCompat() {

    private lateinit var mediaPlayer: MediaPlayer
    lateinit var mediaSession: MediaSessionCompat
    private val listSong = mutableListOf<MediaMetadataCompat>()
    private var positionSong: Int = 0
    private val jobMediaPlayer = Job()
    private var isPlaying = false
    private var isFirstOpenApp = true
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private var handler: Handler = Handler()
    private lateinit var notificationBuilder: NotificationCompat.Builder

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
        return BrowserRoot(MEDIA_ID_ROOT, null)
    }

    override fun onCustomAction(action: String, extras: Bundle?, result: Result<Bundle>) {
        super.onCustomAction(action, extras, result)
        val listSong =
            extras?.getParcelableArrayList<Song>(ConstantBundle.BUNDLE_COMMAND)?.toMutableList()
        this.listSong.apply {
            clear()
            listSong?.let {
                addAll(it.asMediaMetadataCompat())
            }
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == MEDIA_ID_ROOT) {
            result.sendResult(listSong.asMediaItems())
            if (listSong.isNotEmpty()) {
                setDataSource(isPlaying)
                setMediaPlaybackState(PlaybackStateCompat.STATE_NONE, 0L)
                initNotificationCompat()
            }
        }
    }

    private fun initMediaSession() {
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }
        mediaSession = MediaSessionCompat(this@MediaService, TAG).apply {
            setCallback(MediaSessionCallback())
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setVolume(1.0f, 1.0f)
        }
    }

    private fun initNotificationCompat() {
        val description = mediaSession.controller.metadata.description
        notificationBuilder = NotificationCompat.Builder(this, Constant.CHANNEL_ID).apply {
            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)
            setLargeIcon(description.iconBitmap)
            setSmallIcon(R.drawable.ic_music)

            setContentIntent(mediaSession.controller.sessionActivity)

            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@MediaService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_previous,
                    "PreviousSkip",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaService,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

            if (isPlaying) {
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_pause,
                        "Pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@MediaService,
                            PlaybackStateCompat.ACTION_PAUSE
                        )
                    )
                )
            }else {
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_play,
                        "Play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@MediaService,
                            PlaybackStateCompat.ACTION_PLAY
                        )
                    )
                )
            }

            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_next,
                    "NextSkip",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaService,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@MediaService,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }
    }

    private fun getUriSongFromList() =
        Uri.parse(listSong[positionSong].description.mediaUri.toString())

    private fun setDataSource(isPlay: Boolean) {
        mediaSession.setMetadata(listSong[positionSong])
        CoroutineScope(jobMediaPlayer + Dispatchers.IO).launch {
            mediaPlayer.apply {
                withContext(Dispatchers.Main) { reset() }
                setDataSource(this@MediaService, getUriSongFromList())
                withContext(Dispatchers.Main) {
                    prepare()
                    sendSessionEventDuration(duration)
                    ContextCompat.startForegroundService(
                        applicationContext,
                        Intent(this@MediaService, this@MediaService.javaClass)
                    )
                    startForeground(Constant.FOREGROUND_ID, notificationBuilder.build())
                    if (isPlay) MediaSessionCallback().onPlay()
                }
            }
        }
    }

    private fun sendSessionEventDuration(duration: Int) {
        val durationBundle = Bundle()
        durationBundle.putInt(ConstantBundle.BUNDLE_DURATION, duration)
        mediaSession.sendSessionEvent(Constant.DURATION, durationBundle)
    }

    private fun setPositionPlayFromId(mediaId: String, isPlay: Boolean) {
        for (metaSong in listSong) {
            if (mediaId == metaSong.description.mediaId) {
                positionSong = listSong.indexOf(metaSong)
                setDataSource(isPlay)
                return
            }
        }
    }

    private fun setPositionPreviousOrNext(position: Int, isPlay: Boolean) {
        if (position > listSong.size - 1) {
            positionSong = 0
            setDataSource(isPlay)
        } else if (position < 0) {
            positionSong = listSong.size - 1
            setDataSource(isPlay)
        } else {
            positionSong = position
            setDataSource(isPlay)
        }
        if (isPlay) MediaSessionCallback().onPlay()
    }

    private fun setMediaPlaybackState(state: Int, position: Long) {
        stateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_PLAY)
        }
        stateBuilder.setState(state, position, 0f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }


    private fun updateCurrentPosition() {
        handler.postDelayed(java.lang.Runnable {
            val currentPosition = mediaPlayer.currentPosition
            stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
                .setState(PlaybackStateCompat.STATE_PLAYING, currentPosition.toLong(), 1f)
                .build()
            mediaSession.setPlaybackState(stateBuilder.build())
            updateCurrentPosition()
        }, 1000)
    }

    private fun stopPlaybackStateUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            var keyCode: KeyEvent? = null
            keyCode = if (Build.VERSION.SDK_INT >= 33) {
                mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }
            when(keyCode?.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> onPause()
                KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
            }
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onPlay() {
            super.onPlay()
            if (isFirstOpenApp) isFirstOpenApp = false
            setMediaPlaybackState(
                PlaybackStateCompat.STATE_PLAYING,
                mediaPlayer.currentPosition.toLong()
            )
            updateCurrentPosition()
            mediaPlayer.start()
            isPlaying = true
        }

        override fun onPause() {
            super.onPause()
            mediaPlayer.pause()
            setMediaPlaybackState(
                PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer.currentPosition.toLong()
            )
            stopPlaybackStateUpdate()
            isPlaying = false
        }

        override fun onStop() {
            super.onStop()
            mediaPlayer.stop()
            isPlaying = false
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            setPositionPreviousOrNext(positionSong - 1, isPlaying)
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            setPositionPreviousOrNext(positionSong + 1, isPlaying)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            mediaId?.let { idMedia ->
                if (isFirstOpenApp) {
                    setPositionPlayFromId(idMedia, true)
                    isFirstOpenApp = false
                } else setPositionPlayFromId(idMedia, isPlaying)
            }
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            mediaPlayer.seekTo(pos.toInt())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        jobMediaPlayer.cancel()
    }
}
