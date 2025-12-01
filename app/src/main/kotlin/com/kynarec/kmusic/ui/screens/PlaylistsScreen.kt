package com.kynarec.kmusic.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.ui.PlaylistScreen
import com.kynarec.kmusic.ui.components.DismissBackground
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.importPlaylistFromCsv
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistsScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { KmusicDatabase.getDatabase(context) }

    val playlists: List<Playlist> by remember(database) {
        database.playlistDao().getAllPlaylists()
    }.collectAsState(initial = emptyList())

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
                                    importPlaylistFromCsv(csvContent, context) // Use stored content
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
                        tint = MaterialTheme.colorScheme.primary
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistListItem(playlist = playlist, navController, onRemove = {
                        scope.launch {
                            database.playlistDao().deletePlaylist(it)
                        }
                    })
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
    onRemove: (Playlist) -> Unit
) {
    val context = LocalContext.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onRemove(playlist)
                    SmartMessage(
                        "Deleted playlist ${playlist.name}",
                        context = context,
                        type = PopupType.Success
                    )
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onRemove(playlist)
                }

                SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
            }
            return@rememberSwipeToDismissBoxState true
        },
        // positional threshold of 25%
        positionalThreshold = { it * .25f }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { DismissBackground(dismissState) },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        navController.navigate(PlaylistScreen(playlist.id))
                    },
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for a playlist icon or thumbnail
                    Icon(
                        imageVector = Icons.Default.AddToPhotos, // Using same icon as placeholder
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Optional: Show playlist metadata like song count
                        Text(
                            text = "ID: ${playlist.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        })

}