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
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.LibraryAction
import com.kynarec.kmusic.ui.viewModels.LibraryViewModel
import kotlinx.parcelize.Parcelize
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

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
    libraryViewModel: LibraryViewModel = koinActivityViewModel(),
    appViewModel: AppViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val context = LocalContext.current

    val showControlBar = appViewModel.state.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()

    val sortOptions = listOf(
        SortOption("All"),
        SortOption("Favorites"),
        SortOption("Listened"),
        SortOption("Downloads"),
    )
    val selectedSortOption = libraryViewModel.state.collectAsStateWithLifecycle().value.songsSortOption

    val allSongs = database.songDao().getAllSongsFlow().collectAsStateWithLifecycle(emptyList())

    val sortedSongs = when (selectedSortOption.text) {
        "All" -> allSongs
        "Favorites" -> database.songDao().getFavouritesSongFlow()
            .collectAsStateWithLifecycle(emptyList())

        "Listened to" -> database.songDao().getSongsFlowWithPlaytime()
            .collectAsStateWithLifecycle(emptyList())
        "Downloads" -> allSongs

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
            onOptionSelected = { libraryViewModel.onAction(LibraryAction.SetSortOption(it)) }
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
                            allSongs.value,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(LibraryAction.PlayPlaylist(sortedSongs.value, song))
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
                    val favourites = database.songDao().getFavouritesSongFlow()
                        .collectAsStateWithLifecycle(emptyList())
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        items(
                            favourites.value,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(LibraryAction.PlayPlaylist(sortedSongs.value, song))
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
                    val listenedTo by database.songDao().getSongsFlowWithPlaytime()
                        .collectAsStateWithLifecycle(emptyList())
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        items(
                            listenedTo,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(LibraryAction.PlayPlaylist(sortedSongs.value, song))
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
                "Downloads" -> {
                    val downloaded by allSongs
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        items(downloaded.filter { it.id in completedIds }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(LibraryAction.PlayPlaylist(sortedSongs.value, song))
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
                database = database,
                navController = navController
            )
        }
    }
}