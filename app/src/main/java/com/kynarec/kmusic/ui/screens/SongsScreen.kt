package com.kynarec.kmusic.ui.screens

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
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.utils.createMediaItemFromSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
@Composable
fun SongsScreen(
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    val scope = rememberCoroutineScope()


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
                    scope.launch {
                        val mediaItem = withContext(Dispatchers.IO) {
                            createMediaItemFromSong(song, context)
                        }

                        mediaController?.let { controller ->
                            controller.setMediaItem(mediaItem)
                            controller.prepare()
                            controller.play()
                        }
                    }
                }
            }

            SongComponent(
                song = song,
                onClick = onSongClick as () -> Unit
            )
        }
    }
}