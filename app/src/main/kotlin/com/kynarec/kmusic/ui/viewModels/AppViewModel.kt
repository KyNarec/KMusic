package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.repository.PlayerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AppState(
    val isPlaying: Boolean = false,
    val showControlBar: Boolean = false,
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
}

class AppViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    val state: StateFlow<AppState> = combine(
        playerRepository.playerState,
        kotlinx.coroutines.flow.flowOf(Unit) // Just to make it combine if we add more
    ) { playerState, _ ->
        AppState(
            isPlaying = playerState.isPlaying,
            showControlBar = playerState.songsList.isNotEmpty(),
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
        }
    }
}
