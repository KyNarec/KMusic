package com.kynarec.kmusic.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.enums.SortBy
import com.kynarec.kmusic.enums.SortOrder
import com.kynarec.kmusic.utils.Constants.DEFAULT_PLAYLIST_SORT_BY
import com.kynarec.kmusic.utils.Constants.DEFAULT_PLAYLIST_SORT_ORDER
import com.kynarec.kmusic.utils.Constants.PLAYLIST_SORT_BY_KEY
import com.kynarec.kmusic.utils.Constants.PLAYLIST_SORT_ORDER_KEY
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistOfflineDetailState(
    val playlistItems: List<PlaylistItem> = emptyList(),
    val sortedSongs: List<PlaylistItem> = emptyList(),
    val playlist: Playlist? = null,
    val isLoading: Boolean = true,
    val sortBy: SortBy = DEFAULT_PLAYLIST_SORT_BY,
    val sortOrder: SortOrder = DEFAULT_PLAYLIST_SORT_ORDER,
    val showSongDetailBottomSheet: Boolean = false,
    val showPlaylistOptionsBottomSheet: Boolean = false,
    val showPlaylistSortByBottomSheet: Boolean = false,
)

sealed interface PlaylistOfflineDetailActions{
    data object Sync: PlaylistOfflineDetailActions
    data object LockCLick: PlaylistOfflineDetailActions
    data object ToggleSongDetailBottomSheet: PlaylistOfflineDetailActions
    data object TogglePlaylistOptionsBottomSheet: PlaylistOfflineDetailActions
    data object TogglePlaylistSortByBottomSheet: PlaylistOfflineDetailActions
}
class PlaylistOfflineDetailViewModel(
    private val playlistId: Long,
    private val database: KmusicDatabase,
    private val ksafe: KSafe
) : ViewModel() {
    val tag = "PlaylistOfflineDetailViewModel"
    private val _state = MutableStateFlow(PlaylistOfflineDetailState())
    val state: StateFlow<PlaylistOfflineDetailState> = _state.asStateFlow()

    val playlistSortByFlow = ksafe.getFlow(PLAYLIST_SORT_BY_KEY, DEFAULT_PLAYLIST_SORT_BY)
    val playlistSortOrderFlow = ksafe.getFlow(PLAYLIST_SORT_ORDER_KEY, DEFAULT_PLAYLIST_SORT_ORDER)

    init {
        Log.i(tag, "init")
        observePlaylist(playlistId)
        observePlaylistData(playlistId)
        observeSort()
    }

    private fun observePlaylistData(playlistId: Long) {
        database.playlistDao().getSongsForPlaylist(playlistId)
            .distinctUntilChanged { old, new -> old.size == new.size }
            .map { songs -> songs.map { it.toPlaylistItem() } }
            .onEach { items ->
                _state.update { it.copy(
                    playlistItems = items,
                ) }
            }
            .launchIn(viewModelScope)
    }

    private fun observePlaylist(playlistId: Long) {
        database.playlistDao().getPlaylistByIdFlow(playlistId)
            .onEach { playlist ->
                _state.update { it.copy(
                    playlist = playlist,
                    isLoading = playlist == null
                ) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSort() {
        combine(
            flow = playlistSortByFlow,
            flow2 = playlistSortOrderFlow,
            flow3 = state.map { it.playlistItems }.distinctUntilChanged()
        ) { sortBy, sortOrder, currentItems ->
            Triple(sortBy, sortOrder, currentItems)
        }
            .onEach { (sortBy, sortOrder, currentItems) ->
                if (sortBy == SortBy.Position) {
                    _state.update { it.copy(
                        sortedSongs = if (sortOrder == SortOrder.Ascending) currentItems.reversed() else currentItems,
                        sortBy = sortBy,
                        sortOrder = sortOrder,
                    ) }
                } else {
                    _state.update {
                        it.copy(
                            sortedSongs = currentItems.sortedWith(
                                getComparator(
                                    sortBy,
                                    sortOrder
                                )
                            ),
                            sortBy = sortBy,
                            sortOrder = sortOrder,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: PlaylistOfflineDetailActions) {
        when (action) {
            PlaylistOfflineDetailActions.Sync -> {}
            PlaylistOfflineDetailActions.LockCLick -> {
                if (state.value.sortBy == SortBy.Position && state.value.sortOrder == SortOrder.Descending) {
                    viewModelScope.launch {
                        database.playlistDao().toggleIsEditable(playlistId)
                    }
                }
            }
            PlaylistOfflineDetailActions.ToggleSongDetailBottomSheet -> {
                _state.update { it.copy(showSongDetailBottomSheet = !it.showSongDetailBottomSheet) }
            }
            PlaylistOfflineDetailActions.TogglePlaylistOptionsBottomSheet -> {
                _state.update { it.copy(showPlaylistOptionsBottomSheet = !it.showPlaylistOptionsBottomSheet) }
            }
            PlaylistOfflineDetailActions.TogglePlaylistSortByBottomSheet -> {
                _state.update { it.copy(showPlaylistSortByBottomSheet = !it.showPlaylistSortByBottomSheet) }
            }
        }
    }

    fun moveSong(from: Int, to: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            database.playlistDao().moveSongInPlaylist(playlistId, from, to)
        }
    }

    private fun getComparator(sortBy: SortBy, sortOrder: SortOrder): Comparator<PlaylistItem> {
        val baseComparator = when (sortBy) {
            SortBy.Position -> compareBy<PlaylistItem> { 0 }

            SortBy.Title -> compareBy { it.song.title.lowercase() }

            SortBy.Artist -> compareBy {
                it.song.artists.firstOrNull()?.name?.lowercase() ?: ""
            }

            SortBy.Album -> compareBy { it.song.albumId }

            SortBy.Duration -> compareBy { it.song.duration }

            SortBy.DateFavorited -> compareBy { it.song.likedAt }
        }

        return if (sortOrder == SortOrder.Ascending) {
            baseComparator.reversed()
        } else {
            baseComparator
        }
    }

    fun putPlaylistSortBy(value: SortBy) {
        ksafe.putDirect(PLAYLIST_SORT_BY_KEY, value)
    }

    fun putPlaylistSortOrder(value: SortOrder) {
        ksafe.putDirect(PLAYLIST_SORT_ORDER_KEY, value)
    }

    fun toggleSortOrder() {
        val currentOrder = state.value.sortOrder
        val newOrder = if (currentOrder == SortOrder.Ascending) {
            SortOrder.Descending
        } else {
            SortOrder.Ascending
        }
        putPlaylistSortOrder(newOrder)
    }
}