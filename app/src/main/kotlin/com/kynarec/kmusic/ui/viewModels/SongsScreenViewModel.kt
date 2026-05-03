package com.kynarec.kmusic.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.SortBy
import com.kynarec.kmusic.enums.SortOrder
import com.kynarec.kmusic.ui.screens.song.FilterOption
import com.kynarec.kmusic.utils.Constants.DEFAULT_SONGS_SORT_BY
import com.kynarec.kmusic.utils.Constants.DEFAULT_SONGS_SORT_ORDER
import com.kynarec.kmusic.utils.Constants.SONGS_SORT_BY_KEY
import com.kynarec.kmusic.utils.Constants.SONGS_SORT_ORDER_KEY
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

data class SongsScreenState(
    val songsFilterOption: FilterOption = FilterOption("All"),
    val songsSortOption: SortBy = DEFAULT_SONGS_SORT_BY,
    val songsSortOrder: SortOrder = DEFAULT_SONGS_SORT_ORDER,
    val filteredSongs: List<Song> = emptyList(),
    val sortedSongs: List<Song> = emptyList(),
    val allSongs: List<Song> = emptyList(),
    val showSongsSortByBottomSheet: Boolean = false
)

sealed interface SongsScreenAction {
    data object ToggleSongsSortByBottomSheet: SongsScreenAction
    data class ChangeSongsFilterOption(val newFilterOption: FilterOption): SongsScreenAction
    data object ToggleSortOrder: SongsScreenAction
    data class ChangeSongsSortOption(val newSortOption: SortBy): SongsScreenAction
}

class SongsScreenViewModel(
    private val kSafe: KSafe,
    private val database: KmusicDatabase,
    private val dataViewModel: DataViewModel
) : ViewModel() {
    private val tag = "SongsScreenViewModel"
    private val _state = MutableStateFlow(SongsScreenState())
    val state: StateFlow<SongsScreenState> = _state.asStateFlow()

    val songsSortByFlow = kSafe.getFlow(SONGS_SORT_BY_KEY, DEFAULT_SONGS_SORT_BY)
    val songsSortOrderFlow = kSafe.getFlow(SONGS_SORT_ORDER_KEY, DEFAULT_SONGS_SORT_ORDER)

    init {
        observeSongs()
        observeFilter()
        observeSort()
    }

    fun onAction(action: SongsScreenAction) {
        when (action) {
            is SongsScreenAction.ToggleSongsSortByBottomSheet -> {
                _state.update { it.copy(showSongsSortByBottomSheet = !it.showSongsSortByBottomSheet) }
            }

            is SongsScreenAction.ChangeSongsFilterOption -> {
                _state.update { it.copy(songsFilterOption = action.newFilterOption) }
            }
            is SongsScreenAction.ChangeSongsSortOption -> {
                putSongsSortBy(action.newSortOption)

            }
            SongsScreenAction.ToggleSortOrder -> {
                toggleSortOrder()
            }
        }

    }

    private fun observeFilter() {
        combine(
            flow = state.map { it.allSongs },
            flow2 = state.map { it.songsFilterOption }
        ) { allSongs, songsFilterOption ->
            Pair(allSongs, songsFilterOption)
        }.onEach { (allSongs, songsFilterOption) ->
            Log.i(tag, "observeFilter called with $songsFilterOption")
            _state.update {
                it.copy(
                    filteredSongs = when (songsFilterOption.text) {
                        "All" -> allSongs
                        "Favorites" -> allSongs.filter { song -> song.isLiked }
                        "Listened" -> allSongs.filter { song -> song.totalPlayTimeMs > 0 }
                        "Downloads" ->allSongs.filter { song -> song.id in dataViewModel.completedDownloadIds.value }
                        else -> allSongs
                    }
                )
            }
        }
            .launchIn(viewModelScope)
    }

    private fun observeSort() {
        combine(
            flow = songsSortByFlow,
            flow2 = songsSortOrderFlow,
            flow3 = state.map { it.filteredSongs }.distinctUntilChanged(),
            flow4 = state.map { it.songsFilterOption }
        ) { sortBy, sortOrder, filteredSongs, songsFilterOption  ->
            Quadrupel(sortBy, sortOrder, filteredSongs, songsFilterOption)
        } .onEach { (sortBy, sortOrder, filteredSongs, songsFilterOption) ->
            Log.i(tag, "observeSort called with $sortBy , $sortOrder" )
            val sortedSongs = when (sortBy) {
                SortBy.Position -> {
                    if (sortOrder == SortOrder.Ascending) {
                        filteredSongs.reversed()
                    } else {
                        filteredSongs
                    }
                }
                else -> {
                    filteredSongs.sortedWith(
                        getComparator(sortBy, sortOrder)
                    )
                }
            }
            _state.update {
                it.copy(
                    sortedSongs = sortedSongs,
                    songsSortOption = sortBy,
                    songsSortOrder = sortOrder,
                )
            }
        }
            .launchIn(viewModelScope)
    }

    private fun getComparator(sortBy: SortBy, sortOrder: SortOrder): Comparator<Song> {
        val baseComparator = when (sortBy) {
            SortBy.Position -> compareBy<Song> { 0 }

            SortBy.Title -> compareBy { it.title.lowercase() }

            SortBy.Artist -> compareBy {
                it.artists.firstOrNull()?.name?.lowercase() ?: ""
            }

            SortBy.Album -> compareBy { it.albumId }

            SortBy.Duration -> compareBy { it.duration }

            SortBy.DateFavorited -> compareBy { it.likedAt }
        }

        Log.i(tag, "baseComparator: $sortOrder")

        return if (sortOrder == SortOrder.Ascending) {
            baseComparator.reversed()
        } else {
            baseComparator
        }
    }

    private fun observeSongs() {
        database.songDao().getAllSongsFlow()
//            .distinctUntilChanged { old, new -> old.size == new.size }
            .onEach { songs ->
                _state.update { it.copy(
                    allSongs = songs,
                ) }
            }
            .launchIn(viewModelScope)
    }

    private fun putSongsSortBy(value: SortBy) {
        kSafe.putDirect(SONGS_SORT_BY_KEY, value)
    }

    private fun putSongsSortOrder(value: SortOrder) {
        kSafe.putDirect(SONGS_SORT_ORDER_KEY, value)
    }

    private fun toggleSortOrder() {
        val currentOrder = state.value.songsSortOrder
        val newOrder = if (currentOrder == SortOrder.Ascending) {
            SortOrder.Descending
        } else {
            SortOrder.Ascending
        }
        putSongsSortOrder(newOrder)
    }
}

@Serializable
public data class Quadrupel<out A, out B, out C, out D>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
) {

    public override fun toString(): String = "($first, $second, $third, $fourth)"
}


public fun <T> Quadrupel<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)