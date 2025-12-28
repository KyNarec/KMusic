package com.kynarec.kmusic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.innertube.browseSongs
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.components.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongListScreen(
    modifier: Modifier = Modifier,
    browseId: String,
    browseParams: String,
    viewModel: MusicViewModel,
    database: KmusicDatabase
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var longClickSong by remember { mutableStateOf<Song?>(null) }
    val showSongDetailBottomSheet = remember { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (songs.isEmpty()) {
            isLoading = true
            browseSongs(browseId, browseParams)
                .collect {
                    songs = songs + it
                }
            isLoading = false
        }
    }

    LazyColumn(
        modifier.fillMaxSize()
    ) {
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
//                IconButton(
//                    onClick = {
//                        showAlbumOptionsBottomSheet.value = true
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.MoreVert,
//                        contentDescription = "More Options"
//                    )
//                }
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
            items(songs, key = { it.id }) {
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