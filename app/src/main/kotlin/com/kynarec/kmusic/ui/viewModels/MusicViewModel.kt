package com.kynarec.kmusic.ui.viewModels

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.currentRecomposeScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.utils.createMediaItemFromSong
import com.kynarec.kmusic.utils.parseDurationToMillis
import com.kynarec.kmusic.service.innertube.getRadioFlow
import com.kynarec.kmusic.ui.screens.SortOption
import com.kynarec.kmusic.utils.createPartialMediaItemFromSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

// A single state class for the entire music screen
data class MusicUiState(
    val songsList: List<Song> = emptyList(),
    var currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val totalDuration: Long = 0,
    val showControlBar: Boolean = false,
    val songsSortOption: SortOption = SortOption("All")
)

// The new, combined ViewModel
@OptIn(UnstableApi::class)
class MusicViewModel
    (
    private val songDao: SongDao,
    private val context: Context
) : ViewModel() {
    private val tag = "MusicViewModel"

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture.isDone) mediaControllerFuture.get() else null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // When the song changes, find it in our list and update the state
            val currentSong = _uiState.value.songsList.find { it.id == mediaItem?.mediaId }
            val newTotalDuration = mediaController?.duration ?: 0L
            val index = _uiState.value.songsList.indexOf(currentSong)
            val songList = _uiState.value.songsList
            val controller = mediaController ?: return
            viewModelScope.launch {
                val songBefore = if (index - 1 != -2 && index -1 != -1) {
                    withContext(Dispatchers.IO) {
                        createMediaItemFromSong(songList[index - 1], context)
                    }
                } else {
                    null
                }
                val songAfter = if (index + 1 < songList.size) {
                    withContext(Dispatchers.IO) {
                        createMediaItemFromSong(songList[index + 1], context)
                    }
                } else {
                    null
                }
                if (songBefore != null)
                    controller.replaceMediaItem(index - 1, songBefore)
                if (songAfter != null)
                    controller.replaceMediaItem(index + 1, songAfter)
            }


            val newCurrentDuration = parseDurationToMillis(currentSong?.duration ?: "0")

            _uiState.update {
                it.copy(
                    currentSong = currentSong,
                    totalDuration = if (newTotalDuration > 0) newTotalDuration else 0L,
                )
            }
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            _uiState.update { it.copy(showControlBar = !timeline.isEmpty) }
        }
    }

    init {
        // 1. Load the songs from the database
//        loadSongs()

        // 2. Initialize the connection to the MediaService
        val sessionToken =
            SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            // This runs on a background executor, so it's safe to call get()
            val controller = mediaControllerFuture.get()
            controller.addListener(playerListener)

            // Start updating playback position on the main thread
            viewModelScope.launch { updatePosition() }
        }, Executors.newSingleThreadExecutor())

    }

    private fun loadSongs() {
        viewModelScope.launch {
            val songs = songDao.getSongsWithPlaytime() // Assuming this fetches your songs
            _uiState.update { it.copy(songsList = songs) }
        }
    }

    private fun updatePosition() {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                val currentPosition = mediaController?.currentPosition ?: 0L
                val totalDuration = mediaController?.duration ?: 0L // Fetch total duration

                _uiState.update {
                    if (it.currentPosition != currentPosition || it.totalDuration != totalDuration) {
                        it.copy(currentPosition = currentPosition, totalDuration = totalDuration)
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
        val index = songList.indexOf(song)


        viewModelScope.launch {
            val songBefore = if (index - 1 != -2) {
                withContext(Dispatchers.IO) {
                    createMediaItemFromSong(songList[index - 1], context)
                }
            } else {
                null
            }
            val songAfter = if (index + 1 < songList.size) {
                withContext(Dispatchers.IO) {
                    createMediaItemFromSong(songList[index + 1], context)
                }
            } else {
                null
            }

            val mediaItem = withContext(Dispatchers.IO) {
                createMediaItemFromSong(song, context)
            }
            if (songBefore != null)
                controller.replaceMediaItem(index - 1, songBefore)
            controller.setMediaItem(mediaItem)
            if (songAfter != null)
                controller.replaceMediaItem(index + 1, songAfter)
            controller.prepare()
            controller.play()

        }
    }


    /**
     * Plays a playlist starting from a specific song.
     * @param songs The list of songs in the playlist.
     * @param startSong The song to start playing from.
     */
    fun playPlaylist(songs: List<Song>, startSong: Song) {
        Log.i(tag, "playPlaylist called with ${songs.size} songs, starting at ${startSong.title}")

        val controller = mediaController ?: return

        // 1. Update UI State immediately
        _uiState.update {
            it.copy(
                songsList = songs,
                currentSong = startSong
            )
        }

        // 2. Prepare MediaItems and update Player asynchronously
        viewModelScope.launch {
            val startIndex = songs.indexOfFirst { it.id == startSong.id }.takeIf { it != -1 } ?: 0

            // Define the window of songs to load immediately (e.g., 5 before and 5 after)
            val windowSize = 1
            val windowStart = (startIndex - windowSize).coerceAtLeast(0)
            val windowEnd = (startIndex + windowSize).coerceAtMost(songs.size - 1)

            val initialSongs = songs.subList(windowStart, windowEnd + 1)
            val startSongIndexInWindow = startIndex - windowStart

            // Convert initial window to full MediaItems (heavy operation, but for few items)
            val initialMediaItems = withContext(Dispatchers.IO) {
                initialSongs.map { createMediaItemFromSong(it, context) }
            }

            // Update the player with the initial window
            controller.setMediaItems(initialMediaItems, startSongIndexInWindow, 0L)
            controller.prepare()
            controller.play()

            // 3. Load the rest of the songs in the background
            // Load "After" songs first (priority for playback continuity)
            if (windowEnd < songs.size - 1) {
                val afterSongs = songs.subList(windowEnd + 1, songs.size)
                loadChunks(afterSongs, append = true)
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
                loadChunks(beforeSongs, append = false)
            }
        }
    }

    private suspend fun loadChunks(songs: List<Song>, append: Boolean) {
        val chunkSize = 20
        val chunks = songs.chunked(chunkSize)

        val chunksToProcess = if (append) chunks else chunks.asReversed()

        withContext(Dispatchers.IO) {
            chunksToProcess.forEach { chunk ->
                val mediaItems = chunk.map {
                    createPartialMediaItemFromSong(
                        it,
                        context
                    )
                }

                withContext(Dispatchers.Main) {
                    if (append) {
                        mediaController?.addMediaItems(mediaItems)
                    } else {
                        mediaController?.addMediaItems(0, mediaItems)
                    }
                }
            }
        }
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

                            // Update UI immediately for each song
                            withContext(Dispatchers.Main) {
                                _uiState.update {
                                    it.copy(songsList = it.songsList + radioSong)
                                }
                                mediaController?.addMediaItem(mediaItem)
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(tag, "Failed to load radio songs for ${song.id}")
            }
        }
    }

    fun playNext(song: Song) {
        Log.i(tag, "playNext called with song ${song.title}")
        viewModelScope.launch {
            try {
                val mediaItem = createMediaItemFromSong(song, context)
                val currentMediaIndex = mediaController?.currentMediaItemIndex ?: 0
                val nextIndex = currentMediaIndex + 1
                Log.i(
                    "MusicViewModel",
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

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val updated = song.toggleLike()
            songDao.updateSong(updated)
        }
    }

    fun setSortOption(sortOption: SortOption) {
        viewModelScope.launch {
            _uiState.update { it.copy(songsSortOption = sortOption) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(mediaControllerFuture)
    }

    /**
     * Factory for creating the ViewModel with dependencies.
     */
    class Factory(
        private val songDao: SongDao,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(songDao, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}