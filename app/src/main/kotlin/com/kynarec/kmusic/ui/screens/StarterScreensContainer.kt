package com.kynarec.kmusic.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.enums.StartDestination
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.ui.components.MyNavigationRailComponent
import com.kynarec.kmusic.ui.components.TopBarComponent
import com.kynarec.kmusic.ui.components.player.PlayerControlBar
import com.kynarec.kmusic.ui.screens.album.AlbumsScreen
import com.kynarec.kmusic.ui.screens.artist.ArtistsScreen
import com.kynarec.kmusic.ui.screens.home.HomeScreen
import com.kynarec.kmusic.ui.screens.player.PlayerScreen
import com.kynarec.kmusic.ui.screens.playlist.PlaylistsScreen
import com.kynarec.kmusic.ui.screens.song.SongsScreen
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StarterScreensContainer(
    rootNavController: NavHostController,
    musicViewModel: MusicViewModel = koinViewModel(),
    database: KmusicDatabase = koinInject(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val childNavController = rememberNavController()
    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember { mutableStateOf(false) }

    val showControlBar = musicViewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    val transitionEffect by settingsViewModel.transitionEffectFlow.collectAsStateWithLifecycle(settingsViewModel.transitionEffect)
    val startDestination by settingsViewModel.startDestinationFlow.collectAsStateWithLifecycle(settingsViewModel.startDestination)

    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) {
            showBottomSheet.value = false
        }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(top = 16.dp)) {
                TopBarComponent(false, rootNavController, true)
            }
        },
    ) { contentPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                    MyNavigationRailComponent(childNavController, currentRoute)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    NavHost(
                        navController = childNavController,
                        startDestination = when (startDestination) {
                            StartDestination.HomeScreen -> HomeScreen
                            StartDestination.SongsScreen -> SongsScreen
                            StartDestination.ArtistsScreen -> ArtistsScreen
                            StartDestination.AlbumsScreen -> AlbumsScreen
                            StartDestination.PlaylistsScreen -> PlaylistsScreen
                        },
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
                            SongsScreen(
                                viewModel = musicViewModel,
                                database = database,
                                navController = rootNavController
                            )
                        }
                        composable<ArtistsScreen> {
                            ArtistsScreen(
                                viewModel = musicViewModel,
                                database = database,
                                navController = rootNavController
                            )
                        }
                        composable<AlbumsScreen> {
                            AlbumsScreen(
                                viewModel = musicViewModel,
                                database = database,
                                navController = rootNavController
                            )
                        }
                        composable<PlaylistsScreen> {
                            PlaylistsScreen(
                                navController = rootNavController,
                                database = database,
                                viewModel = musicViewModel
                            )
                        }
                    }
                }
            }

            if (showControlBar) {
                AnimatedVisibility(
                    true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    ),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp, start = 12.dp, end = 12.dp)
                ) {
                    PlayerControlBar(
                        onBarClick = {
                            showBottomSheet.value = true
                        },
                        viewModel = musicViewModel,
                    )
                }

            }

            if (showBottomSheet.value) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet.value = false
                    },
                    dragHandle = null,
                    shape = RectangleShape,
                    sheetState = sheetState
                ) {
                    PlayerScreen(
                        onClose = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet.value = false
                                }
                            }
                        },
                        navController = rootNavController
                    )
                }
            }
        }
    }
}

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