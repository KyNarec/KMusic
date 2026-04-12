package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.repository.LibraryRepository
import com.kynarec.kmusic.data.repository.PlayerRepository
import com.kynarec.kmusic.data.repository.PlaylistItem
import com.kynarec.kmusic.enums.PlayerRepeatMode
import com.kynarec.kmusic.utils.parseMillisToDuration
import com.kynarec.kmusic.utils.toSeconds
import com.kynarec.lrclib.LyricsRepository
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.parser.EnhancedLrcParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerScreenState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val currentDurationLong: Long = 0L,
    val shuffleModeEnabled: Boolean = false,
    val repeatModeInt: Int = 0,
    val currentLyrics: SyncedLyrics? = null,
    val isLoadingLyrics: Boolean = false,
    val songsList: List<PlaylistItem> = emptyList(),
    val currentPlayingIndex: Int = 0
)

sealed interface PlayerScreenAction {
    data object TogglePlayPause : PlayerScreenAction
    data object SkipNext : PlayerScreenAction
    data object SkipPrevious : PlayerScreenAction
    data class SeekTo(val positionMs: Long) : PlayerScreenAction
    data class ToggleRepeatMode(val mode: PlayerRepeatMode) : PlayerScreenAction
    data class ToggleFavorite(val song: Song) : PlayerScreenAction
    data class FetchLyrics(val song: Song) : PlayerScreenAction
    data class MoveQueueItem(val from: Int, val to: Int) : PlayerScreenAction
    data class RemoveQueueItem(val songId: Long) : PlayerScreenAction
    data class SkipToQueueItem(val index: Int) : PlayerScreenAction
    data class PlayNextQueueItem(val song: Song) : PlayerScreenAction
}

class PlayerScreenViewModel(
    private val playerRepository: PlayerRepository,
    private val libraryRepository: LibraryRepository,
    private val lyricsRepository: LyricsRepository
) : ViewModel() {

    private val _lyricsState = MutableStateFlow<LyricsState>(LyricsState())

    val state: StateFlow<PlayerScreenState> = combine(
        playerRepository.playerState,
        _lyricsState
    ) { playerState, lyricsState ->
        PlayerScreenState(
            currentSong = playerState.currentSong,
            isPlaying = playerState.isPlaying,
            currentPosition = playerState.currentPosition,
            currentDurationLong = playerState.currentDurationLong,
            shuffleModeEnabled = playerState.shuffleModeEnabled,
            repeatModeInt = playerState.repeatMode,
            currentLyrics = lyricsState.syncedLyrics,
            isLoadingLyrics = lyricsState.isLoading,
            songsList = playerState.songsList,
            currentPlayingIndex = playerRepository.getCurrentPlayingIndex()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerScreenState()
    )

    fun onAction(action: PlayerScreenAction) {
        when (action) {
            PlayerScreenAction.TogglePlayPause -> {
                if (playerRepository.playerState.value.isPlaying) playerRepository.pause()
                else playerRepository.resume()
            }
            PlayerScreenAction.SkipNext -> playerRepository.skipToNext()
            PlayerScreenAction.SkipPrevious -> playerRepository.skipToPrevious()
            is PlayerScreenAction.SeekTo -> playerRepository.seekTo(action.positionMs)
            is PlayerScreenAction.ToggleRepeatMode -> playerRepository.changePlayerRepeatMode(action.mode)
            is PlayerScreenAction.ToggleFavorite -> {
                viewModelScope.launch {
                    libraryRepository.toggleFavoriteSong(action.song)
                }
            }
            is PlayerScreenAction.FetchLyrics -> fetchLyrics(action.song)
            is PlayerScreenAction.MoveQueueItem -> playerRepository.moveSong(action.from, action.to)
            is PlayerScreenAction.RemoveQueueItem -> playerRepository.removeSongFromQueue(action.songId)
            is PlayerScreenAction.SkipToQueueItem -> playerRepository.skipToSong(action.index)
            is PlayerScreenAction.PlayNextQueueItem -> playerRepository.playNext(action.song)
        }
    }

    private fun fetchLyrics(song: Song) {
        viewModelScope.launch {
            _lyricsState.update { it.copy(isLoading = true) }
            try {
                val duration = if (song.duration.isBlank()) {
                    playerRepository.playerState.value.currentDurationLong.parseMillisToDuration()
                } else {
                    song.duration
                }
                
                val parsed = EnhancedLrcParser.parse(
                    lyricsRepository.getLyrics(
                        song.title,
                        artist = song.artists.joinToString(", ") { it.name },
                        duration = duration.toSeconds()
                    ).first().syncedLyrics ?: ""
                )
                _lyricsState.update { it.copy(syncedLyrics = parsed, isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _lyricsState.update { it.copy(syncedLyrics = null, isLoading = false) }
            }
        }
    }
}

private data class LyricsState(
    val syncedLyrics: SyncedLyrics? = null,
    val isLoading: Boolean = false
)
