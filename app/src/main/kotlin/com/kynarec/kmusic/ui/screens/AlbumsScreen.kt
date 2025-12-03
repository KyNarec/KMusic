package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.components.AlbumComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val albums by database.albumDao().getFavouritesAlbumsFlow().collectAsState(null)
    val isLoading by remember { mutableStateOf(false) }

    Column {
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
                contentPadding = PaddingValues(top = 8.dp),
                columns = GridCells.Adaptive(minSize = 100.dp)
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