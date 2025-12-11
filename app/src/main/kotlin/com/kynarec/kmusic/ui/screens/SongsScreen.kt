package com.kynarec.kmusic.ui.screens

import android.os.Parcelable
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SongComponent
import com.kynarec.kmusic.ui.components.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SortOption(
    val text: String,
): Parcelable



@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    database: KmusicDatabase
) {
    val context = LocalContext.current

    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val sortOptions = listOf(
        SortOption("All"),
        SortOption("Favorites"),
        SortOption("Listened to"),
    )
//    val selectedSortOption = rememberSaveable { mutableStateOf(SortOption("Listened to"))}
    val selectedSortOption = viewModel.uiState.collectAsStateWithLifecycle().value.songsSortOption

    val sortedSongs = when(selectedSortOption.text) {
        "All" -> database.songDao().getAllSongsFlow().collectAsStateWithLifecycle(emptyList())
        "Favorites" -> database.songDao().getFavouritesSongFlow().collectAsStateWithLifecycle(emptyList())
        "Listened to" -> database.songDao().getSongsFlowWithPlaytime().collectAsStateWithLifecycle(emptyList())
        else -> {database.songDao().getSongsFlowWithPlaytime().collectAsStateWithLifecycle(emptyList())}
    }


    rememberCoroutineScope()


    Column(
        Modifier.fillMaxSize()
    ) {
        LazyRow(
            Modifier.fillMaxWidth()
        ) {
            items(sortOptions) { sortOption ->
                Button(
                    onClick = {
                        viewModel.setSortOption(sortOption)
                    },
                    enabled = selectedSortOption != sortOption,
                    modifier = Modifier.padding(horizontal = 2.dp)

                ) {
                    Text(text = sortOption.text)
                }
            }
        }

        LazyColumn {
            items(
                sortedSongs.value,
                key = { song -> song.id }
            ) { song ->
                val song = song
                // Create stable onClick callback
                val onSongClick = remember(song.id) {
                    {
                        viewModel.playPlaylist(sortedSongs.value,song)
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
            SongOptionsBottomSheet(
                songId = longClickSong!!.id,
                onDismiss = { showBottomSheet.value = false },
                viewModel = viewModel,
                database = database
            )
        }
    }
}