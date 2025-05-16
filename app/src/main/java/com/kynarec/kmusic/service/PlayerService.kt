package com.kynarec.kmusic.service

import android.content.Intent
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.exoplayer2.MediaItem
import com.kynarec.kmusic.MainActivity
import com.kynarec.kmusic.utils.setPlayerIsPlaying
import com.kynarec.kmusic.utils.setPlayerJustStartedUp


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
                Log.i("PlayerService", "MainActivity.instance = ${MainActivity.instance}")
                MainActivity.instance?.hidePlayerControlBar(false)
            }

            "ACTION_PAUSE" -> {
                applicationContext.setPlayerIsPlaying(false)
                player.pause()
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
        // Chatty thinks, that this does not really work and that this is better
//        player.setMediaItem(mediaItem)
//        player.prepare()
//        player.play() // start playback — might still buffer before actually playing
//
//        player.addListener(object : Player.Listener {
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                if (isPlaying) {
//                    Log.i("PlayerService", "Playback started")
//                    applicationContext.setPlayerIsPlaying(true)
//                    MainActivity.instance?.hidePlayerControlBar(false)
//                }
//            }
//        })
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
