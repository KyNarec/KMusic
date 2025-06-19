package com.kynarec.kmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.LongDef
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.bumptech.glide.Glide
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.kynarec.kmusic.MainActivity
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.utils.ACTION_NEXT
import com.kynarec.kmusic.utils.ACTION_PAUSE
import com.kynarec.kmusic.utils.ACTION_PLAY
import com.kynarec.kmusic.utils.ACTION_PREV
import com.kynarec.kmusic.utils.ACTION_RESUME
import com.kynarec.kmusic.utils.ACTION_RESUME_UPDATES
import com.kynarec.kmusic.utils.ACTION_SEEK
import com.kynarec.kmusic.utils.ACTION_STOP_UPDATES
import com.kynarec.kmusic.utils.IS_PLAYING
import com.kynarec.kmusic.utils.NOTIFICATION_ID
import com.kynarec.kmusic.utils.PLAYBACK_STATE_CHANGED
import com.kynarec.kmusic.utils.PLAYER_PROGRESS_UPDATE
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.getPlayerOpen
import com.kynarec.kmusic.utils.setJustStartedUp
import com.kynarec.kmusic.utils.setPlayerIsPlaying
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext





class PlayerService() : MediaLibraryService() {
    private val TAG = "Player Service"
    private lateinit var player: ExoPlayer
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: MediaNotificationManager

    private lateinit var database: KmusicDatabase
    private lateinit var songDao: SongDao

    // Track currently playing song
    private var currentSongId: String? = null
    private var playbackStartTime: Long = 0
    private var accumulatedPlayTime: Long = 0

    private var progressRunnable: Runnable? = null

    // Handler for periodic database updates
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 3000L // Update DB every 30 seconds

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Log.i("CALLBACK", "onPlay triggered")
            resume()
        }

        override fun onPause() {
            Log.i("CALLBACK", "onPause triggered")
            pause()
        }

        override fun onSkipToNext() {
            Log.i("CALLBACK", "onSkipToNext triggered")
//            next()
        }

        override fun onSkipToPrevious() {
            Log.i("CALLBACK", "onSkipToPrevious triggered")
//            previous()
        }

        override fun onSeekTo(pos: Long) {
            Log.i("CALLBACK", "onSeekTo triggered")
            seekTo(pos)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service created")

        mediaSession = MediaSessionCompat(applicationContext, "PlayerService").apply {
            isActive = true
        }
        mediaSession.setCallback(callback)
        notificationManager = MediaNotificationManager(this, mediaSession)


        database = KmusicDatabase.getDatabase(this)
        songDao = database.songDao()

        player = ExoPlayer.Builder(this).build()


        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }

        setupPlayerListeners()

        // Start periodic updates
        startPeriodicUpdates()

        createNotificationChannel()

        val name = "KMusic Playback"
        val descriptionText = "Shows playback controls and track info"
        val importance = NotificationManager.IMPORTANCE_LOW // low importance avoids sound/vibration

        val channel = NotificationChannel(MediaNotificationManager.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) : MediaLibrarySession = mediaLibrarySession

    override fun onDestroy() {

        if (!player.isPlaying){
            player.stop()
            player.release()
        }

        player.stop()
        player.release()

        super.onDestroy()

        stopProgressUpdates()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //Log.d(tag, "onStartCommand: Received command ${intent?.action}")
//        SmartMessage("Service received ${intent?.action}", PopupType.Info, false, this)
        when (intent?.action) {
            ACTION_PLAY -> {
                val song = intent.getParcelableExtra<Song>("SONG")
                if (song != null) {
                    playSongFromSongId(song.id)
                    currentSongId = song.id

                    CoroutineScope(Dispatchers.IO).launch  {
                            val largeIconBitmap = try {
                                withContext(Dispatchers.IO) {
                                    Glide.with(applicationContext)
                                        .asBitmap()
                                        .load(song.duration)
                                        .submit()
                                        .get()
                                }
                            } catch (e: Exception) {
                                Log.e("GlideLoadError", "Failed to load image: ${song.duration}", e)
                                null
                            }
                            // thumbnail and duration are switched, idk why
                            notificationManager.updateMetadata(song.title, song.artist, largeIconBitmap, song.thumbnail)
//                            notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 1)

                        val notification = notificationManager.buildNotification(song.title, song.artist, largeIconBitmap)
                            startForeground(NOTIFICATION_ID, notification)
                    }
                }
                startProgressUpdates() // Start sending updates when playing
            }

            ACTION_RESUME -> {
                resume()
            }

            ACTION_PAUSE -> {
                pause()
            }

            ACTION_NEXT -> {

            }

            ACTION_PREV -> {

            }

            ACTION_SEEK -> {
                val seekPosition = intent.getLongExtra("seek_position", 0)
                SmartMessage(seekPosition.toString(), PopupType.Warning, false, this)
                seekTo(seekPosition)
            }

            ACTION_STOP_UPDATES -> {
                stopProgressUpdates()
            }
            ACTION_RESUME_UPDATES -> {
                startProgressUpdates()
            }

        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            MediaNotificationManager.CHANNEL_ID,
            "KMusic Playback",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Playback controls"
        }

//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun resume(){
        applicationContext.setPlayerIsPlaying(true)
        player.play()
        notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
        notifyPlaybackStateChanged(true)
        if (MainActivity.instance?.getPlayerOpen() == true) MainActivity.instance?.hidePlayerControlBar(true)
        else MainActivity.instance?.hidePlayerControlBar(false)
        applicationContext.setJustStartedUp(false)
        startProgressUpdates() // Resume updates
    }

    private fun pause(){
        applicationContext.setPlayerIsPlaying(false)
        player.pause()
        notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition)
        notifyPlaybackStateChanged(false)
        stopProgressUpdates() // Stop updates when paused

    }

    private fun seekTo(to: Long){
        notificationManager.updatePlaybackPosition(to)
        player.seekTo(to)
        sendProgressUpdate()

    }


    private fun playSongFromSongId(id: String) {
        Log.i(TAG, "playSongFromId was called")
        val py = Python.getInstance()
        val module = py.getModule("backend")


        val uri = module.callAttr("playSongByIdWithBestBitrate", id)

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
        Log.i(TAG, "The link ExoPlayer should be playing: $uri")
        try {
            player.setMediaItem(mediaItem)
            player.prepare()
            if (player.isPlaying){
                player.play()
                applicationContext.setPlayerIsPlaying(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, e)
        }
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (player.isPlaying) {
                            // Started playing
                            handlePlaybackStarted()
                        } else {
                            // Paused
                            handlePlaybackPaused()
                        }
                    }
                    Player.STATE_ENDED -> {
                        // Track ended
                        handlePlaybackStopped()
                    }
                    Player.STATE_IDLE -> {
                        // Playback stopped
                        handlePlaybackStopped()
                    }
                    // Handle other states as needed
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    handlePlaybackStarted()
                } else {
                    handlePlaybackPaused()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Save accumulated time for previous song
                saveCurrentPlaybackTime()

                // Reset tracking for new song
                mediaItem?.mediaId?.let { newSongId ->
                    currentSongId = newSongId
                    playbackStartTime = System.currentTimeMillis()
                    accumulatedPlayTime = 0
                }
            }
        })
    }

    private fun handlePlaybackStarted() {
        playbackStartTime = System.currentTimeMillis()
    }

    private fun handlePlaybackPaused() {
        // Calculate and add elapsed time to accumulated time
        val now = System.currentTimeMillis()
        val elapsedTime = now - playbackStartTime
        accumulatedPlayTime += elapsedTime

        // Save to database
        saveCurrentPlaybackTime()
    }

    private fun handlePlaybackStopped() {
        // Save final playback time when stopping
        saveCurrentPlaybackTime()

        // Reset tracking data
        currentSongId = null
        accumulatedPlayTime = 0
    }

    private fun saveCurrentPlaybackTime() {
        val songId = currentSongId ?: return

        // Calculate total playback time including current session
        val totalTimeToAdd = if (player.isPlaying) {
            accumulatedPlayTime + (System.currentTimeMillis() - playbackStartTime)
        } else {
            accumulatedPlayTime
        }

        // Skip if no significant playback time
        if (totalTimeToAdd <= 0) return

        // Update in database using coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get current song data
                val song = songDao.getSongById(songId)
                Log.i(TAG, "Song from Dao is $song")

                // Update the song with new total play time
                song?.let {
                    val updatedSong = it.copy(
                        totalPlayTimeMs = it.totalPlayTimeMs + totalTimeToAdd
                    )
                    songDao.updateSong(updatedSong)

                    // Reset accumulated time since we've saved it
                    accumulatedPlayTime = 0
                    playbackStartTime = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating playback time: ${e.message}")
            }
        }
    }

    // Sets up periodic updates during continuous playback
    private fun startPeriodicUpdates() {
        val updateRunnable = object : Runnable {
            override fun run() {
                if (player.isPlaying && currentSongId != null) {
                    saveCurrentPlaybackTime()
                }
                // Schedule next update
                handler.postDelayed(this, updateInterval)
            }
        }

        // Start the periodic updates
        handler.postDelayed(updateRunnable, updateInterval)
    }

    private fun notifyPlaybackStateChanged(isPlaying: Boolean) {
        val intent = Intent(PLAYBACK_STATE_CHANGED).apply {
            putExtra(IS_PLAYING, isPlaying)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendProgressUpdate() {
        val intent = Intent(PLAYER_PROGRESS_UPDATE)
        intent.putExtra("current_position", player.currentPosition)
        intent.putExtra("duration", player.duration)
        Log.i("PlayerSeekbar", player.duration.toString())
        intent.putExtra("is_playing", player.isPlaying)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun startProgressUpdates() {
        Log.i("PlayerSeekbar", "startProgressUpdates is called")
        stopProgressUpdates() // Stop any existing updates
        progressRunnable = object : Runnable {
            override fun run() {
                Log.d("PlayerSeekbar", "Runnable executing - this should appear every second")
                sendProgressUpdate()
                handler.postDelayed(this, 16)
            }
        }
        val posted = handler.post(progressRunnable!!)
        Log.d("PlayerSeekbar", "Handler.post() returned: $posted")
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let { handler.removeCallbacks(it) }
    }


}
