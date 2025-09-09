package com.kynarec.kmusic.ui.viewModels

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kynarec.kmusic.service.PlayerServiceModern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Represents the state of the player UI
data class PlayerUiState(
    val title: String = "No Song Selected",
    val artist: String = "N/A",
    val albumArtUri: String? = null,
    val isPlaying: Boolean = false,
    val mediaController: MediaController? = null,
    val currentPosition: Long = 0,
    val totalDuration: Long = 0,
)

class PlayerViewModel(context: Context) : ViewModel() {
    private var mediaControllerFuture: ListenableFuture<MediaController>
    private val playerListener: Player.Listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _uiState.value = _uiState.value.copy(
                title = mediaItem?.mediaMetadata?.title?.toString() ?: "No Song Selected",
                artist = mediaItem?.mediaMetadata?.artist?.toString() ?: "N/A",
                albumArtUri = mediaItem?.mediaMetadata?.artworkUri?.toString()
            )
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            _showControlBar.value = !timeline.isEmpty
        }
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // State to control the visibility of the control bar
    private val _showControlBar = MutableStateFlow(false)
    val showControlBar: StateFlow<Boolean> = _showControlBar.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            _uiState.value = _uiState.value.copy(mediaController = mediaControllerFuture.get())
            _uiState.value.mediaController?.addListener(playerListener)
            updatePosition()
        }, { it.run() }) // Using a direct executor for immediate execution
    }

    private fun updatePosition() {
        // We're switching this to the main dispatcher. The MediaController API requires
        // all of its methods to be called from the main thread.
        viewModelScope.launch(Dispatchers.Main) {
            while(true) {
                // Ensure the media controller is not null before trying to get its properties.
                val mediaController = _uiState.value.mediaController
                if (mediaController != null) {
                    val duration = mediaController.duration
                    if (duration > 0) {
                        // This is the key fix: we only update the state if a valid duration exists.
                        _uiState.value = _uiState.value.copy(
                            currentPosition = mediaController.currentPosition,
                            totalDuration = duration
                        )
                    } else {
                        // If there is no valid duration yet, we explicitly set the duration to 0
                        // to prevent the UI from trying to divide by zero and throwing an error.
                        _uiState.value = _uiState.value.copy(
                            currentPosition = 0,
                            totalDuration = 0
                        )
                    }
                }
                // Update the current position much more frequently for a smooth UI.
                // A 16ms delay roughly corresponds to a 60fps refresh rate.
                delay(16)
            }
        }
    }

    fun seekTo(position: Long) {
        _uiState.value.mediaController?.seekTo(position)
    }

    fun play() {
        _uiState.value.mediaController?.play()
    }

    fun pause() {
        _uiState.value.mediaController?.pause()
    }

    fun skipToNext() {
        _uiState.value.mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        _uiState.value.mediaController?.seekToPreviousMediaItem()
    }

    // Called when the ViewModel is no longer used, to release resources.
    override fun onCleared() {
        super.onCleared()
        _uiState.value.mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(mediaControllerFuture)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlayerViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
