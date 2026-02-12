package com.kynarec.kmusic.ui.screens.album

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.innertube.NetworkResult
import com.kynarec.kmusic.service.innertube.browseAlbums
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.components.album.AlbumComponent
import com.kynarec.kmusic.ui.components.album.AlbumComponentSkeleton
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumListScreen(
    modifier: Modifier = Modifier,
    browseId: String,
    browseParams: String,
    navController: NavHostController,
    viewModel: MusicViewModel = koinActivityViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var albums by remember { mutableStateOf(emptyList<AlbumPreview>()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            when (val result = browseAlbums(browseId, browseParams)) {
                is NetworkResult.Success -> {
                    albums = result.data
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
                        "List not found", PopupType.Error, false, context
                    )
                }
            }
        }
    }
    fun handleRefresh() {
        isRefreshing = true
        scope.launch(Dispatchers.IO) {
            when (val result = browseAlbums(browseId, browseParams)) {
                is NetworkResult.Success -> {
                    albums = result.data
                    isRefreshing = false
                    isLoading = false
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
                        "List not found", PopupType.Error, false, context
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
        Crossfade(
            targetState = isLoading,
            animationSpec = tween(durationMillis = 400),
            label = "AlbumCrossfade"
        ) { loading ->
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
                if (loading) {
                  items(15) {
                      AlbumComponentSkeleton()
                  }
                } else {
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
}