package com.kynarec.kmusic.ui.screens.artist

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.ArtistPreview
import com.kynarec.kmusic.ui.ArtistDetailScreen
import com.kynarec.kmusic.ui.components.artist.ArtistComponent
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistsScreen(
    modifier: Modifier = Modifier,
    database: KmusicDatabase = koinInject(),
    viewModel: MusicViewModel = koinViewModel(),
    navController: NavHostController
) {
    val artists by database.artistDao().getFavouritesArtistsFlow().collectAsStateWithLifecycle(null)
    val isLoading by remember { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    Column(
        modifier.fillMaxSize()
    ) {
        if (isLoading) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                CircularWavyProgressIndicator()
            }
        } else if (artists != null){
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding),
                columns = GridCells.Adaptive(minSize = 120.dp)
            ) {
                items(artists!!, key = { it.id }) { artist ->
                    ArtistComponent(
                        artistPreview = ArtistPreview(
                            id = artist.id,
                            name = artist.name,
                            thumbnailUrl = artist.thumbnailUrl,
                            monthlyListeners = artist.subscriber ?: "",
                        ),
                        onClick = {
                            navController.navigate(ArtistDetailScreen(artist.id))
                        },
                        imageWith = 120,
                        imageHeight = 120
                    )
                }

            }
        }
    }
}