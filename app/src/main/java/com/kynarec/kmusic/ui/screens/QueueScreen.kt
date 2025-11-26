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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory((LocalContext.current.applicationContext as Application as MyApp).database.songDao(),LocalContext.current)),
    sheetState: SheetState,
    showBottomSheet: MutableState<Boolean>,
//    hazeState: HazeState
) {

    rememberCoroutineScope()

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
                        onClick = { viewModel.playSong(songList[index]) }
                    )
                }
            }
    }
}