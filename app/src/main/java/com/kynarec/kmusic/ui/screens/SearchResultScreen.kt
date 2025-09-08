package com.kynarec.kmusic.ui.screens

import android.content.ComponentName
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.utils.createMediaItemFromSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SearchResultScreen(
    query: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Use LaunchedEffect to perform the side effect (data fetching)
    // The coroutine will be launched when the query changes.
    LaunchedEffect(query) {
        isLoading = true
        errorMessage = null
        try {
            // Perform all backend calls on the I/O dispatcher
            val result = withContext(Dispatchers.IO) {
                val py = Python.getInstance()
                val module = py.getModule("backend")
                val pyResult = module.callAttr("searchSongs", query)

                // Check for errors and handle empty list
                if (pyResult.asList() == emptyList<PyObject>()) {
                    return@withContext emptyList<Song>()
                }

                // Use map to transform PyObjects to Song objects efficiently
                pyResult.asList().map { item ->
                    val d = item.callAttr("get", "duration").toString()
                    Song(
                        id = item.callAttr("get", "id").toString(),
                        title = item.callAttr("get", "title").toString(),
                        artist = item.callAttr("get", "artist").toString(),
                        thumbnail = item.callAttr("get", "thumbnail").toString(),
                        duration = if (Regex("""^(\d{1,2}):(\d{1,2})$""").matches(d)) d else "NA"
                    )
                }
            }
            if (result.isEmpty()) {
                errorMessage = "No results found for '$query'."
            } else {
                songs = result
            }
        } catch (e: Exception) {
            errorMessage = "An error occurred: ${e.message}"
        } finally {
            isLoading = false
        }
    }

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