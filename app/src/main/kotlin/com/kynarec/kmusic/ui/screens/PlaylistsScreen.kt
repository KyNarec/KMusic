package com.kynarec.kmusic.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.ui.PlaylistScreen
import com.kynarec.kmusic.ui.components.TwoByTwoImageGrid
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.importPlaylistFromCsv
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    database: KmusicDatabase
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val playlists: List<Playlist> by remember(database) {
        database.playlistDao().getAllPlaylists()
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val isLoading = playlists.isEmpty()

    val totalLines = remember { mutableIntStateOf(0) }
    val currentLine = remember { mutableIntStateOf(0) }
    val isImportingPlaylist = remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Playlists",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            val file = FileKit.openFilePicker(mode = FileKitMode.Single)
                            // Call the extracted import function
                            if (file?.extension == "csv") {
                                SmartMessage("Importing...", context = context, durationLong = true)
                                val csvContent = file.readString()
                                totalLines.intValue = csvContent.lines().drop(1).filter { it.isNotBlank() }.size
                                currentLine.intValue = 0
                                isImportingPlaylist.value = true
                                try {
                                    // 3. Start the import and collect progress
                                    importPlaylistFromCsv(csvContent, context, database) // Use stored content
                                        .collect { currentIndex ->
                                            currentLine.intValue =
                                                currentIndex + 1 // Increment to show songs processed (1-based)
                                            Log.d("Import", "Progress: ${currentIndex + 1} / ${totalLines.intValue}")
                                        }
                                } catch (e: Exception) {
                                    Log.e("Import", "Import failed", e)
                                    SmartMessage(
                                        "Import failed: ${e.message}",
                                        context = context,
                                        type = PopupType.Error
                                    )
                                } finally {
                                    // 4. Hide indicator regardless of success or failure
                                    Log.i("Import", "setting isImportinPlaylist to false")
                                    isImportingPlaylist.value = false
                                }
                            } else {
                                SmartMessage(
                                    "Seems like you didn't select a compatible CSV file",
                                    context = context,
                                    type = PopupType.Error
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add new playlist (Import CSV)",
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (!isImportingPlaylist.value && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp),
                columns = GridCells.Adaptive(minSize = 100.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistListItem(
                        playlist = playlist, navController, onRemove = {
                            scope.launch {
                                database.playlistDao().deletePlaylist(it)
                            }
                        },
                        database = database
                    )
                }
                if (isImportingPlaylist.value && totalLines.intValue > 0) {
                    item {
                        // https://proandroiddev.com/cheatsheet-for-centering-items-in-jetpack-compose-1e3534415237
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularWavyProgressIndicator(
                                // Use .intValue for both the numerator and denominator
                                progress = { currentLine.intValue.toFloat() / totalLines.intValue.toFloat() },
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistListItem(
    playlist: Playlist,
    navController: NavHostController,
    onRemove: (Playlist) -> Unit,
    database: KmusicDatabase
) {
    val context = LocalContext.current
    val songsThumbnailList = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        database.playlistDao()
            .getFirstFourSongsForPlaylistFlow(playlist.id)
            // Use the suspending collect function here
            .collect { songsList ->
                // This lambda runs every time the Flow emits a new list
                songsThumbnailList.clear()
                songsList.forEach { song ->
                    songsThumbnailList.add(song.thumbnail)
                }
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .background(Color.Transparent),
        onClick = {
            navController.navigate(PlaylistScreen(playlist.id))
        },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .align(Alignment.CenterHorizontally)
            )
            {
                TwoByTwoImageGrid(
                    songsThumbnailList
                )
            }
            ConditionalMarqueeText(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .basicMarquee(initialDelayMillis = 1000)

            )
        }
    }
}