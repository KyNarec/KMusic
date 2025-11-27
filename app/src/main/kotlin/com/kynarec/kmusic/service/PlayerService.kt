package com.kynarec.kmusic.service
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.support.v4.media.MediaDescriptionCompat
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.util.Log
//import androidx.core.net.toUri
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import androidx.media3.common.MediaItem
//import androidx.media3.common.Player
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.session.MediaLibraryService
//import androidx.media3.session.MediaSession
//import com.bumptech.glide.Glide
//import com.chaquo.python.Python
//import com.chaquo.python.android.AndroidPlatform
//import com.kynarec.kmusic.data.db.KmusicDatabase
//import com.kynarec.kmusic.data.db.dao.PersistedQueueDao
//import com.kynarec.kmusic.data.db.dao.SongDao
//import com.kynarec.kmusic.data.db.entities.PersistedQueueItem
//import com.kynarec.kmusic.data.db.entities.QueuedMediaItem
//import com.kynarec.kmusic.data.db.entities.Song
//import com.kynarec.kmusic.enums.PopupType
//import com.kynarec.kmusic.utils.ACTION_NEXT
//import com.kynarec.kmusic.utils.ACTION_PAUSE
//import com.kynarec.kmusic.utils.ACTION_PLAY
//import com.kynarec.kmusic.utils.ACTION_PREV
//import com.kynarec.kmusic.utils.ACTION_RESUME
//import com.kynarec.kmusic.utils.ACTION_RESUME_UPDATES
//import com.kynarec.kmusic.utils.ACTION_SEEK
//import com.kynarec.kmusic.utils.ACTION_STOP_UPDATES
//import com.kynarec.kmusic.utils.IS_PLAYING
//import com.kynarec.kmusic.utils.NOTIFICATION_ID
//import com.kynarec.kmusic.utils.PLAYBACK_STATE_CHANGED
//import com.kynarec.kmusic.utils.PLAYER_PROGRESS_UPDATE
//import com.kynarec.kmusic.utils.SmartMessage
//import com.kynarec.kmusic.utils.parseDurationToMillis
//import com.kynarec.kmusic.utils.setJustStartedUp
//import com.kynarec.kmusic.utils.setPlayerIsPlaying
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
@Deprecated("Use PlayerServiceModern")
class PlayerService()
//class PlayerService() : MediaLibraryService() {
//    private val TAG = "Player Service"
//    private lateinit var player: ExoPlayer
//    private lateinit var mediaLibrarySession: MediaLibrarySession
//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var notificationManager: MediaNotificationManager
//
//    private lateinit var database: KmusicDatabase
//    private lateinit var songDao: SongDao
//    private lateinit var persistedQueueDao: PersistedQueueDao
//
//    // Track currently playing song
//    private var currentSongId: String? = null
//    private var playbackStartTime: Long = 0
//    private var accumulatedPlayTime: Long = 0
//
//    private var progressRunnable: Runnable? = null
//
//    // Handler for periodic database updates
//    private val handler = Handler(Looper.getMainLooper())
//    private val updateInterval = 3000L // Update DB every 30 seconds
//
//    private val callback = object : MediaSessionCompat.Callback() {
//        override fun onPlay() {
//            Log.i("CALLBACK", "onPlay triggered")
//            // If queue is empty, onPlay might not do anything unless you load a default item.
//            // Or, rely on onPlayFromMediaId to start something.
//            val currentItem = mediaSession.controller.metadata?.description?.mediaId
//            if (currentItem != null && player.playbackState == Player.STATE_IDLE) {
//                // If there's metadata but player is idle, try playing it.
//                // This assumes your playSongFromSongId prepares and plays.
//                playSongFromSongId(currentItem) // Might need adjustments
//
//            }
//
//            resume()
//        }
//
//        override fun onPause() {
//            Log.i("CALLBACK", "onPause triggered")
//            pause()
//            saveCurrentMediaSessionQueue()
//        }
//
//        override fun onSkipToNext() {
//            Log.i("CALLBACK", "onSkipToNext triggered")
////            next()
//                player.seekToNextMediaItem() // ExoPlayer handles this if queue is set on it
////            // playSongFromSongId might be called by ExoPlayer listener
////            // Or you might need to get the new current item from MediaSession and play it.
//        }
//
//        override fun onSkipToPrevious() {
//            Log.i("CALLBACK", "onSkipToPrevious triggered")
////            previous()
//            player.seekToPreviousMediaItem()
//        }
//
//        override fun onSeekTo(pos: Long) {
//            Log.i("CALLBACK", "onSeekTo triggered")
//            seekTo(pos)
//        }
//
//        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
//            description?.let {
//                val currentQueue = mediaSession.controller.queue?.toMutableList() ?: mutableListOf()
//                // Ensure mediaId is present
//                if (it.mediaId == null) {
//                    Log.w(TAG, "onAddQueueItem: MediaDescriptionCompat has no mediaId. Cannot add to queue.")
//                    return
//                }
//                currentQueue.add(MediaSessionCompat.QueueItem(it, currentQueue.size.toLong())) // ID can be index or unique
//                mediaSession.setQueue(currentQueue)
//                mediaSession.setQueueTitle("Current Playlist") // Optional
//                Log.i(TAG, "Added item to queue: ${it.title}, new size: ${currentQueue.size}")
//                saveCurrentMediaSessionQueue()
//            }
//        }
//
//        override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
//            description?.let {
//                val currentQueue = mediaSession.controller.queue?.toMutableList() ?: mutableListOf()
//                if (it.mediaId == null) {
//                    Log.w(TAG, "onAddQueueItem at index: MediaDescriptionCompat has no mediaId.")
//                    return
//                }
//                currentQueue.add(index, MediaSessionCompat.QueueItem(it, System.currentTimeMillis())) // Use unique ID
//                mediaSession.setQueue(currentQueue)
//                mediaSession.setQueueTitle("Current Playlist")
//                Log.i(TAG, "Added item to queue at $index: ${it.title}, new size: ${currentQueue.size}")
//                saveCurrentMediaSessionQueue()
//            }
//        }
//
//        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
//            description?.mediaId?.let { mediaIdToRemove ->
//                val currentQueue = mediaSession.controller.queue?.toMutableList() ?: return
//                currentQueue.removeAll { it.description.mediaId == mediaIdToRemove }
//                mediaSession.setQueue(currentQueue)
//                if (currentQueue.isEmpty()) mediaSession.setQueueTitle(null)
//                Log.i(TAG, "Removed item(s) with mediaId: $mediaIdToRemove, new size: ${currentQueue.size}")
//                saveCurrentMediaSessionQueue()
//            }
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d(TAG, "onCreate: Service created")
//
//        mediaSession = MediaSessionCompat(applicationContext, "PlayerService").apply {
//            setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
//            )
//            // Set an initial PlaybackState with ACTION_PLAY_FROM_MEDIA_ID and ACTION_PLAY_FROM_SEARCH
//            val stateBuilder = PlaybackStateCompat.Builder()
//                .setActions(
//                    PlaybackStateCompat.ACTION_PLAY or
//                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
//                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
//                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
//                            PlaybackStateCompat.ACTION_SEEK_TO or
//                            PlaybackStateCompat.ACTION_STOP or
//                            PlaybackStateCompat.ACTION_SET_REPEAT_MODE or     // Add if you support
//                            PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE or    // Add if you support
//                            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or  // Essential for loading items
//                            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID  // If you have a prepare step
//                )
//            setPlaybackState(stateBuilder.build())
//
//            // Set the callback
//            setCallback(callback) // Your existing callback
//            isActive = true
//
//        }
//        //mediaSession.setCallback(callback)
//        notificationManager = MediaNotificationManager(this, mediaSession)
//
//
//        database = KmusicDatabase.getDatabase(this)
//        persistedQueueDao = KmusicDatabase.getDatabase(applicationContext).persistedQueueDao()
//        songDao = KmusicDatabase.getDatabase(applicationContext).songDao()
//        CoroutineScope(Dispatchers.IO).launch {
//            database.songDao().getAllSongs()
//        }
//
//
//
//        player = ExoPlayer.Builder(this)
//            .build()
//
//
//        if (! Python.isStarted()) {
//            Python.start(AndroidPlatform(this));
//        }
//
//        setupPlayerListeners()
//
//        // Start periodic updates
//        startPeriodicUpdates()
//
//        createNotificationChannel()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            loadPersistedQueueIntoMediaSession()
//        }
//
//        val name = "KMusic Playback"
//        val descriptionText = "Shows playback controls and track info"
//        val importance = NotificationManager.IMPORTANCE_LOW // low importance avoids sound/vibration
//
//        val channel = NotificationChannel(MediaNotificationManager.CHANNEL_ID, name, importance).apply {
//            description = descriptionText
//        }
//
//        val notificationManager: NotificationManager =
//            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.createNotificationChannel(channel)
//
//    }
//
//    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) : MediaLibrarySession = mediaLibrarySession
//
//    override fun onDestroy() {
//
//        if (!player.isPlaying){
//            player.stop()
//            player.release()
//        }
//
//        player.stop()
//        player.release()
//
//        super.onDestroy()
//
//        stopProgressUpdates()
//
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
//        //Log.d(tag, "onStartCommand: Received command ${intent?.action}")
////        SmartMessage("Service received ${intent?.action}", PopupType.Info, false, this)
//        when (intent?.action) {
//            ACTION_PLAY -> {
//                val song = intent.getParcelableExtra<Song>("SONG")
//                if (song != null) {
//                    playSongFromSongId(song.id)
//                    currentSongId = song.id
//
//                    CoroutineScope(Dispatchers.IO).launch  {
//                            val largeIconBitmap = try {
//                                withContext(Dispatchers.IO) {
//                                    Glide.with(applicationContext)
//                                        .asBitmap()
//                                        .load(song.duration)
//                                        .submit()
//                                        .get()
//                                }
//                            } catch (e: Exception) {
//                                Log.e("GlideLoadError", "Failed to load image: ${song.duration}", e)
//                                null
//                            }
//                            // thumbnail and duration are switched, idk why
//                            notificationManager.updateMetadata(song.title, song.artist, largeIconBitmap, song.thumbnail)
////                            notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 1)
//
//                        val notification = notificationManager.buildNotification(song.title, song.artist, largeIconBitmap)
//                            startForeground(NOTIFICATION_ID, notification)
//                    }
//                }
//                startProgressUpdates() // Start sending updates when playing
//            }
//
//            ACTION_RESUME -> {
//                resume()
//            }
//
//            ACTION_PAUSE -> {
//                pause()
//            }
//
//            ACTION_NEXT -> {
//
//            }
//
//            ACTION_PREV -> {
//
//            }
//
//            ACTION_SEEK -> {
//                val seekPosition = intent.getLongExtra("seek_position", 0)
//                //SmartMessage(seekPosition.toString(), PopupType.Warning, false, this)
//                seekTo(seekPosition)
//            }
//
//            ACTION_STOP_UPDATES -> {
//                stopProgressUpdates()
//            }
//            ACTION_RESUME_UPDATES -> {
//                startProgressUpdates()
//            }
//
//        }
//
//        return START_NOT_STICKY
//    }
//
//    private fun createNotificationChannel() {
//        val channel = NotificationChannel(
//            MediaNotificationManager.CHANNEL_ID,
//            "KMusic Playback",
//            NotificationManager.IMPORTANCE_DEFAULT
//        ).apply {
//            description = "Playback controls"
//        }
//
////        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val manager = getSystemService(NotificationManager::class.kotlin)
//        manager.createNotificationChannel(channel)
//    }
//
//    private fun resume(){
//        applicationContext.setPlayerIsPlaying(true)
//        player.play()
//        notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
//        notifyPlaybackStateChanged(true)
////        if (MainActivity.instance?.getPlayerOpen() == true) MainActivity.hidePlayerControlBar(true)
////        else MainActivity.instance?.hidePlayerControlBar(false)
//        applicationContext.setJustStartedUp(false)
//        startProgressUpdates() // Resume updates
//    }
//
//    private fun pause(){
//        applicationContext.setPlayerIsPlaying(false)
//        player.pause()
//        notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition)
//        notifyPlaybackStateChanged(false)
//        stopProgressUpdates() // Stop updates when paused
//    }
//
//    private fun stop(){
//        applicationContext.setPlayerIsPlaying(false)
//        player.pause()
//        notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition)
//        notifyPlaybackStateChanged(false)
//        stopProgressUpdates() // Stop updates when paused
//    }
//
//    private fun seekTo(to: Long){
//        notificationManager.updatePlaybackPosition(to)
//        player.seekTo(to)
//        sendProgressUpdate()
//
//    }
//
//
//    // Make sure playSongFromSongId correctly updates MediaSession metadata
//    // and prepares ExoPlayer with the new MediaItem.
//    private fun playSongFromSongId(id: String) {
//        Log.i(TAG, "playSongFromSongId was called with ID: $id")
//
//        // 1. Update MediaSession Metadata (if not already done by onPlayFromMediaId)
//        // It's good practice to ensure metadata is set before playback starts.
//        CoroutineScope(Dispatchers.Main).launch { // Use Main for UI-related or quick tasks
//            val song = songDao.getSongById(id) // Ensure this is suspend or handled correctly
//            song?.let {
//                updateMediaSessionMetadata(it) // This will set it on mediaSession
//
//                // 2. Prepare and Play with ExoPlayer
//                val py = Python.getInstance()
//                val module = py.getModule("backend")
//                val uri = module.callAttr("playSongByIdWithBestBitrate", id) // Your Python call
//                Log.i(TAG, "ExoPlayer URI: $uri")
//
//                uri?.toString()?.let { playbackUriString ->
//                    if (playbackUriString.isNotBlank()) {
//                        val mediaItem = MediaItem.Builder()
//                            .setMediaId(id) // Important: Set mediaId on ExoPlayer's MediaItem
//                            .setUri(playbackUriString)
//                            .build()
//
//                        player.setMediaItem(mediaItem)
//                        player.prepare()
//                        player.play() // Start playback
//                        applicationContext.setPlayerIsPlaying(true) // Your helper
//                        Log.i(TAG, "ExoPlayer is now playing: $id")
//
//                        // Update notification based on new song and playing state
//                        // Your notificationManager.updateMetadata and updatePlaybackState calls
//                        val largeIconBitmap = try {
//                            withContext(Dispatchers.IO) {
//                                Glide.with(applicationContext)
//                                    .asBitmap()
//                                    .load(song.thumbnail) // Make sure this is the correct field for large icon
//                                    .submit()
//                                    .get()
//                            }
//                        } catch (e: Exception) {
//                            Log.e("GlideLoadError", "Failed to load image: ${song.thumbnail}", e)
//                            null
//                        }
//                        notificationManager.updateMetadata(song.title, song.artist, largeIconBitmap, song.duration) // Pass actual duration for progress
//                        notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
//
//                        addNewQueue(id)
//                    } else {
//                        Log.w(TAG, "Python backend returned empty or null URI for song ID: $id")
//                    }
//                } ?: Log.w(TAG, "Python backend returned null for song ID: $id")
//            } ?: Log.w(TAG, "Song with ID $id not found in database for playback.")
//        }
//    }
//
//
//    private fun setupPlayerListeners() {
//        player.addListener(object : Player.Listener {
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                // ... your existing logic ...
//                // Update MediaSession PlaybackState
//                val currentPosition = player.currentPosition
//                val stateBuilder = PlaybackStateCompat.Builder()
//                    .setActions(mediaSession.controller.playbackState?.actions ?: 0L) // Preserve existing actions
//                    .setState(
//                        when (playbackState) {
//                            Player.STATE_IDLE -> PlaybackStateCompat.STATE_NONE
//                            Player.STATE_BUFFERING -> if (player.playWhenReady) PlaybackStateCompat.STATE_BUFFERING else PlaybackStateCompat.STATE_PAUSED
//                            Player.STATE_READY -> if (player.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//                            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED // Or PAUSED if you want to replay
//                            else -> PlaybackStateCompat.STATE_NONE
//                        },
//                        currentPosition,
//                        1.0f // Playback speed
//                    )
//                mediaSession.setPlaybackState(stateBuilder.build())
//
//                // Also update your notification if its state depends on ExoPlayer's state directly
//                notificationManager.updatePlaybackState(
//                    when (playbackState) {
//                        Player.STATE_READY -> if (player.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//                        Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
//                        else -> PlaybackStateCompat.STATE_PAUSED // Default to paused for notification
//                    }, currentPosition
//                )
//
//
//                when (playbackState) {
//                    Player.STATE_READY -> {
//                        if (player.playWhenReady) handlePlaybackStarted() else handlePlaybackPaused()
//                    }
//                    Player.STATE_ENDED -> handlePlaybackStopped() // Your existing logic
//                    Player.STATE_IDLE -> handlePlaybackStopped()  // Your existing logic
//                }
//            }
//
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                // This is often a more reliable indicator for UI updates
//                val currentPosition = player.currentPosition
//                val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//                mediaSession.setPlaybackState(
//                    PlaybackStateCompat.Builder(mediaSession.controller.playbackState) // Copy existing state
//                        .setState(state, currentPosition, 1.0f)
//                        .build()
//                )
//                notificationManager.updatePlaybackState(state, currentPosition) // Update notification
//
//                if (isPlaying) handlePlaybackStarted() else handlePlaybackPaused()
//            }
//
//            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//                saveCurrentPlaybackTime() // Your existing DB save for play time
//
//                mediaItem?.mediaId?.let { newSongId ->
//                    currentSongId = newSongId // Your internal tracking
//                    playbackStartTime = System.currentTimeMillis()
//                    accumulatedPlayTime = 0
//
//                    // IMPORTANT: Update MediaSession metadata to reflect the new playing item
//                    CoroutineScope(Dispatchers.Main).launch {
//                        val newSong = songDao.getSongById(newSongId)
//                        newSong?.let {
//                            updateMediaSessionMetadata(it) // Update MediaSession
//                            // Notification metadata is likely updated inside playSongFromSongId or here
//                            val largeIconBitmap = try {
//                                withContext(Dispatchers.IO) {
//                                    Glide.with(applicationContext)
//                                        .asBitmap()
//                                        .load(it.thumbnail)
//                                        .submit()
//                                        .get()
//                                }
//                            } catch (e: Exception) { null }
//                            notificationManager.updateMetadata(it.title, it.artist, largeIconBitmap, it.duration)
//                        }
//                    }
//                }
//            }
//        })
//    }
//
//
//    private fun handlePlaybackStarted() {
//        playbackStartTime = System.currentTimeMillis()
//    }
//
//    private fun handlePlaybackPaused() {
//        // Calculate and add elapsed time to accumulated time
//        val now = System.currentTimeMillis()
//        val elapsedTime = now - playbackStartTime
//        accumulatedPlayTime += elapsedTime
//
//        // Save to database
//        saveCurrentPlaybackTime()
//    }
//
//    private fun handlePlaybackStopped() {
//        // Save final playback time when stopping
//        saveCurrentPlaybackTime()
//
//        // Reset tracking data
//        currentSongId = null
//        accumulatedPlayTime = 0
//
//        // SmartMessage("Playback ended", PopupType.Info, false, this)
//
//        // TODO: if there is no more queue, pause playing
//        //if ( ...) {
//        stop()
//        //}
//    }
//
//    private fun saveCurrentPlaybackTime() {
//        val songId = currentSongId ?: return
//
//        // Calculate total playback time including current session
//        val totalTimeToAdd = if (player.isPlaying) {
//            accumulatedPlayTime + (System.currentTimeMillis() - playbackStartTime)
//        } else {
//            accumulatedPlayTime
//        }
//
//        // Skip if no significant playback time
//        if (totalTimeToAdd <= 0) return
//
//        // Update in database using coroutine
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Get current song data
//                val song = songDao.getSongById(songId)
//                //Log.i(TAG, "Song from Dao is $song")
//
//                // Update the song with new total play time
//                song?.let {
//                    val updatedSong = it.copy(
//                        totalPlayTimeMs = it.totalPlayTimeMs + totalTimeToAdd
//                    )
//                    songDao.updateSong(updatedSong)
//
//                    // Reset accumulated time since we've saved it
//                    accumulatedPlayTime = 0
//                    playbackStartTime = System.currentTimeMillis()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating playback time: ${e.message}")
//            }
//        }
//    }
//
//    // Sets up periodic updates during continuous playback
//    private fun startPeriodicUpdates() {
//        val updateRunnable = object : Runnable {
//            override fun run() {
//                if (player.isPlaying && currentSongId != null) {
//                    saveCurrentPlaybackTime()
//                }
//                // Schedule next update
//                handler.postDelayed(this, updateInterval)
//            }
//        }
//
//        // Start the periodic updates
//        handler.postDelayed(updateRunnable, updateInterval)
//    }
//
//    private fun notifyPlaybackStateChanged(isPlaying: Boolean) {
//        val intent = Intent(PLAYBACK_STATE_CHANGED).apply {
//            putExtra(IS_PLAYING, isPlaying)
//        }
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//    }
//
//    private fun sendProgressUpdate() {
//        val intent = Intent(PLAYER_PROGRESS_UPDATE)
//        intent.putExtra("current_position", player.currentPosition)
//        intent.putExtra("duration", player.duration)
//        //Log.i("PlayerSeekbar", player.duration.toString())
//        intent.putExtra("is_playing", player.isPlaying)
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//    }
//
//    private fun startProgressUpdates() {
//        //Log.i("PlayerSeekbar", "startProgressUpdates is called")
//        stopProgressUpdates() // Stop any existing updates
//        progressRunnable = object : Runnable {
//            override fun run() {
//                Log.d("PlayerSeekbar", "Runnable executing - this should appear every second")
//                sendProgressUpdate()
//                handler.postDelayed(this, 16)
//            }
//        }
//        val posted = handler.post(progressRunnable!!)
//        //Log.d("PlayerSeekbar", "Handler.post() returned: $posted")
//    }
//
//    private fun stopProgressUpdates() {
//        progressRunnable?.let { handler.removeCallbacks(it) }
//    }
//
//
//
//
//    private fun saveCurrentMediaSessionQueue() {
//        Log.d(TAG, "Starting save of MediaSessionQueue")
//        CoroutineScope(Dispatchers.IO).launch {
//            val currentQueue = mediaSession.controller.queue
//            if(currentQueue != null) persistedQueueDao.saveQueue(currentQueue as List<PersistedQueueItem>)
//            Log.d(TAG, "Persisted MediaSession queue. Size: ${currentQueue?.size ?: 0}")
//        }
//    }
//
//    private suspend fun loadPersistedQueueIntoMediaSession() {
//        val persistedItems = persistedQueueDao.getPersistedQueue()
//        if (persistedItems.isNotEmpty()) {
//            val mediaSessionQueue = mutableListOf<MediaSessionCompat.QueueItem>()
//            var currentPlayingMediaId: String? = null // Logic to restore last playing item if needed
//
//            persistedItems.forEach { persistedItem ->
//                val song = songDao.getSongById(persistedItem.mediaId) // Make sure songDao.getSongById is suspend or run in right scope
//                song?.let { s ->
//                    val description = MediaDescriptionCompat.Builder()
//                        .setMediaId(s.id)
//                        .setTitle(s.title)
//                        .setSubtitle(s.artist)
//                        .setIconUri(s.thumbnail.toUri()) // Ensure thumbnail is a valid URI string
//                        // .setMediaUri(s.mediaUri.toUri()) // For playback if needed by client
//                        .build()
//                    mediaSessionQueue.add(MediaSessionCompat.QueueItem(description, persistedItem.persistenceId /* or new ID */))
//
//                    // Optional: Logic to find the last playing item to set as current metadata
//                    // For example, if you also persisted the last playing mediaId
//                    // if (s.id == lastPlayedMediaIdFromPersistence) currentPlayingMediaId = s.id
//
//                } ?: Log.w(TAG, "Song with mediaId ${persistedItem.mediaId} not found in DB during queue load.")
//            }
//
//            if (mediaSessionQueue.isNotEmpty()) {
//                mediaSession.setQueue(mediaSessionQueue)
//                mediaSession.setQueueTitle("Restored Playlist") // Or your preferred title
//
//                // Optional: Restore the last playing item's metadata and player state
//                // This is a simplified example. You'd need to persist last playback position too.
////                currentPlayingMediaId?.let { mediaId ->
////                    val songToRestore = songDao.getSongById(mediaId)
////                    songToRestore?.let {
////                        updateMediaSessionMetadata(it)
////                        // player.setMediaItem(MediaItem.fromUri(getPlaybackUrlForSong(it)))
////                        // player.prepare()
////                        // Seek to last known position if you stored it
////                        // notificationManager.updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, lastPosition)
////                    }
////                } ?: run {
////                    // If no specific item was playing, set metadata for the first item in the loaded queue
////                    mediaSessionQueue.firstOrNull()?.description?.mediaId?.let { firstMediaId ->
////                        val firstSong = songDao.getSongById(firstMediaId)
////                        firstSong?.let { updateMediaSessionMetadata(it) }
////                    }
////                }
//                Log.i(TAG, "Loaded ${mediaSessionQueue.size} items into MediaSession queue from persistence.")
//            }
//        } else {
//            Log.i(TAG, "No persisted queue found.")
//            mediaSession.setQueue(null) // Ensure queue is empty if nothing to load
//            mediaSession.setQueueTitle(null)
//        }
//    }
//
//    // Helper to update MediaSession metadata (you might have a variation of this)
//    private fun updateMediaSessionMetadata(song: Song) {
//        val metadata = MediaMetadataCompat.Builder()
//            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id)
//            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
//            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
//            //.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album ?: "") // Assuming album is nullable
//            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, parseDurationToMillis(song.duration)) // Assuming duration is Long in ms
//            // For album art, use METADATA_KEY_ALBUM_ART_URI or METADATA_KEY_DISPLAY_ICON_URI
//            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.thumbnail) // Assuming thumbnail is URI string
//            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.thumbnail)
//            .build()
//        mediaSession.setMetadata(metadata)
//
//        // Also update ExoPlayer if it's about to play this song
//        // This part depends on how your playSongFromSongId works
//        // if (player.currentMediaItem?.mediaId != song.id) {
//        //    player.setMediaItem(MediaItem.Builder().setMediaId(song.id).setUri(getPlaybackUrlForSong(song)).build())
//        //    player.prepare()
//        // }
//    }
//
//
//    private fun addNewQueue(id: String)
////    : ArrayList<QueuedMediaItem>
//    {
//        val py = Python.getInstance()
//        val module = py.getModule("backend")
//        val pyResult = module.callAttr("getRadio",id)
//
//        val queuedMediaItemList = ArrayList<QueuedMediaItem>()
//
//        if (pyResult.asList().isEmpty()) {
//            SmartMessage("Error while fetching Queue", PopupType.Warning, false, this)
//        }
//        else {
//            mediaSession.setQueue(null)
//            mediaSession.setQueueTitle("Radio Station: $id")
//            var positionInQueue = 0
//            pyResult.asList().forEach { pySongObject ->
//                CoroutineScope(Dispatchers.IO).launch {
//
//                    // Extract necessary details from the Python object
//                    val songId = pySongObject.callAttr("get", "id").toString()
//                    val title = pySongObject.callAttr("get", "title")?.toString() ?: "Unknown Title"
//                    val artist = pySongObject.callAttr("get", "artist")?.toString() ?: "Unknown Artist"
//                    val thumbnailUri = pySongObject.callAttr("get", "thumbnail")?.toString()?.toUri() // Convert to Uri
//                    val durationString = pySongObject.callAttr("get", "duration")?.toString()
//                    val durationMs = parseDurationToMillis(durationString?: "NA") // Assuming you have a helper for this
//
//                    // Create a MediaDescriptionCompat for the item
//                    val description = MediaDescriptionCompat.Builder()
//                        .setMediaId(songId)
//                        .setTitle(title)
//                        .setSubtitle(artist)
//                        .setIconUri(thumbnailUri)
//                        // You might need to set Media URI if clients use it for direct playback info
//                        // .setMediaUri(pySongObject.callAttr("get", "streamUrl")?.toString()?.toUri())
//                        .setExtras(Bundle().apply {
//                            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
//                        }) // Store duration here
//                        .build()
//
//                    // Call the session callback's onAddQueueItem method
//                    // This will internally add it to the mediaSession's queue
//                    // and trigger any observer updates.
//                    // Note: The second parameter 'index' for onAddQueueItem(description, index)
//                    // is not directly used here since mediaSession.controller.addQueueItem
//                    // adds to the end by default if you call the single argument version.
//                    // If your onAddQueueItem in the callback doesn't create and add
//                    // the QueueItem itself, you might need to adjust.
//
//                    // To directly simulate the controller adding an item (which then calls onAddQueueItem)
//                    // This is the standard way to trigger it.
//                    mediaSession.controller?.addQueueItem(description)
//
//                    // --- Alternative if you want to bypass the controller and call your callback directly ---
//                    // --- (Less standard, but possible if MediaController isn't readily available here) ---
//                    // (this.mediaSession.mCallback as? MediaSessionCallback)?.onAddQueueItem(description)
//                    // Be cautious with direct callback calls as it bypasses some controller logic.
//
//                    Log.d(TAG, "Attempting to add to queue: $title (ID: $songId)")
//
//                    // If you were building a list to return (as in your original code):
//                    // queuedMediaItemList.add(QueuedMediaItem(...))
//                    // But if the goal is to add to the MediaSession queue directly,
//                    // the above mediaSession.controller?.addQueueItem(description) is key.
//
//                    positionInQueue++
//                }
//                if (positionInQueue > 0) {
//                    SmartMessage("Queue updated with $positionInQueue songs.", PopupType.Info, false, this)
//                }
//                }
//
//            }
//
////                    val d = item.callAttr("get", "duration").toString()
////                    queuedMediaItemList.add(
////                        QueuedMediaItem(
////                            songId = item.callAttr("get", "id").toString(),
////                            position = positionInQueue,
//////                            title = item.callAttr("get", "title").toString(),
//////                            artist = item.callAttr("get", "artist").toString(),
//////                            thumbnail = item.callAttr("get", "thumbnail").toString(),
////                            // filtering out wrong durations here and not in backend, because Kotlin is faster
//////                            duration = (if (Regex("""^(\d{1,2}):(\d{1,2})$""").matchEntire(d) == null) "NA" else d).toString(),
////                        )
////                    )
////                i++
//            }
//        }
//
//
//
//
//
//
//
