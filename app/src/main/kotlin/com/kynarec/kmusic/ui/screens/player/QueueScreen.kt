package com.kynarec.kmusic.ui.screens.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.player.QueueSongComponent
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun QueueScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    sheetState: SheetState,
    showBottomSheet: MutableState<Boolean>,
    database: KmusicDatabase,
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
        val playingIndex = songList.indexOfFirst { it.id == uiState.currentSong?.id }
        if (playingIndex != -1) {
            lazyListState.animateScrollToItem(index = playingIndex)
        }
    }

    BackHandler {
        onClose()
    }

    ModalBottomSheet(
        onDismissRequest = {
            showBottomSheet.value = false
        },
        dragHandle = null,
        shape = RectangleShape,
        sheetState = sheetState,
        containerColor = Color.Black.copy(alpha = 0.5f),
    ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState
            ) {
                val songList = uiState.songsList
                itemsIndexed(localSongList, key = { _, song -> song.id}) { index, song ->
                    ReorderableItem(reorderableLazyListState, key = song.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                        Box(
                            modifier = Modifier
                                .shadow(elevation)
                                .zIndex(if (isDragging) 1f else 0f)
                        ) {
                            QueueSongComponent(
                                song,
                                onClick = { viewModel.skipToSong(song) },
                                onLongClick = {
                                    longClickSong = song
                                    showInfoSheet.value = true
                                },
                                isPlaying = song == uiState.currentSong,
                                reorderableCollectionItemScope = this@ReorderableItem,
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
}