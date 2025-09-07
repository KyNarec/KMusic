package com.kynarec.kmusic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.ui.Navigation
import com.kynarec.kmusic.ui.PlayerScreen
import com.kynarec.kmusic.ui.components.MyNavigationRailComponent
import com.kynarec.kmusic.ui.components.PlayerControlBar
import com.kynarec.kmusic.ui.components.TopBarComponent
import com.kynarec.kmusic.ui.theme.KMusicTheme
import com.kynarec.kmusic.ui.viewModels.PlayerViewModel

@Composable
fun MainScreen() {
    val playerViewModel: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(LocalContext.current))
    val showControlBar by playerViewModel.showControlBar.collectAsState()

    KMusicTheme {
        val navController = rememberNavController()
        // We use a top-level Box to contain both the main Scaffold and the floating player bar
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    topBar = {
                        TopBarComponent(navController)
                    },
                    // We remove the bottomBar slot here, as it will be placed separately
                ) { contentPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                    ) {
                        MyNavigationRailComponent(navController)
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Navigation(navController)
                        }
                    }
                }
            }

            // This is the PlayerControlBar, now correctly positioned to float
            AnimatedVisibility(
                visible = showControlBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                // Modifiers to float the bar in the bottom-center of the screen
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                PlayerControlBar(
                    onBarClick = { navController.navigate(PlayerScreen) },
                    viewModel = playerViewModel,
                    // Modifiers for the background and rounded corners
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(16.dp))
//                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}
