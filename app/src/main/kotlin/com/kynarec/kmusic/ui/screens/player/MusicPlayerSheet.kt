package com.kynarec.kmusic.ui.screens.player

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.ui.components.player.PlayerControlBar
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.PlayerViewModel
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MusicPlayerSheet(
    onClose: () -> Unit,
    navController: NavHostController,
    musicViewModel: MusicViewModel = koinActivityViewModel(),
    playerViewModel: PlayerViewModel = koinActivityViewModel()
) {
    val uiState by musicViewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit, uiState.currentSong) {
        val currentSong = uiState.currentSong

        musicViewModel.setLoadingLyrics(true)
        val syncedLyrics = musicViewModel.getSyncedLyrics(currentSong!!)
        Log.i("lyrics", "Synced Lyrics: ${syncedLyrics?.lines}")
        Log.i("lyrics", "Synced Lyrics: ${syncedLyrics?.title}")
        syncedLyrics?.let { musicViewModel.setCurrentLyrics(it) }
        musicViewModel.setLoadingLyrics(false)
    }

    SharedTransitionLayout {
        Column(modifier = Modifier.fillMaxSize()) {

            AnimatedVisibility(visible = playerUiState.currentPlayerState != PlayerSheetMode.MainPlayer && playerUiState.currentPlayerState != PlayerSheetMode.Options,
            ) {
                PlayerControlBar(
                    onBarClick = { playerViewModel.setPlayerState(PlayerSheetMode.MainPlayer) },
                    viewModel = musicViewModel
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
                            onClose = onClose
                        )
                    }

                    PlayerSheetMode.Lyrics -> LyricsScreen(
                        onDismiss = { playerViewModel.setPlayerState(PlayerSheetMode.MainPlayer) }
                    )

                    PlayerSheetMode.Queue -> QueueScreen(
                        onClose = { playerViewModel.setPlayerState(PlayerSheetMode.MainPlayer) },
                        viewModel = musicViewModel,
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