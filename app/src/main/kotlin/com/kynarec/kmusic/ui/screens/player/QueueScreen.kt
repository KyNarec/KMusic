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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
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
    viewModel: MusicViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val showInfoSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                    viewModel.moveSong(start, end)
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
                val isPlaying = song.id == uiState.songsList.get(viewModel.getCurrentPlayingIndex()).id
                Box(
                    modifier = Modifier
                        .shadow(elevation)
                        .zIndex(if (isDragging) 1f else 0f)
                ) {

                    val dragState2 = remember {
                        AnchoredDraggableState(
                            initialValue = QueueItemSwipeState.Resting,
                        )
                    }
                    val density = LocalDensity.current
                    val anchors = remember(density) {
                        val deleteOffset = with(density) { -48.dp.toPx() }
                        val playNextOffset = with(density) { 48.dp.toPx() }
                        DraggableAnchors {
                            QueueItemSwipeState.Resting at 0f
                            QueueItemSwipeState.Delete at deleteOffset
                            QueueItemSwipeState.PlayNext at playNextOffset
                        }
                    }
                    SideEffect { dragState2.updateAnchors(anchors) }

                    val itemOverscroll = rememberOverscrollEffect()

                    LaunchedEffect(dragState2) {
                        snapshotFlow { dragState2.settledValue }
                            .collectLatest {
                                when (it) {
                                    QueueItemSwipeState.Delete -> {
                                        viewModel.removeSongFromQueue(song.id)
                                    }
                                    QueueItemSwipeState.PlayNext -> {
                                        scope.launch {
                                            viewModel.playNext(song.song)
                                        }
                                        delay(50)
                                        dragState2.animateTo(
                                            QueueItemSwipeState.Resting
                                        )
                                    }
                                    else -> {}
                                }
                            }
                    }
                    QueueSongComponent(
                        song.song,
                        onClick = { viewModel.skipToSong(index) },
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
                                state = dragState2,
                                orientation = Orientation.Horizontal,
                                enabled = true,
                                overscrollEffect = itemOverscroll,
                                // https://www.youtube.com/watch?v=JYtLy4V2x-A
                            )
                            .overscroll(itemOverscroll)
                            .offset {
                                IntOffset(
                                    x = dragState2
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
            viewModel = viewModel,
            database =  database,
            navController = navController
        )
    }
}