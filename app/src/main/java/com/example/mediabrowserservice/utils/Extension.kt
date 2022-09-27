package com.example.mediabrowserservice.utils

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.core.net.toUri
import com.example.mediabrowserservice.model.Song

fun List<MediaMetadataCompat>.asMediaItems() = this.map { song ->
    val desc = MediaDescriptionCompat.Builder()
        .setMediaUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
        .setTitle(song.description.title)
        .setSubtitle(song.description.subtitle)
        .setMediaId(song.description.mediaId)
        .setIconUri(song.description.iconUri)
        .build()
    MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
}.toMutableList()

fun MutableList<Song>.asMediaMetadataCompat() = this.map { song ->
    MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.subtitle)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.mediaId)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.title)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.songUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
        .build()
}.toMutableList()

fun List<MediaBrowserCompat.MediaItem>.asSong() =
    map { mediaItems ->
    Song(
        mediaItems.mediaId!!,
        mediaItems.description.title.toString(),
        mediaItems.description.subtitle.toString(),
        mediaItems.description.mediaUri.toString(),
        mediaItems.description.iconUri.toString()
    )
}

fun <T> Resource<T>.execute(
    onSuccess : (T) -> Unit = {},
    onError : (String) -> Unit = {},
    onLoading : () -> Unit = {}
) {
    when(this.status){
        Status.SUCCESS -> {
            this.data?.let { onSuccess(it) }
        }
        Status.ERROR -> {
            this.message?.let { onError(it) }
        }
        Status.LOADING -> {
            onLoading()
        }
    }
}

fun View.setAlphaAnimation(){
    this.startAnimation(AlphaAnimation(9f, 0.1f))
}
