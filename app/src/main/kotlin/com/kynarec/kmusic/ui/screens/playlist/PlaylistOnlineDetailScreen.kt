package com.kynarec.kmusic.ui.screens.playlist

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
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
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.innertube.NetworkResult
import com.kynarec.kmusic.service.innertube.PlaylistWithSongsAndIndices
import com.kynarec.kmusic.service.innertube.getPlaylistAndSongs
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.components.playlist.PlaylistOnlineOptionsBottomSheet
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistOnlineDetailScreen(
    modifier: Modifier = Modifier,
    playlistId: String,
    thumbnail: String,
    viewModel: MusicViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var songs by retain { mutableStateOf(emptyList<Song>()) }
    var playlist by retain { mutableStateOf<PlaylistWithSongsAndIndices?>(null) }

    var isLoading by retain { mutableStateOf(true) }
    var isRefreshing by retain { mutableStateOf(false) }

    var longClickSong by retain { mutableStateOf<Song?>(null) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    val showSongDetailBottomSheet = retain { mutableStateOf(false) }
    val showPlaylistOptionsBottomSheet = retain { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()

    val allDownloaded = if (songs.isNotEmpty()) songs.all { it.id in completedIds } else false
    val isAnyDownloading = songs.any { it.id in downloadingSongs }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            when (val result = getPlaylistAndSongs(playlistId)) {
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
                    playlist = playlistWithSongsAndIndices
                    playlistWithSongsAndIndices.songs.forEach {
                        songs = songs + it
                    }
                    isLoading = false
                }
            }
        }
    }

    fun handleRefresh() {
        isRefreshing = true
        scope.launch(Dispatchers.IO) {
            when (val result = getPlaylistAndSongs(playlistId)) {
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
                    playlist = playlistWithSongsAndIndices
                    songs = emptyList()
                    playlistWithSongsAndIndices.songs.forEach {
                        songs = songs + it
                    }
                    isLoading = false
                }
            }
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { handleRefresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Crossfade(
            targetState = isLoading,
            animationSpec = tween(durationMillis = 400),
            label = "PlaylistOnlineDetailScreenCrossfade"
        ) { loading ->
            if (loading)
                PlaylistOnlineDetailScreenSkeleton()
            else {
                Column(
                    Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        MarqueeBox(
                            text = playlist?.playlist?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            boxModifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                        )
                        when {
                            isAnyDownloading -> {
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

                            allDownloaded -> {
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

                            else -> {
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
        )
    }

    if (showPlaylistOptionsBottomSheet.value) {
        PlaylistOnlineOptionsBottomSheet(
//            playlistId = playlistId,
            thumbnail = thumbnail,
            playlist = playlist?.playlist ?: Playlist(
                name = "Cannot load playlist name"
            ),
            songs = songs,
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