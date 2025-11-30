package com.kynarec.kmusic.ui.screens

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SongBottomSheet
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory((LocalContext.current.applicationContext as Application as KMusic).database.songDao(),LocalContext.current)),
    sheetState: SheetState,
    showBottomSheet: MutableState<Boolean>,
//    hazeState: HazeState
) {

    rememberCoroutineScope()

    val showInfoSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val uiState by viewModel.uiState.collectAsState()

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
                modifier = Modifier.fillMaxSize()
            ) {
                val songList = uiState.songsList
                items(songList.size) { index ->
                    SongComponent(
                        songList[index],
                        onClick = { viewModel.playSong(songList[index]) },
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
            SongBottomSheet(
                songId = longClickSong!!.id,
                onDismiss = { showInfoSheet.value = false },
                viewModel = viewModel
            )
        }
    }
}