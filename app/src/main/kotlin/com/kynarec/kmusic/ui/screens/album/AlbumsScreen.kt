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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.components.album.AlbumComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.rememberColumnCount
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val albums by database.albumDao().getFavouritesAlbumsFlow().collectAsStateWithLifecycle(null)
    val isLoading by remember { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

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
        } else if (albums != null){
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding),
                columns = GridCells.Fixed(rememberColumnCount())
            ) {
                items(albums!!, key = { it.id }) { album ->
                    AlbumComponent(
                        albumPreview = AlbumPreview(
                            id = album.id,
                            title = album.title,
                            artist = album.artist,
                            year = album.year,
                            thumbnail = album.thumbnailUrl
                        ),
                        onClick = {
                            navController.navigate(AlbumDetailScreen(album.id))
                        },
                    )
                }

            }
        }
    }
}