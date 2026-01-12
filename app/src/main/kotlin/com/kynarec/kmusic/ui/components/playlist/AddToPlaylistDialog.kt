package com.kynarec.kmusic.ui.components.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddToPlaylistDialog(
    modifier: Modifier = Modifier,
    song: Song,
    database: KmusicDatabase,
    navController: NavHostController,
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playlists: List<Playlist> by remember(database) {
        database.playlistDao().getAllPlaylists()
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading = playlists.isEmpty()

    val addToPlaylists = remember { mutableListOf<Playlist>() }


    AlertDialog(
        icon = {
//            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = "Add to Playlists")
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                    ),
                    columns = GridCells.Adaptive(minSize = 80.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        var showCheckmark by remember { mutableStateOf(false) }
                        PlaylistComponent(
                            playlist = playlist, navController, onRemove = {
                                scope.launch {
                                    database.playlistDao().deletePlaylist(it)
                                }
                            },
                            database = database,
                            onClick = {
                                if (showCheckmark) {
                                    showCheckmark = false
                                    addToPlaylists.remove(playlist)
                                } else {
                                    showCheckmark = true
                                    addToPlaylists.add(playlist)
                                }
                            }
                        )
                        if (showCheckmark) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.CheckCircleOutline,
                                    contentDescription = "Checked",
                                )
                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    addToPlaylists.forEach { playlist ->
                        scope.launch {
                            database.playlistDao().insertSongAtEndOfPlaylist(
                                songId = song.id,
                                playlistId = playlist.id
                            )
                        }
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}
