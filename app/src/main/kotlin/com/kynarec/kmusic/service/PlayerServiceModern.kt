package com.kynarec.kmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import com.kynarec.kmusic.MainActivity
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.utils.createMediaItemFromSong
import com.kynarec.kmusic.utils.createPartialMediaItemFromSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor

@UnstableApi
class PlayerServiceModern : MediaLibraryService() {
    private val tag = "PlayerServiceModern"
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
            MIN_PLAYBACK_RESUME_BUFFER
        )
        .setTargetBufferBytes(-1)
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var periodicUpdateJob: Job? = null
    private val updateInterval = 10_000L // 10 seconds

    private lateinit var songDao: SongDao
    private var currentSongId: String? = null
    private var accumulatedPlayTime = 0L
    private var playbackStartTime = 0L

    private val playerListener = object : Player.Listener {
        // Called when the player transitions to a new song or the playlist ends
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            // Save playback time for the previous song before transitioning
            saveCurrentPlaybackTime()
            // Reset trackers for the new song
            accumulatedPlayTime = 0L
            playbackStartTime = System.currentTimeMillis()
            // Update the current song ID
            currentSongId = mediaItem?.mediaId
            Log.i("PlayerService", "Transitioned to new media item: ${mediaItem?.mediaId}")
        }

        // Called when play/pause state changes
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                // Started playing
                playbackStartTime = System.currentTimeMillis()
                startPeriodicUpdates()
            } else {
                // Paused or stopped
                saveCurrentPlaybackTime()
                stopPeriodicUpdates()
            }
        }

        // Called when the player encounters an error
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            super.onPlayerError(error)
            Log.e("PlayerService", "Player Error: ", error)
            // Here you could stop the service, show a toast, or try to recover.
        }
    }

    private val executor: Executor = MoreExecutors.directExecutor()

    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            Log.d(tag, "onAddMediaItems called with ${mediaItems.size} items")
            val future = SettableFuture.create<MutableList<MediaItem>>()
            if (mediaItems.isEmpty() || mediaItems.firstOrNull()?.mediaId == null) {
                future.set(mutableListOf())
                return future
            }

            val mediaId = mediaItems.first().mediaId
            Log.i(tag, "Attempting to retrieve full song data for mediaId: $mediaId")

            serviceScope.launch(Dispatchers.IO) {
                try {
                    val song = songDao.getSongById(mediaId)
                    if (song != null) {
                        Log.i(tag, "Found song in database: ${song.title}")
                        val fullMediaItem = createMediaItemFromSong(context = applicationContext, song = song)
                        future.set(mutableListOf(fullMediaItem))
                    } else {
                        Log.e(tag, "Song not found for mediaId: $mediaId")
                        future.set(mutableListOf())
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error retrieving song from database.", e)
                    future.setException(e)
                }
            }
            return future
        }


        // MODIFIED: This is where Android Auto requests the root of your media library.
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            // Android Auto expects a browsable root item. We use a "Songs" category.
            val libraryRoot = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .setTitle("Songs")
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(libraryRoot, params))
        }

        // MODIFIED: This handles the different browsable folders, including the new sort options.
        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            Log.d(tag, "onGetChildren called with parentId: $parentId")

            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

            serviceScope.launch(Dispatchers.IO) {
                try {
                    val mediaItems = when (parentId) {
                        ROOT_ID -> {
                            listOf(
                                MediaItem.Builder()
                                    .setMediaId(ALL_SONGS_ID)
                                    .setMediaMetadata(MediaMetadata.Builder().setTitle("All Songs").setIsBrowsable(true).setIsPlayable(false).build())
                                    .build(),
                                MediaItem.Builder()
                                    .setMediaId(SORT_OPTIONS_ID)
                                    .setMediaMetadata(MediaMetadata.Builder().setTitle("Sort").setIsBrowsable(true).setIsPlayable(false).build())
                                    .build()
                            )
                        }
                        ALL_SONGS_ID -> {
                            val allSongs = songDao.getAllSongs()
                            allSongs.map { createPartialMediaItemFromSong(it, applicationContext) }
                        }
                        SORT_OPTIONS_ID -> {
                            listOf(
                                MediaItem.Builder()
                                    .setMediaId(SORT_BY_TITLE_ID)
                                    .setMediaMetadata(MediaMetadata.Builder().setTitle("Title (A-Z)").setIsBrowsable(true).setIsPlayable(false).build())
                                    .build(),
                                MediaItem.Builder()
                                    .setMediaId(SORT_BY_ARTIST_ID)
                                    .setMediaMetadata(MediaMetadata.Builder().setTitle("Artist (A-Z)").setIsBrowsable(true).setIsPlayable(false).build())
                                    .build()
                            )
                        }
                        SORT_BY_TITLE_ID -> {
                            val sortedSongs = songDao.getAllSongs().sortedBy { it.title }
                            sortedSongs.map { createPartialMediaItemFromSong(it, applicationContext) }
                        }
                        SORT_BY_ARTIST_ID -> {
                            val sortedSongs = songDao.getAllSongs().sortedBy { it.artist }
                            sortedSongs.map { createPartialMediaItemFromSong(it, applicationContext) }
                        }
                        else -> {
                            Log.w(tag, "Invalid parentId: $parentId")
                            emptyList<MediaItem>()
                        }
                    }
                    future.set(LibraryResult.ofItemList(mediaItems.toMutableList(), params))
                } catch (e: Exception) {
                    Log.e(tag, "Error loading songs from database.", e)
                    future.setException(e)
                }
            }
            return future
        }


        // MODIFIED: This must return a FULL MediaItem with the URI.
        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d(tag, "onGetItem called with mediaId: $mediaId")

            val future = SettableFuture.create<LibraryResult<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    // Check if the mediaId corresponds to a browsable folder
                    val browsableItem = when (mediaId) {
                        ROOT_ID -> {
                            MediaItem.Builder().setMediaId(ROOT_ID).setMediaMetadata(MediaMetadata.Builder().setTitle("Songs").setIsBrowsable(true).setIsPlayable(false).build()).build()
                        }
                        ALL_SONGS_ID -> {
                            MediaItem.Builder().setMediaId(ALL_SONGS_ID).setMediaMetadata(MediaMetadata.Builder().setTitle("All Songs").setIsBrowsable(true).setIsPlayable(false).build()).build()
                        }
                        SORT_OPTIONS_ID -> {
                            MediaItem.Builder().setMediaId(SORT_OPTIONS_ID).setMediaMetadata(MediaMetadata.Builder().setTitle("Sort").setIsBrowsable(true).setIsPlayable(false).build()).build()
                        }
                        SORT_BY_TITLE_ID -> {
                            MediaItem.Builder().setMediaId(SORT_BY_TITLE_ID).setMediaMetadata(MediaMetadata.Builder().setTitle("Title (A-Z)").setIsBrowsable(true).setIsPlayable(false).build()).build()
                        }
                        SORT_BY_ARTIST_ID -> {
                            MediaItem.Builder().setMediaId(SORT_BY_ARTIST_ID).setMediaMetadata(MediaMetadata.Builder().setTitle("Artist (A-Z)").setIsBrowsable(true).setIsPlayable(false).build()).build()
                        }
                        else -> null // This is a song, not a browsable folder
                    }

                    if (browsableItem != null) {
                        Log.i(tag, "Returning a browsable item for mediaId: $mediaId")
                        future.set(LibraryResult.ofItem(browsableItem, null))
                    } else {
                        // This is a song, so fetch the full song data
                        val song = songDao.getSongById(mediaId)
                        if (song != null) {
                            Log.i(tag, "Found song with mediaId: $mediaId. Creating full MediaItem.")
                            val mediaItem = createMediaItemFromSong(song, applicationContext)
                            future.set(LibraryResult.ofItem(mediaItem, null))
                        } else {
                            Log.e(tag, "Could not find song with mediaId: $mediaId")
                            future.set(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error in onGetItem: ${e.message}", e)
                    future.setException(e)
                }
            }
            return future
        }
    }


    companion object {
        private const val CACHE_DIR = "kmusic_cache"
        private const val MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB
        private var cache: SimpleCache? = null

        // New constants for sorting functionality
        const val ROOT_ID = "root_id"
        const val ALL_SONGS_ID = "all_songs"
        const val SORT_OPTIONS_ID = "sort_options"
        const val SORT_BY_TITLE_ID = "sort_by_title"
        const val SORT_BY_ARTIST_ID = "sort_by_artist"
    }

    // Function to initialize the cache
    @Synchronized
    private fun getCache(): SimpleCache {
        if (cache == null) {
            val cacheDirectory = File(this.cacheDir, CACHE_DIR)
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
            cache = SimpleCache(cacheDirectory, evictor, StandaloneDatabaseProvider(this))
        }
        return cache!!
    }

    override fun onCreate() {
        super.onCreate()

        try {// Ensure the DAO is initialized here.
            songDao = (application as KMusic).database.songDao()
            Log.i("PlayerService", "songDao has been initialized.")

            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(getCache())
                .setUpstreamDataSourceFactory(DefaultDataSource.Factory(this))

            val mediaSourceFactory = DefaultMediaSourceFactory(this)
                .setDataSourceFactory(cacheDataSourceFactory)

            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory) // used for cashing
                .setAudioAttributes(
                    AudioAttributes.DEFAULT, true
                )
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_LOCAL)
                .setLoadControl(loadController)
                .build()

            player?.addListener(playerListener)

            mediaLibrarySession = MediaLibrarySession.Builder(
                this,
                player!!,
                MediaLibrarySessionCallback()
            ).build()

            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            mediaLibrarySession?.setSessionActivity(pendingIntent)

            // Commented out the debug song deletion code. You can uncomment this if needed for testing.
            CoroutineScope(Dispatchers.IO).launch {
                songDao.deleteSongById("s6GIT4RhFv0")
                songDao.deleteSongById("O-A7It0bZ2w")
                songDao.deleteSongById("4UnU3r0M3zg")
                songDao.deleteSongById("D4INE2zO9OU")
                songDao.deleteSongById("8901V1M5lDk")
                songDao.deleteSongById("A__cH65WRvE")
                songDao.deleteSongById("4FkfyssnHqU")
                songDao.deleteSongById("ZHLNudYcQ0c")
                songDao.deleteSongById("RRQwn8rmZfo")
                songDao.deleteSongById("hfyi9cewKe4")
                songDao.deleteSongById("0-HQzVZRO68")
                songDao.deleteSongById("63xPXEB4fjA")
                songDao.deleteSongById("_Zm6Iy0wMWk")
                songDao.deleteSongById("X-o8eGhKhlI")
                songDao.deleteSongById("ZICUilv4KF0")
                songDao.deleteSongById("XhjqmAoBKCQ")
                songDao.deleteSongById("_UWOHofs0kA")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error initializing PlayerService: ${e.message}", e)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i("PlayerService", "onTaskRemoved called")
        if (player?.playWhenReady == false) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()

        cache?.release()
        cache = null

        mediaLibrarySession?.run {
            player?.release()
            release()
            mediaLibrarySession = null
        }
        player?.removeListener(playerListener)
        player = null
        super.onDestroy()
    }

    private fun startPeriodicUpdates() {
        if (periodicUpdateJob?.isActive == true) {
            return
        }

        periodicUpdateJob = serviceScope.launch {
            while (isActive) {
                saveCurrentPlaybackTime()
                delay(updateInterval)
            }
        }
    }

    private fun stopPeriodicUpdates() {
        periodicUpdateJob?.cancel()
    }

    private fun saveCurrentPlaybackTime() {
        Log.i("PlayerService", "Saving current playback time")
        Log.i("PlayerService", "currentSongId = $currentSongId")

        val songId = currentSongId ?: return

        serviceScope.launch(Dispatchers.IO) {
            try {
                // Get playback info on the main thread
                val (isPlaying, _) = withContext(Dispatchers.Main) {
                    Pair(player?.isPlaying, player?.currentPosition)
                }

                val totalTimeToAdd = if (isPlaying == true) {
                    accumulatedPlayTime + (System.currentTimeMillis() - playbackStartTime)
                } else {
                    accumulatedPlayTime
                }

                if (totalTimeToAdd <= 0) {
                    // Avoid unnecessary database writes if no time has passed.
                    Log.d("PlayerService", "No new playback time to save.")
                    return@launch
                }

                // Get current song data from the database
                val song = songDao.getSongById(songId)

                if (song != null) {
                    val updatedSong = song.copy(
                        totalPlayTimeMs = song.totalPlayTimeMs + totalTimeToAdd
                    )
                    songDao.updateSong(updatedSong)

                    Log.i("PlayerService", "Updated song in database by $totalTimeToAdd to ${song.totalPlayTimeMs + totalTimeToAdd}")

                    // Reset trackers after successful save
                    accumulatedPlayTime = 0
                    playbackStartTime = System.currentTimeMillis()
                } else {
                    Log.w("PlayerService", "Song with ID $songId not found in database. Cannot update playback time.")
                }
            } catch (e: Exception) {
                Log.e("PlayerService", "Error updating playback time: ${e.message}", e)
            }
        }
    }
}