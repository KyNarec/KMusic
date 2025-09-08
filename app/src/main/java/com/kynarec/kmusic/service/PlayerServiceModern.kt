package com.kynarec.kmusic.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

@UnstableApi
class PlayerServiceModern : MediaLibraryService() {

    private var player: ExoPlayer? = null
    private var mediaLibrarySession: MediaLibrarySession? = null

    val MIN_BUFFER_DURATION: Int = 50000 // 50 seconds
    val MAX_BUFFER_DURATION: Int = 50000 // 50 seconds
    val MIN_PLAYBACK_RESUME_BUFFER: Int = 1500 // 1.5 seconds
    val MIN_PLAYBACK_START_BUFFER: Int = 500 // 0.5 seconds

    private val loadController = DefaultLoadControl.Builder()
        .setAllocator(DefaultAllocator(true, 16))
        .setBufferDurationsMs(
        MIN_BUFFER_DURATION,
        MAX_BUFFER_DURATION,
        MIN_PLAYBACK_START_BUFFER,
        MIN_PLAYBACK_RESUME_BUFFER)
        .setTargetBufferBytes(-1)
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()


    // Create your Player and MediaLibrarySession in the onCreate lifecycle event.
    // This is called when the first controller connects.
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.DEFAULT, true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setLoadControl(loadController)
            .build()

        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            player!!,
            MediaLibrarySessionCallback()).build()
    }

    // This method is called when a MediaController wants to connect.
    // It is the entry point for all client connection requests.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        // This example always accepts the connection.
        // In a production app, you might validate the controller's package name or signature.
        return mediaLibrarySession
    }

    // Remember to release the player and media session in onDestroy.
    // This is a critical step to prevent memory leaks and free up resources.
    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        player = null
        super.onDestroy()
    }

    // TODO: Implement the MediaLibrarySessionCallback class in Part III.
}