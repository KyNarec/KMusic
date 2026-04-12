package com.kynarec.kmusic.ui.screens.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.QueueItemSwipeState
import com.kynarec.kmusic.ui.components.player.QueueSongComponent
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.PlayerScreenAction
import com.kynarec.kmusic.ui.viewModels.PlayerScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun QueueScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerScreenViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val showInfoSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val songList = uiState.songsList
    var localSongList by remember(uiState.songsList) { mutableStateOf(uiState.songsList) }

    val hapticFeedback = LocalHapticFeedback.current

    var initialDraggingIndex by remember { mutableStateOf<Int?>(null) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        Log.i("QueueScreen", "Reordering songs from $initialDraggingIndex")
        if (initialDraggingIndex == null) {
            initialDraggingIndex = from.index
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)

        localSongList = localSongList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    val isDragging = reorderableLazyListState.isAnyItemDragging

    LaunchedEffect(isDragging) {
        if (!isDragging && localSongList != uiState.songsList) {
            val start = initialDraggingIndex
            if (start != null) {
                val songToMove = uiState.songsList[start]
                val end = localSongList.indexOfFirst { it.id == songToMove.id }
                Log.i("QueueScreen", "Moving songs from $start to $end")

                if (end != -1 && start != end) {
                    viewModel.onAction(PlayerScreenAction.MoveQueueItem(start, end))
                }
            }
            initialDraggingIndex = null
        }
    }

    LaunchedEffect(uiState.currentSong) {
        val playingIndex = songList.indexOfFirst { it.song.id == uiState.currentSong?.id }
        if (playingIndex != -1) {
            lazyListState.animateScrollToItem(index = playingIndex)
        }
    }
    BackHandler {
        onClose()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {

        itemsIndexed(localSongList, key = { _, song -> song.id}) { index, song ->
            ReorderableItem(reorderableLazyListState, key = song.id) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                val isPlaying = song.id == uiState.songsList.getOrNull(uiState.currentPlayingIndex)?.id
                Box(
                    modifier = Modifier
                        .shadow(elevation)
                        .zIndex(if (isDragging) 1f else 0f)
                ) {

                    val queueItemDragState = remember {
                        AnchoredDraggableState(
                            initialValue = QueueItemSwipeState.Resting,
                        )
                    }
                    val density = LocalDensity.current
                    val anchors = remember(density) {
                        val deleteOffset = with(density) { -64.dp.toPx() }
                        val playNextOffset = with(density) { 64.dp.toPx() }
                        DraggableAnchors {
                            QueueItemSwipeState.Resting at 0f
                            QueueItemSwipeState.Delete at deleteOffset
                            QueueItemSwipeState.PlayNext at playNextOffset
                        }
                    }
                    SideEffect { queueItemDragState.updateAnchors(anchors) }

                    val itemOverscroll = rememberOverscrollEffect()

                    LaunchedEffect(queueItemDragState) {
                        snapshotFlow { queueItemDragState.settledValue }
                            .collectLatest {
                                when (it) {
                                    QueueItemSwipeState.Delete -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onAction(PlayerScreenAction.RemoveQueueItem(song.id))
                                    }
                                    QueueItemSwipeState.PlayNext -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            viewModel.onAction(PlayerScreenAction.PlayNextQueueItem(song.song))
                                        }
                                        delay(50)
                                        queueItemDragState.animateTo(
                                            QueueItemSwipeState.Resting
                                        )
                                    }
                                    else -> {}
                                }
                            }
                    }
                    SwipeBackgroundActions(queueItemDragState)
                    QueueSongComponent(
                        song.song,
                        onClick = { viewModel.onAction(PlayerScreenAction.SkipToQueueItem(index)) },
                        onLongClick = {
                            longClickSong = song.song
                            showInfoSheet.value = true
                        },
                        isPlaying = isPlaying,
                        reorderableCollectionItemScope = this@ReorderableItem,
                        onDragStarted = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDragStopped = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .anchoredDraggable(
                                state = queueItemDragState,
                                orientation = Orientation.Horizontal,
                                enabled = true,
                                overscrollEffect = itemOverscroll,
                                // https://www.youtube.com/watch?v=JYtLy4V2x-A
                            )
                            .overscroll(itemOverscroll)
                            .offset {
                                IntOffset(
                                    x = queueItemDragState
                                        .requireOffset()
                                        .roundToInt(),
                                    y = 0
                                )
                            }
                    )
                }
            }
        }
    }
    if (showInfoSheet.value && longClickSong != null) {
        SongOptionsBottomSheet(
            song = longClickSong!!,
            onDismiss = { showInfoSheet.value = false },
            database =  database,
            navController = navController
        )
    }
}

@Composable
fun SwipeBackgroundActions(
    dragState: AnchoredDraggableState<QueueItemSwipeState>,
    modifier: Modifier = Modifier
) {
    val offset = dragState.offset.takeIf { !it.isNaN() } ?: 0f

    Box(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (offset > 0) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Play Next",
                modifier = Modifier.align(Alignment.CenterStart).size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (offset < 0) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.align(Alignment.CenterEnd).size(32.dp),
                tint = MaterialTheme.colorScheme.errorContainer
            )
        }
    }
}