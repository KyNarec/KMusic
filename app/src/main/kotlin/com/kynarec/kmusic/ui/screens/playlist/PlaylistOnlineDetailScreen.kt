package com.kynarec.kmusic.ui.screens.playlist

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kynarec.kmusic.service.innertube.PlaylistWithSongsAndIndices
import com.kynarec.kmusic.ui.components.playlist.PlaylistOnlineOptionsBottomSheet
import com.kynarec.kmusic.ui.components.playlist.TwoByTwoImageGrid
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.PlaylistOnlineDetailActions
import com.kynarec.kmusic.ui.viewModels.PlaylistOnlineDetailViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.formatDuration
import com.kynarec.kmusic.utils.shimmerEffect
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PlaylistOnlineDetailScreen(
    viewModel: MusicViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    settingsViewModel: SettingsViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    playlistOnlineDetailViewModel: PlaylistOnlineDetailViewModel,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val state by playlistOnlineDetailViewModel.state.collectAsStateWithLifecycle()
    val songs = state.songs
    val playlist = state.playlist
    val isRefreshing = state.isRefreshing

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val coloredDownloadIndicator = settingsViewModel.coloredDownloadIndicator
    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()
    val allDownloaded = if (songs.isNotEmpty()) songs.all { it.id in completedIds } else false
    val isAnyDownloading = songs.any { it.id in downloadingSongs }

    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator()
    val isSinglePane =
        listDetailNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { playlistOnlineDetailViewModel.onAction(PlaylistOnlineDetailActions.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        NavigableListDetailPaneScaffold(
            navigator = listDetailNavigator,
            listPane = {
                AnimatedPane {
                    Crossfade(
                        targetState = state.isLoading,
                        animationSpec = tween(durationMillis = 400),
                        label = "PlaylistOnlineCrossfade"
                    ) { loading ->
                        if (loading) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                playlistHeaderSkeletonOnline(isSinglePane)
                                if (isSinglePane) {
                                    playlistControlRow(
                                        allDownloaded = allDownloaded,
                                        isAnyDownloading = isAnyDownloading,
                                        onAllDownloadedClick = { },
                                        onNoneDownloadedClick = { },
                                        coloredDownloadIndicator = coloredDownloadIndicator,
                                        playlistOfflineDetailAction = { },
                                        onShuffleClick = { },
                                    )
                                    items(15) {
                                        SongComponentSkeleton()
                                    }
                                    if (showControlBar)
                                        item {
                                            Spacer(Modifier.height(70.dp))
                                        }
                                }

                            }
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                            ) {
                                playlistHeader(songs, playlist, isSinglePane)
                                if (isSinglePane) {
                                    playlistControlRow(
                                        allDownloaded = allDownloaded,
                                        isAnyDownloading = isAnyDownloading,
                                        onAllDownloadedClick = {
                                            scope.launch {
                                                dataViewModel.removeDownloads(songs)
                                            }
                                        },
                                        onNoneDownloadedClick = { dataViewModel.addDownloads(songs) },
                                        coloredDownloadIndicator = coloredDownloadIndicator,
                                        playlistOfflineDetailAction = playlistOnlineDetailViewModel::onAction,
                                        onShuffleClick = { viewModel.playShuffledPlaylist(songs) },
                                    )
                                    items(songs) { song ->
                                        SongComponent(
                                            song = song,
                                            onClick = {
                                                scope.launch {
                                                    viewModel.playPlaylist(songs, song)
                                                }
                                            },
                                            onLongClick = {
                                                playlistOnlineDetailViewModel.onAction(PlaylistOnlineDetailActions.ShowSongDetailBottomSheet(song))
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
            },
            detailPane = {
                if (!isSinglePane) {
                    AnimatedPane {
                        Crossfade(
                            targetState = state.isLoading,
                            animationSpec = tween(durationMillis = 400),
                            label = "PlaylistOnlineCrossfade"
                        ) { loading ->
                            LazyColumn(
                                Modifier.fillMaxSize(),
                            ) {
                                if (loading) {
                                    playlistControlRow(
                                        allDownloaded = allDownloaded,
                                        isAnyDownloading = isAnyDownloading,
                                        onAllDownloadedClick = { },
                                        onNoneDownloadedClick = { },
                                        coloredDownloadIndicator = coloredDownloadIndicator,
                                        playlistOfflineDetailAction = { },
                                        onShuffleClick = { },
                                    )
                                    items(15) {
                                        SongComponentSkeleton()
                                    }
                                    if (showControlBar)
                                        item {
                                            Spacer(Modifier.height(70.dp))
                                        }
                                } else {
                                    playlistControlRow(
                                        allDownloaded = allDownloaded,
                                        isAnyDownloading = isAnyDownloading,
                                        onAllDownloadedClick = {
                                            scope.launch {
                                                dataViewModel.removeDownloads(songs)
                                            }
                                        },
                                        onNoneDownloadedClick = { dataViewModel.addDownloads(songs) },
                                        coloredDownloadIndicator = coloredDownloadIndicator,
                                        playlistOfflineDetailAction = playlistOnlineDetailViewModel::onAction,
                                        onShuffleClick = { viewModel.playShuffledPlaylist(songs) },
                                    )
                                    items(songs) { song ->
                                        SongComponent(
                                            song = song,
                                            onClick = {
                                                scope.launch {
                                                    viewModel.playPlaylist(songs, song)
                                                }
                                            },
                                            onLongClick = {
                                                playlistOnlineDetailViewModel.onAction(PlaylistOnlineDetailActions.ShowSongDetailBottomSheet(song))
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
            }
        )
    }

    if (state.showSongDetailBottomSheet) {
        Log.i("SongsScreen", "Showing bottom sheet")
        Log.i("SongsScreen", "Title = ${state.longClickSong!!.title}")
        SongOptionsBottomSheet(
            song = state.longClickSong!!,
            onDismiss = { playlistOnlineDetailViewModel.onAction(PlaylistOnlineDetailActions.HideSongDetailBottomSheet) },
            viewModel = viewModel,
            database = database,
            navController = navController,
            isInPlaylistDetailScreen = true,
        )
    }

    if (state.showPlaylistOptionsBottomSheet) {
        PlaylistOnlineOptionsBottomSheet(
//            playlistId = playlistId,
            thumbnail = state.playlistPreview.thumbnail,
            playlist = playlist?.playlist ?: Playlist(
                name = "Cannot load playlist name"
            ),
            songs = songs,
            onDismiss = {
                playlistOnlineDetailViewModel.onAction(PlaylistOnlineDetailActions.TogglePlaylistOptionsBottomSheet)
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            viewModel = viewModel,
            database = database,
            navController = navController
        )
    }
}
fun LazyListScope.playlistHeader(
    songs: List<Song>,
    playlist: PlaylistWithSongsAndIndices?,
    isSinglePane: Boolean = false
) {
    item {
        ElevatedCard(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .then(
                    if (isSinglePane) Modifier
                    else Modifier.padding(top = 16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.fillMaxWidth(0.5f)
                ) {
                    TwoByTwoImageGrid(
                        songs.take(4).map { it.thumbnail }
                    )
                }

                Text(
                    playlist?.playlist?.name?: "",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null
                    )

                    Text(
                        "${songs.size} Songs",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.fillMaxWidth(0.2f))

                    Icon(
                        Icons.Rounded.AccessTime,
                        contentDescription = null
                    )

                    Text(
                        songs.formatDuration(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.playlistHeaderSkeletonOnline(
    isSinglePane: Boolean = false
) {
    item {
        ElevatedCard(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .then(
                    if (isSinglePane) Modifier
                    else Modifier.padding(top = 16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .padding(bottom = 2.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerEffect()
                ) {}

                Text(
                    "",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null
                    )

                    Text(
                        "",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth(0.15f)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )

                    Spacer(Modifier.fillMaxWidth(0.2f))

                    Icon(
                        Icons.Rounded.AccessTime,
                        contentDescription = null
                    )

                    Text(
                        "",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .padding(start = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}
fun LazyListScope.playlistControlRow(
    allDownloaded: Boolean,
    isAnyDownloading: Boolean,
    onAllDownloadedClick: () -> Unit,
    onNoneDownloadedClick: () -> Unit,
    coloredDownloadIndicator: Boolean,
    playlistOfflineDetailAction: (PlaylistOnlineDetailActions) -> Unit,
    onShuffleClick: () -> Unit,
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                            onAllDownloadedClick()
                            //scope.launch {
                            //    dataViewModel.removeDownloads(songs)
                            //}
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_download_done_24),
                            contentDescription = "Downloaded",
                            tint = if (coloredDownloadIndicator) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                }

                else -> {
                    IconButton(
                        onClick = {
                            onNoneDownloadedClick()
                            //dataViewModel.addDownloads(songs)
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
                onClick = { onShuffleClick() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle"
                )
            }
            IconButton(
                onClick = { playlistOfflineDetailAction(PlaylistOnlineDetailActions.TogglePlaylistOptionsBottomSheet) }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
        }
    }
}