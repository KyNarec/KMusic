package com.kynarec.kmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheKeyFactory
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import coil.imageLoader
import coil.request.ImageRequest
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.MainActivity
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.createFullMediaItem
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayOutputStream

@UnstableApi
class PlayerServiceModern : MediaLibraryService(), KoinComponent {
    private val downloadManager: DownloadManager by inject() // Inject this!
    private val downloadCache: SimpleCache by inject()
    private val httpDataSourceFactory: DefaultHttpDataSource.Factory by inject()
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
        /**
         * Called when the player transitions to a new song or the playlist ends
         */
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//            super.onMediaItemTransition(mediaItem, reason)
            when (reason) {
                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ->
                    Log.i("PlayerService", "MEDIA_ITEM_TRANSITION_REASON_AUTO (Playback has automatically transitioned to the next media item)")
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED ->
                    Log.i("PlayerService", "MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED (The current media item has changed because of a change in the playlist)")
                Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT ->
                    Log.i("PlayerService", "MEDIA_ITEM_TRANSITION_REASON_REPEAT (The media item has been repeated)")
                Player.MEDIA_ITEM_TRANSITION_REASON_SEEK ->
                    Log.i("PlayerService", "MEDIA_ITEM_TRANSITION_REASON_SEEK (A seek to another media item has occurred)")
            }
            Log.i("PlayerService", "mediaItem?.localConfiguration?.uri.toString() ${mediaItem?.localConfiguration?.uri.toString()}")
            saveCurrentPlaybackTime()

            val mediaItem = mediaItem
            val currentIndex = player?.currentMediaItemIndex ?: -1
            val songId = mediaItem?.mediaId ?: return

            val download = downloadManager.downloadIndex.getDownload(songId)
            val isDownloaded = download != null && download.state == Download.STATE_COMPLETED

            if (isDownloaded) {
                Log.i("PlayerService", "Song $songId is downloaded. Ensuring local playback.")
                if (downloadCache.getCachedSpans(songId).isNotEmpty()) {
                    Log.w(
                        "PlayerService",
                        "Download record exists and cache is notEmpty for $songId"
                    )
                } else {
                    Log.w("PlayerService", "Download record exists but cache is empty for $songId. Playing online.")
                }

                // Check if the current MediaItem already has the custom cache key set
                if (mediaItem.localConfiguration?.customCacheKey != songId) {
                    Log.i(tag, "No customCacheKey found")
                    val context = this@PlayerServiceModern
                    serviceScope.launch(Dispatchers.IO) {
                        val request = ImageRequest.Builder(context)
                            .data(mediaItem.mediaMetadata.artworkUri)
                            .allowHardware(false)
                            .build()

                        val result = context.imageLoader.execute(request)
                        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                        val artworkData = bitmap?.let {
                            val outputStream = ByteArrayOutputStream()
                            it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.toByteArray()
                        }
                        val offlineMediaItem = MediaItem.Builder()
                            .setMediaId(mediaItem.mediaId)
                            .setUri(mediaItem.localConfiguration?.uri.toString())
                            .setMediaMetadata(MediaMetadata.Builder()
                                .setTitle(mediaItem.mediaMetadata.title)
                                .setArtist(mediaItem.mediaMetadata.artist)
                                .setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FRONT_COVER)                                .build())
                            .setCustomCacheKey(songId)
                            .build()

                        withContext(Dispatchers.Main) {
                            Log.i("PlayerService", "Replacing MediaItem with cached version for $songId")
                            player?.replaceMediaItem(currentIndex, offlineMediaItem)
                        }
                    }
                    return
                }
            }

            when (mediaItem.localConfiguration?.uri.toString()) {
                "EMPTY" -> {
                    Log.i("PlayerService", "mediaItem is empty")

                    player?.stop()

                    serviceScope.launch {
                        val fullMediaItem = mediaItem.createFullMediaItem()

                        withContext(Dispatchers.Main) {
                            if (fullMediaItem != MediaItem.EMPTY && currentIndex == player?.currentMediaItemIndex) {
                                player?.replaceMediaItem(currentIndex, fullMediaItem)
                                player?.prepare()
                                player?.play()
                            }
                        }
                    }
                    return
                }

                "NA" -> {
                    Log.e("PlayerService", "mediaItem URI cannot be fetched")
                    SmartMessage("Fetching error", PopupType.Error, false, this@PlayerServiceModern)
                    player?.seekToNextMediaItem()
                    return
                }

                else -> {
                    accumulatedPlayTime = 0L
                    playbackStartTime = System.currentTimeMillis()
                    currentSongId = mediaItem.mediaId
                    Log.i("PlayerService", "Playing: ${mediaItem.mediaId}")                }
            }

            val playlistSize = player?.mediaItemCount ?: 0
            val neighbors = listOf(currentIndex - 1, currentIndex + 1)

            serviceScope.launch {
                neighbors.forEach { index ->
                    if (index in 0 until playlistSize) {
                        // Get the item currently at that index
                        val neighborItem = withContext(Dispatchers.Main) { player?.getMediaItemAt(index) }

                        if (neighborItem?.localConfiguration?.uri.toString() == "EMPTY") {
                            Log.i("PlayerService", "Pre-fetching neighbor at index $index")
                            val fullNeighbor = neighborItem?.createFullMediaItem()

                            withContext(Dispatchers.Main) {
                                // Re-verify index and item identity before replacing
                                if (index < (player?.mediaItemCount ?: 0) &&
                                    player?.getMediaItemAt(index)?.mediaId == neighborItem?.mediaId) {
                                    player?.replaceMediaItem(index, fullNeighbor!!)
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Called when play/pause state changes
         */
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

        /**
         * Called when the player encounters an error
         */
        override fun onPlayerError(error: PlaybackException) {
            val exoPlaybackException = error as ExoPlaybackException
            when (exoPlaybackException.message) {
                "Source error" -> {
                    SmartMessage("Source Error", PopupType.Error, false, this@PlayerServiceModern)
                    player?.seekToNextMediaItem()
                    player?.prepare()
                    player?.play()
                }
                else -> {
                    SmartMessage(
                        "Unknown playback error",
                        PopupType.Error,
                        false,
                        this@PlayerServiceModern
                    )
                    player?.seekToNextMediaItem()
                    player?.prepare()
                    player?.play()
                }
            }
            Log.e("PlayerService", "Player Error: ", error)
            super.onPlayerError(error)
        }
    }


    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedItems = mediaItems.map { item ->
                // is only false when partialMediaItem was created
                if (item.mediaMetadata.isPlayable == false)
                {
                    item.buildUpon()
                        .setUri(item.mediaId) // Set the URI to the ID temporarily or a dummy value
                        .setCustomCacheKey(item.mediaId)
                        .build()
                } else {
                    item
                }
            }.toMutableList()

            return Futures.immediateFuture(updatedItems)
        }


        // This is where Android Auto requests the root of your media library.
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
                            val sortedSongs = songDao.getAllSongs().sortedBy { it.artists.first().name }
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
        // New constants for sorting functionality
        const val ROOT_ID = "root_id"
        const val ALL_SONGS_ID = "all_songs"
        const val SORT_OPTIONS_ID = "sort_options"
        const val SORT_BY_TITLE_ID = "sort_by_title"
        const val SORT_BY_ARTIST_ID = "sort_by_artist"
    }

    val cacheKeyFactory = CacheKeyFactory { dataSpec ->
        // Media3 will check dataSpec.key (which is set by setCustomCacheKey) first
        dataSpec.key ?: dataSpec.uri.toString()
    }

    override fun onCreate() {
        super.onCreate()

        try {
            songDao = (application as KMusic).database.songDao()
            Log.i("PlayerService", "songDao has been initialized.")

            val notificationProvider = DefaultMediaNotificationProvider(this)
            notificationProvider.setSmallIcon(R.drawable.ic_launcher_foreground_scaled)
            setMediaNotificationProvider(notificationProvider)

            val cacheDataSourceFactory: DataSource.Factory =
                CacheDataSource.Factory()
                    .setCache(downloadCache)
                    .setUpstreamDataSourceFactory(httpDataSourceFactory)
                    .setCacheWriteDataSinkFactory(null) // Disable writing.
//
//            val cacheDataSourceFactory = CacheDataSource.Factory()
//                .setCache(downloadCache)
//                .setUpstreamDataSourceFactory(httpDataSourceFactory)
//                .setCacheWriteDataSinkFactory(null)
//                .setCacheKeyFactory(cacheKeyFactory)
//                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR) // Fallback to upstream if cache fails

            val mediaSourceFactory = DefaultMediaSourceFactory(this)
                .setDataSourceFactory(cacheDataSourceFactory)

            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory) // used for cashing and download
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

                    Log.i("PlayerService", "Updated song $songId in database by $totalTimeToAdd to ${song.totalPlayTimeMs + totalTimeToAdd}")

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