package com.kynarec.kmusic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.components.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    sheetState: SheetState,
    showBottomSheet: MutableState<Boolean>,
    database: KmusicDatabase,
    navController: NavHostController
//    hazeState: HazeState
) {

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val showInfoSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val songList = uiState.songsList

    LaunchedEffect(uiState.currentSong) {
        val playingIndex = songList.indexOfFirst { it.id == uiState.currentSong?.id }
        if (playingIndex != -1) {
            // animateScrollToItem is a suspend function
            listState.animateScrollToItem(index = playingIndex)
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
//        modifier = Modifier.hazeEffect(state = hazeState)
    ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                val songList = uiState.songsList
                items(songList.size) { index ->
                    SongComponent(
                        songList[index],
                        onClick = { viewModel.skipToSong(songList[index]) },
                        onLongClick = {
                            longClickSong = songList[index]
                            showInfoSheet.value = true
                        }
                    )
                }
            }
        if (showInfoSheet.value && longClickSong != null) {
//            Log.i("SongsScreen", "Showing bottom sheet")
//            Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
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