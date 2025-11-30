package com.kynarec.kmusic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@Composable
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
                    }
                )
            }
        }
    }
}