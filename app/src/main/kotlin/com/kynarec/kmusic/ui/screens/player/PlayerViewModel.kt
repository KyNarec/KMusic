package com.kynarec.kmusic.ui.screens.player

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun setPlayerState(newPlayerState: PlayerSheetMode) {
        _uiState.value = _uiState.value.copy(currentPlayerState = newPlayerState)
    }
}

data class PlayerUiState(
    var currentPlayerState: PlayerSheetMode = PlayerSheetMode.MainPlayer
)