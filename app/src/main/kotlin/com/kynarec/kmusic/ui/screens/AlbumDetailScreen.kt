package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.innertube.getAlbumAndSongs
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumDetailScreen(
    modifier: Modifier = Modifier,
    albumId: String,
    viewModel: MusicViewModel,
    database: KmusicDatabase
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val albumFLow by database.albumDao().getAlbumByIdFlow(albumId).collectAsState(initial = null)

    var songs by remember { mutableStateOf(emptyList<Song>()) }

    var isLoading by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (albumFLow == null) {
            isLoading = true
            scope.launch {
                getAlbumAndSongs(albumId).collect { albumWithSongs ->
                    albumWithSongs.songs.forEach {
                        songs = songs + it
                    }
                    database.albumDao().insertAlbum(albumWithSongs.album)
                }
                isLoading = false
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Box(
                Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = albumFLow?.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    imageLoader = LocalContext.current.imageLoader
                )
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ConditionalMarqueeText(
                        text = albumFLow?.title ?: "NA",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ConditionalMarqueeText(
                        text = "${albumFLow?.year} - songs count",
//                    style = MaterialTheme.typography.titleLarge
                    )
                }
            }

        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                ConditionalMarqueeText(
                    text = "Songs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
        if (isLoading){
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                        CircularWavyProgressIndicator()
                }
            }
        } else {
            items(songs) {
                SongComponent(
                    song = it,
                    onClick = {},
                    onLongClick = {},
                )
            }
        }
    }
}