package com.kynarec.kmusic.ui.viewModels

import android.app.Application
import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kynarec.kmusic.data.db.dao.AlbumDao
import com.kynarec.kmusic.data.db.dao.ArtistDao
import com.kynarec.kmusic.data.db.dao.PlaylistDao
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.Artist
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongAlbumMap
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.service.innertube.getRadioFlow
import com.kynarec.kmusic.ui.screens.song.SortOption
import com.kynarec.kmusic.utils.createMediaItemFromSong
import com.kynarec.kmusic.utils.createPartialMediaItemFromSong
import com.kynarec.kmusic.utils.parseDurationToMillis
import com.kynarec.kmusic.utils.toSeconds
import com.kynarec.kmusic.utils.toSong
import com.kynarec.lrclib.LyricsRepository
import com.kynarec.lrclib.model.Lyrics
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.parser.LrcParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MusicUiState(
    val songsList: List<PlaylistItem> = emptyList(),
    var currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val currentDurationLong: Long = 0,
    val showControlBar: Boolean = false,
    val songsSortOption: SortOption = SortOption("All"),
    val searchParam: SortOption = SortOption("Song"),
    val timeLeftMillis: Long = 0,
    val currentLyrics: SyncedLyrics? = null,
    val isLoadingLyrics: Boolean = true,
)

/**
 * Using this because queue screen needs a key for the list
 */
data class PlaylistItem(
    val id: Long = Random.nextLong(),
    val song: Song
)

@OptIn(UnstableApi::class)
class MusicViewModel
    (
    private val application: Application,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val lyricsRepository: LyricsRepository,
    private val downloadManager: DownloadManager,
    private val downloadCache: SimpleCache
) : ViewModel() {
    private val tag = "MusicViewModel"

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    private var mediaController: MediaController? = null

    private var playlistLoadJob: Job? = null
    private var currentLoadingPlaylistId: String? = null

    private var sleepTimerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.i(tag, "onMediaItemTransition called with reason: $reason")

            // When the song changes, find it in list and update the state
            val songsList = _uiState.value.songsList
            val currentSong = songsList.find { it.song.id == mediaItem?.mediaId }?.song

            _uiState.update {
                it.copy(
                    currentSong = currentSong,
                    currentDurationLong = _uiState.value.currentSong?.duration?.parseDurationToMillis()
                        ?: 0L,
                )
            }
            _uiState.update { it.copy(currentLyrics = null) }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            _uiState.update { it.copy(showControlBar = !timeline.isEmpty) }
        }
    }

    init {
        initializeController()
        calledStackTrace()
        removeNotLikedAlbums()
        removeNotLikedArtists()
    }

    private fun initializeController() {
        val context = application.applicationContext
        val sessionToken =
            SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))

        // Store the future so we can release it later
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture = future

        future.addListener({
            try {
                val controller = future.get()
                this.mediaController = controller

                // Reconstruct the Playlist from the Player's current queue
                val itemsInQueue = mutableListOf<PlaylistItem>()
                for (i in 0 until controller.mediaItemCount) {
                    itemsInQueue.add(PlaylistItem(song = controller.getMediaItemAt(i).toSong()))
                }

                val currentSong = controller.currentMediaItem?.toSong()

                _uiState.update { it.copy(
                    songsList = itemsInQueue,
                    currentSong = currentSong,
//                    currentDurationLong = currentSong?.duration?.parseDurationToMillis()
//                        ?: 0L,
                    isPlaying = controller.isPlaying,
                    showControlBar = itemsInQueue.isNotEmpty()
                ) }

                controller.addListener(playerListener)
                updatePosition()
            } catch (e: Exception) {
                Log.e(tag, "Failed to connect", e)
            }
        }, ContextCompat.getMainExecutor(context)) // Runs on Main Thread safely
    }

    private fun calledStackTrace() {
        val stackTrace = Thread.currentThread().stackTrace
        // We print the first 10 elements to see the "chain of command"
        Log.d("MusicViewModel", "Initialization Trace:")
        stackTrace.take(15).forEach { element ->
            Log.d("MusicViewModel", "  at ${element.className}.${element.methodName}")
        }
    }

    private fun removeNotLikedAlbums() {
        viewModelScope.launch(Dispatchers.IO) {
            albumDao.deleteUnbookmarkedAlbums()
        }
    }

    private fun removeNotLikedArtists() {
        viewModelScope.launch(Dispatchers.IO) {
            artistDao.deleteUnbookmarkedArtists()
        }
    }
    private fun updatePosition() {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                val currentPosition = mediaController?.currentPosition ?: 0L
                val totalDuration = mediaController?.duration ?: 0L // Fetch total duration

                _uiState.update {
                    if (it.currentPosition != currentPosition || it.currentDurationLong != totalDuration) {
                        it.copy(
                            currentPosition = currentPosition,
                            currentDurationLong = totalDuration
                        )
                    } else {
                        it
                    }
                }

                delay(16L) // 60 fps
            }
        }
    }

    /**
     * Plays a specific song.
     * @param song The song to play.
     */
    fun playSong(song: Song) {
        Log.i(tag, "playSong called")
        val context = application.applicationContext

        val controller = mediaController ?: return
        val songList = _uiState.value.songsList

        // Find the index of the tapped song

        _uiState.update { it.copy(currentSong = song) }
        val alreadyInQueue = _uiState.value.songsList.any { it.song.id == song.id }

        if (!alreadyInQueue) {
            val newItem = PlaylistItem(song = song)
            _uiState.update { it.copy(songsList = it.songsList + newItem) }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val mediaItem = createMediaItemFromSong(song, context)
            withContext(Dispatchers.Main) {
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
            }
        }
    }


    /**
     * Plays a playlist starting from a specific song.
     * @param songs The list of songs in the playlist.
     * @param startSong The song to start playing from.
     */
    @kotlin.OptIn(ExperimentalUuidApi::class)
    fun playPlaylist(
        songs: List<Song>,
        startSong: Song,
        playlistId: String = Uuid.random().toString()
    ) {
        Log.i(tag, "playPlaylist called with ${songs.size} songs, starting at ${startSong.title}")
        val context = application.applicationContext

        playlistLoadJob?.cancel()
        currentLoadingPlaylistId = playlistId

        val controller = mediaController ?: return
        controller.stop()

        // 1. Update UI State immediately
        _uiState.update { it ->
            it.copy(
                songsList = songs.map { song -> song.toPlaylistItem() },
                currentSong = startSong
            )
        }

        // 2. Prepare MediaItems and update Player asynchronously
        playlistLoadJob = viewModelScope.launch {
            val startIndex = songs.indexOfFirst { it.id == startSong.id }.takeIf { it != -1 } ?: 0

            // Define the window of songs to load immediately (e.g., 5 before and 5 after)
            val windowSize = 2
            val windowStart = (startIndex - windowSize).coerceAtLeast(0)
            val windowEnd = (startIndex + windowSize).coerceAtMost(songs.size - 1)

            val initialSongs = songs.subList(windowStart, windowEnd + 1)
            val startSongIndexInWindow = startIndex - windowStart

            // Convert initial window to full MediaItems (heavy operation, but for few items)
            val initialMediaItems = withContext(Dispatchers.IO) {
                initialSongs.map { createMediaItemFromSong(it, context) }
            }

            if (currentLoadingPlaylistId != playlistId) return@launch

            // Update the player with the initial window
            controller.setMediaItems(initialMediaItems, startSongIndexInWindow, 0L)
            controller.prepare()
            controller.play()

            // 3. Load the rest of the songs in the background
            // Load "After" songs first (priority for playback continuity)
            if (windowEnd < songs.size - 1) {
                val afterSongs = songs.subList(windowEnd + 1, songs.size)
                loadChunks(afterSongs, append = true, playlistId)
            }

            // Load "Before" songs (reverse order to maintain index 0 insertion)
            if (windowStart > 0) {
                val beforeSongs = songs.subList(0, windowStart)
                // We chunk the before list, but we need to add them in a way that preserves order.
                // If we add at index 0, we should add the LAST chunk first.
                // Example: [1, 2, 3, 4, 5]. Window starts at 6.
                // Chunk size 2. Chunks: [1, 2], [3, 4], [5].
                // Add [5] at 0 -> [5, 6...]
                // Add [3, 4] at 0 -> [3, 4, 5, 6...]
                // Add [1, 2] at 0 -> [1, 2, 3, 4, 5, 6...]
                loadChunks(beforeSongs, append = false, playlistId)
            }
        }
    }

    private suspend fun loadChunks(songs: List<Song>, append: Boolean, playlistId: String) {
        val context = application.applicationContext
        val chunkSize = 30
        val chunks = songs.chunked(chunkSize)

        val chunksToProcess = if (append) chunks else chunks.asReversed()

        chunksToProcess.forEach { chunk ->
            yield()
            if (currentLoadingPlaylistId != playlistId) return@forEach
            val mediaItems = withContext(Dispatchers.IO) {
                chunk.map {
                    createPartialMediaItemFromSong(it, context)
                }
            }

            withContext(Dispatchers.Main) {
                if (currentLoadingPlaylistId == playlistId) {
                    if (append) {
                        mediaController?.addMediaItems(mediaItems)
                    } else {
                        mediaController?.addMediaItems(0, mediaItems)
                    }
                    Log.i(tag, "Loaded chunk of ${chunk.size} songs")
                }
            }
        }
    }

    fun playShuffledPlaylist(songs: List<Song>) {
        Log.i(tag, "playShuffledPlaylist called with ${songs.size} songs")
        val shuffledSongs = songs.shuffled()

        playPlaylist(shuffledSongs, shuffledSongs.first())
    }

    fun playSongByIdWithRadio(song: Song, removeViewModelList: Boolean = true) {
        Log.i(tag, "playSongByIdWithRadio called with songId: ${song.id}")
        val context = application.applicationContext

        if (removeViewModelList) {
            _uiState.update {
                it.copy(songsList = emptyList())
            }
        }

    // Play the selected song immediately
        playSong(song)

        viewModelScope.launch {
            try {
                getRadioFlow(song.id)
                    .flowOn(Dispatchers.IO) // network + parsing off main
                    .collect { radioSong ->
                        if (radioSong.id != song.id) {
                            val mediaItem = createPartialMediaItemFromSong(radioSong, context)
                            _uiState.update {
                                it.copy(songsList = it.songsList + radioSong.toPlaylistItem())
                            }
                            // Update UI immediately for each song
                            withContext(Dispatchers.Main) {
                                mediaController?.addMediaItem(mediaItem)
                                Log.i(tag, "Added radio song ${radioSong.title}")
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(tag, "Failed to load radio songs for ${song.id}")
            } finally {
                val nextMediaItem =
                    createMediaItemFromSong(_uiState.value.songsList[1].song, context)
                withContext(Dispatchers.Main) {
                    mediaController?.replaceMediaItem(1, nextMediaItem)
                }
            }
        }
        Log.i(tag, "playSongByIdWithRadio done")
    }

    fun playNext(song: Song) {
        Log.i(tag, "playNext called with song ${song.title}")
        val context = application.applicationContext
        viewModelScope.launch {
            try {
                val mediaItem = createMediaItemFromSong(song, context)
                val currentMediaIndex = mediaController?.currentMediaItemIndex ?: 0
                val nextIndex = currentMediaIndex + 1
                Log.i(
                    tag,
                    "Current index: $currentMediaIndex, Insertion index: $nextIndex"
                )

                mediaController?.addMediaItem(nextIndex, mediaItem)
                _uiState.update { currentState ->
                    val updatedList = currentState.songsList.toMutableList()

                    if (nextIndex <= updatedList.size) {
                        updatedList.add(nextIndex, song.toPlaylistItem())
                    } else {
                        updatedList.add(song.toPlaylistItem())
                    }

                    currentState.copy(songsList = updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(tag, "Failed to play next song ${song.title}")
            }
        }
    }

    fun enqueueSong(song: Song) {
        val context = application.applicationContext
        Log.i(tag, "enqueue called with song ${song.title}")
        viewModelScope.launch {
            try {
                val mediaItem = if (_uiState.value.songsList.size < 2)
                    createMediaItemFromSong(song, context)
                    else createPartialMediaItemFromSong(song, context)
                mediaController?.addMediaItem(mediaItem)

                _uiState.update { currentState ->
                    currentState.copy(songsList = currentState.songsList + song.toPlaylistItem())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(tag, "Failed to play next song ${song.title}")
            }
        }
    }

    fun playNextList(songs: List<Song>) {
        val context = application.applicationContext
        viewModelScope.launch {
            try {
                val mediaItems = songs.map { song ->
                    createPartialMediaItemFromSong(song, context)
                }
                val currentMediaIndex = mediaController?.currentMediaItemIndex ?: 0
                val nextIndex = currentMediaIndex + 1
                Log.i(
                    tag,
                    "Current index: $currentMediaIndex, Insertion index: $nextIndex"
                )

                mediaController?.addMediaItems(nextIndex, mediaItems)
                _uiState.update { currentState ->
                    val currentList = currentState.songsList.toMutableList()

                    if (nextIndex <= currentList.size) {
                        currentList.addAll(nextIndex, songs.map { it.toPlaylistItem() })
                    } else {
                        currentList.addAll(nextIndex, songs.map { it.toPlaylistItem() })
                    }

                    currentState.copy(songsList = currentList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(tag, "Failed to play next song list")
            }
        }
    }

    fun enqueueSongList(songs: List<Song>) {
        val context = application.applicationContext
        viewModelScope.launch {
            try {
                val mediaItems = songs.map { song ->
                    createPartialMediaItemFromSong(song, context)
                }
                mediaController?.addMediaItems(mediaItems)

                _uiState.update { currentState ->
                    currentState.copy(songsList = currentState.songsList + songs.map { it.toPlaylistItem() })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCurrentPlayingIndex(): Int {
        return mediaController?.currentMediaItemIndex ?: 0
    }

    fun moveSong(fromIndex: Int, toIndex: Int) {
        Log.i(tag, "moveSong called with fromIndex: $fromIndex, toIndex: $toIndex")
        _uiState.update { currentState ->
            val newList = currentState.songsList.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            currentState.copy(songsList = newList)
        }
        viewModelScope.launch {
            mediaController?.moveMediaItem(fromIndex, toIndex)
        }
    }

    /**
     * Only adds Song, when it is not already in the db
     */
    fun maybeAddSongToDB(song: Song) {
        viewModelScope.launch {
            if (songDao.getSongById(song.id) != null) return@launch
            songDao.insertSong(song)
        }
    }


    /**
     * Pauses playback.
     */
    fun pause() {
        mediaController?.pause()
    }

    /**
     * Resumes playback.
     */
    fun resume() {
        mediaController?.play()
    }

    /**
     * Seeks to a specific position in the current song.
     * @param position The position to seek to in milliseconds.
     */
    fun seekTo(position: Long) {
        _uiState.update { it.copy(currentPosition = position) }
        mediaController?.seekTo(position)
    }

    /**
     * Skips to the next song in the queue.
     */
    fun skipToNext() {
        Log.i(tag, "skipToNext called. Has next: ${mediaController?.hasNextMediaItem()}")
        if (mediaController?.hasNextMediaItem() == true) {
            mediaController?.seekToNextMediaItem()
        } else {
            Log.w(tag, "No next media item to skip to.")
        }
    }

    /**
     * Skips to the previous song in the queue.
     */
    fun skipToPrevious() {
        Log.i(
            tag,
            "skipToPrevious called. Has previous: ${mediaController?.hasPreviousMediaItem()}"
        )
        if (mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekToPreviousMediaItem()
        } else {
            Log.w(tag, "No previous media item to skip to.")
        }
    }

    fun skipToSong(index: Int) {
        Log.i(tag, "skipToSong index: $index")
        Log.i(tag, "mediaControllerCount: ${mediaController?.mediaItemCount}")
        mediaController?.seekTo(index, 0L)
    }

    fun toggleFavoriteSong(song: Song) {
        viewModelScope.launch {
            val updated = song.toggleLike()
            songDao.updateSong(updated)
            uiState.value.currentSong = updated
        }
    }

    fun toggleFavoriteAlbum(album: Album, albumSongs: List<Song>) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = album.toggleBookmark()
            albumDao.updateAlbum(updated)

            // it was bookmarked before
            if (album.bookmarkedAt != null) {
                albumDao.getSongsForAlbum(album.id).forEach { song ->
                        albumDao.removeSongFromAlbum(album.id, song.id)
                    }
            } else {
                albumSongs.forEachIndexed { index, it ->
                    songDao.upsertSong(it)
                    albumDao.insertSongToAlbum(SongAlbumMap(
                        it.id,
                        album.id,
                        index
                    ))
                }
            }
        }
    }

    fun toggleFavoriteArtist(artist: Artist) {
        viewModelScope.launch {
            val updated = artist.toggleBookmark()
            artistDao.updateArtist(updated)
        }
    }

    fun setSortOption(sortOption: SortOption) {
        viewModelScope.launch {
            _uiState.update { it.copy(songsSortOption = sortOption) }
        }
    }

    fun setSearchParam(searchParam: SortOption) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchParam = searchParam) }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistDao.deletePlaylist(playlist)
        }
    }

    fun startSleepTimer(minutes: Long) {
        sleepTimerJob?.cancel() // Reset existing timer
        val endTime = System.currentTimeMillis() + (minutes * 60 * 1000)

        sleepTimerJob = viewModelScope.launch {
            while (System.currentTimeMillis() < endTime) {
                _uiState.update {
                    it.copy(
                        timeLeftMillis = endTime - System.currentTimeMillis()
                    )
                }
                delay(1000) // Update every second
            }
            _uiState.update {
                it.copy(
                    timeLeftMillis = 0L
                )
            }
            pause()
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        _uiState.update {
            it.copy(
                timeLeftMillis = 0L
            )
        }
    }

    suspend fun getLyrics(song: Song): List<Lyrics>? {
        return try {
            lyricsRepository.getLyrics(
                song.title,
                artist = song.artists.joinToString(", ") { it.name },
                duration = song.duration.toSeconds()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSyncedLyrics(song: Song): SyncedLyrics? {
        return try {
            LrcParser.parse(
                lyricsRepository.getLyrics(
                    song.title,
                    artist = song.artists.joinToString(", ") { it.name },
                    duration = song.duration.toSeconds()
                ).first().syncedLyrics ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setLoadingLyrics(loading: Boolean) {
        _uiState.update { it.copy(isLoadingLyrics = loading) }
    }

    fun setCurrentLyrics(syncedLyrics: SyncedLyrics) {
        _uiState.update { it.copy(currentLyrics = syncedLyrics) }
    }

    override fun onCleared() {
        super.onCleared()
        // Critical: Release the controller to prevent memory leaks
        mediaController?.removeListener(playerListener)
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}

fun Song.toPlaylistItem(): PlaylistItem {
    return PlaylistItem(song = this)
}