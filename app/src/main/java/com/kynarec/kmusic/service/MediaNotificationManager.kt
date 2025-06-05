package com.kynarec.kmusic.service

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.kynarec.kmusic.R

class MediaNotificationManager(
    private val context: Context,
    private val mediaSession: MediaSessionCompat
) {

    companion object {
        const val CHANNEL_ID = "kmusic_playback_channel"
        private const val TAG = "MediaNotificationManager"
    }

    private val playbackState = mediaSession.controller.playbackState
    private val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
    private val isPaused = playbackState?.state == PlaybackStateCompat.STATE_PAUSED


    fun buildNotification(
        title: String,
        artist: String,
        albumArt: Bitmap?,
    ): Notification {
        // Intents for media buttons

        // Media style
        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(albumArt)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.play_skip_back, "Previous", null)
            .addAction(
                if (isPlaying) R.drawable.pause else R.drawable.play,
                if (isPlaying) "Pause" else "Play",
                null
            )
            .addAction(R.drawable.play_skip_forward, "Next", null)

            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .build()
    }

    fun updateMetadata(
        title: String,
        artist: String,
        albumArt: Bitmap?,
        duration: String
    ) {
        val durationMillis = parseDurationToMillis(duration)
        val metadata = android.support.v4.media.MediaMetadataCompat.Builder()
            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putLong(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION, durationMillis)
            .putBitmap(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
            .build()

        mediaSession.setMetadata(metadata)
    }

    fun updatePlaybackState(state: Int, position: Long) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(state, position, 1.0f)
            .build()

        mediaSession.setPlaybackState(playbackState)
    }

    fun updatePlaybackPosition(position: Long) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(mediaSession.controller.playbackState.state, position, 1.0f)
            .build()
        mediaSession.setPlaybackState(playbackState)

    }

    private fun parseDurationToMillis(durationStr: String): Long {
        val parts = durationStr.split(":")
        return when (parts.size) {
            2 -> {
                val minutes = parts[0].toLongOrNull() ?: 0L
                val seconds = parts[1].toLongOrNull() ?: 0L
                (minutes * 60 + seconds) * 1000
            }
            3 -> { // For "HH:mm:ss" format
                val hours = parts[0].toLongOrNull() ?: 0L
                val minutes = parts[1].toLongOrNull() ?: 0L
                val seconds = parts[2].toLongOrNull() ?: 0L
                (hours * 3600 + minutes * 60 + seconds) * 1000
            }
            else -> 0L
        }
    }
}
