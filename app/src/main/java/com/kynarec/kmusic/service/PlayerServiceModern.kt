package com.kynarec.kmusic.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class PlayerServiceModern : MediaLibraryService() {

    private var player: ExoPlayer? = null
    private var mediaLibrarySession: MediaLibrarySession? = null

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