package com.kynarec.kmusic.ui.screens.song

import android.os.Parcelable
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.SortOrder
import com.kynarec.kmusic.ui.components.SortByBottomSheet
import com.kynarec.kmusic.ui.components.SortSection
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.LibraryAction
import com.kynarec.kmusic.ui.viewModels.LibraryViewModel
import com.kynarec.kmusic.ui.viewModels.SongsScreenAction
import com.kynarec.kmusic.ui.viewModels.SongsScreenViewModel
import kotlinx.parcelize.Parcelize
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel
import org.koin.compose.viewmodel.koinViewModel

@Parcelize
data class FilterOption(
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
    songsScreenViewModel: SongsScreenViewModel = koinViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val context = LocalContext.current

    val showControlBar = appViewModel.state.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }

    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()

    val filterOptions = listOf(
        FilterOption("All"),
        FilterOption("Favorites"),
        FilterOption("Listened"),
        FilterOption("Downloads"),
    )

    val state by songsScreenViewModel.state.collectAsStateWithLifecycle()

    val selectedFilterOption = state.songsFilterOption
    val downloadedSongs = state.allSongs.filter { it.id in completedIds }

    val sortedSongs = when (selectedFilterOption.text) {
        "Downloads" -> downloadedSongs
        else -> state.sortedSongs
    }



    Column(
        Modifier.fillMaxSize()
    ) {
        SortSection(
            filterOptions = filterOptions,
            selectedFilterOption = selectedFilterOption,
            onOptionSelected = { songsScreenViewModel.onAction(SongsScreenAction.ChangeSongsFilterOption(it)) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rotationAngle by animateFloatAsState(
                targetValue = if (state.songsSortOrder == SortOrder.Ascending) 0f else 180f,
                label = "SortOrderRotation",
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            )

            IconButton(
                onClick = { songsScreenViewModel.onAction(SongsScreenAction.ToggleSortOrder) }
            ) {
                Icon(
                    Icons.Rounded.ArrowUpward,
                    contentDescription = "sort",
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotationAngle
                    }
                )
            }

            Box(Modifier.clickable(onClick = { songsScreenViewModel.onAction(SongsScreenAction.ToggleSongsSortByBottomSheet) })) {
                Text(
                    state.songsSortOption.title,
                )
            }
            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { libraryViewModel.onAction(LibraryAction.PlayShuffled(sortedSongs))}
            ) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = "shuffle"
                )
            }
        }

        AnimatedContent(
            targetState = selectedFilterOption
        ) { targetState ->
            when (targetState.text) {
                "All" -> {
                    val listState = rememberLazyListState()

                    LaunchedEffect(state.songsSortOrder) {
                        listState.requestScrollToItem(0)
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding),
                        state = listState
                    ) {
                        items(
                            sortedSongs,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(
                                        LibraryAction.PlayPlaylist(
                                            sortedSongs,
                                            song
                                        )
                                    )
                                }
                            }

                            SongComponent(
                                modifier = Modifier.animateItem(),
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
                    val listState = rememberLazyListState()

                    LaunchedEffect(state.songsSortOrder) {
                        listState.requestScrollToItem(0)
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding),
                        state = listState
                    ) {
                        items(
                            sortedSongs,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(
                                        LibraryAction.PlayPlaylist(
                                            sortedSongs,
                                            song
                                        )
                                    )
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
                    val listState = rememberLazyListState()

                    LaunchedEffect(state.songsSortOrder) {
                        listState.requestScrollToItem(0)
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding),
                        state = listState
                    ) {
                        items(
                            sortedSongs,
                            key = { song -> song.id }
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(
                                        LibraryAction.PlayPlaylist(
                                            sortedSongs,
                                            song
                                        )
                                    )
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
                    val listState = rememberLazyListState()

                    LaunchedEffect(state.songsSortOrder) {
                        listState.requestScrollToItem(0)
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = bottomPadding),
                        state = listState
                    ) {
                        items(
                            sortedSongs
                        ) { song ->
                            val song = song
                            val onSongClick = remember(song.id) {
                                {
                                    libraryViewModel.onAction(
                                        LibraryAction.PlayPlaylist(
                                            sortedSongs,
                                            song
                                        )
                                    )
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

        if (state.showSongsSortByBottomSheet) {
            SortByBottomSheet(
                onClick = { songsScreenViewModel.onAction(SongsScreenAction.ChangeSongsSortOption(it)) },
                onDismiss = { songsScreenViewModel.onAction(SongsScreenAction.ToggleSongsSortByBottomSheet) }
            )
        }
    }
}