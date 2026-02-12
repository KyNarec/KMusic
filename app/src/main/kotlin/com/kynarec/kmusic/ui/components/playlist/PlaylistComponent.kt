package com.kynarec.kmusic.ui.components.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.PlaylistPreview
import com.kynarec.kmusic.ui.components.MarqueeBox

@Composable
fun PlaylistComponent(
    playlist: Playlist,
    navController: NavHostController,
    onRemove: (Playlist) -> Unit,
    database: KmusicDatabase,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val songsThumbnailList = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        database.playlistDao()
            .getFirstFourSongsForPlaylistFlow(playlist.id)
            .collect { songsList ->
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
        onClick = onClick,
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
            MarqueeBox(
                contentAlignment = Alignment.Center,
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun PlaylistComponent(
    playlistPreview: PlaylistPreview,
//    navController: NavHostController,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .background(Color.Transparent),
        onClick = onClick,
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
                AsyncImage(
                    model = playlistPreview.thumbnail,
                    contentDescription = "Playlist art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    imageLoader = LocalContext.current.imageLoader
                )
            }
            MarqueeBox(
                contentAlignment = Alignment.Center,
                text = playlistPreview.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            MarqueeBox(
                contentAlignment = Alignment.Center,
                text = playlistPreview.author,
            )
            MarqueeBox(
                contentAlignment = Alignment.Center,
                text = playlistPreview.views,

            )
        }
    }
}