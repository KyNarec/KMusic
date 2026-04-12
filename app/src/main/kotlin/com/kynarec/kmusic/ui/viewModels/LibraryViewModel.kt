package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.Artist
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.repository.LibraryRepository
import com.kynarec.kmusic.data.repository.PlayerRepository
import com.kynarec.kmusic.ui.screens.song.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val songsSortOption: SortOption = SortOption("All"),
    val searchParam: SortOption = SortOption("Song")
)

sealed interface LibraryAction {
    data class PlaySong(val song: Song, val withRadio: Boolean = true) : LibraryAction
    data class EnqueueSong(val song: Song) : LibraryAction
    data class PlayNext(val song: Song) : LibraryAction
    data class PlayPlaylist(val songs: List<Song>, val startSong: Song) : LibraryAction
    data class PlayShuffled(val songs: List<Song>) : LibraryAction
    data class PlayNextList(val songs: List<Song>) : LibraryAction
    data class EnqueueList(val songs: List<Song>) : LibraryAction

    data class MaybeAddSongToDB(val song: Song) : LibraryAction

    data class ToggleFavoriteSong(val song: Song) : LibraryAction
    data class ToggleFavoriteAlbum(val album: Album, val songs: List<Song>) : LibraryAction
    data class ToggleFavoriteArtist(val artist: Artist) : LibraryAction
    data class DeletePlaylist(val playlist: Playlist) : LibraryAction

    data class SetSortOption(val option: SortOption) : LibraryAction
    data class SetSearchParam(val param: SortOption) : LibraryAction
}

class LibraryViewModel(
    private val playerRepository: PlayerRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.PlaySong -> {
                if (action.withRadio) {
                    playerRepository.playSongByIdWithRadio(action.song)
                } else {
                    playerRepository.playSong(action.song)
                }
            }
            is LibraryAction.EnqueueSong -> playerRepository.enqueueSong(action.song)
            is LibraryAction.PlayNext -> playerRepository.playNext(action.song)
            is LibraryAction.PlayPlaylist -> playerRepository.playPlaylist(action.songs, action.startSong)
            is LibraryAction.PlayShuffled -> playerRepository.playShuffledPlaylist(action.songs)
            is LibraryAction.PlayNextList -> playerRepository.playNextList(action.songs)
            is LibraryAction.EnqueueList -> playerRepository.enqueueSongList(action.songs)
            
            is LibraryAction.MaybeAddSongToDB -> {
                viewModelScope.launch { libraryRepository.maybeAddSongToDB(action.song) }
            }
            
            is LibraryAction.ToggleFavoriteSong -> {
                viewModelScope.launch { libraryRepository.toggleFavoriteSong(action.song) }
            }
            is LibraryAction.ToggleFavoriteAlbum -> {
                viewModelScope.launch { libraryRepository.toggleFavoriteAlbum(action.album, action.songs) }
            }
            is LibraryAction.ToggleFavoriteArtist -> {
                viewModelScope.launch { libraryRepository.toggleFavoriteArtist(action.artist) }
            }
            is LibraryAction.DeletePlaylist -> {
                viewModelScope.launch { libraryRepository.deletePlaylist(action.playlist) }
            }
            
            is LibraryAction.SetSortOption -> _state.update { it.copy(songsSortOption = action.option) }
            is LibraryAction.SetSearchParam -> _state.update { it.copy(searchParam = action.param) }
        }
    }
}
