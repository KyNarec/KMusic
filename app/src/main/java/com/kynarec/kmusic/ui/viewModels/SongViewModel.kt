package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
@Deprecated("Use new MusicViewModel")
class SongViewModel(private val songDao: SongDao) : ViewModel() {

    private val _songsList = MutableStateFlow<List<Song>>(emptyList())
    val songsList: StateFlow<List<Song>> = _songsList

    init {
        // We start the data fetching when the ViewModel is first created.
        loadSongs()
    }

    private fun loadSongs() {
        // The viewModelScope is a coroutine scope tied to the ViewModel's lifecycle,
        // which prevents memory leaks.
        viewModelScope.launch {
            // Your original data fetching and mapping logic
            val songs = songDao.getSongsWithPlaytime()
            val mappedSongs = songs.map { s ->
                Song(
                    id = s.id,
                    title = s.title,
                    artist = s.artist,
                    thumbnail = s.thumbnail,
                    duration = s.duration
                )
            }
            // We update the StateFlow, which will automatically trigger a recomposition
            // in any composable observing this state.
            _songsList.value = mappedSongs
        }
    }
}