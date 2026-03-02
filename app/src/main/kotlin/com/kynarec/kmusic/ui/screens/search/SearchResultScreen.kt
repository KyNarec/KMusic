@file:kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.kynarec.kmusic.ui.screens.search

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
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
import com.kynarec.kmusic.data.db.entities.toPlaylistOnlineDetailScreen
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.innertube.searchAlbums
import com.kynarec.kmusic.service.innertube.searchArtists
import com.kynarec.kmusic.service.innertube.searchCommunityPlaylists
import com.kynarec.kmusic.service.innertube.searchSongsFlow
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.ArtistDetailScreen
import com.kynarec.kmusic.ui.SearchScreen
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.components.SortSection
import com.kynarec.kmusic.ui.components.album.AlbumComponent
import com.kynarec.kmusic.ui.components.album.AlbumComponentSkeleton
import com.kynarec.kmusic.ui.components.artist.ArtistComponent
import com.kynarec.kmusic.ui.components.artist.ArtistComponentSkeleton
import com.kynarec.kmusic.ui.components.playlist.PlaylistComponent
import com.kynarec.kmusic.ui.components.playlist.PlaylistComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.screens.song.SortOption
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.rememberColumnCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var songs by retain { mutableStateOf(emptyList<Song>()) }
    var albums by retain { mutableStateOf(emptyList<AlbumPreview>()) }
    var artists by retain { mutableStateOf(emptyList<ArtistPreview>()) }
    var playlists by retain { mutableStateOf(emptyList<PlaylistPreview>()) }

    var isLoading by retain { mutableStateOf(true) }
    var isRefreshing by retain { mutableStateOf(false) }

    val showBottomSheet = retain { mutableStateOf(false) }
    var longClickSong by retain { mutableStateOf<Song?>(null) }
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

    val columnCount = rememberColumnCount()

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
                            isLoading = false
                        }

                    if (songs.isEmpty()) {
                        Log.w("SearchResultScreen", "No results found for Song '$query'.")
                        SmartMessage("No results found for Song '$query'", PopupType.Error, false, context)
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
                            isLoading = false
                        }

                    if (albums.isEmpty()) {
                        Log.w("SearchResultScreen", "No results found for Album '$query'.")
                        SmartMessage("No results found for Album '$query'", PopupType.Error, false, context)
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
                            isLoading = false
                        }
                    if (artists.isEmpty()) {
                        Log.w("SearchResultScreen", "No results found for Artist '$query'.")
                        SmartMessage("No results found for Artist '$query'", PopupType.Error, false, context)
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
                            isLoading = false
                        }
                    if (playlists.isEmpty()) {
                        Log.w("SearchResultScreen", "No results found for Playlist '$query'.")
                        SmartMessage("No results found for Playlist '$query'", PopupType.Error, false, context)
                    }
                }
            }
        }
    }

    suspend fun refresh() {
        isRefreshing = true
        when (selectedSearchParam.text) {
            "Song" -> {
                isLoading = true
                val refreshedSongsList = mutableListOf<Song>()

                searchSongsFlow(query)
                    .flowOn(Dispatchers.IO)
                    .collect { song ->
                        refreshedSongsList += song
                    }

                if (refreshedSongsList.isEmpty()) {
                    SmartMessage("No results found for query: '$query'", PopupType.Error, false, context)
                    delay(500)
                    if (!songs.isEmpty()) isLoading = false
                } else {
                    songs = refreshedSongsList
                    isLoading = false
                }
            }

            "Album" -> {
                isLoading = true

                val updatedAlbumList = mutableListOf<AlbumPreview>()
                searchAlbums(query)
                    .flowOn(Dispatchers.IO)
                    .collect { album ->
                        updatedAlbumList += album
                    }

                if (updatedAlbumList.isEmpty()) {
                    SmartMessage("No results found for query: '$query'", PopupType.Error, false, context)
                    delay(500)
                    if (!albums.isEmpty()) isLoading = false
                } else {
                    albums = updatedAlbumList
                    isLoading = false
                }
            }

            "Artist" -> {
                isLoading = true

                val refreshedArtistList = mutableListOf<ArtistPreview>()
                searchArtists(query)
                    .flowOn(Dispatchers.IO)
                    .collect {
                        refreshedArtistList += it
                    }

                if (refreshedArtistList.isEmpty()) {
                    SmartMessage("No results found for query: '$query'", PopupType.Error, false, context)
                    delay(500)
                    if (!artists.isEmpty()) isLoading = false
                } else {
                    artists = refreshedArtistList
                    isLoading = false
                }
            }

            "Playlist" -> {
                isLoading = true

                val refreshedPlaylistList = mutableListOf<PlaylistPreview>()
                searchCommunityPlaylists(query)
                    .flowOn(Dispatchers.IO)
                    .collect {
                        refreshedPlaylistList += it
                    }

                if (refreshedPlaylistList.isEmpty()) {
                    SmartMessage("No results found for query: '$query'", PopupType.Error, false, context)
                    delay(500)
                    if (!playlists.isEmpty()) isLoading = false
                } else {
                    playlists = refreshedPlaylistList
                    isLoading = false
                }
            }
        }
        isRefreshing = false
    }


    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { scope.launch(Dispatchers.IO) { refresh() } },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
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
                        Crossfade(
                            targetState = isLoading,
                            animationSpec = tween(durationMillis = 400),
                            label = "SongCrossfade"
                        ) { loading ->
                            if (loading) {
                                LazyColumn(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 8.dp)
                                ) {
                                    items(30) {
                                        SongComponentSkeleton()
                                    }

                                    if (showControlBar)
                                        item {
                                            Spacer(Modifier.height(70.dp))
                                        }
                                }
                            } else {
                                LazyColumn {
                                    items(
                                        count = songs.size,
                                        key = { index -> songs[index].id } // Add stable key!
                                    ) { index ->
                                        val song = songs[index]

                                        SongComponent(
                                            song = song,
                                            onClick = {
                                                Log.d("SongClick", "Song clicked: ${song.title}")
                                                viewModel.playSongByIdWithRadio(song)
                                            },
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
                        Crossfade(
                            targetState = isLoading,
                            animationSpec = tween(durationMillis = 400),
                            label = "SongCrossfade"
                        ) { loading ->
                            if (loading) {
                                LazyVerticalGrid(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    contentPadding = PaddingValues(
                                        top = 8.dp,
                                        bottom = bottomPadding
                                    ),
                                    columns = GridCells.Fixed(columnCount)
                                ) {
                                    items(20) {
                                        AlbumComponentSkeleton()
                                    }

                                    if (showControlBar)
                                        item {
                                            Spacer(Modifier.height(70.dp))
                                        }
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
                                    columns = GridCells.Fixed(columnCount)
                                ) {
                                    items(albums, key = { it.id }) { album ->
                                        AlbumComponent(
                                            albumPreview = album,
                                            onClick = {
                                                navController.navigate(AlbumDetailScreen(album.id))
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "Artist" -> {
                        Crossfade(
                            targetState = isLoading,
                            animationSpec = tween(durationMillis = 400),
                            label = "SongCrossfade"
                        ) { loading ->
                            if (loading) {
                                LazyVerticalGrid(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    contentPadding = PaddingValues(
                                        top = 8.dp,
                                        bottom = bottomPadding
                                    ),
                                    columns = GridCells.Fixed(columnCount)
                                ) {
                                    items(20) {
                                        ArtistComponentSkeleton()
                                    }

                                    if (showControlBar)
                                        item {
                                            Spacer(Modifier.height(70.dp))
                                        }
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
                                    columns = GridCells.Fixed(columnCount)
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
                        Crossfade(
                            targetState = isLoading,
                            animationSpec = tween(durationMillis = 400),
                            label = "SongCrossfade"
                        ) { loading ->
                            if (loading) {
                                LazyVerticalGrid(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    contentPadding = PaddingValues(
                                        top = 8.dp,
                                        bottom = bottomPadding
                                    ),
                                    columns = GridCells.Fixed(columnCount)
                                ) {
                                    items(12) {
                                        PlaylistComponentSkeleton()
                                    }

                                    if (showControlBar)
                                        item {
                                            Spacer(Modifier.height(70.dp))
                                        }
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
                                    columns = GridCells.Fixed(columnCount)
                                ) {
                                    items(playlists, key = { it.id }) { playlist ->
                                        PlaylistComponent(
                                            playlistPreview = playlist,
                                            onClick = {
                                                navController.navigate(
                                                    playlist.toPlaylistOnlineDetailScreen()
                                                )
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
}