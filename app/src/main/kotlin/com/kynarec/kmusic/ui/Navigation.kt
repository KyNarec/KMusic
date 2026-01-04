package com.kynarec.kmusic.ui

import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.service.update.UpdateManager
import com.kynarec.kmusic.ui.screens.AlbumDetailScreen
import com.kynarec.kmusic.ui.screens.AlbumListScreen
import com.kynarec.kmusic.ui.screens.AlbumsScreen
import com.kynarec.kmusic.ui.screens.ArtistDetailScreen
import com.kynarec.kmusic.ui.screens.ArtistsScreen
import com.kynarec.kmusic.ui.screens.HomeScreen
import com.kynarec.kmusic.ui.screens.PlaylistDetailScreen
import com.kynarec.kmusic.ui.screens.PlaylistsScreen
import com.kynarec.kmusic.ui.screens.SearchResultScreen
import com.kynarec.kmusic.ui.screens.SearchScreen
import com.kynarec.kmusic.ui.screens.SettingsScreen
import com.kynarec.kmusic.ui.screens.SongListScreen
import com.kynarec.kmusic.ui.screens.SongsScreen
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Navigation(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    updateManager: UpdateManager,
    updateViewModel: UpdateViewModel,
    musicViewModel: MusicViewModel,
    database: KmusicDatabase
) {
    // Get the Application context using LocalContext.
    // This is safe to use in a Composable because it's tied to the Composable's scope.
    val application = LocalContext.current.applicationContext as Application

    // Use a custom ViewModel factory to inject the database DAO.
    val songDao = remember { database.songDao() }

    val transitionEffect by settingsViewModel.transitionEffectFlow.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = HomeScreen,
        enterTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> EnterTransition.None
                TransitionEffect.Expand -> expandIn(
                    animationSpec = tween(
                        350,
                        easing = LinearOutSlowInEasing
                    ), expandFrom = Alignment.TopStart
                )

                TransitionEffect.Fade -> fadeIn(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleIn(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                )

                TransitionEffect.SlideHorizontal -> slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left
                )
            }
        },
        exitTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> ExitTransition.None
                TransitionEffect.Expand -> shrinkOut(
                    animationSpec = tween(
                        350,
                        easing = FastOutSlowInEasing
                    ), shrinkTowards = Alignment.TopStart
                )

                TransitionEffect.Fade -> fadeOut(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleOut(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down
                )

                TransitionEffect.SlideHorizontal -> slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right
                )
            }
        },
        popEnterTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> EnterTransition.None
                TransitionEffect.Expand -> expandIn(
                    animationSpec = tween(
                        350,
                        easing = LinearOutSlowInEasing
                    ), expandFrom = Alignment.TopStart
                )

                TransitionEffect.Fade -> fadeIn(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleIn(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                )

                TransitionEffect.SlideHorizontal -> slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left
                )
            }
        },
        popExitTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> ExitTransition.None
                TransitionEffect.Expand -> shrinkOut(
                    animationSpec = tween(
                        350,
                        easing = FastOutSlowInEasing
                    ), shrinkTowards = Alignment.TopStart
                )

                TransitionEffect.Fade -> fadeOut(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleOut(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down
                )

                TransitionEffect.SlideHorizontal -> slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right
                )
            }
        }
    ) {
        composable<HomeScreen> {
            HomeScreen()
        }

        composable<SongsScreen> {
//            val songs = viewModel.songsList.collectAsStateWithLifecycle()
//            val songs = musicViewModel.uiState.collectAsStateWithLifecycle().value.songsList
            SongsScreen(viewModel = musicViewModel, database = database)
        }
        composable<ArtistsScreen> {
            ArtistsScreen(
                viewModel = musicViewModel,
                database = database,
                navController = navController
            )
        }
        composable<PlaylistsScreen> {
            PlaylistsScreen(
                navController = navController,
                database = database,
                viewModel = musicViewModel
            )
        }
        composable<PlaylistScreen> {
            val args = it.toRoute<PlaylistScreen>()
            PlaylistDetailScreen(
                playlistId = args.playlistId,
                viewModel = musicViewModel,
                database = database,
                navController = navController
            )
        }
        composable<AlbumsScreen> {
            AlbumsScreen(
                viewModel = musicViewModel,
                database = database,
                navController = navController
            )
        }
        composable<SearchScreen> {
            SearchScreen(navController)
        }
        composable<SearchResultScreen> {
            val args = it.toRoute<SearchResultScreen>()
            SearchResultScreen(args.query, musicViewModel, database = database, navController = navController)
        }
        composable<SettingsScreen> {
            SettingsScreen(prefs = settingsViewModel, navController = navController, updateManager = updateManager, updateViewModel = updateViewModel)
        }
        composable<AlbumDetailScreen> {
            Log.i("Navigation", "AlbumDetailScreen")
            val args = it.toRoute<AlbumDetailScreen>()
            AlbumDetailScreen(
                albumId = args.albumId,
                viewModel = musicViewModel,
                database = database,
                navController = navController
            )
        }
        composable<ArtistDetailScreen> {
            Log.i("Navigation", "ArtistDetailScreen")
            val args = it.toRoute<ArtistDetailScreen>()
            ArtistDetailScreen(
                artistId = args.artistId,
                viewModel = musicViewModel,
                database = database,
                navController = navController
            )
        }

        composable<SongListScreen> {
            Log.i("Navigation", "SongListScreen")
            val args = it.toRoute<SongListScreen>()
            SongListScreen(
                browseId = args.browseId,
                browseParams = args.browseParams,
                viewModel = musicViewModel,
                database = database
            )
        }

        composable<AlbumListScreen> {
            Log.i("Navigation", "AlbumListScreen")
            val args = it.toRoute<AlbumListScreen>()
            AlbumListScreen(
                browseId = args.browseId,
                browseParams = args.browseParams,
                navController = navController,
                viewModel = musicViewModel,
                database = database
            )
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
object PlaylistsScreen

@Serializable
data class PlaylistScreen(
    val playlistId: Long
)

@Serializable
object SearchScreen

@Serializable
data class SearchResultScreen(
    val query: String
)

@Serializable
object SettingsScreen

@Serializable
data class AlbumDetailScreen(
    val albumId: String
)

@Serializable
data class ArtistDetailScreen(
    val artistId: String
)

@Serializable
data class SongListScreen(
    val browseId: String,
    val browseParams: String
)

@Serializable
data class AlbumListScreen(
    val browseId: String,
    val browseParams: String
)
