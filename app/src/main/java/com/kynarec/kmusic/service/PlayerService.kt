package com.kynarec.kmusic.service

import android.content.Intent
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.exoplayer2.MediaItem
import com.kynarec.kmusic.utils.setPlayerIsPlaying


class PlayerService() : MediaLibraryService() {
    private val tag = "Player Service"
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession



    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate: Service created")

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
        Log.d(tag, "onStartCommand: Received command")

        when (intent?.action) {
            "ACTION_PLAY" -> {
                val songId = intent.getStringExtra("SONG_ID")
                if (songId != null) {
                    playSongFromSongId(songId)
                }
            }

            "ACTION_RESUME" -> {
                applicationContext.setPlayerIsPlaying(true)
                player.play()
            }

            "ACTION_PAUSE" -> {
                applicationContext.setPlayerIsPlaying(false)
                player.pause()
            }

            "REQUEST_PLAYER_STATUS" -> {
                val statusIntent = Intent("PLAYER_STATUS")
                statusIntent.putExtra("isPlaying", player.isPlaying)
                Log.i(tag, "Sending back player Status ${player.isPlaying}")
                sendBroadcast(statusIntent)
            }
        }

        return START_NOT_STICKY
    }

    private fun playSongFromSongId(id: String) {
        Log.i(tag, "playSongFromId was called")
        val py = Python.getInstance()
        val module = py.getModule("backend")


        val uri = module.callAttr("playSongById", id)

        val mediaItem = MediaItem.fromUri(uri.toString())
        try {
            player.setMediaItem(mediaItem)
            player.prepare()
            if (player.isPlaying){
                player.play()
                applicationContext.setPlayerIsPlaying(true)
            }
        } catch (e: Exception) {
            Log.w(tag, e)
        }
    }

}
