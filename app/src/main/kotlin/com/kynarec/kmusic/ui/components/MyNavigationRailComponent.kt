package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.R
import com.kynarec.kmusic.ui.screens.AlbumsScreen
import com.kynarec.kmusic.ui.screens.ArtistsScreen
import com.kynarec.kmusic.ui.screens.HomeScreen
import com.kynarec.kmusic.ui.screens.PlaylistsScreen
import com.kynarec.kmusic.ui.screens.SongsScreen

// Define data for navigation destinations
data class NavigationDestination(
    val title: String,
    val icon: @Composable () -> Unit,
    val navigationScreen: Any
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNavigationRailComponent(
    navController: NavHostController,
    currentRoute: String?
) {

    val destinations = listOf(
        NavigationDestination("Home", { Icon(Icons.Default.Home, contentDescription = "Home") }, HomeScreen),
        NavigationDestination("Songs", { Icon(Icons.Default.MusicNote, contentDescription = "Songs") }, SongsScreen),
        NavigationDestination("Artists", { Icon(Icons.Default.People, contentDescription = "Artists") }, ArtistsScreen),
        NavigationDestination("Albums", { Icon(painterResource(R.drawable.album), contentDescription = "Albums") }, AlbumsScreen),
        NavigationDestination("Playlists", { Icon(painterResource(R.drawable.library), contentDescription = "Playlists") }, PlaylistsScreen)
    )

    var selectedDestination by rememberSaveable { mutableIntStateOf(0) }

    val isHomeScreen = remember(currentRoute) {
        currentRoute?.startsWith(HomeScreen::class.qualifiedName!!) == true
    }
    if (isHomeScreen) selectedDestination = 0

    val isSongsScreen = remember(currentRoute) {
        currentRoute?.startsWith(SongsScreen::class.qualifiedName!!) == true
    }
    if (isSongsScreen) selectedDestination = 1

    val isArtistsScreen = remember(currentRoute) {
        currentRoute?.startsWith(ArtistsScreen::class.qualifiedName!!) == true
    }
    if (isArtistsScreen) selectedDestination = 2

    val isAlbumsScreen = remember(currentRoute) {
        currentRoute?.startsWith(AlbumsScreen::class.qualifiedName!!) == true
    }
    if (isAlbumsScreen) selectedDestination = 3

    val isPlaylistsScreen = remember(currentRoute) {
        currentRoute?.startsWith(PlaylistsScreen::class.qualifiedName!!) == true
    }
    if (isPlaylistsScreen) selectedDestination = 4


    Column {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            destinations.forEachIndexed { index, destination ->
                NavigationRailItem(
                    modifier = Modifier
                        .graphicsLayer { rotationZ = 270f }
                        .padding(0.dp, 14.dp),
                    selected = selectedDestination == index,
                    onClick = {
                        if (selectedDestination != index) {
                            selectedDestination = index
                            navController.navigate(destination.navigationScreen)
                        }
                    },
                    icon = {
//                         Conditionally display the icon
                        if (selectedDestination == index) {
                            Box {
                                destination.icon()
                            }
                        }
                    },
                    label = {
                        Text(
                            text = destination.title,
//                            modifier = Modifier.graphicsLayer {
//                                rotationZ = 270f
//                            }
                        )
                    }
                )
            }
        }
    }
}

