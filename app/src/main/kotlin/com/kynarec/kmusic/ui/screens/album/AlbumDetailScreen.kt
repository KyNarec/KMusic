package com.kynarec.kmusic.ui.screens.album

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Shuffle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.innertube.NetworkResult
import com.kynarec.kmusic.service.innertube.getAlbumAndSongs
import com.kynarec.kmusic.ui.components.album.AlbumOptionsBottomSheet
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.shimmerEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AlbumDetailScreen(
    modifier: Modifier = Modifier,
    albumId: String,
    viewModel: MusicViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    settingsViewModel: SettingsViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator()
    val isSinglePane =
        listDetailNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden

    val coloredDownloadIndicator = settingsViewModel.coloredDownloadIndicator

    var album by retain { mutableStateOf<Album?>(null) }
    var songs by retain { mutableStateOf(emptyList<Song>()) }

    var isLoading by retain { mutableStateOf(true) }
    var isRefreshing by retain { mutableStateOf(false) }

    var longClickSong by retain { mutableStateOf<Song?>(null) }
    val showAlbumOptionsBottomSheet = retain { mutableStateOf(false) }
    val showSongDetailBottomSheet = retain { mutableStateOf(false) }

    var readMore by retain { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()

    val allDownloaded = if (songs.isNotEmpty()) songs.all { it.id in completedIds } else false
    val isAnyDownloading = songs.any { it.id in downloadingSongs }

    val albumDownloadStatus by retain(allDownloaded, isAnyDownloading) {
        derivedStateOf {
            when {
                isAnyDownloading -> AlbumDownloadStatus.Downloading
                allDownloaded -> AlbumDownloadStatus.AllDownloaded
                else -> AlbumDownloadStatus.SomeOrNoneDownloaded
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            album = database.albumDao().getAlbumById(albumId)
            songs = database.albumDao().getSongsForAlbum(albumId)

            if (album == null || songs.isEmpty()) {
                when (val result = getAlbumAndSongs(albumId)) {
                    is NetworkResult.Success -> {
                        val albumWithSongs = result.data
                        songs = emptyList()
                        songs = songs + albumWithSongs.songs

                        album = albumWithSongs.album
                        isLoading = false
                        Log.i("AlbumDetailScreen", "album is now set")
                    }

                    is NetworkResult.Failure.NetworkError -> {
                        SmartMessage(
                            "No Internet",
                            PopupType.Error,
                            false,
                            context
                        )
                        Log.e("AlbumDetailScreen", "Failed to fetch: No Internet")
                    }

                    is NetworkResult.Failure.ParsingError -> {
                        SmartMessage(
                            "Parsing Error",
                            PopupType.Error,
                            false,
                            context
                        )
                        Log.e("AlbumDetailScreen", "Failed to fetch: YouTube JSON changed")
                    }

                    is NetworkResult.Failure.NotFound -> {
                        SmartMessage(
                            "Album not found",
                            PopupType.Error,
                            false,
                            context
                        )
                        Log.e("AlbumDetailScreen", "Album not found")
                    }
                }
            } else isLoading = false
            Log.i("AlbumDetailScreen", "$album")
        }
    }

    fun handleRefresh() {
        if (album != null) return
        scope.launch {
            isRefreshing = true
            songs = emptyList()
            when (val result = getAlbumAndSongs(albumId)) {
                is NetworkResult.Success -> {
                    val albumWithSongs = result.data
                    songs = emptyList()
                    songs = songs + albumWithSongs.songs

                    album = albumWithSongs.album
                    Log.i("AlbumDetailScreen", "album is now set")
                    isLoading = false
                    isRefreshing = false
                }

                is NetworkResult.Failure.NetworkError -> {
                    SmartMessage(
                        "No Internet",
                        PopupType.Error,
                        false,
                        context
                    )
                    Log.e("AlbumDetailScreen", "Failed to fetch: No Internet")
                    isRefreshing = false
                }

                is NetworkResult.Failure.ParsingError -> {
                    SmartMessage(
                        "Parsing Error",
                        PopupType.Error,
                        false,
                        context
                    )
                    Log.e("AlbumDetailScreen", "Failed to fetch: YouTube JSON changed")
                    isRefreshing = false
                }

                is NetworkResult.Failure.NotFound -> {
                    SmartMessage(
                        "Album not found",
                        PopupType.Error,
                        false,
                        context
                    )
                    Log.e("AlbumDetailScreen", "Album not found")
                    isRefreshing = false
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { handleRefresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        NavigableListDetailPaneScaffold(
            navigator = listDetailNavigator,
            listPane = {
                AnimatedPane {
                    Crossfade(
                        targetState = isLoading,
                        animationSpec = tween(durationMillis = 400),
                        label = "AlbumCrossfade"
                    ) { loading ->
                        if (loading) {
                            LazyColumn(
                                modifier = modifier.fillMaxSize()
                            ) {
                                albumHeaderSkeleton(isSinglePane)
                                if (isSinglePane) albumContentSkeleton(true)
                                if (showControlBar)
                                    item {
                                        Spacer(Modifier.height(75.dp))
                                    }
                            }
                        } else {
                            LazyColumn(
                                modifier = modifier.fillMaxSize()
                            ) {
                                albumHeader(
                                    album = album,
                                    songs = songs,
                                    readMore = readMore,
                                    onReadMoreToggle = { readMore = !readMore },
                                    isSinglePane
                                )

                                if (isSinglePane)
                                    albumContent(
                                        songs = songs,
                                        albumDownloadStatus = albumDownloadStatus,
                                        musicViewModel = viewModel,
                                        dataViewModel = dataViewModel,
                                        coloredDownloadIndicator = coloredDownloadIndicator,
                                        setLongClickSong = { longClickSong = it },
                                        showSongDetailBottomSheet = {
                                            showSongDetailBottomSheet.value = it
                                        },
                                        showAlbumOptionsBottomSheet = {
                                            showAlbumOptionsBottomSheet.value = it
                                        },
                                        isSinglePane = true
                                    )

                                if (showControlBar)
                                    item {
                                        Spacer(Modifier.height(70.dp))
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
                            targetState = isLoading,
                            animationSpec = tween(durationMillis = 400),
                            label = "OverviewCrossfade"
                        ) { loading ->
                            LazyColumn {
                                if (loading) {
                                    albumContentSkeleton()
                                    if (showControlBar)
                                        item {
                                            Spacer(Modifier.height(75.dp))
                                        }
                                } else {
                                    albumContent(
                                        songs = songs,
                                        albumDownloadStatus = albumDownloadStatus,
                                        musicViewModel = viewModel,
                                        dataViewModel = dataViewModel,
                                        coloredDownloadIndicator = coloredDownloadIndicator,
                                        setLongClickSong = { longClickSong = it },
                                        showSongDetailBottomSheet = {
                                            showSongDetailBottomSheet.value = it
                                        },
                                        showAlbumOptionsBottomSheet = {
                                            showAlbumOptionsBottomSheet.value = it
                                        },
                                    )
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

    if (showSongDetailBottomSheet.value && longClickSong != null) {
        Log.i("SongsScreen", "Showing bottom sheet")
        Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
        SongOptionsBottomSheet(
            song = longClickSong!!,
            onDismiss = { showSongDetailBottomSheet.value = false },
            viewModel = viewModel,
            database = database,
            navController = navController
        )
    }
    if (showAlbumOptionsBottomSheet.value) {
        AlbumOptionsBottomSheet(
            album = album,
            albumSongs = songs,
            onDismiss = { showAlbumOptionsBottomSheet.value = false },
            viewModel = viewModel,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.albumHeader(
    album: Album?,
    songs: List<Song>,
    readMore: Boolean,
    onReadMoreToggle: () -> Unit,
    isSinglePane: Boolean = false
) {
    item {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .then(
                    if (isSinglePane) Modifier
                    else Modifier.padding(top = 16.dp)
                )
        ) {
            AsyncImage(
                model = album?.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                imageLoader = LocalContext.current.imageLoader,
            )
        }
    }

    item {
        Column(
            Modifier.fillMaxWidth(),
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ConditionalMarqueeText(
                    text = album?.title ?: "NA",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ConditionalMarqueeText(
                    text = "${album?.year} - ${songs.size} Songs",
                )
            }
        }
    }

    if (album?.authorsText?.isNotEmpty() == true) {
        item {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = "“",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    modifier = Modifier
                        .offset(y = (-8).dp)
                        .align(Alignment.Top),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (!readMore) {
                        album.authorsText.take(
                            if (album.authorsText.length >= 100
                            ) 100 else album.authorsText.length
                        ).plus("...")
                    } else {
                        album.authorsText
                    },
                    style = TextStyle.Default.copy(textAlign = TextAlign.Justify),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .clickable { onReadMoreToggle() },
                )

                Text(
                    text = "„",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    modifier = Modifier
                        .offset(y = 4.dp)
                        .align(Alignment.Bottom),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = album?.copyright ?: "NA",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.albumHeaderSkeleton(
    isSinglePane: Boolean = false
) {
    item {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .then(
                    if (isSinglePane) Modifier
                    else Modifier.padding(top = 16.dp)
                )
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )
    }
    item {
        Column(
            Modifier.fillMaxWidth(),
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }
    }
    item {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text(
                text = "“",
                style = MaterialTheme.typography.titleLargeEmphasized,
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .align(Alignment.Top),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "",
                style = TextStyle.Default.copy(textAlign = TextAlign.Justify),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
                    .shimmerEffect(),
                minLines = 3
            )


            Text(
                text = "„",
                style = MaterialTheme.typography.titleLargeEmphasized,
                modifier = Modifier
                    .offset(y = 4.dp)
                    .align(Alignment.Bottom),
                fontWeight = FontWeight.Bold
            )
        }
    }
    item {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(),
                )
            }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.albumContent(
    songs: List<Song>,
    albumDownloadStatus: AlbumDownloadStatus,
    musicViewModel: MusicViewModel,
    dataViewModel: DataViewModel,
    coloredDownloadIndicator: Boolean,
    setLongClickSong: (Song) -> Unit,
    showSongDetailBottomSheet: (Boolean) -> Unit,
    showAlbumOptionsBottomSheet: (Boolean) -> Unit,
    isSinglePane: Boolean = false
) {
    item {
        Row(
            Modifier
                .fillMaxWidth()
                .then(
                    if (isSinglePane) Modifier.padding(8.dp)
                    else Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 0.dp)
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConditionalMarqueeText(
                text = "Songs",
                style = MaterialTheme.typography.titleLargeEmphasized.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )

            Spacer(Modifier.weight(1f))
            when(albumDownloadStatus) {
                AlbumDownloadStatus.Downloading -> {
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

                AlbumDownloadStatus.AllDownloaded -> {
                    val scope = rememberCoroutineScope()
                    IconButton(
                        onClick = {
                            scope.launch {
                                dataViewModel.removeDownloads(songs)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_download_done_24),
                            contentDescription = "Downloaded",
                            tint = if (coloredDownloadIndicator) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                }

                AlbumDownloadStatus.SomeOrNoneDownloaded -> {
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
                    musicViewModel.playShuffledPlaylist(songs)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle"
                )
            }

            IconButton(
                onClick = {
                    showAlbumOptionsBottomSheet(true)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
        }
    }

    items(songs) {
        val scope = rememberCoroutineScope()
        SongComponent(
            song = it,
            onClick = {
                scope.launch {
                    musicViewModel.playPlaylist(songs, it)
                }
            },
            onLongClick = {
                setLongClickSong(it)
                showSongDetailBottomSheet(true)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.albumContentSkeleton(
    isSinglePane: Boolean = false
) {
    item {
        Row(
            Modifier
                .fillMaxWidth()
                .then(
                    if (isSinglePane) Modifier.padding(8.dp)
                    else Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 0.dp)
                ),            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Songs",
                style = MaterialTheme.typography.titleLargeEmphasized.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )

            Spacer(Modifier.weight(1f))

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
    items(8) {
        SongComponentSkeleton()
    }
}
enum class AlbumDownloadStatus {
    Downloading,
    AllDownloaded,
    SomeOrNoneDownloaded
}