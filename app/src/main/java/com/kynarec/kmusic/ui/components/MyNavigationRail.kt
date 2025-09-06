package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview

// Define data for navigation destinations
data class NavigationDestination(
    val title: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedNavigationRail() {
    val destinations = listOf(
        NavigationDestination("Home", { Icon(Icons.Default.Home, contentDescription = "Home") }),
        NavigationDestination("Songs", { Icon(Icons.Default.MusicNote, contentDescription = "Songs") }),
        NavigationDestination("Artists", { Icon(Icons.Default.Person, contentDescription = "Artists") }),
        NavigationDestination("Albums", { Icon(Icons.Default.Album, contentDescription = "Albums") }),
        NavigationDestination("Playlists", { Icon(Icons.Default.PlaylistPlay, contentDescription = "Playlists") })
    )

    var selectedDestination by rememberSaveable { mutableIntStateOf(0) }

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        destinations.forEachIndexed { index, destination ->
            NavigationRailItem(
                selected = selectedDestination == index,
                onClick = { selectedDestination = index },
                icon = {
                    // Conditionally display the icon
                    if (selectedDestination == index) {
                        Box {
                            destination.icon()
                        }
                    }
                },
                label = {
                    Text(
                        text = destination.title,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = 90f
                        }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThemedNavigationRailPreview() {
    ThemedNavigationRail()
}