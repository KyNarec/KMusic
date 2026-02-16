package com.kynarec.kmusic.ui.screens.playlist

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongArtist
import com.kynarec.kmusic.ui.components.TopBarComponentPreview
import com.kynarec.kmusic.ui.components.playlist.PlaylistOfflineOptionsBottomSheet
import com.kynarec.kmusic.ui.components.playlist.TwoByTwoImageGrid
import com.kynarec.kmusic.ui.components.song.PreviewSongComponent
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.theme.KMusicTheme
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.formatDuration
import com.kynarec.kmusic.utils.shimmerEffect
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
fun PlaylistOfflineDetailScreen(
    playlistId: Long,
    viewModel: MusicViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    settingsViewModel: SettingsViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val coloredDownloadIndicator = settingsViewModel.coloredDownloadIndicator

    val playlistFlow = retain(playlistId) {
        database.playlistDao().getPlaylistByIdFlow(playlistId)
    }
    val songsFlow = retain(playlistId) {
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

    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator()
    val isSinglePane =
        listDetailNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden

    NavigableListDetailPaneScaffold(
        navigator = listDetailNavigator,
        listPane = {
            AnimatedPane {
                Crossfade(
                    targetState = playlist == null,
                    animationSpec = tween(durationMillis = 400),
                    label = "PlaylistCrossfade"
                ) { loading ->
                    if (loading) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            playlistHeaderSkeleton(isSinglePane)
                            if (isSinglePane) {
                                playlistControlRow(
                                    allDownloaded = allDownloaded,
                                    isAnyDownloading = isAnyDownloading,
                                    onAllDownloadedClick = {  },
                                    onNoneDownloadedClick = {  },
                                    coloredDownloadIndicator = coloredDownloadIndicator,
                                    isEditable = false,
                                    onLockClick = {  },
                                    onShuffleClick = {  },
                                    onMoreClick = {  }
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
                            Modifier.fillMaxSize()
                        ) {
                            playlistHeader(songs, playlist!!, isSinglePane)
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
                                    isEditable = playlist!!.isEditable,
                                    onLockClick = { scope.launch { database.playlistDao().toggleIsEditable(playlistId) } },
                                    onShuffleClick = { viewModel.playShuffledPlaylist(songs) },
                                    onMoreClick = { showPlaylistOptionsBottomSheet.value = true }
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
        },
        detailPane = {
            if (!isSinglePane) {
                AnimatedPane {
                    Crossfade(
                        targetState = playlist == null,
                        animationSpec = tween(durationMillis = 400),
                        label = "PlaylistCrossfade"
                    ) { loading ->
                        LazyColumn {
                            if (loading) {
                                playlistControlRow(
                                    allDownloaded = allDownloaded,
                                    isAnyDownloading = isAnyDownloading,
                                    onAllDownloadedClick = {  },
                                    onNoneDownloadedClick = {  },
                                    coloredDownloadIndicator = coloredDownloadIndicator,
                                    isEditable = false,
                                    onLockClick = {  },
                                    onShuffleClick = {  },
                                    onMoreClick = {  }
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
                                    isEditable = playlist!!.isEditable,
                                    onLockClick = { scope.launch { database.playlistDao().toggleIsEditable(playlistId) } },
                                    onShuffleClick = { viewModel.playShuffledPlaylist(songs) },
                                    onMoreClick = { showPlaylistOptionsBottomSheet.value = true }
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
        }
    )

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

fun LazyListScope.playlistHeader(
    songs: List<Song>,
    playlist: Playlist,
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
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp),
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
                    playlist.name,
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
                        songs.formatDuration()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.playlistHeaderSkeleton(
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
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.fillMaxWidth(0.5f)
                        .padding(bottom = 2.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerEffect()
                ) {}

                Text(
                    "",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    modifier = Modifier.fillMaxWidth(0.25f)
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
                        modifier = Modifier.fillMaxWidth(0.15f)
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
                        modifier = Modifier.fillMaxWidth(0.25f)
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
    isEditable: Boolean,
    onLockClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {}
            ) {
                Icon(
                    Icons.Rounded.ArrowDownward,
                    contentDescription = "sort"
                )
            }

            Box(Modifier.clickable(onClick = {})) {
                Text(
                    "Position",
                )
            }
            Spacer(Modifier.weight(1f))

            IconButton(onClick = {
                onLockClick()
            }) {
                Icon(
                    if (isEditable) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                    contentDescription = "lock playlist"
                )
            }
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
                onClick = { onMoreClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
        }
    }
}


@Preview(device = "id:pixel_9_pro")
@Composable
fun PODS() {
    val songList = listOf(
        Song(
            id = "",
            title = "505",
            artists = listOf(SongArtist("", "Arctic Monkeys")),
            duration = "4:14",
            thumbnail = "https://lh3.googleusercontent.com/F_Qgb74OCFJGWwkQpua2ITH4Z27HABNXVNxLGtTksu_BKUlQOLnftS2-NQ3nemam3AhSIBN35AfP_Qu4",
            ),
        Song(
            id = "",
            title = "Chamber of Reflection",
            artists = listOf(SongArtist("", "Marc DeMarco")),
            duration = "3:52",
            thumbnail = "https://lh3.googleusercontent.com/kD050a9mq1WH-QF6KkZPxqG-FEOdp_W425-zUgrHsyTP8kLg4QRmzgnzcHve6fCY-Vz7_xdGXmxdvICQ",
        ),
        Song(
            id = "",
            title = "For the First Time",
            artists = listOf(SongArtist("", "Marc DeMarco")),
            duration = "3:03",
            thumbnail = "https://lh3.googleusercontent.com/EyoVvyz6OU37qG5TixB5089qX3AwzkoLBWbTeo-PExE-XDBktc74QPw4psN6_g0dft1S05ZAidx_8nRI",
        ),
        Song(
            id = "",
            title = "Chinese New Year",
            artists = listOf(SongArtist("", "SALES")),
            duration = "2:40",
            thumbnail = "https://lh3.googleusercontent.com/sWQHTWpQ8Fqq271HmnJB9uLb4s4Z8BEYdDgt-4lG-OLvW4wkDwzEOgW5IYf_aLD5XLDrIxgS_rEmiDjF",
        ),
        Song(
            id = "BoKPa1wXAAQ",
            title = "Something About You",
            artists = listOf(SongArtist(id = "", name = "Eyedress")),
            duration = "2:34",
            thumbnail = "https://lh3.googleusercontent.com/xAdZS1Koqa5XQ3E3ezIwF29F22ultucLWbuUSTifftVJngasukV4t3gQfBhTGPWM7EuMLbbceh8gXYIv8Q"
        ),
        Song(
            id = "cZCm_i6YvAk",
            title = "For the First Time",
            artists = listOf(SongArtist(id = "", name = "Mac DeMarco")),
            duration = "3:03",
            thumbnail = "https://lh3.googleusercontent.com/EyoVvyz6OU37qG5TixB5089qX3AwzkoLBWbTeo-PExE-XDBktc74QPw4psN6_g0dft1S05ZAidx_8nRI"
        ),
        Song(
            id = "oAur1xQx-sc",
            title = "Young",
            artists = listOf(SongArtist(id = "", name = "Vacations")),
            duration = "3:10",
            thumbnail = "https://lh3.googleusercontent.com/lFea1VIpCOoxKfRaXWy2YBB6BwKYLngSsfFLA0yHVuq3qCYVfJ9HQhjDTivuYJYW5RW7BJMLvL4UkB7_Cg"
        ),
        Song(
            id = "FJX0JPXD2nM",
            title = "we fell in love in october",
            artists = listOf(SongArtist(id = "", name = "girl in red")),
            duration = "3:05",
            thumbnail = "https://lh3.googleusercontent.com/GmIRWAk7lT4AZWdUVgVV_eM3Tk-MZ8fR8tjadmg_lOJwxgtqY6YrhOUlHWLr3xntUHnWgJ7XOoYYzSBC5w"
        ),
        Song(
            id = "JtWaCViY_tc",
            title = "Rape Me",
            artists = listOf(SongArtist(id = "", name = "Nirvana")),
            duration = "2:50",
            thumbnail = "https://lh3.googleusercontent.com/AU54rAq1sj8xzHtArON5sp8gE7eUc_4c2I1suZz85nkTCpBYIxOYil6vH5PL4Ue49MtHWqbRWefjC2mW"
        ),
    )
    KMusicTheme(
        darkTheme = true,
        dynamicColor = false
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopBarComponentPreview()
            }
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.background
                    )
                    .padding(contentPadding)
            ) {
                item {
                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.fillMaxWidth(0.5f)
                            ) {
                                TwoByTwoImageGrid(
                                    listOf(
                                        "https://lh3.googleusercontent.com/F_Qgb74OCFJGWwkQpua2ITH4Z27HABNXVNxLGtTksu_BKUlQOLnftS2-NQ3nemam3AhSIBN35AfP_Qu4",
                                        "https://lh3.googleusercontent.com/kD050a9mq1WH-QF6KkZPxqG-FEOdp_W425-zUgrHsyTP8kLg4QRmzgnzcHve6fCY-Vz7_xdGXmxdvICQ",
                                        "https://lh3.googleusercontent.com/EyoVvyz6OU37qG5TixB5089qX3AwzkoLBWbTeo-PExE-XDBktc74QPw4psN6_g0dft1S05ZAidx_8nRI",
                                        "https://lh3.googleusercontent.com/sWQHTWpQ8Fqq271HmnJB9uLb4s4Z8BEYdDgt-4lG-OLvW4wkDwzEOgW5IYf_aLD5XLDrIxgS_rEmiDjF"
                                    )
                                )
                            }

                            Text(
                                "Playlist Title",
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
                                    "25 Songs",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.fillMaxWidth(0.2f))

                                Icon(
                                    Icons.Rounded.AccessTime,
                                    contentDescription = null
                                )

                                Text(
                                    "2h 13m"
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                Icons.Rounded.ArrowDownward,
                                contentDescription = "sort"
                            )
                        }

                        Box(Modifier.clickable(onClick = {})) {
                            Text(
                                "Position",
                            )
                        }
                        Spacer(Modifier.weight(1f))

                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.LockOpen,
                                contentDescription = "lock playlist"
                            )
                        }
                        IconButton(
                            onClick = {
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_download_24),
                                contentDescription = "Download"
                            )
                        }
                        IconButton(
                            onClick = {
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle"
                            )
                        }
                        IconButton(
                            onClick = {
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options"
                            )
                        }
                    }
                }
                items(songList) { song ->
                    PreviewSongComponent(song)
                }
            }
        }
    }
}