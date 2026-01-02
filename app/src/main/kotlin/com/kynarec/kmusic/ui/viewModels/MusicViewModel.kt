package com.kynarec.kmusic.ui.viewModels

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
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
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.service.innertube.getRadioFlow
import com.kynarec.kmusic.ui.screens.SortOption
import com.kynarec.kmusic.utils.createMediaItemFromSong
import com.kynarec.kmusic.utils.createPartialMediaItemFromSong
import com.kynarec.kmusic.utils.parseDurationToMillis
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// A single state class for the entire music screen
data class MusicUiState(
    val songsList: List<Song> = emptyList(),
    var currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val currentDurationLong: Long = 0,
    val currentDurationString: String = "0:00",
    val showControlBar: Boolean = false,
    val songsSortOption: SortOption = SortOption("All"),
    val searchParam: SortOption = SortOption("Song")
)

// The new, combined ViewModel
@OptIn(UnstableApi::class)
class MusicViewModel
    (
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val context: Context
) : ViewModel() {
    private val tag = "MusicViewModel"

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    private var mediaController: MediaController? = null

    private var playlistLoadJob: Job? = null
    private var currentLoadingPlaylistId: String? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.i(tag, "onMediaItemTransition called with reason: $reason")

            // When the song changes, find it in list and update the state
            val songsList = _uiState.value.songsList
            val currentSong = songsList.find { it.id == mediaItem?.mediaId }

            _uiState.update {
                it.copy(
                    currentSong = currentSong,
                    currentDurationLong = _uiState.value.currentSong?.duration?.parseDurationToMillis()
                        ?: 0L,
                    currentDurationString = _uiState.value.currentSong?.duration ?: "0:00"
                )
            }

            val index = songsList.indexOf(currentSong)
            val controller = mediaController ?: return

            viewModelScope.launch {
                // Handle Previous Song
                if (index > 0) {
                    val songBefore = withContext(Dispatchers.IO) {
                        createMediaItemFromSong(songsList[index - 1], context)
                    }
                    controller.replaceMediaItem(index - 1, songBefore)
                }

                // Handle Next Song
                if (index < songsList.size - 1) {
                    val songAfter = withContext(Dispatchers.IO) {
                        createMediaItemFromSong(songsList[index + 1], context)
                    }
                    controller.replaceMediaItem(index + 1, songAfter)
                }
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            _uiState.update { it.copy(showControlBar = !timeline.isEmpty) }
        }
    }

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))

        // Store the future so we can release it later
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture = future

        future.addListener({
            try {
                val controller = future.get()
                this.mediaController = controller

                controller.addListener(playerListener)

                // Sync initial state
                _uiState.update { it.copy(isPlaying = controller.isPlaying) }
                updatePosition()

            } catch (e: Exception) {
                Log.e(tag, "Failed to connect to MediaController", e)
            }
        }, ContextCompat.getMainExecutor(context)) // Runs on Main Thread safely
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

                delay(200) // Update position 5 times a second (sufficient for UI)
            }
        }
    }

    /**
     * Plays a specific song.
     * @param song The song to play.
     */
    fun playSong(song: Song) {
        Log.i(tag, "playSong called")

        val controller = mediaController ?: return
        val songList = _uiState.value.songsList

        // Find the index of the tapped song

        _uiState.update { it.copy(currentSong = song) }

        if (!songList.contains(song)) {
            _uiState.update { it.copy(songsList = songList + song) }
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

        playlistLoadJob?.cancel()
        currentLoadingPlaylistId = playlistId

        val controller = mediaController ?: return
        val startIndex = songs.indexOfFirst { it.id == startSong.id }.coerceAtLeast(0)

        // 1. Update UI State immediately
        _uiState.update {
            it.copy(
                songsList = songs,
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
                                it.copy(songsList = it.songsList + radioSong)
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
                    createMediaItemFromSong(_uiState.value.songsList[1], context)
                withContext(Dispatchers.Main) {
                    mediaController?.replaceMediaItem(1, nextMediaItem)
                }
            }
        }
        Log.i(tag, "playSongByIdWithRadio done")
    }

    fun playNext(song: Song) {
        Log.i(tag, "playNext called with song ${song.title}")
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
                    // Create a mutable copy of the current list
                    val updatedList = currentState.songsList.toMutableList()

                    // Insert the new song at the determined index
                    if (nextIndex <= updatedList.size) {
                        updatedList.add(nextIndex, song)
                    } else {
                        // Handle case where index is out of bounds (should append)
                        updatedList.add(song)
                    }

                    // Return the updated state
                    currentState.copy(songsList = updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(tag, "Failed to play next song ${song.title}")
            }
        }
    }

    fun enqueueSong(song: Song) {
        Log.i(tag, "enqueue called with song ${song.title}")
        viewModelScope.launch {
            try {
                val mediaItem = createMediaItemFromSong(song, context)
                mediaController?.addMediaItem(mediaItem)

                _uiState.update { currentState ->
                    currentState.copy(songsList = currentState.songsList + song)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(tag, "Failed to play next song ${song.title}")
            }
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

    fun skipToSong(song: Song) {
        Log.i(tag, "skipToSong called with song ${song.title}")
        val index = _uiState.value.songsList.indexOf(song)
        Log.i(tag, "skipToSong index: $index")
        Log.i(tag, "mediaControllerCount: ${mediaController?.mediaItemCount}")
        mediaController?.seekTo(index, 0L)
    }

    fun toggleFavoriteSong(song: Song) {
        viewModelScope.launch {
            val updated = song.toggleLike()
            songDao.updateSong(updated)
        }
    }

    fun toggleFavoriteAlbum(album: Album) {
        viewModelScope.launch {
            val updated = album.toggleBookmark()
            albumDao.updateAlbum(updated)
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

    override fun onCleared() {
        super.onCleared()
        // Critical: Release the controller to prevent memory leaks
        mediaController?.removeListener(playerListener)
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    /**
     * Factory for creating the ViewModel with dependencies.
     */
    class Factory(
        private val songDao: SongDao,
        private val playlistDao: PlaylistDao,
        private val albumDao: AlbumDao,
        private val artistDao: ArtistDao,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(songDao, playlistDao, albumDao, artistDao, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}