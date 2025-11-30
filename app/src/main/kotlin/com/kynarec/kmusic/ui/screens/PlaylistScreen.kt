package com.kynarec.kmusic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SongBottomSheet
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun PlaylistScreen(
    modifier: Modifier = Modifier,
    playlistId: Long,
    viewModel: MusicViewModel,
) {
    Log.i("PlaylistScreen", "PlaylistScreen: $playlistId")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { KmusicDatabase.getDatabase(context) }

    val playlistFlow = remember(playlistId) {
        database.playlistDao().getPlaylistByIdFlow(playlistId)
    }
    val songsFlow = remember(playlistId) {
        database.playlistDao().getSongsForPlaylist(playlistId)
    }

    val playlist by playlistFlow.collectAsState(initial = null)
    val songs by songsFlow.collectAsState(initial = emptyList())

    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    Log.i(
        "PlaylistScreen",
        "State: Playlist: ${playlist?.name ?: "N/A"}, Songs: ${songs.size}"
    )
    Column(
        Modifier.fillMaxSize()
    ) {
        LazyColumn(
            Modifier.fillMaxWidth()
        ) {
            items(songs) { song ->
                Log.i("PlaylistScreen", "Song: ${song.title}")
                SongComponent(
                    song = song,
                    onClick = {
                        scope.launch {
                            viewModel.playPlaylist(songs, song)
                        }
                    },
                    onLongClick = {
                        longClickSong = song
                        showBottomSheet.value = true
                    }
                )
            }
        }
    }

    if (showBottomSheet.value && longClickSong != null) {
        Log.i("SongsScreen", "Showing bottom sheet")
        Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
        SongBottomSheet(
            songId = longClickSong!!.id,
            onDismiss = { showBottomSheet.value = false },
            viewModel = viewModel
        )
    }
}