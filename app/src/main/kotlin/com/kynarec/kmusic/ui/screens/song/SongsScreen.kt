package com.kynarec.kmusic.ui.screens.song

import android.os.Parcelable
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SortSection
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SortOption(
    val text: String,
) : Parcelable


@OptIn(
    UnstableApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@ExperimentalMaterial3ExpressiveApi
@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {
    val context = LocalContext.current

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val sortOptions = listOf(
        SortOption("All"),
        SortOption("Favorites"),
        SortOption("Listened"),
    )
    val selectedSortOption = viewModel.uiState.collectAsStateWithLifecycle().value.songsSortOption

    val sortedSongs = when (selectedSortOption.text) {
        "All" -> database.songDao().getAllSongsFlow().collectAsStateWithLifecycle(emptyList())
        "Favorites" -> database.songDao().getFavouritesSongFlow()
            .collectAsStateWithLifecycle(emptyList())

        "Listened to" -> database.songDao().getSongsFlowWithPlaytime()
            .collectAsStateWithLifecycle(emptyList())

        else -> {
            database.songDao().getSongsFlowWithPlaytime().collectAsStateWithLifecycle(emptyList())
        }
    }


    Column(
        Modifier.fillMaxSize()
    ) {
        SortSection(
            sortOptions = sortOptions,
            selectedSortOption = selectedSortOption,
            onOptionSelected = { viewModel.setSortOption(it) }
        )

        AnimatedContent(
            targetState = selectedSortOption
        ) { targetState ->
            when (targetState.text) {
                "All" -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        items(
                            sortedSongs.value,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    viewModel.playPlaylist(sortedSongs.value, song)
                                }
                            }

                            SongComponent(
                                song = song,
                                onClick = onSongClick,
                                onLongClick = {
                                    longClickSong = song
                                    showBottomSheet.value = true
                                }
                            )
                        }
                    }
                }

                "Favorites" -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        items(
                            sortedSongs.value,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    viewModel.playPlaylist(sortedSongs.value, song)
                                }
                            }

                            SongComponent(
                                song = song,
                                onClick = onSongClick,
                                onLongClick = {
                                    longClickSong = song
                                    showBottomSheet.value = true
                                }
                            )
                        }
                    }
                }

                "Listened" -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        items(
                            sortedSongs.value,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    viewModel.playPlaylist(sortedSongs.value, song)
                                }
                            }

                            SongComponent(
                                song = song,
                                onClick = onSongClick,
                                onLongClick = {
                                    longClickSong = song
                                    showBottomSheet.value = true
                                }
                            )
                        }
                    }
                }
            }
        }
        if (showBottomSheet.value && longClickSong != null) {
            Log.i("SongsScreen", "Showing bottom sheet")
            Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
            SongOptionsBottomSheet(
                song = longClickSong!!,
                onDismiss = { showBottomSheet.value = false },
                viewModel = viewModel,
                database = database,
                navController = navController
            )
        }
    }
}