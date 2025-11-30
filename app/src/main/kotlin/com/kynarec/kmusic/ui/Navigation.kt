package com.kynarec.kmusic.ui

import android.app.Application
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.service.update.UpdateManager
import com.kynarec.kmusic.ui.screens.AlbumsScreen
import com.kynarec.kmusic.ui.screens.ArtistsScreen
import com.kynarec.kmusic.ui.screens.HomeScreen
import com.kynarec.kmusic.ui.screens.PlaylistScreen
import com.kynarec.kmusic.ui.screens.PlaylistsScreen
import com.kynarec.kmusic.ui.screens.SearchResultScreen
import com.kynarec.kmusic.ui.screens.SearchScreen
import com.kynarec.kmusic.ui.screens.SettingsScreen
import com.kynarec.kmusic.ui.screens.SongsScreen
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    updateManager: UpdateManager,
    updateViewModel: UpdateViewModel,
) {
    // Get the Application context using LocalContext.
    // This is safe to use in a Composable because it's tied to the Composable's scope.
    val application = LocalContext.current.applicationContext as Application

    // Use a custom ViewModel factory to inject the database DAO.
    val songDao = remember { (application as KMusic).database.songDao() }
    // Use a custom ViewModel factory to inject the stable DAO instance.
    val musicViewModel: MusicViewModel = viewModel(
        factory = MusicViewModel.Factory(
            songDao, // Use the remembered, stable DAO
            LocalContext.current
        )
    )



    val transitionEffect by settingsViewModel.transitionEffectFlow.collectAsState()

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
//            val songs = viewModel.songsList.collectAsState()
            val songs = musicViewModel.uiState.collectAsState().value.songsList
            SongsScreen(songs = songs)
        }
        composable<ArtistsScreen> {
            ArtistsScreen()
        }
        composable<PlaylistsScreen> {
            val playlists =
            PlaylistsScreen(navController = navController)
        }
        composable<PlaylistScreen> {
            val args = it.toRoute<PlaylistScreen>()
            PlaylistScreen(playlistId = args.playlistId, viewModel = musicViewModel)
        }
        composable<AlbumsScreen> {
            AlbumsScreen()
        }
        composable<SearchScreen> {
            SearchScreen(navController)
        }
        composable<SearchResultScreen> {
            val args = it.toRoute<SearchResultScreen>()
            SearchResultScreen(args.query, musicViewModel)
        }
        composable<SettingsScreen> {
            SettingsScreen(prefs = settingsViewModel, navController = navController, updateManager = updateManager, updateViewModel = updateViewModel)
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
