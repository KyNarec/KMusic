package com.kynarec.kmusic.ui.screens

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.ui.Navigation
import com.kynarec.kmusic.ui.SearchResultScreen
import com.kynarec.kmusic.ui.SearchScreen
import com.kynarec.kmusic.ui.components.MyNavigationRailComponent
import com.kynarec.kmusic.ui.components.PlayerControlBar
import com.kynarec.kmusic.ui.components.TopBarComponent
import com.kynarec.kmusic.ui.theme.KMusicTheme
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.PlayerViewModel

@Composable
fun MainScreen() {
    val playerViewModel: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(LocalContext.current))
    //val showControlBar by playerViewModel.showControlBar.collectAsState()

    val application = LocalContext.current.applicationContext as Application
    val musicViewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory((application as MyApp).database.songDao(),LocalContext.current))
    val showControlBar = musicViewModel.uiState.collectAsState().value.showControlBar

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // New state to control PlayerScreen visibility
    var showPlayerScreen by remember { mutableStateOf(false) }

    val shouldHideNavElements = remember(currentRoute) {
        currentRoute?.startsWith(SearchScreen::class.qualifiedName!!) == true ||
                currentRoute?.startsWith(SearchResultScreen::class.qualifiedName!!) == true
    }

    KMusicTheme {
        // The top-level Box for layering
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Main UI Scaffold, including the NavHost
                Scaffold(
                    topBar = {
                        Box(modifier = Modifier.padding(top = 8.dp)) {
                            TopBarComponent(shouldHideNavElements, navController)
                        }
                    },
                ) { contentPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                    ) {
                        if (!shouldHideNavElements) {
                            MyNavigationRailComponent(navController)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Navigation(navController)
                        }
                    }
                }
            }

            // This is the PlayerControlBar, now correctly positioned to float
            AnimatedVisibility(
                visible = showControlBar && !shouldHideNavElements && !showPlayerScreen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                // Modifiers to float the bar in the bottom-center of the screen
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 12.dp, end = 12.dp)
            ) {
                PlayerControlBar(
                    onBarClick = { showPlayerScreen = true },
                    viewModel = musicViewModel,
                )
            }

            // This is the new PlayerScreen overlay
            AnimatedVisibility(
                visible = showPlayerScreen,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 400)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 400)
                )
            ) {
                PlayerScreen(
                    onClose = { showPlayerScreen = false },
                    musicViewModel
                )
            }
        }
    }
}
