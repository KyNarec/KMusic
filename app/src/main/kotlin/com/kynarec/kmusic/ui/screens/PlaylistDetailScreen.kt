package com.kynarec.kmusic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.PlaylistOptionsBottomSheet
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.components.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun PlaylistDetailScreen(
    modifier: Modifier = Modifier,
    playlistId: Long,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {
    Log.i("PlaylistScreen", "PlaylistScreen: $playlistId")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val playlistFlow = remember(playlistId) {
        database.playlistDao().getPlaylistByIdFlow(playlistId)
    }
    val songsFlow = remember(playlistId) {
        database.playlistDao().getSongsForPlaylist(playlistId)
    }

    val playlist by playlistFlow.collectAsStateWithLifecycle(null)
    val songs by songsFlow.collectAsStateWithLifecycle(emptyList())

    val showSongDetailBottomSheet = remember { mutableStateOf(false) }
    val showPlaylistOptionsBottomSheet = remember { mutableStateOf(false) }

    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar


    Log.i(
        "PlaylistScreen",
        "State: Playlist: ${playlist?.name ?: "N/A"}, Songs: ${songs.size}"
    )
    Column(
        Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    showPlaylistOptionsBottomSheet.value = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
        }
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
                        showSongDetailBottomSheet.value = true
                    }
                )
            }
            if (showControlBar)
                item {
                    Spacer(Modifier.height(70.dp))
                }
        }
    }

    if (showSongDetailBottomSheet.value && longClickSong != null) {
        Log.i("SongsScreen", "Showing bottom sheet")
        Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
        SongOptionsBottomSheet(
            songId = longClickSong!!.id,
            onDismiss = { showSongDetailBottomSheet.value = false },
            viewModel = viewModel,
            database = database
        )
    }

    if (showPlaylistOptionsBottomSheet.value) {
        PlaylistOptionsBottomSheet(
            playlistId = playlistId,
            onDismiss = {
                showPlaylistOptionsBottomSheet.value = false
            },
            viewModel = viewModel,
            database = database,
            navController = navController
        )
    }
}