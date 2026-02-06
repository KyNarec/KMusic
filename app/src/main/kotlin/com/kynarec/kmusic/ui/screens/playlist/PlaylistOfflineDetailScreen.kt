package com.kynarec.kmusic.ui.screens.playlist

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.playlist.PlaylistOfflineOptionsBottomSheet
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun PlaylistOfflineDetailScreen(
    playlistId: Long,
    viewModel: MusicViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
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

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()

    val allDownloaded = if (songs.isNotEmpty()) songs.all { it.id in completedIds } else false
    val isAnyDownloading = songs.any { it.id in downloadingSongs }

    Column(
        Modifier.fillMaxSize()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                isAnyDownloading -> {
                    item {
                        IconButton(
                            onClick = {

                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_downloading_24),
                                contentDescription = "Downloaded"
                            )
                        }
                    }
                }

                allDownloaded -> {
                    item {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    dataViewModel.removeDownloads(songs)
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_download_done_24),
                                contentDescription = "Downloaded"
                            )
                        }
                    }
                }

                else -> {
                    item {
                        IconButton(
                            onClick = {
                                dataViewModel.addDownloads(songs)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_download_24),
                                contentDescription = "Download"
                            )
                        }
                    }
                }
            }

            item {
                IconButton(
                    onClick = {
                        viewModel.playShuffledPlaylist(songs)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle"
                    )
                }
            }
            item {
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
            song = longClickSong!!,
            onDismiss = { showSongDetailBottomSheet.value = false },
            viewModel = viewModel,
            database = database,
            navController = navController,
            isInPlaylistDetailScreen = true,
            playlistIdLong = playlistId
        )
    }

    if (showPlaylistOptionsBottomSheet.value) {
        PlaylistOfflineOptionsBottomSheet(
            playlistId = playlistId,
            onDismiss = {
                showPlaylistOptionsBottomSheet.value = false
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            viewModel = viewModel,
            database = database,
            navController = navController
        )
    }
}