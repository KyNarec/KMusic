package com.kynarec.kmusic.ui.screens.artist

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.data.db.entities.Artist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.innertube.ArtistPage
import com.kynarec.kmusic.service.innertube.NetworkResult
import com.kynarec.kmusic.service.innertube.getArtist
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.AlbumListScreen
import com.kynarec.kmusic.ui.SongListScreen
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.components.album.AlbumComponent
import com.kynarec.kmusic.ui.components.album.AlbumComponentSkeleton
import com.kynarec.kmusic.ui.components.artist.ArtistOptionsBottomSheet
import com.kynarec.kmusic.ui.components.song.SongComponent
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.screens.album.AlbumListScreen
import com.kynarec.kmusic.ui.screens.song.SongListScreen
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.shimmerEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArtistDetailScreen(
    modifier: Modifier = Modifier,
    artistId: String,
    viewModel: MusicViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator()
    val isSinglePane =
        listDetailNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden

    var artistDestination by remember { mutableStateOf(ArtistDestination.Overview) }

    BackHandler() {
        if (artistDestination != ArtistDestination.Overview)
            artistDestination = ArtistDestination.Overview
        else navController.popBackStack()
    }

    var artist by retain { mutableStateOf<Artist?>(null) }

    var songs by retain { mutableStateOf(emptyList<Song>()) }
    var albums by retain { mutableStateOf(emptyList<AlbumPreview>()) }
    var singlesAndEps by retain { mutableStateOf(emptyList<AlbumPreview>()) }
    var artistPage by retain { mutableStateOf(emptyList<ArtistPage>()) }

    var isLoading by retain { mutableStateOf(true) }
    var isRefreshing by retain { mutableStateOf(false) }

    var longClickSong by retain { mutableStateOf<Song?>(null) }
    val showArtistOptionsBottomSheet = retain { mutableStateOf(false) }
    val showSongDetailBottomSheet = retain { mutableStateOf(false) }

    var readMore by retain { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            when (val result = getArtist(artistId)) {
                is NetworkResult.Success -> {
                    val fetchedArtistPage = result.data
                    artistPage = emptyList()
                    albums = emptyList()
                    singlesAndEps = emptyList()

                    artistPage = artistPage + fetchedArtistPage
                    fetchedArtistPage.topSongs.forEach {
                        songs = songs + it
                    }
                    fetchedArtistPage.albums.forEach {
                        albums = albums + it
                    }
                    fetchedArtistPage.singlesAndEps.forEach {
                        singlesAndEps = singlesAndEps + it
                    }
                    artist = fetchedArtistPage.artist
                    database.artistDao().upsertArtist(artist!!)
                    isLoading = false
                }

                is NetworkResult.Failure.NetworkError -> {
                    SmartMessage(
                        "No Internet", PopupType.Error, false, context
                    )
                }

                is NetworkResult.Failure.ParsingError -> {
                    SmartMessage(
                        "Parsing Error", PopupType.Error, false, context
                    )
                }

                is NetworkResult.Failure.NotFound -> {
                    SmartMessage(
                        "Album not found", PopupType.Error, false, context
                    )
                }
            }
        }
    }

    fun handleRefresh() {
        scope.launch(Dispatchers.IO) {
            isRefreshing = true
            when (val result = getArtist(artistId)) {
                is NetworkResult.Success -> {
                    val fetchedArtistPage = result.data
                    artistPage = emptyList()
                    albums = emptyList()
                    singlesAndEps = emptyList()
                    songs = emptyList()

                    artistPage = artistPage + fetchedArtistPage
                    fetchedArtistPage.topSongs.forEach {
                        songs = songs + it
                    }
                    fetchedArtistPage.albums.forEach {
                        albums = albums + it
                    }
                    fetchedArtistPage.singlesAndEps.forEach {
                        singlesAndEps = singlesAndEps + it
                    }
                    artist = fetchedArtistPage.artist
                    database.artistDao().upsertArtist(artist!!)
                    isLoading = false
                    isRefreshing = false

                }

                is NetworkResult.Failure.NetworkError -> {
                    SmartMessage(
                        "No Internet", PopupType.Error, false, context
                    )
                    isRefreshing = false
                }

                is NetworkResult.Failure.ParsingError -> {
                    SmartMessage(
                        "Parsing Error", PopupType.Error, false, context
                    )
                    isRefreshing = false
                }

                is NetworkResult.Failure.NotFound -> {
                    SmartMessage(
                        "Album not found", PopupType.Error, false, context
                    )
                    isRefreshing = false
                }
            }
        }
    }
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { handleRefresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        NavigableListDetailPaneScaffold(
            navigator = listDetailNavigator,
            listPane = {
                AnimatedPane(
                    modifier = if (isSinglePane) Modifier.fillMaxSize() else Modifier.fillMaxWidth(
                        0.5f
                    )
                ) {
                    Crossfade(
                        targetState = isLoading,
                        animationSpec = tween(durationMillis = 400),
                        label = "ArtistPageCrossfade"
                    ) { loading ->
                        if (loading) {
                            LazyColumn(
                                modifier = modifier.fillMaxSize()
                            ) {
                                artistHeaderSkeleton()
                                if (isSinglePane) artistContentListSkeleton()
                                if (showControlBar)
                                    item {
                                        Spacer(Modifier.height(75.dp))
                                    }
                            }
                        } else {
                            LazyColumn(
                                modifier = modifier.fillMaxSize()
                            ) {
                                artistHeader(
                                    artist = artist,
                                    readMore = readMore,
                                    onReadMoreToggle = { readMore = !readMore }
                                )

                                if (isSinglePane) {
                                    artistContentList(
                                        navController = navController,
                                        viewModel = viewModel,
                                        songs = songs,
                                        albums = albums,
                                        singlesAndEps = singlesAndEps,
                                        artistPage = artistPage,
                                        showArtistOptionsBottomSheet = {
                                            showArtistOptionsBottomSheet.value = it
                                        },
                                        setLongClickSong = { longClickSong = it },
                                        showSongDetailBottomSheet = {
                                            showSongDetailBottomSheet.value = it
                                        },
                                        onShowSongList = {
                                            navController.navigate(
                                                SongListScreen(
                                                    browseId = artistPage.first().topSongsBrowseId,
                                                    browseParams = artistPage.first().topSongsParams
                                                )
                                            )
                                        },
                                        onShowAlbumList = {
                                            navController.navigate(
                                                AlbumListScreen(
                                                    browseId = artistPage.first().albumsBrowseId,
                                                    browseParams = artistPage.first().albumsParams

                                                )
                                            )
                                        },
                                        showSinglesAndEpsList = {
                                            navController.navigate(
                                                AlbumListScreen(
                                                    browseId = artistPage.first().singlesAndEpsBrowseId,
                                                    browseParams = artistPage.first().singlesAndEpsBrowseId
                                                )
                                            )
                                        }
                                    )
                                }

                                if (showControlBar)
                                    item {
                                        Spacer(Modifier.height(75.dp))
                                    }

                            }
                        }
                    }
                }
            },
            detailPane = {
                if (!isSinglePane) {
                    when (artistDestination) {
                        ArtistDestination.Overview -> {
                            AnimatedPane(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Crossfade(
                                    targetState = isLoading,
                                    animationSpec = tween(durationMillis = 400),
                                    label = "OverviewCrossfade"
                                ) { loading ->
                                    LazyColumn {
                                        if (loading) {
                                            artistContentListSkeleton()
                                            if (showControlBar)
                                                item {
                                                    Spacer(Modifier.height(75.dp))
                                                }
                                        } else {
                                            artistContentList(
                                                navController = navController,
                                                viewModel = viewModel,
                                                songs = songs,
                                                albums = albums,
                                                singlesAndEps = singlesAndEps,
                                                artistPage = artistPage,
                                                showArtistOptionsBottomSheet = {
                                                    showArtistOptionsBottomSheet.value = it
                                                },
                                                setLongClickSong = { longClickSong = it },
                                                showSongDetailBottomSheet = {
                                                    showSongDetailBottomSheet.value = it
                                                },
                                                onShowSongList = {
                                                    artistDestination =
                                                        ArtistDestination.SongListScreen
                                                },
                                                onShowAlbumList = {
                                                    artistDestination =
                                                        ArtistDestination.AlbumListScreen
                                                },
                                                showSinglesAndEpsList = {
                                                    artistDestination =
                                                        ArtistDestination.SingleAndEpListScreen

                                                }
                                            )
                                            if (showControlBar)
                                                item {
                                                    Spacer(Modifier.height(75.dp))
                                                }
                                        }
                                    }
                                }
                            }
                        }

                        ArtistDestination.SongListScreen -> {
                            AnimatedPane() {
                                SongListScreen(
                                    navController = navController,
                                    browseId = artistPage.first().topSongsBrowseId,
                                    browseParams = artistPage.first().topSongsParams,
                                    backIconButton = {
                                        IconButton(
                                            onClick = {
                                                artistDestination = ArtistDestination.Overview
                                            }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Rounded.ArrowBack,
                                                contentDescription = "Close"
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        ArtistDestination.AlbumListScreen -> {
                            AnimatedPane() {
                                AlbumListScreen(
                                    navController = navController,
                                    browseId = artistPage.first().albumsBrowseId,
                                    browseParams = artistPage.first().albumsParams,
                                    title = "Albums",
                                    backIconButton = {
                                        IconButton(
                                            onClick = {
                                                artistDestination = ArtistDestination.Overview
                                            }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Rounded.ArrowBack,
                                                contentDescription = "Close"
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        ArtistDestination.SingleAndEpListScreen -> {
                            AnimatedPane() {
                                AlbumListScreen(
                                    navController = navController,
                                    browseId = artistPage.first().singlesAndEpsBrowseId,
                                    browseParams = artistPage.first().singlesAndEpsParams,
                                    title = "Single & Eps",
                                    backIconButton = {
                                        IconButton(
                                            onClick = {
                                                artistDestination = ArtistDestination.Overview
                                            }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Rounded.ArrowBack,
                                                contentDescription = "Close"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
        )
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

    if (showArtistOptionsBottomSheet.value) {
        ArtistOptionsBottomSheet(
            artistId = artist?.id ?: "",
            onDismiss = { showArtistOptionsBottomSheet.value = false },
            viewModel = viewModel,
            database = database,
            navController = navController
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.artistHeader(
    artist: Artist?,
    readMore: Boolean,
    onReadMoreToggle: () -> Unit
) {

    item {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = artist?.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                imageLoader = LocalContext.current.imageLoader,
                contentScale = ContentScale.FillWidth
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
                MarqueeBox(
                    contentAlignment = Alignment.Center,
                    text = artist?.name ?: "NA",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MarqueeBox(
                    contentAlignment = Alignment.Center,
                    text = "${artist?.subscriber} Followers",
                )
            }
        }
    }

    if (artist?.description?.isNotEmpty() == true) {
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
                        artist.description.take(
                            if (artist.description.length >= 100
                            ) 100 else artist.description.length
                        ).plus("...")
                    } else {
                        artist.description
                    },
                    style = TextStyle.Default.copy(textAlign = TextAlign.Justify),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
//                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .clickable { onReadMoreToggle() },
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.artistHeaderSkeleton() {
    item {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )
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
                Text(
                    text = "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }
    }
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
                text = "",
                style = TextStyle.Default.copy(textAlign = TextAlign.Justify),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
                    .shimmerEffect(),
                minLines = 3
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.artistContentList(
    navController: NavHostController,
    viewModel: MusicViewModel,
    songs: List<Song>,
    albums: List<AlbumPreview>,
    singlesAndEps: List<AlbumPreview>,
    artistPage: List<ArtistPage>,
    showArtistOptionsBottomSheet: (Boolean) -> Unit,
    setLongClickSong: (Song) -> Unit,
    showSongDetailBottomSheet: (Boolean) -> Unit,
    onShowSongList: () -> Unit,
    onShowAlbumList: () -> Unit,
    showSinglesAndEpsList: () -> Unit
) {
    item {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarqueeBox(
                boxModifier = Modifier
                    .weight(1f),
                text = "Top Songs",
                style = MaterialTheme.typography.titleLargeEmphasized.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )

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
                    showArtistOptionsBottomSheet(true)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }

            if (artistPage.isNotEmpty() && artistPage.first().topSongsBrowseId.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onShowSongList()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Show all Songs"
                    )
                }
            }
        }
    }

    items(songs) {
        val scope = rememberCoroutineScope()
        SongComponent(
            song = it,
            onClick = {
                scope.launch {
                    viewModel.playPlaylist(songs, it)
                }
            },
            onLongClick = {
                setLongClickSong(it)
                showSongDetailBottomSheet(true)
            }
        )
    }

    item {
        if (artistPage.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarqueeBox(
                    boxModifier = Modifier
                        .weight(1f),
                    text = "Albums",
                    style = MaterialTheme.typography.titleLargeEmphasized.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                )

                if (artistPage.first().albumsBrowseId.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onShowAlbumList()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Show all Albums"
                        )
                    }
                }
            }
        }
    }
    item {
        LazyRow() {
            items(albums) {
                AlbumComponent(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(100.dp),
                    albumPreview = it,
                    onClick = {
                        navController.navigate(
                            AlbumDetailScreen(
                                it.id
                            )
                        )
                    }
                )
            }
        }
    }

    item {
        if (artistPage.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarqueeBox(
                    boxModifier = Modifier
                        .weight(1f),
                    text = "Singles & EPs",
                    style = MaterialTheme.typography.titleLargeEmphasized.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                )

                if (artistPage.first().singlesAndEpsBrowseId.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            showSinglesAndEpsList()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Show all Singles & EPs"
                        )
                    }
                }
            }
        }
    }

    item {
        LazyRow() {
            items(singlesAndEps) {
                AlbumComponent(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(100.dp),
                    albumPreview = it,
                    onClick = {
                        navController.navigate(
                            AlbumDetailScreen(
                                it.id
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.artistContentListSkeleton() {
    item {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarqueeBox(
                boxModifier = Modifier
                    .weight(1f),
                text = "Top Songs",
                style = MaterialTheme.typography.titleLargeEmphasized.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )

            IconButton(
                onClick = {
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle"
                )
            }

            IconButton(
                onClick = {
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
            IconButton(
                onClick = {
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Show all Songs"
                )
            }
        }
    }
    items(5) {
        SongComponentSkeleton()
    }

    item {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarqueeBox(
                boxModifier = Modifier
                    .weight(1f),
                text = "Albums",
                style = MaterialTheme.typography.titleLargeEmphasized.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )
            IconButton(
                onClick = {
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Show all Songs"
                )
            }
        }
    }

    item {
        LazyRow {
            items(8) {
                AlbumComponentSkeleton(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(horizontal = 4.dp)
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
            MarqueeBox(
                boxModifier = Modifier
                    .weight(1f),
                text = "Singles & EPs",
                style = MaterialTheme.typography.titleLargeEmphasized.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )
            IconButton(
                onClick = {
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Show all Songs"
                )
            }
        }
    }

    item {
        LazyRow {
            items(8) {
                AlbumComponentSkeleton(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

enum class ArtistDestination {
    Overview,
    SongListScreen,
    AlbumListScreen,
    SingleAndEpListScreen
}