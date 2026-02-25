package com.kynarec.kmusic.ui.screens.playlist

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.SortBy
import com.kynarec.kmusic.enums.SortOrder
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.components.playlist.PlaylistOfflineOptionsBottomSheet
import com.kynarec.kmusic.ui.components.playlist.PlaylistSortByBottomSheet
import com.kynarec.kmusic.ui.components.playlist.TwoByTwoImageGrid
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.toPlaylistItem
import com.kynarec.kmusic.utils.Constants.DEFAULT_PLAYLIST_SORT_BY
import com.kynarec.kmusic.utils.Constants.DEFAULT_PLAYLIST_SORT_ORDER
import com.kynarec.kmusic.utils.formatDuration
import com.kynarec.kmusic.utils.shimmerEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
            .distinctUntilChanged { old, new ->
                old.size == new.size
            }
    }

    val playlist by playlistFlow.collectAsStateWithLifecycle(null)
    val rawSongs by songsFlow.collectAsStateWithLifecycle(emptyList())

    val sortBy by settingsViewModel.playlistSortByFlow.collectAsStateWithLifecycle(
        DEFAULT_PLAYLIST_SORT_BY
    )
    val sortOrder by settingsViewModel.playlistSortOrderFlow.collectAsStateWithLifecycle(
        DEFAULT_PLAYLIST_SORT_ORDER
    )

    val showSongDetailBottomSheet = remember { mutableStateOf(false) }
    val showPlaylistOptionsBottomSheet = remember { mutableStateOf(false) }
    val showPlaylistSortByBottomSheet = remember { mutableStateOf(false) }

    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()

    val allDownloaded = if (rawSongs.isNotEmpty()) rawSongs.all { it.id in completedIds } else false
    val isAnyDownloading = rawSongs.any { it.id in downloadingSongs }

    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator()
    val isSinglePane =
        listDetailNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden

    val rawPlaylistItems by retain(rawSongs) {
        derivedStateOf {
            rawSongs.map { it.toPlaylistItem() }
        }
    }

    val sortedSongs by retain(rawPlaylistItems, sortBy, sortOrder) {
        derivedStateOf {
            val sorted = when (sortBy) {
                SortBy.Position -> rawPlaylistItems
                SortBy.Title -> rawPlaylistItems.sortedBy { it.song.title.lowercase() }
                SortBy.Artist -> rawPlaylistItems.sortedBy {
                    it.song.artists.firstOrNull()?.name?.lowercase() ?: ""
                }

                SortBy.Album -> rawPlaylistItems.sortedBy { it.song.albumId }
                SortBy.Duration -> rawPlaylistItems.sortedBy { it.song.duration }
                SortBy.DateFavorited -> rawPlaylistItems.sortedBy { it.song.likedAt }
            }

            if (sortOrder == SortOrder.Ascending) sorted.reversed() else sorted
        }
    }

    val hapticFeedback = LocalHapticFeedback.current
    var initialDraggingIndex by remember { mutableStateOf<Int?>(null) }

    val lazyListState = rememberLazyListState()
    var localSongList by retain(sortedSongs) { mutableStateOf(sortedSongs) }
    LaunchedEffect(sortedSongs) { localSongList = sortedSongs }
    var currentTargetIndex by remember { mutableStateOf<Int?>(null) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        Log.i("PlaylistOfflineDetailScreen", "Reordering songs from $initialDraggingIndex")
        if (initialDraggingIndex == null) {
            initialDraggingIndex = from.index - 2
        }
        currentTargetIndex = to.index - 2

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)

        localSongList = localSongList.toMutableList().apply {
            add(to.index - 2, removeAt(from.index - 2))
        }
    }

    val isDragging = reorderableLazyListState.isAnyItemDragging

    LaunchedEffect(isDragging) {
        if (!isDragging && localSongList != sortedSongs) {
            val start = initialDraggingIndex
            val end = currentTargetIndex
            if (start != null && end != null && start != end) {
                Log.i("PlaylistOfflineDetailScreen", "Moving from $start to $end")
                scope.launch(Dispatchers.IO) {
                    database.playlistDao().moveSongInPlaylist(playlistId, start, end)
                }
            }
            currentTargetIndex = null
            initialDraggingIndex = null
        }
    }

    fun handleIsEditable(playlistId: Long, sortBy: SortBy, sortOrder: SortOrder) {
        if (sortBy == SortBy.Position && sortOrder == SortOrder.Descending) {
            scope.launch {
                database.playlistDao().toggleIsEditable(playlistId)
            }
        }
    }

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
                                    sortBy = sortBy,
                                    onSortByClick = { showPlaylistSortByBottomSheet.value = true },
                                    sortOrder = sortOrder,
                                    onSortOrderClick = { toggleSortOrder(settingsViewModel, it) },
                                    allDownloaded = allDownloaded,
                                    isAnyDownloading = isAnyDownloading,
                                    onAllDownloadedClick = { },
                                    onNoneDownloadedClick = { },
                                    coloredDownloadIndicator = coloredDownloadIndicator,
                                    isEditable = false,
                                    onLockClick = { },
                                    onShuffleClick = { },
                                    onMoreClick = { }
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
                            state = lazyListState
                        ) {
                            playlistHeader(localSongList.map { it.song }, playlist!!, isSinglePane)
                            if (isSinglePane) {
                                playlistControlRow(
                                    sortBy = sortBy,
                                    onSortByClick = { showPlaylistSortByBottomSheet.value = true },
                                    sortOrder = sortOrder,
                                    onSortOrderClick = { toggleSortOrder(settingsViewModel, it) },
                                    allDownloaded = allDownloaded,
                                    isAnyDownloading = isAnyDownloading,
                                    onAllDownloadedClick = {
                                        scope.launch {
                                            dataViewModel.removeDownloads(sortedSongs.map { it.song })
                                        }
                                    },
                                    onNoneDownloadedClick = { dataViewModel.addDownloads(sortedSongs.map { it.song }) },
                                    coloredDownloadIndicator = coloredDownloadIndicator,
                                    isEditable = sortOrder == SortOrder.Descending && sortBy == SortBy.Position && playlist!!.isEditable,
                                    onLockClick = {
                                        handleIsEditable(playlistId, sortBy, sortOrder)
                                    },
                                    onShuffleClick = { viewModel.playShuffledPlaylist(sortedSongs.map { it.song }) },
                                    onMoreClick = { showPlaylistOptionsBottomSheet.value = true }
                                )
                                itemsIndexed(
                                    localSongList,
                                    key = { _, song -> song.id }) { index, playlistItem ->
                                    ReorderableItem(
                                        reorderableLazyListState,
                                        key = playlistItem.id,
                                    ) { isDragging ->
                                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                                        Box(
                                            modifier = Modifier
                                                .shadow(elevation)
                                                .zIndex(if (isDragging) 1f else 0f)
                                        ) {
                                            DraggablePlaylistComponent(
                                                draggingEnabled = sortOrder == SortOrder.Descending && sortBy == SortBy.Position && playlist!!.isEditable,
                                                song = playlistItem.song,
                                                onClick = {
                                                    scope.launch {
                                                        viewModel.playPlaylist(
                                                            sortedSongs.map { it.song },
                                                            playlistItem.song
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    longClickSong = playlistItem.song
                                                    showSongDetailBottomSheet.value = true
                                                },
                                                reorderableCollectionItemScope = this@ReorderableItem,
                                                onDragStarted = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                },
                                                onDragStopped = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                },
                                                dataViewModel = dataViewModel,
                                                settingsViewModel = settingsViewModel
                                            )
                                        }
                                    }
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
                        LazyColumn(
                            state = lazyListState
                        ) {
                            if (loading) {
                                playlistControlRow(
                                    sortBy = sortBy,
                                    onSortByClick = { showPlaylistSortByBottomSheet.value = true },
                                    sortOrder = sortOrder,
                                    onSortOrderClick = { toggleSortOrder(settingsViewModel, it) },
                                    allDownloaded = allDownloaded,
                                    isAnyDownloading = isAnyDownloading,
                                    onAllDownloadedClick = { },
                                    onNoneDownloadedClick = { },
                                    coloredDownloadIndicator = coloredDownloadIndicator,
                                    isEditable = false,
                                    onLockClick = { },
                                    onShuffleClick = { },
                                    onMoreClick = { }
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
                                    sortBy = sortBy,
                                    onSortByClick = { showPlaylistSortByBottomSheet.value = true },
                                    sortOrder = sortOrder,
                                    onSortOrderClick = { toggleSortOrder(settingsViewModel, it) },
                                    allDownloaded = allDownloaded,
                                    isAnyDownloading = isAnyDownloading,
                                    onAllDownloadedClick = {
                                        scope.launch {
                                            dataViewModel.removeDownloads(sortedSongs.map { it.song })
                                        }
                                    },
                                    onNoneDownloadedClick = { dataViewModel.addDownloads(sortedSongs.map { it.song }) },
                                    coloredDownloadIndicator = coloredDownloadIndicator,
                                    isEditable = sortOrder == SortOrder.Descending && sortBy == SortBy.Position && playlist!!.isEditable,
                                    onLockClick = {
                                        handleIsEditable(playlistId, sortBy, sortOrder)
                                    },
                                    onShuffleClick = { viewModel.playShuffledPlaylist(sortedSongs.map { it.song }) },
                                    onMoreClick = { showPlaylistOptionsBottomSheet.value = true }
                                )
                                itemsIndexed(
                                    localSongList,
                                    key = { _, song -> song.id }) { index, playlistItem ->
                                    ReorderableItem(
                                        reorderableLazyListState,
                                        key = playlistItem.id,
                                    ) { isDragging ->
                                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                                        Box(
                                            modifier = Modifier
                                                .shadow(elevation)
                                                .zIndex(if (isDragging) 1f else 0f)
                                        ) {
                                            DraggablePlaylistComponent(
                                                draggingEnabled = sortOrder == SortOrder.Descending && sortBy == SortBy.Position && playlist!!.isEditable,
                                                song = playlistItem.song,
                                                onClick = {
                                                    scope.launch {
                                                        viewModel.playPlaylist(
                                                            sortedSongs.map { it.song },
                                                            playlistItem.song
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    longClickSong = playlistItem.song
                                                    showSongDetailBottomSheet.value = true
                                                },
                                                reorderableCollectionItemScope = this@ReorderableItem,
                                                onDragStarted = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                },
                                                onDragStopped = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                },
                                                dataViewModel = dataViewModel,
                                                settingsViewModel = settingsViewModel
                                            )
                                        }
                                    }
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

    if (showPlaylistSortByBottomSheet.value) {
        PlaylistSortByBottomSheet(
            onClick = { settingsViewModel.putPlaylistSortBy(it) },
            onDismiss = { showPlaylistSortByBottomSheet.value = false }
        )
    }
}

fun toggleSortOrder(
    settingsViewModel: SettingsViewModel,
    currentSortOrder: SortOrder
) {
    println("toggleSortOrder")

    when (currentSortOrder) {
        SortOrder.Ascending -> settingsViewModel.putPlaylistSortOrder(SortOrder.Descending)
        SortOrder.Descending -> settingsViewModel.putPlaylistSortOrder(SortOrder.Ascending)
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
                        songs.formatDuration(),
                        style = MaterialTheme.typography.titleMedium
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
    sortBy: SortBy,
    onSortByClick: () -> Unit,
    sortOrder: SortOrder,
    onSortOrderClick: (SortOrder) -> Unit,
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
            val rotationAngle by animateFloatAsState(
                targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
                label = "SortOrderRotation",
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            )

            IconButton(
                onClick = { onSortOrderClick(sortOrder) }
            ) {
                Icon(
                    Icons.Rounded.ArrowUpward,
                    contentDescription = "sort",
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotationAngle
                    }
                )
            }

            Box(Modifier.clickable(onClick = { onSortByClick() })) {
                Text(
                    sortBy.title,
                )
            }
            Spacer(Modifier.weight(1f))

            IconButton(onClick = {
                onLockClick()
            }) {

                Icon(
                    if (isEditable) painterResource(R.drawable.round_lock_open_24) else painterResource(
                        R.drawable.round_lock_outline_24
                    ),
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DraggablePlaylistComponent(
    song: Song,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isPlaying: Boolean = false,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
    draggingEnabled: Boolean = true,
    dataViewModel: DataViewModel,
    settingsViewModel: SettingsViewModel
) {
    val title = song.title
    val artist = song.artists.joinToString(", ") { it.name }
    val duration = song.duration
    val imageUrl = song.thumbnail

    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()
    val isDownloading = downloadingSongs.containsKey(song.id)
    val isDownloaded = completedIds.contains(song.id)
    val coloredDownloadIndicator = settingsViewModel.coloredDownloadIndicator

    Box(
        modifier = with(reorderableCollectionItemScope) {
            Modifier
                .fillMaxWidth()
        }
    ) {
        Row(
            modifier = with(reorderableCollectionItemScope) {
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        hapticFeedbackEnabled = true
                    )
                    .padding(vertical = 8.dp)
                    .background(Color.Transparent)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = imageUrl,
                contentDescription = "Album art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(62.dp)
                    .padding(start = 16.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                imageLoader = LocalContext.current.imageLoader

            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                MarqueeBox(
                    text = title,
                    fontSize = 24.sp,
                    maxLines = 1
                )

                MarqueeBox(
                    text = artist,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp, start = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isDownloading -> {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(ButtonDefaults.LargeIconSize),

                                shape = IconButtonDefaults.smallSquareShape
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.rounded_downloading_24),
                                    contentDescription = "Downloading",
                                )
                            }
                        }

                        isDownloaded -> {
                            IconButton(
                                onClick = { dataViewModel.removeDownload(song) },
                                modifier = Modifier.size(ButtonDefaults.LargeIconSize),

                                shape = IconButtonDefaults.smallSquareShape
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
                                onClick = { dataViewModel.addDownload(song) },
                                modifier = Modifier.size(ButtonDefaults.LargeIconSize),
                                shape = IconButtonDefaults.smallSquareShape
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.rounded_download_24),
                                    contentDescription = "Download"
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = duration,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )
            }
        }
        if (draggingEnabled) {
            Box(
                modifier = with(reorderableCollectionItemScope) {
                    Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(2f)
                        .padding(top = 6.dp)
                        .size(width = 40.dp, height = 12.dp)
                        .background(Color.Transparent)
                        .draggableHandle(
                            onDragStarted = {
                                onDragStarted()
                            },
                            onDragStopped = {
                                onDragStopped()
                            },
                        )
                }
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .size(width = 32.dp, height = 4.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )
            }

        }
    }
}
