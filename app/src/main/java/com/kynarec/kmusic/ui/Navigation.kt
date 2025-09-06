package com.kynarec.kmusic.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.ui.screens.AlbumsScreen
import com.kynarec.kmusic.ui.screens.ArtistsScreen
import com.kynarec.kmusic.ui.screens.HomeScreen
import com.kynarec.kmusic.ui.screens.PlayerScreen
import com.kynarec.kmusic.ui.screens.PlaylistScreen
import com.kynarec.kmusic.ui.screens.SongsScreen
import com.kynarec.kmusic.ui.viewModels.SongViewModel
import com.kynarec.kmusic.ui.viewModels.SongViewModelFactory
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    navController: NavHostController
) {
    // Get the Application context using LocalContext.
    // This is safe to use in a Composable because it's tied to the Composable's scope.
    val application = LocalContext.current.applicationContext as Application

    // Use a custom ViewModel factory to inject the database DAO.
    val viewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(
            (application as MyApp).database.songDao()
        )
    )

//    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            HomeScreen()
        }

        composable<SongsScreen> {
            val songs by viewModel.songsList.collectAsState()
            SongsScreen(songs = songs)
        }
        composable<ArtistsScreen> {
            ArtistsScreen()
        }
        composable<PlayerScreen> {
            PlayerScreen()
        }
        composable<PlaylistScreen> {
            PlaylistScreen()
        }
        composable<AlbumsScreen> {
            AlbumsScreen()
        }
    }
}

@Serializable
object MainScreen

@Serializable
object PlayerScreen

@Serializable
object HomeScreen

@Serializable
object SongsScreen

@Serializable
object ArtistsScreen

@Serializable
object AlbumsScreen

@Serializable
object PlaylistScreen




