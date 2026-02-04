package com.kynarec.kmusic.ui.viewModels

import android.app.Application
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.DownloadService
import com.kynarec.kmusic.service.innertube.playSongById
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File

@OptIn(UnstableApi::class)
class DataViewModel (
    private val application: Application,
    private val downloadManager: DownloadManager,
    private val downloadCache: SimpleCache,
    private val database: KmusicDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    init {
        updateStats()
    }

    fun updateStats() {
        Log.i("DataViewModel", downloadCache.cacheSpace.toString())
//        viewModelScope.launch(Dispatchers.IO) {
            val dbName = "kmusic_database"
            val dbFile = application.getDatabasePath(dbName)
            val dbDir = dbFile.parentFile
            val dbBytes = dbDir?.listFiles()?.filter { it.name.startsWith(dbName) }?.sumOf { it.length() } ?: 0L
            val coilCacheDir = File(application.cacheDir, "image_cache")
            val imgBytes = if (coilCacheDir.exists()) {
                coilCacheDir.walkTopDown().map { it.length() }.sum()
            } else 0L

            val downloadsCount = getDownloadedSongsCount()
            val downloadsBytes = getDownloadSize()
            _uiState.value = DataUiState(
                downloadsCount = downloadsCount,
                downloadsBytes = downloadsBytes,
                imageBytes = imgBytes,
                databaseBytes = dbBytes
            )
//        }
    }

    fun addDownload(song: Song) {
        val context = application.applicationContext
        viewModelScope.launch {
            val uri = playSongById(song.id)
            if (uri == "NA" || uri.isEmpty()) return@launch
            val downloadRequest = DownloadRequest.Builder(
                song.id,
                uri.toUri()
            )
                .setCustomCacheKey(song.id)
                .setData(song.title.toByteArray(Charsets.UTF_8))
                .build()

            androidx.media3.exoplayer.offline.DownloadService.sendAddDownload(
                context,
                DownloadService::class.java,
                downloadRequest,
                /* foreground = */ false
            )
            Log.i("MusicViewModel", "Sent request to add download: ${song.id}")

        }
        updateStats()
    }

    fun removeDownload(song: Song) {
        val context = application.applicationContext
        androidx.media3.exoplayer.offline.DownloadService.sendRemoveDownload(
            context,
            DownloadService::class.java,
            song.id,
            /* foreground = */ false
        )
        Log.i("MusicViewModel", "Sent request to remove download: ${song.id}")
        updateStats()
    }

    fun removeAllDownloads() {
        val context = application.applicationContext
        androidx.media3.exoplayer.offline.DownloadService.sendRemoveAllDownloads(
            context,
            DownloadService::class.java,
            /* foreground = */ false
        )
        Log.i("MusicViewModel", "Sent request to remove ALL downloads")
        updateStats()
    }

    fun isSongDownloaded(songId: String): Boolean {
        val download = downloadManager.downloadIndex.getDownload(songId)
        return download != null && download.state == Download.STATE_COMPLETED
    }

    fun getDownloadStats(): String {
        val count = getDownloadedSongsCount()
        val size = "%.2f".format(getDownloadSizeMb())
        return "$count songs downloaded ($size MB used)"
    }

    fun getDownloadedSongsCount(): Int {
        val cursor = downloadManager.downloadIndex.getDownloads()
        var count = 0
        try {
            while (cursor.moveToNext()) {
                if (cursor.download.state == Download.STATE_COMPLETED) count++
            }
        } finally {
            cursor.close()
        }
        return count
    }
    fun clearEntireDownloadCache() {
        viewModelScope.launch(Dispatchers.IO) {
            // SimpleCache stores data by 'keys'
            val allKeys = downloadCache.keys

            allKeys.forEach { key ->
                // This physically deletes the files for each key
                downloadCache.removeResource(key)
            }

            // Also clear the DownloadManager's database so the UI
            // doesn't think songs are still downloaded
            downloadManager.removeAllDownloads()
            updateStats()
            Log.i("MusicViewModel", "Cache is now completely empty.")
        }
    }

    fun getDownloadedSongsCountFormatted(): String {
        val count = getDownloadedSongsCount()
        val songText = if (count == 1) "song" else "songs"
        return "$count $songText"
    }

    fun getDownloadSizeMb(): Double =
        downloadCache.cacheSpace / (1024.0 * 1024.0)

    fun getDownloadSize(): Long =
        downloadCache.cacheSpace

    fun getImageCacheStats(): String {
        val coilCacheDir = File(application.cacheDir, "image_cache")
        val sizeBytes = if (coilCacheDir.exists()) {
            coilCacheDir.walkTopDown().map { it.length() }.sum()
        } else 0L

        val sizeMb = sizeBytes / (1024.0 * 1024.0)
        return "%.2f MB used by artworks".format(sizeMb)
    }

    @kotlin.OptIn(ExperimentalCoilApi::class)
    fun clearImageCache() {
        val imageLoader = application.imageLoader
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
        updateStats()
    }

    fun getDatabaseStats(): String {
        val dbName = "kmusic_database"
        val dbFile = application.getDatabasePath(dbName)

        // Room creates 3 files: the .db, the -wal (write-ahead log), and the -shm (shared memory)
        val dbDir = dbFile.parentFile
        val totalBytes = dbDir?.listFiles()?.filter { it.name.startsWith(dbName) }?.sumOf { it.length() } ?: 0L

        val sizeMb = totalBytes / (1024.0 * 1024.0)
        return "%.2f MB (Songs, Playlists, History)".format(sizeMb)
    }

    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAllTables()
            Log.i("MusicViewModel", "Database library cleared successfully")
            updateStats()
        }
    }
}

@Serializable
data class DataUiState(
    val downloadsCount: Int = 0,
    val downloadsBytes: Long = 0,
    val imageBytes: Long = 0,
    val databaseBytes: Long = 0
)