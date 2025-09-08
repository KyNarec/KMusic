package com.kynarec.kmusic.utils

import android.content.Context
import android.util.Log
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

fun createMediaItemFromSong(song: Song, context: Context): MediaItem {
    var mediaItem = MediaItem.Builder().build()
    val py = Python.getInstance()
    val module = py.getModule("backend")
    val uri = module.callAttr("playSongByIdWithBestBitrate", song.id) // Your Python call
    Log.i("Main Activity", "ExoPlayer URI: $uri")

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