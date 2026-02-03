package com.kynarec.kmusic.ui.screens.album

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.service.innertube.browseAlbums
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.components.album.AlbumComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
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
    var albums by remember { mutableStateOf(emptyList<AlbumPreview>()) }
    var isLoading by remember { mutableStateOf(true) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    LaunchedEffect(Unit) {
        if (albums.isEmpty()) {
            isLoading = true
            browseAlbums(browseId, browseParams).collect {
                albums = albums + it
            }
            isLoading = false
        }
    }

    Column(
        modifier.fillMaxSize()
    ) {
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