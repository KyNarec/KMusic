package com.kynarec.kmusic.ui.viewModels

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
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
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // When the song changes, find it in our list and update the state
            val currentSong = _uiState.value.songsList.find { it.id == mediaItem?.mediaId }
            val newTotalDuration = mediaController?.duration ?: 0L
            val newCurrentDuration = parseDurationToMillis(currentSong?.duration ?: "0")

            _uiState.value = _uiState.value.copy(
                currentSong = currentSong,
                totalDuration = if (newTotalDuration > 0) newTotalDuration else 0L,
            )
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            _uiState.value = _uiState.value.copy(showControlBar = !timeline.isEmpty)
        }
    }

    init {
        // 1. Load the songs from the database
        loadSongs()

        // 2. Initialize the connection to the MediaService
        val sessionToken = SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))
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
            _uiState.value = _uiState.value.copy(songsList = songs)
        }
    }

    private fun updatePosition() {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                val currentPosition = mediaController?.currentPosition ?: 0L
                val totalDuration = mediaController?.duration ?: 0L // Fetch total duration
//                if (_uiState.value.currentPosition != currentPosition) {
//                    _uiState.value = _uiState.value.copy(currentPosition = currentPosition, totalDuration = totalDuration)
//                }
                _uiState.value = _uiState.value.copy(currentPosition = currentPosition, totalDuration = totalDuration)

                delay(50) // Update position 10 times a second
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
        songList.indexOf(song)
        _uiState.update { it.copy(currentSong = song) }

        if (!songList.contains(song)) {
            _uiState.update { it.copy(songsList = songList + song) }
        }

        viewModelScope.launch {
            val mediaItem = withContext(Dispatchers.IO) {
                createMediaItemFromSong(song, context)
            }
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()

        }
    }

    fun playSongByIdWithRadio(song: Song) {
        Log.i(tag, "playSongByIdWithRadio called with songId: ${song.id}")

        // Play the selected song immediately
        playSong(song)

        viewModelScope.launch {
            try {
                getRadioFlow(song.id)
                    .flowOn(Dispatchers.IO) // network + parsing off main
                    .collect { radioSong ->
                        if (radioSong.id != song.id) {
                            val mediaItem = createMediaItemFromSong(radioSong, context)

                            // Update UI immediately for each song
                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    songsList = _uiState.value.songsList + radioSong
                                )
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
        mediaController?.seekToNextMediaItem()
    }

    /**
     * Skips to the previous song in the queue.
     */
    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
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