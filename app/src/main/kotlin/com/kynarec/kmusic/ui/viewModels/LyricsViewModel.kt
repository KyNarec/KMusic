package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.klyrics.UiLyrics
import com.kynarec.kmusic.data.repository.LyricsDataState
import com.kynarec.kmusic.data.repository.LyricsRepository
import com.kynarec.kmusic.data.repository.PlayerRepository
import com.kynarec.kmusic.ui.screens.player.toUiLyrics
import com.kynarec.kmusic.utils.toSeconds
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LyricsUiState(
    val currentLyrics: SyncedLyrics? = null,
    val currentUiLyrics: UiLyrics? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)


class LyricsViewModel(
    private val lyricsRepository: LyricsRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {
    val uiState: StateFlow<LyricsUiState> = lyricsRepository.currentLyricsState
        .map { dataState ->
            when (dataState) {
                is LyricsDataState.Idle, is LyricsDataState.Loading -> LyricsUiState(isLoading = true)
                is LyricsDataState.Success -> LyricsUiState(
                    currentLyrics = dataState.lyrics,
                    isLoading = false,
                    currentUiLyrics = dataState.lyrics.toUiLyrics(playerRepository.playerState.value.currentDurationLong.toSeconds())
                )
                is LyricsDataState.Error -> LyricsUiState(error = dataState.message, isLoading = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LyricsUiState(isLoading = true)
        )
}