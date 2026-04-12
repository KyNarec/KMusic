package com.kynarec.kmusic.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.ui.components.player.PlayerControlBar
import com.kynarec.kmusic.ui.viewModels.PlayerScreenViewModel
import com.kynarec.kmusic.ui.viewModels.PlayerViewModel
import org.koin.compose.viewmodel.koinActivityViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MusicPlayerSheet(
    onClose: () -> Unit,
    navController: NavHostController,
    playerScreenViewModel: PlayerScreenViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel = koinActivityViewModel()
) {
    val uiState by playerScreenViewModel.state.collectAsStateWithLifecycle()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    SharedTransitionLayout {
        Column(modifier = Modifier.fillMaxSize()) {

            AnimatedVisibility(visible = playerUiState.currentPlayerState != PlayerSheetMode.MainPlayer,
            ) {
                PlayerControlBar(
                    onBarClick = { playerViewModel.setPlayerState(PlayerSheetMode.MainPlayer) }
                )
            }

            AnimatedContent(
                targetState = playerUiState.currentPlayerState,
                transitionSpec = {
                    if (targetState == PlayerSheetMode.MainPlayer) {
                        // Slide down
                        (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
                    } else {
                        // Slide up
                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                    }
                },
                label = "SheetContentTransition"
            ) { mode ->
                when (mode) {
                    PlayerSheetMode.MainPlayer -> {
                        PlayerScreen(
                            onLyricsClick = { playerViewModel.setPlayerState(PlayerSheetMode.Lyrics) },
                            onQueueClick = { playerViewModel.setPlayerState(PlayerSheetMode.Queue) },
                            onMoreClick = { playerViewModel.setPlayerState(PlayerSheetMode.Options) },
                            navController = navController,
                            onClose = onClose,
                            viewModel = playerScreenViewModel,
                        )
                    }

                    PlayerSheetMode.Lyrics -> LyricsScreen(
                        onDismiss = { playerViewModel.setPlayerState(PlayerSheetMode.MainPlayer) },
                        playerScreenViewModel = playerScreenViewModel
                    )

                    PlayerSheetMode.Queue -> QueueScreen(
                        onClose = { playerViewModel.setPlayerState(PlayerSheetMode.MainPlayer) },
                        navController = navController
                    )

                    PlayerSheetMode.Options -> PlayerOptionsScreen(
                        onDismiss = { playerViewModel.setPlayerState(PlayerSheetMode.MainPlayer) },
                        song = uiState.currentSong!!,
                        navController = navController,
                    )
                }
            }
        }
    }
}