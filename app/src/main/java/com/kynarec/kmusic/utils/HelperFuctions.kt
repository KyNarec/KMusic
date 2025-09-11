package com.kynarec.kmusic.utils

import android.content.Context
import android.util.Log
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.chaquo.python.Python
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


fun parseDurationToMillis(durationStr: String): Long {
    val parts = durationStr.split(":")
    return when (parts.size) {
        2 -> {
            val minutes = parts[0].toLongOrNull() ?: 0L
            val seconds = parts[1].toLongOrNull() ?: 0L
            (minutes * 60 + seconds) * 1000
        }
        3 -> { // For "HH:mm:ss" format
            val hours = parts[0].toLongOrNull() ?: 0L
            val minutes = parts[1].toLongOrNull() ?: 0L
            val seconds = parts[2].toLongOrNull() ?: 0L
            (hours * 3600 + minutes * 60 + seconds) * 1000
        }
        else -> 0L
    }
}

fun parseMillisToDuration(timeMs: Long): String {
    val minutes = timeMs / 1000 / 60
    val seconds = (timeMs / 1000 % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

fun createMediaItemFromSong(song: Song, context: Context): MediaItem {
    var mediaItem = MediaItem.Builder().build()
    val py = Python.getInstance()
    val module = py.getModule("backend")
    val uri = module.callAttr("playSongByIdWithBestBitrate", song.id) // Your Python call
//    Log.i("Main Activity", "ExoPlayer URI: $uri")

    uri?.toString()?.let { playbackUriString ->
        if (playbackUriString.isNotBlank()) {
            mediaItem = MediaItem.Builder()
                .setMediaId(song.id) // Important: Set mediaId on ExoPlayer's MediaItem
                .setUri(playbackUriString)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(song.thumbnail.toUri())
                        .build()
                )
                .build()
        } else {
            Log.w("Main Activity", "Python backend returned empty or null URI for song ID: $song.id")
        }
        val songDao = KmusicDatabase.getDatabase(context).songDao()
        val serviceScope = CoroutineScope(Dispatchers.Main + Job())
        serviceScope.launch {
//            songDao.deleteSong(song)
            if (songDao.getSongById(song.id) == null) {
                songDao.insertSong(song)
                Log.i("Main Activity", "Song with ID ${song.id} inserted into database.")
            }
        }
    }
    return mediaItem
}


@Composable
fun ConditionalMarqueeText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    // Make state more stable
    var isTextOverflowing by remember(text) { mutableStateOf(false) }
    var hasCheckedOverflow by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (!hasCheckedOverflow) {
                isTextOverflowing = textLayoutResult.hasVisualOverflow
                hasCheckedOverflow = true
            }
        },
        modifier = modifier.then(
            // Only apply marquee after we've determined overflow AND text is long enough
            if (isTextOverflowing && hasCheckedOverflow && text.length > 30) {
                Modifier.basicMarquee(
                    animationMode = MarqueeAnimationMode.Immediately,
//                    delayMillis = 1000,
                    initialDelayMillis = 1000,
                    velocity = 30.dp
                )
            } else {
                Modifier
            }
        )
    )
}