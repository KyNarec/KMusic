package com.kynarec.kmusic.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.ui.PlaylistScreen
import com.kynarec.kmusic.utils.importPlaylistFromCsv
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@Composable
fun PlaylistsScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val playlistCSV = "PlaylistBrowseId,PlaylistName,MediaId,Title,Artists,Duration,ThumbnailUrl,AlbumId,AlbumTitle,ArtistIds\n" +
            "VLPLysxZNolVzGNRnPA2CzvEILRF1LKimF8S,Chill,A__cH65WRvE,Californication,Red Hot Chili Peppers,5:29,https://lh3.googleusercontent.com/vEzFuxLILzCtIKEdZWOwSoIfXN3462-JcZ7AYXgV7i9n06NzazzUQOloJ7ghh2Weswt7OcJQH79BI9kePA=w60-h60-l90-rj,MPREb_9zDg9WFf7KR,Californication (Deluxe Version),UCrSorX845CEWXzU4Z7BojjA\n" +
            "VLPLysxZNolVzGNRnPA2CzvEILRF1LKimF8S,Chill,8901V1M5lDk,Otherside,Red Hot Chili Peppers,4:15,https://lh3.googleusercontent.com/vEzFuxLILzCtIKEdZWOwSoIfXN3462-JcZ7AYXgV7i9n06NzazzUQOloJ7ghh2Weswt7OcJQH79BI9kePA=w60-h60-l90-rj,MPREb_9zDg9WFf7KR,Californication (Deluxe Version),UCrSorX845CEWXzU4Z7BojjA\n"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { KmusicDatabase.getDatabase(context) }

    val playlists: List<Playlist> by remember(database) {
        database.playlistDao().getAllPlaylists()
    }.collectAsState(initial = emptyList())

    val isLoading = playlists.isEmpty()


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
                            // Call the extracted import function
                            importPlaylistFromCsv(playlistCSV, context)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.AddToPhotos,
                        contentDescription = "Add new playlist (Import CSV)",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistListItem(playlist = playlist, navController)
                }
            }
        }
    }
}

@Composable
fun PlaylistListItem(playlist: Playlist, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                Log.i("PlaylistListItem", "Playlist clicked: ${playlist.name}")
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
                modifier = Modifier.size(24.dp).padding(end = 8.dp)
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
}