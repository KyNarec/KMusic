package com.kynarec.kmusic.ui.screens

import android.app.Application
import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel

@OptIn(UnstableApi::class)
@Composable
fun SongsScreen(
    songs: List<Song>,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory((LocalContext.current.applicationContext as Application as MyApp).database.songDao(),LocalContext.current))
) {
    val context = LocalContext.current

    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    rememberCoroutineScope()


    LaunchedEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    LazyColumn {
        items(
            songs.size,
            key = { index -> songs[index].id }
        ) { index ->
            val song = songs[index]
            // Create stable onClick callback
            val onSongClick = remember(song.id) {
                {
                    viewModel.playSong(song)
                }
            }

            SongComponent(
                song = song,
                onClick = onSongClick as () -> Unit
            )
        }
    }
}