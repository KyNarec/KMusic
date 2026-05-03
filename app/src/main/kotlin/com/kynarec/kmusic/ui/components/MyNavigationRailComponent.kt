package com.kynarec.kmusic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.R
import com.kynarec.kmusic.ui.screens.AlbumsScreen
import com.kynarec.kmusic.ui.screens.ArtistsScreen
import com.kynarec.kmusic.ui.screens.HomeScreen
import com.kynarec.kmusic.ui.screens.PlaylistsScreen
import com.kynarec.kmusic.ui.screens.SongsScreen

// Define data for navigation destinations
data class NavigationDestination(
    val title: String,
    val icon: @Composable (visible: Boolean) -> Unit,
    val navigationScreen: Any
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNavigationRailComponent(
    navController: NavHostController,
    currentRoute: String?
) {
    val destinations = listOf(
        NavigationDestination(
            "Home",
            { visible ->
                AnimatedNavIcon(visible) {
                    Icon(
                        Icons.Rounded.Home, contentDescription = "Home", modifier = Modifier
                            .vertical()
                            .rotate(-90f)
                            .padding(horizontal = 16.dp)
                            .visible(visible),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            HomeScreen
        ),
        NavigationDestination(
            "Songs",
            { visible ->
                AnimatedNavIcon(visible) {
                    Icon(
                        Icons.Rounded.MusicNote, contentDescription = "Songs", modifier = Modifier
                            .vertical()
                            .rotate(-90f)
                            .padding(horizontal = 16.dp)
                            .visible(visible),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            SongsScreen
        ),
        NavigationDestination(
            "Artists",
            { visible ->
                AnimatedNavIcon(visible) {
                    Icon(
                        Icons.Rounded.People, contentDescription = "Artists", modifier = Modifier
                            .vertical()
                            .rotate(-90f)
                            .padding(horizontal = 16.dp)
                            .visible(visible),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            ArtistsScreen
        ),
        NavigationDestination(
            "Albums",
            { visible ->
                AnimatedNavIcon(visible) {
                    Icon(
                        painterResource(R.drawable.album),
                        contentDescription = "Albums",
                        modifier = Modifier
                            .vertical()
                            .rotate(-90f)
                            .padding(horizontal = 16.dp)
                            .visible(visible),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer

                    )
                }
            },
            AlbumsScreen
        ),
        NavigationDestination(
            "Playlists",
            { visible ->
                AnimatedNavIcon(visible) {
                    Icon(
                        painterResource(R.drawable.library),
                        contentDescription = "Playlists",
                        modifier = Modifier
                            .vertical()
                            .rotate(-90f)
                            .padding(horizontal = 16.dp)
                            .visible(visible),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            PlaylistsScreen
        )
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

    LazyColumn(
        Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        itemsIndexed(destinations) { index, destination ->
            val selected = selectedDestination == index
            Row(
                modifier = Modifier
                    .padding(top = 12.dp, start = 6.dp, end = 0.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = {
                        if (selectedDestination != index) {
                            selectedDestination = index
                            navController.navigate(destination.navigationScreen)
                        }
                    }),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    Modifier
                        .size(24.dp, 24.dp)
                        .padding(2.dp)
                        .visible(selected),
                    contentAlignment = Alignment.Center
                ) {
                    destination.icon(selected)
                }

                Text(
                    destination.title, modifier = Modifier
                        .vertical()
                        .rotate(-90f)
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun NavRailPreview() {
    Scaffold() { paddingValues ->
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        Row(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MyNavigationRailComponent(navController, currentRoute)
        }
    }
}

fun Modifier.vertical(enabled: Boolean = true) =
    if (enabled)
        layout { measurable, constraints ->
            val c: Constraints = constraints.copy(maxWidth = Int.MAX_VALUE)
            val placeable = measurable.measure(c)

            layout(placeable.height, placeable.width) {
                placeable.place(
                    x = -(placeable.width / 2 - placeable.height / 2),
                    y = -(placeable.height / 2 - placeable.width / 2)
                )
            }
        }
    else this

@Composable
fun AnimatedNavIcon(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally { -it } + fadeIn(),
        exit = slideOutHorizontally { it } + fadeOut()
    ) {
        content()
    }
}