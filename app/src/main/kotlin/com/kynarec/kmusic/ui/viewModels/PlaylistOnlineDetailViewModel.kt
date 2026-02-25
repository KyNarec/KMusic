package com.kynarec.kmusic.ui.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.entities.PlaylistPreview
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.innertube.NetworkResult
import com.kynarec.kmusic.service.innertube.PlaylistWithSongsAndIndices
import com.kynarec.kmusic.service.innertube.getPlaylistAndSongs
import com.kynarec.kmusic.utils.SmartMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaylistOnlineDetailState(
    val playlistPreview: PlaylistPreview,
    val songs: List<Song> = emptyList(),
    val playlist: PlaylistWithSongsAndIndices? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val longClickSong: Song? = null,
    val showSongDetailBottomSheet: Boolean = false,
    val showPlaylistOptionsBottomSheet: Boolean = false
)

sealed interface PlaylistOnlineDetailActions{
    data object Refresh: PlaylistOnlineDetailActions
    data object Fetch: PlaylistOnlineDetailActions
    data class ShowSongDetailBottomSheet(val song: Song): PlaylistOnlineDetailActions
    data object HideSongDetailBottomSheet: PlaylistOnlineDetailActions
    data object TogglePlaylistOptionsBottomSheet: PlaylistOnlineDetailActions
}

class PlaylistOnlineDetailViewModel(
    private val playlistPreview: PlaylistPreview,
    private val application: Application,
) : ViewModel() {
    val tag = "PlaylistOnlineDetailViewModel"
    private val _state = MutableStateFlow(PlaylistOnlineDetailState(playlistPreview = playlistPreview))
    val state: StateFlow<PlaylistOnlineDetailState> = _state.asStateFlow()

    init {
        Log.i(tag, "PlaylistOnlineDetailViewModel initialized")
        onAction(PlaylistOnlineDetailActions.Fetch)
    }

    fun onAction(action: PlaylistOnlineDetailActions) {
        when (action) {
            PlaylistOnlineDetailActions.Fetch -> {
                val context = application.applicationContext
                viewModelScope.launch(Dispatchers.IO) {
                    when (val result = getPlaylistAndSongs(playlistPreview.id)) {
                        is NetworkResult.Failure.NetworkError -> {
                            SmartMessage("No Internet", PopupType.Error, false, context)
                        }

                        is NetworkResult.Failure.ParsingError -> {
                            SmartMessage("Parsing Error", PopupType.Error, false, context)
                        }

                        is NetworkResult.Failure.NotFound -> {
                            SmartMessage("List not found", PopupType.Error, false, context)
                        }

                        is NetworkResult.Success -> {
                            val playlistWithSongsAndIndices = result.data
                            withContext(Dispatchers.Main) {
                                _state.update { it.copy(
                                    playlist = playlistWithSongsAndIndices,
                                    songs = playlistWithSongsAndIndices.songs,
                                    isLoading = false
                                ) }
                            }
                        }
                    }
                }
            }
            PlaylistOnlineDetailActions.Refresh -> {
                val context = application.applicationContext
                _state.update { it.copy(isRefreshing = true) }
                viewModelScope.launch(Dispatchers.IO) {
                    when (val result = getPlaylistAndSongs(playlistPreview.id)) {
                        is NetworkResult.Failure.NetworkError -> {
                            SmartMessage("No Internet", PopupType.Error, false, context)
                        }

                        is NetworkResult.Failure.ParsingError -> {
                            SmartMessage("Parsing Error", PopupType.Error, false, context)
                        }

                        is NetworkResult.Failure.NotFound -> {
                            SmartMessage("List not found", PopupType.Error, false, context)
                        }

                        is NetworkResult.Success -> {
                            val playlistWithSongsAndIndices = result.data
                            withContext(Dispatchers.Main) {
                                _state.update {
                                    it.copy(
                                        playlist = playlistWithSongsAndIndices,
                                        songs = playlistWithSongsAndIndices.songs,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(isRefreshing = false) }
                    }
                }
            }

            is PlaylistOnlineDetailActions.ShowSongDetailBottomSheet -> {
                _state.update { it.copy(showSongDetailBottomSheet = true, longClickSong = action.song) }
            }

            PlaylistOnlineDetailActions.HideSongDetailBottomSheet -> {
                _state.update { it.copy(showSongDetailBottomSheet = false) }
            }

            PlaylistOnlineDetailActions.TogglePlaylistOptionsBottomSheet -> {
                _state.update { it.copy(showPlaylistOptionsBottomSheet = !it.showPlaylistOptionsBottomSheet) }
            }
        }
    }
}