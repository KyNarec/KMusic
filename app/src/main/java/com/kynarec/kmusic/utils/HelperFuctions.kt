package com.kynarec.kmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import innertube.playSongByIdWithBestBitrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


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

    val artworkByteArray = convertThumbnailUriToSquareByteArray(context, song.thumbnail.toUri())
    if (artworkByteArray != null) {
        mediaMetadataBuilder.setArtworkData(artworkByteArray, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        Log.i("PlayerControlBar", "artworkByteArray is not null")

    } else {
        Log.i("PlayerControlBar", "artworkByteArray is null")
        mediaMetadataBuilder.setArtworkUri(song.thumbnail.toUri())
    }

    // Database insertion should also be offloaded
    val songDao = KmusicDatabase.getDatabase(context).songDao()
    if (songDao.getSongById(song.id) == null) {
        songDao.insertSong(song)
    }

    MediaItem.Builder()
        .setMediaId(song.id)
        .setUri(uri)
        .setMediaMetadata(mediaMetadataBuilder.build())
        .build()
}


fun convertThumbnailUriToSquareByteArray(context: Context, uri: Uri): ByteArray? {
    val TAG = "ArtworkByteArrayConverter"
    Log.i(TAG, "Starting conversion for URI: $uri")

    val inputStream: InputStream? = try {
        // Check if the URI is a web URL
        if (uri.scheme == "http" || uri.scheme == "https") {
            Log.d(TAG, "URI is a web URL. Opening network connection.")
            val url = URL(uri.toString())
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            connection.inputStream
        } else {
            // Assume it's a content URI, file URI, etc.
            Log.d(TAG, "URI is a local content URI. Using ContentResolver.")
            context.contentResolver.openInputStream(uri)
        }
    } catch (e: Exception) {
        //Log.e(TAG, "Error opening InputStream for URI: $uri", e)
        Log.e(TAG, "Error opening InputStream for URI: $uri")
        return null
    }

    val originalBitmap: Bitmap? = try {
        BitmapFactory.decodeStream(inputStream).also {
            Log.d(TAG, "Bitmap decoded successfully.")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error decoding Bitmap from InputStream.", e)
        return null
    } finally {
        try {
            inputStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing InputStream.", e)
        }
    }

    originalBitmap ?: run {
        Log.w(TAG, "Original bitmap was null after decoding.")
        return null
    }

    // 2. Create a new square bitmap
    val size = minOf(originalBitmap.width, originalBitmap.height)
    val xOffset = (originalBitmap.width - size) / 2
    val yOffset = (originalBitmap.height - size) / 2

    val squareBitmap: Bitmap = try {
        Bitmap.createBitmap(originalBitmap, xOffset, yOffset, size, size).also {
            Log.d(TAG, "Square bitmap (1:1 ratio) created. Size: ${it.width}x${it.height}")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error creating square bitmap.", e)
        originalBitmap.recycle()
        return null
    }

    // 3. Compress the square bitmap to a ByteArray
    val outputStream = ByteArrayOutputStream()
    val byteArray: ByteArray? = try {
        squareBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        Log.d(TAG, "Bitmap compressed to ByteArray.")
        outputStream.toByteArray()
    } catch (e: Exception) {
        Log.e(TAG, "Error compressing bitmap to ByteArray.", e)
        null
    } finally {
        // Clean up resources
        originalBitmap.recycle()
        squareBitmap.recycle()
        try {
            outputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ByteArrayOutputStream.", e)
        }
    }

    if (byteArray != null) {
        Log.i(TAG, "Conversion complete. Resulting byte array size: ${byteArray.size} bytes.")
    } else {
        Log.e(TAG, "Conversion failed, returning null.")
    }

    return byteArray
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