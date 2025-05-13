package com.kynarec.kmusic.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.exoplayer2.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.exoplayer2.MediaItem


class PlayerService() : MediaLibraryService() {
    private val TAG = "Player Service"
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession



    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service created")

        player = ExoPlayer.Builder(this).build()

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) : MediaLibrarySession = mediaSession

    override fun onDestroy() {

        if (!player.isPlaying){
            player.stop()
            player.release()
        }

        super.onDestroy()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand: Received command")

        when (intent?.action) {
            "ACTION_PLAY" -> {
                val songId = intent.getStringExtra("SONG_ID")
                if (songId != null) {
                    playSongFromSongId(songId)
                }
            }

            "ACTION_RESUME" -> player.play()

            "ACTION_PAUSE" -> player.pause()

            "REQUEST_PLAYER_STATUS" -> {
                val statusIntent = Intent("PLAYER_STATUS")
                statusIntent.putExtra("isPlaying", player.isPlaying)
            }
        }

        return START_NOT_STICKY
    }

    private fun playSongFromSongId(id: String) {
        Log.i(TAG, "playSongFromId was called")
        val py = Python.getInstance()
        val module = py.getModule("backend")


        val uri = module.callAttr("playSongById", id)

        val mediaItem = MediaItem.fromUri(uri.toString())
        try {
            player.setMediaItem(mediaItem)
            player.prepare()
            if (player.isPlaying){
                player.play()
            }
        } catch (e: Exception) {
            Log.w("PLAYER SERVICE", e)
        }
    }

}
