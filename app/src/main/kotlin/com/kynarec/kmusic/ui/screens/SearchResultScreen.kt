@file:kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.kynarec.kmusic.ui.screens

import android.app.Application
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.service.innertube.searchSongsFlow
import com.kynarec.kmusic.ui.components.SongBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

@OptIn(UnstableApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SearchResultScreen(
    query: String,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    database: KmusicDatabase
) {
    LocalContext.current
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var isLoading by remember { mutableStateOf(true) }

    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    // Use LaunchedEffect to perform the side effect (data fetching)
    // The coroutine will be launched when the query changes.
    LaunchedEffect(query) {
        isLoading = true
        Log.i("SearchResultScreen", "isLoading is now true")
        songs = emptyList()

        searchSongsFlow(query)
            .flowOn(Dispatchers.IO)
            .collect { song ->
                songs = songs + song // progressively add each song
                isLoading = false
            }

        if (songs.isEmpty()) {
            Log.w("SearchResultScreen", "No results found for '$query'.")
        }
    }

    MaterialTheme.colorScheme.primaryContainer

    when {
        isLoading -> {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            ) {
                //CircularProgressIndicator() // The M3 Loading Indicator
                ContainedLoadingIndicator(
//                    color = Color(0xFF2B3233)
                )
            }
        }
        else -> {
            LazyColumn {
                items(
                    count = songs.size,
                    key = { index -> songs[index].id } // Add stable key!
                ) { index ->
                    val song = songs[index]

                    // Create stable onClick callback
                    val onSongClick = remember(song.id) {
                        {
                            Log.d("SongClick", "Song clicked: ${song.title}")
                            //viewModel.playSong(song)
                            Log.i("PlayerControlBar", "Song clicked: ${song.title}")
                            Log.i("PlayerControlBar", "Song thumbnail: ${song.thumbnail}")

                            viewModel.playSongByIdWithRadio(song)
                        }
                    }

                    SongComponent(
                        song = song,
                        onClick = onSongClick as () -> Unit,
                        onLongClick = {
                            longClickSong = song
                            showBottomSheet.value = true
                        }
                    )
                }
            }
            if (showBottomSheet.value && longClickSong != null) {
                Log.i("SongsScreen", "Showing bottom sheet")
                Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
                viewModel.maybeAddSongToDB(longClickSong!!)
                SongBottomSheet(
                    songId = longClickSong!!.id,
                    onDismiss = { showBottomSheet.value = false },
                    viewModel = viewModel,
                    database = database
                )
            }
        }
    }
}