package com.kynarec.kmusic.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.text.TextLayoutResult
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
import com.kynarec.kmusic.data.db.entities.SongArtist
import com.kynarec.kmusic.data.db.entities.SongPlaylistMap
import com.kynarec.kmusic.service.innertube.ClientName
import com.kynarec.kmusic.service.innertube.InnerTube
import com.kynarec.kmusic.service.innertube.getHighestDefinitionThumbnailFromPlayer
import com.kynarec.kmusic.service.innertube.playSongById
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext

@JvmName("StringParseDurationToMillis")
fun String.parseDurationToMillis(): Long {
    val parts = this.split(":")
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

@JvmName("LongParseMillisToDuration")
fun Long.parseMillisToDuration(): String {
    val minutes = this / 1000 / 60
    val seconds = (this / 1000 % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

/**
 * Converts a time string (e.g., "3:21" or "1:02:10") into total seconds.
 * Returns 0 if the string is empty or invalid.
 */
fun String?.toSeconds(): Int {
    if (this.isNullOrBlank()) return 0
    return this.split(":")
        .map { it.toIntOrNull() ?: 0 }
        .fold(0) { acc, time -> acc * 60 + time }
}
/**
 * Converts milliseconds (Long) to seconds (Int).
 */
fun Long.toSeconds(): Int {
    return (this / 1000).toInt()
}

suspend fun createMediaItemFromSong(song: Song, context: Context): MediaItem = withContext(Dispatchers.IO) {
    val uri = playSongById(song.id) ?: return@withContext MediaItem.Builder().build()

    val extras = Bundle().apply {
        putString("ALBUM_ID", song.albumId)
        putString("DURATION", song.duration)
        putString("THUMBNAIL", song.thumbnail)
        // Store artists as a ParcelableArrayList
        putParcelableArrayList("ARTISTS", ArrayList(song.artists))
    }

    val mediaMetadataBuilder = MediaMetadata.Builder()
        .setTitle(song.title)
        .setArtist(song.artists.joinToString(", ") { it.name })
        .setIsBrowsable(false)
        .setIsPlayable(true)
        .setExtras(extras)
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
    val extras = Bundle().apply {
        putString("ALBUM_ID", song.albumId)
        putString("DURATION", song.duration)
        putString("THUMBNAIL", song.thumbnail)
        // Store artists as a ParcelableArrayList
        putParcelableArrayList("ARTISTS", ArrayList(song.artists))
    }

    return MediaItem.Builder()
        .setMediaId(song.id)
        .setUri("EMPTY")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artists.joinToString(", ") { it.name })
                .setArtworkUri(song.thumbnail.toUri())
                .setExtras(extras)
                .setIsBrowsable(false) // Songs are not folders.
                .setIsPlayable(true)   // Songs are playable.
                .build()
        )
        .build()
}

suspend fun MediaItem.createFullMediaItem(): MediaItem {
    val uri = playSongById(this.mediaId)
    return this.buildUpon()
        .setUri(uri)
        .build()
}

fun MediaItem.toSong(): Song {
    val metadata = this.mediaMetadata
    val extras = metadata.extras ?: Bundle.EMPTY

    val artistsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        extras.getParcelableArrayList("ARTISTS", SongArtist::class.java)
    } else {
        @Suppress("DEPRECATION")
        extras.getParcelableArrayList("ARTISTS")
    } ?: emptyList<SongArtist>()

    return Song(
        id = this.mediaId,
        title = metadata.title?.toString() ?: "Unknown",
        artists = artistsList,
        albumId = extras.getString("ALBUM_ID"),
        duration = extras.getString("DURATION") ?: "0:00",
        thumbnail = extras.getString("THUMBNAIL") ?: ""
    )
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
    // 1. State to track overflow status and check completion
    var isTextOverflowing by remember(text) { mutableStateOf(false) }
    var hasCheckedOverflow by remember(text) { mutableStateOf(false) }

    // 2. Condition to apply the marquee modifier
    val shouldMarqueeRun = isTextOverflowing && hasCheckedOverflow && text.length > 30

    Text(
        text = text,
        fontSize = fontSize,
        maxLines = maxLines,
        color = color,
        overflow = TextOverflow.Ellipsis,
        style = style,
        // 3. Check for overflow on the first layout pass
        onTextLayout = { textLayoutResult: TextLayoutResult ->
            if (!hasCheckedOverflow) {
                // Changing this state triggers the needed recomposition
                isTextOverflowing = textLayoutResult.hasVisualOverflow
                hasCheckedOverflow = true
            }
        },
        modifier = modifier.then(
            // 4. Conditionally apply the ENTIRE basicMarquee modifier
            if (shouldMarqueeRun) {
                Modifier.basicMarquee(
                    animationMode = MarqueeAnimationMode.Immediately,
                    initialDelayMillis = 1000,
                    velocity = 30.dp
                )
            } else {
                // Must return a simple Modifier when the condition is false
                Modifier
            }
        )
    )
}

fun parseCsvLine(line: String): List<String>? {
    if (line.isBlank()) return null
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false

    // State machine-like parsing
    for (i in line.indices) {
        val char = line[i]

        if (char == '"') {
            if (i + 1 < line.length && line[i + 1] == '"' && inQuotes) {
                // Escaped quote: "" becomes " inside the field
                sb.append('"')
                // Note: The loop naturally advances i, no need to manually skip in this 'for' structure.
            } else {
                // Toggle quote state
                inQuotes = !inQuotes
            }
        } else if (char == ',' && !inQuotes) {
            // Comma outside of quotes is a delimiter
            result.add(sb.toString().trim())
            sb.clear()
        } else {
            // Regular character
            sb.append(char)
        }
    }
    // Add the last field
    result.add(sb.toString().trim())

    // Post-processing to remove surrounding quotes from fields if they exist
    return result.map { field ->
        if (field.length >= 2 && field.startsWith('"') && field.endsWith('"')) {
            // Unescape inner quotes for true CSV format (e.g. "" becomes ")
            field.substring(1, field.length - 1).replace("\"\"", "\"")
        } else {
            field
        }
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
                val cleanedArtists = data[4]
                    .replace(",", ", ") // Simple: Replaces all ',' with ', '
                    .replace(",  ", ", ") // Optional cleanup for any double spaces created

                val artists = mutableListOf<SongArtist>()
                for (index in 0..<cleanedArtists.split(", ").size) {
                    artists.add(SongArtist(
                        id = data[9].split(",").get(index),
                        name = cleanedArtists.split(", ").get(index)
                    ))
                }
                val songEntity = Song(
                    id = data[2],                       // MediaId
                    title = data[3],                    // Title
                    artists = artists,                   // Artists
                    duration = data[5],                 // Duration
//                    thumbnail = data[6]                 // ThumbnailUrl
                    albumId = data[7],
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