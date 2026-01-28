package com.kynarec.kmusic.ui

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.ui.screens.ScreenWithContent
import com.kynarec.kmusic.ui.screens.StarterScreensContainer
import com.kynarec.kmusic.ui.screens.album.AlbumDetailScreen
import com.kynarec.kmusic.ui.screens.album.AlbumListScreen
import com.kynarec.kmusic.ui.screens.artist.ArtistDetailScreen
import com.kynarec.kmusic.ui.screens.playlist.PlaylistOfflineDetailScreen
import com.kynarec.kmusic.ui.screens.playlist.PlaylistOnlineDetailScreen
import com.kynarec.kmusic.ui.screens.search.SearchResultScreen
import com.kynarec.kmusic.ui.screens.search.SearchScreen
import com.kynarec.kmusic.ui.screens.settings.AboutScreen
import com.kynarec.kmusic.ui.screens.settings.AppearanceScreen
import com.kynarec.kmusic.ui.screens.settings.InterfaceScreen
import com.kynarec.kmusic.ui.screens.settings.SettingsScreen
import com.kynarec.kmusic.ui.screens.song.SongListScreen
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Navigation(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = koinViewModel(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current

    val transitionEffect by settingsViewModel.transitionEffectFlow.collectAsStateWithLifecycle(settingsViewModel.transitionEffect)

    NavHost(
        navController = navController,
        startDestination = StarterScreens,
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
        composable<StarterScreens> {
            StarterScreensContainer(
                rootNavController = navController,
            )
        }

        composable<PlaylistOfflineDetailScreen> {
            val args = it.toRoute<PlaylistOfflineDetailScreen>()
            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = false,
                hideVertNavElements = true
            ) {
                PlaylistOfflineDetailScreen(
                    playlistId = args.playlistId,
                    navController = navController
                )
            }
        }
        composable<PlaylistOnlineDetailScreen> {
            val args = it.toRoute<PlaylistOnlineDetailScreen>()
            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = false,
                hideVertNavElements = true
            ) {
                PlaylistOnlineDetailScreen(
                    playlistId = args.playlistId,
                    thumbnail = args.thumbnail,
                    navController = navController
                )
            }
        }

        composable<SearchScreen> {
            val args = it.toRoute<SearchScreen>()

            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = true,
                hideVertNavElements = true
            ) {
                SearchScreen(navController, args.query)
            }
        }
        composable<SearchResultScreen> {
            val args = it.toRoute<SearchResultScreen>()
            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = false,
                hideVertNavElements = true
            ) {
                SearchResultScreen(
                    args.query,
                    navController = navController
                )
            }
        }

        navigation<SettingsGraph>(startDestination = Settings.SettingsScreen) {
            composable<Settings.SettingsScreen> {
                ScreenWithContent(
                    navController = navController,
                    currentRoute = currentRoute,
                    isSearchScreen = false,
                    hideVertNavElements = true,
                    isSettingsScreen = true
                ) {
                    SettingsScreen(
                        navController = navController,
                    )
                }
            }
            composable<Settings.Appearance> {
                ScreenWithContent(
                    navController = navController,
                    currentRoute = currentRoute,
                    isSearchScreen = false,
                    hideVertNavElements = true,
                    isSettingsScreen = true
                ) {
                    AppearanceScreen()
                }
            }
            composable<Settings.Interface> {
                ScreenWithContent(
                    navController = navController,
                    currentRoute = currentRoute,
                    isSearchScreen = false,
                    hideVertNavElements = true,
                    isSettingsScreen = true
                ) {
                    InterfaceScreen()
                }
            }

            composable<Settings.AboutScreen> {
                ScreenWithContent(
                    navController = navController,
                    currentRoute = currentRoute,
                    isSearchScreen = false,
                    hideVertNavElements = true,
                    isSettingsScreen = true
                ) {
                    AboutScreen(
                        onOpenUrl = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }

        }

        composable<AlbumDetailScreen> {
            Log.i("Navigation", "AlbumDetailScreen")
            val args = it.toRoute<AlbumDetailScreen>()
            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = false,
                hideVertNavElements = true
            ) {
                AlbumDetailScreen(
                    albumId = args.albumId,
                    navController = navController
                )
            }
        }
        composable<ArtistDetailScreen> {
            Log.i("Navigation", "ArtistDetailScreen")
            val args = it.toRoute<ArtistDetailScreen>()
            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = false,
                hideVertNavElements = true
            ) {
                ArtistDetailScreen(
                    artistId = args.artistId,
                    navController = navController
                )
            }
        }

        composable<SongListScreen> {
            Log.i("Navigation", "SongListScreen")
            val args = it.toRoute<SongListScreen>()
            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = false,
                hideVertNavElements = true
            ) {
                SongListScreen(
                    browseId = args.browseId,
                    browseParams = args.browseParams,
                    navController = navController
                )
            }
        }

        composable<AlbumListScreen> {
            Log.i("Navigation", "AlbumListScreen")
            val args = it.toRoute<AlbumListScreen>()
            ScreenWithContent(
                navController = navController,
                currentRoute = currentRoute,
                isSearchScreen = false,
                hideVertNavElements = true
            ) {
                AlbumListScreen(
                    browseId = args.browseId,
                    browseParams = args.browseParams,
                    navController = navController,
                )
            }
        }
    }
}
@Serializable object StarterScreens

@Serializable
data class PlaylistOfflineDetailScreen(
    val playlistId: Long
)

@Serializable
data class PlaylistOnlineDetailScreen(
    val playlistId: String,
    val thumbnail: String
)


@Serializable
data class SearchScreen(
    val query: String? = null
)

@Serializable
data class SearchResultScreen(
    val query: String
)

@Serializable
data object SettingsGraph

@Serializable
sealed class Settings {
    @Serializable
    object SettingsScreen : Settings()

    @Serializable
    object Appearance : Settings()

    @Serializable
    object Interface : Settings()

    @Serializable
    object AboutScreen : Settings()
}


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
