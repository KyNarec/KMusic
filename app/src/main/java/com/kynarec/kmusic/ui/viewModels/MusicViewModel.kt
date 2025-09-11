package com.kynarec.kmusic.ui.viewModels

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import androidx.core.net.toUri
import com.kynarec.kmusic.utils.parseDurationToMillis

// A single state class for the entire music screen
data class MusicUiState(
    val songsList: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val totalDuration: Long = 0,
    val showControlBar: Boolean = false,
    val currentDuration: Long = 0
)

// The new, combined ViewModel
@OptIn(UnstableApi::class)
class MusicViewModel
    (
    private val songDao: SongDao,
    context: Context
) : ViewModel() {

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
            _uiState.value = _uiState.value.copy(
                currentSong = currentSong,
                totalDuration = mediaController?.duration ?: 0L,
                currentDuration = parseDurationToMillis(currentSong?.duration ?: "0")
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
            mediaController?.addListener(playerListener)
            // Start the coroutine that updates the playback position
            updatePosition()
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
                if (_uiState.value.currentPosition != currentPosition) {
                    _uiState.value = _uiState.value.copy(currentPosition = currentPosition)
                }
                delay(100) // Update position 10 times a second
            }
        }
    }

    // This is the key function that connects the song list to the player
    fun play(tappedSong: Song) {
        val controller = mediaController ?: return
        val songList = _uiState.value.songsList

        // Find the index of the tapped song
        val startIndex = songList.indexOf(tappedSong)
        if (startIndex == -1) return // Song not found

        // Create a playlist of MediaItems from our song list
        val mediaItems = songList.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(song.thumbnail) // Assuming thumbnail URL is the song URL
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(song.thumbnail.toUri())
                        .build()
                )
                .build()
        }

        // Set the full playlist on the controller, starting at the tapped song
        controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
        controller.prepare()
        controller.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(mediaControllerFuture)
    }

    // A Factory is needed to pass arguments (songDao, context) to the ViewModel
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