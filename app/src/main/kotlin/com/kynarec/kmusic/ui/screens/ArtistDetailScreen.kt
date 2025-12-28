package com.kynarec.kmusic.ui.screens

import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.innertube.ArtistPage
import com.kynarec.kmusic.service.innertube.getArtist
import com.kynarec.kmusic.ui.SongListScreen
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.components.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistDetailScreen(
    modifier: Modifier = Modifier,
    artistId: String,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val artistFlow by database.artistDao().getArtistByIdFlow(artistId)
        .collectAsStateWithLifecycle(null)

    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var allSongs by remember { mutableStateOf(emptyList<Song>()) }
    var artistPage by remember { mutableStateOf(emptyList<ArtistPage>())}

    var isLoading by remember { mutableStateOf(false) }

    var longClickSong by remember { mutableStateOf<Song?>(null) }
    val showSongDetailBottomSheet = remember { mutableStateOf(false) }

    var readMore by remember { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    LaunchedEffect(Unit) {
        if (artistFlow == null) {
            isLoading = true
            scope.launch {
                getArtist(artistId).collect { fetchedArtistPage ->
                    artistPage = emptyList()
                    artistPage = artistPage + fetchedArtistPage
                    fetchedArtistPage.topSongs.forEach {
                        songs = songs + it
                    }
                    Log.i("ArtistDetailScreen", "upserting artist")
                    withContext(Dispatchers.IO) {
                        database.artistDao().upsertArtist(fetchedArtistPage.artist)
                    }
                    Log.i("ArtistDetailScreen", "upserting artist done")

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
                    model = artistFlow?.thumbnailUrl,
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
                        text = artistFlow?.name ?: "NA",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ConditionalMarqueeText(
                        text = "${artistFlow?.subscriber} Followers",
                    )
                }
            }
        }

        if (artistFlow?.description?.isNotEmpty() == true) {
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

                    if (!readMore)
                        Text(
                            text = artistFlow?.description?.substring(
                                0,
                                if ((artistFlow?.description?.length
                                        ?: 0) >= 100
                                ) 100 else artistFlow?.description?.length ?: 0
                            ).plus("..."),
                            style = TextStyle.Default.copy(textAlign = TextAlign.Justify),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(1f)
                                .clickable {
                                    readMore = !readMore
                                }
                        )

                    if (readMore)
                        Text(
                            text = artistFlow?.description ?: "NA",
                            style = TextStyle.Default.copy(textAlign = TextAlign.Justify),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(1f)
                                .clickable {
                                    readMore = !readMore
                                }
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
                    text = "Top Songs",
                    style = MaterialTheme.typography.titleLargeEmphasized.copy(fontWeight = FontWeight.SemiBold),
                )

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = {
                        viewModel.playShuffledPlaylist(songs)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle"
                    )
                }

                IconButton(
                    onClick = {
//                        TODO(In extractors add browse top songs, use it here and give the fetched list of songs to the SongListScreen)
                        navController.navigate(
                            SongListScreen(
                                browseId = artistPage.first().topSongsBrowseId,
                                browseParams = artistPage.first().topSongsParams
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Show all Songs"
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
            songId = longClickSong!!.id,
            onDismiss = { showSongDetailBottomSheet.value = false },
            viewModel = viewModel,
            database = database
        )
    }
}
