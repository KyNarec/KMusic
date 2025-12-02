package com.kynarec.kmusic.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.withTransaction
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongPlaylistMap
import com.kynarec.kmusic.service.innertube.getHighestDefinitionThumbnailFromPlayer
import com.kynarec.kmusic.service.innertube.playSongByIdWithBestBitrate
import com.kynarec.kmusic.service.innertube.ClientName
import com.kynarec.kmusic.service.innertube.InnerTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext


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

suspend fun createMediaItemFromSong(song: Song, context: Context): MediaItem = withContext(Dispatchers.IO) {
    val uri = playSongByIdWithBestBitrate(song.id) ?: return@withContext MediaItem.Builder().build()

    val mediaMetadataBuilder = MediaMetadata.Builder()
        .setTitle(song.title)
        .setArtist(song.artist)
        .setIsBrowsable(false)
        .setIsPlayable(true)
        .setArtworkUri(song.thumbnail.toUri())

    // Database insertion should also be offloaded
    val songDao = KmusicDatabase.getDatabase(context).songDao()
    songDao.upsertSong(song)

    MediaItem.Builder()
        .setMediaId(song.id)
        .setUri(uri)
        .setMediaMetadata(mediaMetadataBuilder.build())
        .build()
}

// This function is for creating browsable items. It should not contain the playback URI.
fun createPartialMediaItemFromSong(song: Song, context: Context): MediaItem {
    // This is a browsable item, it only needs the MediaId and Metadata.
    return MediaItem.Builder()
        .setMediaId(song.id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setArtworkUri(song.thumbnail.toUri())
                .setIsBrowsable(false) // Songs are not folders.
                .setIsPlayable(true)   // Songs are playable.
                .build()
        )
        .build()
}

@Composable
fun ConditionalMarqueeText(
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier
) {
    // Make state more stable
    var isTextOverflowing by remember(text) { mutableStateOf(false) }
    var hasCheckedOverflow by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = fontSize,
        maxLines = maxLines,
        color = color,
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


/**
 * Parses a single CSV line into a list of strings.
 * This is a simple parser that assumes comma separation and no internal commas/quotes.
 */
fun parseCsvLine(line: String): List<String>? {
    return try {
        line.split(",").map { it.trim() }
    } catch (e: Exception) {
        Log.e("DataImportService", "Error parsing CSV line: $line", e)
        null
    }
}

fun importPlaylistFromCsv(
    csvContent: String,
    context: Context,
    database: KmusicDatabase
): Flow<Int> = channelFlow {
    val songDao = database.songDao()
    val playlistDao = database.playlistDao()

    // Split the CSV content into lines, skipping the header line.
    val lines = csvContent.lines().drop(1).filter { it.isNotBlank() }

    if (lines.isEmpty()) {
        Log.w("DataImportService", "CSV content is empty or only contains a header.")
        return@channelFlow
    }

    // Parse the first song to extract the common Playlist data (Name, BrowseId)
    val firstSongData = parseCsvLine(lines.first())
    if (firstSongData == null) {
        Log.e("DataImportService", "Failed to parse the first CSV line.")
        return@channelFlow
    }

    val playlistName = firstSongData.getOrNull(1) ?: return@channelFlow
    val playlistBrowseId = firstSongData.getOrNull(0)

    // 🏆 Room Transaction: Ensure all insertions (Playlist, Songs, Maps) succeed or fail together.
    database.withTransaction {
        Log.i("DataImportService", "Starting import transaction for playlist: $playlistName")

        // 1. Create and Insert the Playlist
        val newPlaylist = Playlist(
            name = playlistName,
            browseId = playlistBrowseId
            // id will be generated by the database
        )
        val newPlaylistId = playlistDao.insertPlaylist(newPlaylist)
        Log.d("DataImportService", "Inserted new playlist with ID: $newPlaylistId")

        // 2. Iterate through all songs, insert them, and create the map entries
        lines.forEachIndexed { index, line ->
            val data = parseCsvLine(line)
            if (data != null && data.size >= 7) {
                val songEntity = Song(
                    id = data[2],                       // MediaId
                    title = data[3],                    // Title
                    artist = data[4],                   // Artists
                    duration = data[5],                 // Duration
//                    thumbnail = data[6]                 // ThumbnailUrl
                    thumbnail = getHighestDefinitionThumbnailFromPlayer(InnerTube(ClientName.WebRemix).player(data[2]))?: ""
//                    thumbnail = InnerTube(CLIENTNAME.WEB_REMIX).(data[2])?: ""
                    // likedAt and totalPlayTimeMs use defaults (null/0L)
                )

                // Insert/Replace the Song entity using upsert to preserve user data
                songDao.upsertSong(songEntity)

                // Create the mapping entity (position starts at 0 or 1, using 0-based index)
                val mapEntry = SongPlaylistMap(
                    songId = songEntity.id,
                    playlistId = newPlaylistId,
                    position = index // position 0, 1, 2, ...
                )

                // Insert the mapping entity
                playlistDao.insertSongToPlaylist(mapEntry)
                send(index)
            } else {
                Log.w("DataImportService", "Skipping malformed CSV line: $line")
            }
        }
        Log.i("DataImportService", "Import transaction completed successfully.")
    }
}

/**
 * Creates and launches the Android Share Sheet to share a URL.
 * @param context The application context.
 * @param url The URL string to be shared.
 * @param title The title text for the Share Sheet prompt.
 */
fun shareUrl(context: Context, url: String, title: String = "Share Song Link") {
    // 1. Create a simple SEND Intent
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url) // The content (the URL)
        type = "text/plain" // Mime type for plain text
    }

    // 2. Create the Chooser Intent
    // This forces Android to show the list of apps instead of picking one automatically
    val shareIntent = Intent.createChooser(sendIntent, title)

    // 3. Launch the activity
    context.startActivity(shareIntent)
}