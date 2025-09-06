package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.R

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
        NavigationDestination("Artists", { Icon(painterResource(androidx.media3.session.R.drawable.media3_icon_artist), contentDescription = "Artists") }),
        NavigationDestination("Albums", { Icon(painterResource(R.drawable.album), contentDescription = "Albums") }),
        NavigationDestination("Playlists", { Icon(painterResource(R.drawable.library), contentDescription = "Playlists") })
    )

    var selectedDestination by rememberSaveable { mutableIntStateOf(0) }
//    Column (
//        modifier = Modifier.width(50.dp)
//    ){
//        destinations.forEachIndexed {
//            index, destination ->
//            destination.icon()
//        }
//    }
    Column {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            destinations.forEachIndexed { index, destination ->
                NavigationRailItem(
                    modifier = Modifier.graphicsLayer { rotationZ =  270f }
                        .padding(0.dp,14.dp),
                    selected = selectedDestination == index,
                    onClick = { selectedDestination = index },
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

@Preview(showBackground = true)
@Composable
fun ThemedNavigationRailPreview() {
    ThemedNavigationRail()
}