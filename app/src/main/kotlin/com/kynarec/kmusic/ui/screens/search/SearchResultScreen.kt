@file:kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.kynarec.kmusic.ui.screens.search

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.data.db.entities.ArtistPreview
import com.kynarec.kmusic.data.db.entities.PlaylistPreview
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.innertube.searchAlbums
import com.kynarec.kmusic.service.innertube.searchArtists
import com.kynarec.kmusic.service.innertube.searchCommunityPlaylists
import com.kynarec.kmusic.service.innertube.searchSongsFlow
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.ArtistDetailScreen
import com.kynarec.kmusic.ui.PlaylistOnlineDetailScreen
import com.kynarec.kmusic.ui.SearchScreen
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.components.SortSection
import com.kynarec.kmusic.ui.components.album.AlbumComponent
import com.kynarec.kmusic.ui.components.artist.ArtistComponent
import com.kynarec.kmusic.ui.components.playlist.PlaylistComponent
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.screens.song.SortOption
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(
    UnstableApi::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SearchResultScreen(
    query: String,
    viewModel: MusicViewModel = koinActivityViewModel(),
    navController: NavHostController,
    database: KmusicDatabase = koinInject()
) {
    LocalContext.current
    var initialLoading by remember { mutableStateOf(true) }
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var albums by remember { mutableStateOf(emptyList<AlbumPreview>()) }
    var artists by remember { mutableStateOf(emptyList<ArtistPreview>()) }
    var playlists by remember { mutableStateOf(emptyList<PlaylistPreview>()) }

    var isLoading by remember { mutableStateOf(true) }

    val showBottomSheet = remember { mutableStateOf(false) }
    var longClickSong by remember { mutableStateOf<Song?>(null) }
    val selectedSearchParam = viewModel.uiState.collectAsStateWithLifecycle().value.searchParam

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    val searchParams = listOf(
        SortOption("Song"),
        SortOption("Album"),
        SortOption("Artist"),
        SortOption("Playlist"),
        SortOption("Videos"),
        SortOption("Podcasts"),
    )

    LaunchedEffect(query, selectedSearchParam) {
        when (selectedSearchParam.text) {
            "Song" -> {
                if (songs.isEmpty()) {
                    isLoading = true
                    Log.i("SearchResultScreen", "isLoading is now true")
                    songs = emptyList()

                    searchSongsFlow(query)
                        .flowOn(Dispatchers.IO)
                        .collect { song ->
                            songs = songs + song // progressively add each song
                            initialLoading = false
                            isLoading = false
                        }

                    if (songs.isEmpty()) {
                        Log.w("SearchResultScreen", "No results found for Song '$query'.")
                    }
                }
            }

            "Album" -> {
                if (albums.isEmpty()) {
                    isLoading = true
                    Log.i("SearchResultScreen", "isLoading is now true")
                    albums = emptyList()

                    searchAlbums(query)
                        .flowOn(Dispatchers.IO)
                        .collect { album ->
                            albums = albums + album
                            initialLoading = false
                            isLoading = false
                        }

                    if (albums.isEmpty()) {
                        Log.w("SearchResultScreen", "No results found for Album '$query'.")
                    }
                }
            }

            "Artist" -> {
                if (artists.isEmpty()) {
                    isLoading = true
                    Log.i("SearchResultScreen", "isLoading is now true")
                    artists = emptyList()

                    searchArtists(query)
                        .flowOn(Dispatchers.IO)
                        .collect {
                            artists = artists + it
                            initialLoading = false
                            isLoading = false
                        }
                }
            }

            "Playlist" -> {
                if (playlists.isEmpty()) {
                    isLoading = true
                    Log.i("SearchResultScreen", "isLoading is now true")
                    playlists = emptyList()

                    searchCommunityPlaylists(query)
                        .flowOn(Dispatchers.IO)
                        .collect {
                            playlists = playlists + it
                            initialLoading = false
                            isLoading = false
                        }
                }
            }

            else -> {
                initialLoading = false
            }
        }
    }



    when {
        initialLoading -> {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularWavyProgressIndicator()
            }
        }

        else -> {
            Column(
                Modifier.fillMaxSize()
            ) {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MarqueeBox(
                        text = query,
                        style = MaterialTheme.typography.titleLarge,
                        boxModifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    IconButton(
                        onClick = {
                            navController.navigate(SearchScreen(query))
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
                SortSection(
                    sortOptions = searchParams,
                    selectedSortOption = selectedSearchParam,
                    onOptionSelected = {
                        viewModel.setSearchParam(it)
                    }
                )
                AnimatedContent(
                    targetState = selectedSearchParam.text
                ) { targetState ->
                    when (targetState) {
                        "Song" -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isLoading) {
                                    LazyColumn(
                                        Modifier.fillMaxSize()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        items(30) {
                                            SongComponentSkeleton()
                                        }
                                    }
//                                    Box(
//                                        contentAlignment = Alignment.TopCenter,
//                                        modifier = Modifier.fillMaxSize()
//                                            .padding(vertical = 8.dp)
//                                    ) {
//                                        CircularWavyProgressIndicator()
//                                    }
                                } else {
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
                                                    Log.i(
                                                        "PlayerControlBar",
                                                        "Song clicked: ${song.title}"
                                                    )
                                                    Log.i(
                                                        "PlayerControlBar",
                                                        "Song thumbnail: ${song.thumbnail}"
                                                    )

                                                    viewModel.playSongByIdWithRadio(song)
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
                                        if (showControlBar)
                                            item {
                                                Spacer(Modifier.height(70.dp))
                                            }
                                    }
                                }
                            }
                        }

                        "Album" -> {
                            Column {
                                if (isLoading) {
                                    Box(
                                        contentAlignment = Alignment.TopCenter,
                                        modifier = Modifier.fillMaxSize()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        CircularWavyProgressIndicator()
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp),
                                        contentPadding = PaddingValues(
                                            top = 8.dp,
                                            bottom = bottomPadding
                                        ),
                                        columns = GridCells.Adaptive(minSize = 100.dp)
                                    ) {
                                        items(albums, key = { it.id }) { album ->
                                            AlbumComponent(
                                                albumPreview = album,
                                                navController = navController,
                                                onClick = {
                                                    navController.navigate(AlbumDetailScreen(album.id))
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "Artist" -> {
                            Column {
                                if (isLoading) {
                                    Box(
                                        contentAlignment = Alignment.TopCenter,
                                        modifier = Modifier.fillMaxSize()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        CircularWavyProgressIndicator()
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp),
                                        contentPadding = PaddingValues(
                                            top = 8.dp,
                                            bottom = bottomPadding
                                        ),
                                        columns = GridCells.Adaptive(minSize = 100.dp)
                                    ) {
                                        items(artists, key = { it.id }) { artist ->
                                            ArtistComponent(
                                                artistPreview = artist,
                                                onClick = {
                                                    navController.navigate(ArtistDetailScreen(artist.id))
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "Playlist" -> {
                            Column {
                                if (isLoading) {
                                    Box(
                                        contentAlignment = Alignment.TopCenter,
                                        modifier = Modifier.fillMaxSize()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        CircularWavyProgressIndicator()
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp),
                                        contentPadding = PaddingValues(
                                            top = 8.dp,
                                            bottom = bottomPadding
                                        ),
                                        columns = GridCells.Adaptive(minSize = 100.dp)
                                    ) {
                                        items(playlists, key = { it.id }) { playlist ->
                                            PlaylistComponent(
                                                playlistPreview = playlist,
                                                onClick = {
                                                    navController.navigate(PlaylistOnlineDetailScreen(playlist.id, playlist.thumbnail))
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            Box(
                                contentAlignment = Alignment.TopCenter,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularWavyProgressIndicator()
                            }
                        }
                    }
                }

            }


            if (showBottomSheet.value && longClickSong != null) {
                Log.i("SongsScreen", "Showing bottom sheet")
                Log.i("SongsScreen", "Title = ${longClickSong!!.title}")
                viewModel.maybeAddSongToDB(longClickSong!!)
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
}