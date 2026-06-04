package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.repository.PlayerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppState(
    val isPlaying: Boolean = false,
    val showControlBar: Boolean = false,
    val showPlayerSheet: Boolean = false,
    val currentSong: Song? = null,
    val timeLeftMillis: Long = 0L,
    val currentPosition: Long = 0L,
    val currentDurationLong: Long = 0L
)

sealed interface AppAction {
    data object TogglePlayPause : AppAction
    data object SkipNext : AppAction
    data object SkipPrevious : AppAction
    data class StartSleepTimer(val minutes: Long) : AppAction
    data object StopSleepTimer : AppAction
    data object OpenPlayerSheet: AppAction

    data object ClosePlayerSheet : AppAction
    data class OpenAlbumDetailScreen(val albumId: String) : AppAction
    data class OpenArtistDetailScreen(val artistId: String) : AppAction
    data class OpenSearchResultScreen(val query: String) : AppAction
    data class OpenPlaylistOnlineDetailScreen(val playlistId: String) : AppAction
}

sealed interface AppEvent {
    data class NavigateToAlbumDetailScreen(val albumId: String) : AppEvent
    data class NavigateToPlaylistOnlineDetailScreen(val playlistId: String) : AppEvent
    data class NavigateToArtistDetailScreen(val artistId: String) : AppEvent
    data class NavigateToSearchResultScreen(val query: String) : AppEvent
}


class AppViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = combine(
        playerRepository.playerState,
        _uiState
    ) { playerState, uiState ->
        AppState(
            isPlaying = playerState.isPlaying,
            showControlBar = playerState.songsList.isNotEmpty(),
            showPlayerSheet = uiState.showPlayerSheet,
            currentSong = playerState.currentSong,
            timeLeftMillis = playerState.timeLeftMillis,
            currentPosition = playerState.currentPosition,
            currentDurationLong = playerState.currentDurationLong
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppState()
    )

    private val _events = Channel<AppEvent>()
    val events = _events.receiveAsFlow()


    fun onAction(action: AppAction) {
        when (action) {
            AppAction.TogglePlayPause -> {
                if (playerRepository.playerState.value.isPlaying) playerRepository.pause()
                else playerRepository.resume()
            }
            AppAction.SkipNext -> playerRepository.skipToNext()
            AppAction.SkipPrevious -> playerRepository.skipToPrevious()
            is AppAction.StartSleepTimer -> playerRepository.startSleepTimer(action.minutes)
            AppAction.StopSleepTimer -> playerRepository.stopSleepTimer()
            AppAction.OpenPlayerSheet -> _uiState.update { it.copy(showPlayerSheet = true) }
            AppAction.ClosePlayerSheet -> _uiState.update { it.copy(showPlayerSheet = false) }
            is AppAction.OpenAlbumDetailScreen -> {
                viewModelScope.launch {
                    _events.send(AppEvent.NavigateToAlbumDetailScreen(action.albumId))
                }
            }
            is AppAction.OpenPlaylistOnlineDetailScreen -> {
                viewModelScope.launch {
                    _events.send(AppEvent.NavigateToPlaylistOnlineDetailScreen(action.playlistId))
                }
            }
            is AppAction.OpenArtistDetailScreen -> {
                viewModelScope.launch {
                    _events.send(AppEvent.NavigateToArtistDetailScreen(action.artistId))
                }
            }
            is AppAction.OpenSearchResultScreen -> {
                viewModelScope.launch {
                    _events.send(AppEvent.NavigateToSearchResultScreen(action.query))
                }
            }
        }
    }
}
