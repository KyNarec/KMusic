package com.kynarec.kmusic.service

import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
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
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.ListenableFuture
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.data.db.dao.SongDao
import io.ktor.http.ContentDisposition.Companion.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var periodicUpdateJob: Job? = null
    private val updateInterval = 10_000L // 10 seconds

    private lateinit var songDao: SongDao
    private var currentSongId: String? = null
//    private var currentSong : Song? = null
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

    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            currentSongId = mediaItems.firstOrNull()?.mediaId
//            Log.i("PlayerService", "currentSongId has been set to: $currentSongId")
//            if (mediaItems.firstOrNull() != null) currentSong =
//                Song(
//                    id = mediaItems.firstOrNull()?.mediaId?: "",
//                    title = mediaItems.firstOrNull()?.mediaMetadata?.title.toString(),
//                    artist = mediaItems.firstOrNull()?.mediaMetadata?.artist.toString(),
//                    duration = mediaItems.firstOrNull()?.mediaMetadata?.durationMs.toString(),
//                    thumbnail = mediaItems.firstOrNull()?.mediaMetadata?.artworkUri.toString(),
//                )


            val updatedMediaItems = mediaItems.map { it.buildUpon().setMimeType("audio/mpeg").build() }.toMutableList()
            return super.onAddMediaItems(mediaSession, controller, updatedMediaItems)
        }
    }

    // Add this inside your PlayerServiceModern class
    companion object {
        private const val CACHE_DIR = "kmusic_cache"
        private const val MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB
        private var cache: SimpleCache? = null
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

        // Ensure the DAO is initialized here.
        songDao = (application as MyApp).database.songDao()
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
            MediaLibrarySessionCallback()).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        serviceScope.cancel()

        cache?.release()
        cache = null

        mediaLibrarySession?.run {
            player.release()
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

        val songId = currentSongId?: return

        serviceScope.launch(Dispatchers.IO) {
            try {
                // Get playback info on the main thread
                val (isPlaying, currentPosition) = withContext(Dispatchers.Main) {
                    Pair(player?.isPlaying, player?.currentPosition)
                }

                val totalTimeToAdd = if (isPlaying == true) {
                    accumulatedPlayTime + (System.currentTimeMillis() - playbackStartTime)
                } else {
                    accumulatedPlayTime
                }

                // Get current song data from the database
                val song = songDao.getSongById(songId)

                if (song != null) {
                    val updatedSong = song.copy(
                        totalPlayTimeMs = song.totalPlayTimeMs + totalTimeToAdd
                    )
                    songDao.updateSong(updatedSong)

                    Log.i("PlayerService", "Updated song in database by $totalTimeToAdd to ${song.totalPlayTimeMs + totalTimeToAdd}")

                    accumulatedPlayTime = 0
                    playbackStartTime = System.currentTimeMillis()
                } else {
                    Log.w("PlayerService", "Song with ID $songId not found in database. Cannot update playback time.")
//                    songDao.insertSong(currentSong!!)
//                    Log.w("PlayerService", "Song with ID ${currentSong!!.id} inserted into database.")
                }

            } catch (e: Exception) {
                Log.e("PlayerService", "Error updating playback time: ${e.message}", e)
            }
        }
    }
}
