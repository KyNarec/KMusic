package com.kynarec.kmusic.ui.screens

import android.app.Application
import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.room.util.TableInfo
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.ui.components.SongBottomSheet
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    database: KmusicDatabase
) {
    val context = LocalContext.current

    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val songs = database.songDao().getSongsFlowWithPlaytime().collectAsState(initial = emptyList())

    rememberCoroutineScope()


    Column(
        Modifier.fillMaxSize()
    ) {
        Row(
            Modifier.fillMaxWidth()
        ) {
            // TODO: Add filter bar
        }

        LazyColumn {
            items(
                songs.value.size,
                key = { index -> songs.value[index].id }
            ) { index ->
                val song = songs.value[index]
                // Create stable onClick callback
                val onSongClick = remember(song.id) {
                    {
                        viewModel.playSong(song)
                    }
                }

                SongComponent(
                    song = song,
                    onClick = onSongClick as () -> Unit,
                    onLongClick = {
                        longClickSong = song
                        showBottomSheet.value = true
                    }
                )
            }
        }
        if (showBottomSheet.value && longClickSong != null) {
            Log.i("SongsScreen", "Showing bottom sheet")
            Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
            SongBottomSheet(
                songId = longClickSong!!.id,
                onDismiss = { showBottomSheet.value = false },
                onToggleFavorite = { viewModel.toggleFavorite(longClickSong!!) },
                viewModel = viewModel
            )
        }
    }
}