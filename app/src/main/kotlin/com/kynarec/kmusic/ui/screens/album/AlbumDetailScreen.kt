package com.kynarec.kmusic.ui.screens.album

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.innertube.getAlbumAndSongs
import com.kynarec.kmusic.ui.components.album.AlbumOptionsBottomSheet
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumDetailScreen(
    modifier: Modifier = Modifier,
    albumId: String,
    viewModel: MusicViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val albumFLow by database.albumDao().getAlbumByIdFlow(albumId).collectAsStateWithLifecycle(null)

    var songs by remember { mutableStateOf(emptyList<Song>()) }

    var isLoading by remember { mutableStateOf(false) }

    var longClickSong by remember { mutableStateOf<Song?>(null) }
    val showAlbumOptionsBottomSheet = remember { mutableStateOf(false) }
    val showSongDetailBottomSheet = remember { mutableStateOf(false) }

    var readMore by remember { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()

    val allDownloaded = if (songs.isNotEmpty()) songs.all { it.id in completedIds } else false
    val isAnyDownloading = songs.any { it.id in downloadingSongs }

    LaunchedEffect(Unit) {
        if (albumFLow == null) {
            isLoading = true
            scope.launch {
                getAlbumAndSongs(albumId).collect { albumWithSongs ->
                    albumWithSongs.songs.forEach {
                        songs = songs + it
                    }
                    Log.i("AlbumDetailScreen", "upserting album")
                    withContext(Dispatchers.IO) {
                        database.albumDao().upsertAlbum(albumWithSongs.album)
                    }
                    Log.i("AlbumDetailScreen", "upserting album done")

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    imageLoader = LocalContext.current.imageLoader
                )
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Spacer(Modifier.height(16.dp))
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
                        text = "${albumFLow?.year} - ${songs.size} Songs",
//                    style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

        if (albumFLow?.authorsText?.isNotEmpty() == true) {
            item {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = "“",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .align(Alignment.Top),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (!readMore) {
                            albumFLow?.authorsText?.substring(
                                0,
                                if ((albumFLow?.authorsText?.length
                                        ?: 0) >= 100
                                ) 100 else albumFLow?.authorsText?.length ?: 0
                            ).plus("...")
                        } else {
                            albumFLow?.authorsText ?: "NA"
                        },
                        style = TextStyle.Default.copy(textAlign = TextAlign.Justify),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                            .clickable {
                                readMore = !readMore
                            },
                    )

                    Text(
                        text = "„",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier
                            .offset(y = 4.dp)
                            .align(Alignment.Bottom),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = albumFLow?.copyright ?: "NA",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConditionalMarqueeText(
                    text = "Songs",
                    style = MaterialTheme.typography.titleLargeEmphasized.copy(fontWeight = FontWeight.SemiBold),
                )

                Spacer(Modifier.weight(1f))
                when {
                    isAnyDownloading -> {
                        IconButton(
                            onClick = {

                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_downloading_24),
                                contentDescription = "Downloaded"
                            )
                        }
                    }

                    allDownloaded -> {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    dataViewModel.removeDownloads(songs)
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_download_done_24),
                                contentDescription = "Downloaded"
                            )
                        }
                    }

                    else -> {
                        IconButton(
                            onClick = {
                                dataViewModel.addDownloads(songs)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_download_24),
                                contentDescription = "Download"
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.playShuffledPlaylist(songs)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle"
                    )
                }

                IconButton(
                    onClick = {
                        showAlbumOptionsBottomSheet.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options"
                    )
                }
            }
        }
        if (isLoading) {
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
                    onClick = {
                        scope.launch {
                            viewModel.playPlaylist(songs, it)
                        }
                    },
                    onLongClick = {
                        longClickSong = it
                        showSongDetailBottomSheet.value = true
                    }
                )
            }
        }
        if (showControlBar)
            item {
                Spacer(Modifier.height(70.dp))
            }
    }
    if (showSongDetailBottomSheet.value && longClickSong != null) {
        Log.i("SongsScreen", "Showing bottom sheet")
        Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
        SongOptionsBottomSheet(
            song = longClickSong!!,
            onDismiss = { showSongDetailBottomSheet.value = false },
            viewModel = viewModel,
            database = database,
            navController = navController
        )
    }
    if (showAlbumOptionsBottomSheet.value) {
        AlbumOptionsBottomSheet(
            albumId = albumId,
            albumSongs = songs,
            onDismiss = { showAlbumOptionsBottomSheet.value = false },
            viewModel = viewModel,
            database = database,
            navController = navController
        )
    }
}