package com.kynarec.kmusic.ui.screens

import android.content.ComponentName
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.utils.createMediaItemFromSong

@Composable
fun SongsScreen(
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyColumn {
        items(songs.size) { index ->
            val song = songs[index]
            SongComponent(song = song, onClick = {
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))
                val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

                controllerFuture.addListener(
                    {
                        // Step 2: Once connected, retrieve the controller and send the command
                        val mediaController = controllerFuture.get()

                        // Create a MediaItem from your Song data class
                        val mediaItem = createMediaItemFromSong(song)

                        // Use the MediaController to set the media item and start playback.
                        mediaController.setMediaItem(mediaItem)
                        mediaController.prepare()
                        mediaController.play()

                        // Optional: You could now navigate to the PlayerFragment
//                        (activity as? MainActivity)?.navigatePlayer()
                    },
                    MoreExecutors.directExecutor()
                ) })
        }
    }
}