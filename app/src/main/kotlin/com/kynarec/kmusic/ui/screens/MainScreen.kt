package com.kynarec.kmusic.ui.screens

import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.service.update.PlatformUpdateManager
import com.kynarec.kmusic.ui.Navigation
import com.kynarec.kmusic.ui.PlaylistScreen
import com.kynarec.kmusic.ui.PlaylistsScreen
import com.kynarec.kmusic.ui.SearchResultScreen
import com.kynarec.kmusic.ui.SearchScreen
import com.kynarec.kmusic.ui.SettingsScreen
import com.kynarec.kmusic.ui.components.MyNavigationRailComponent
import com.kynarec.kmusic.ui.components.PlayerControlBar
import com.kynarec.kmusic.ui.components.TopBarComponent
import com.kynarec.kmusic.ui.theme.AppTheme
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()

    val updateManager = remember { PlatformUpdateManager() }
    val updateViewModel = remember { UpdateViewModel(updateManager) }

    val application = LocalContext.current.applicationContext as Application
    val database = remember { (application as KMusic).database }

    val musicViewModel: MusicViewModel = viewModel(
        factory = MusicViewModel.Factory(
            (application as KMusic).database.songDao(),
            LocalContext.current
        )
    )
    val ksafeInstance = remember { (application as KMusic).ksafe }
    DEFAULT_DARK_MODE = isSystemInDarkTheme()
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            ksafeInstance, // Use the remembered, stable KSafe instance
            LocalContext.current
        )
    )
    val showControlBar = musicViewModel.uiState.collectAsState().value.showControlBar

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // New state to control PlayerScreen visibility
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember { mutableStateOf(false) }

    val isSearchScreen = remember(currentRoute) {
        currentRoute?.startsWith(SearchScreen::class.qualifiedName!!) == true
    }
    val isSearchResultScreen = remember(currentRoute) {
        currentRoute?.startsWith(SearchResultScreen::class.qualifiedName!!) == true
    }
    val isSettingsScreen = remember(currentRoute) {
        currentRoute?.startsWith(SettingsScreen::class.qualifiedName!!) == true
    }
    val isPlaylistScreen = remember(currentRoute) {
        currentRoute?.startsWith(PlaylistScreen::class.qualifiedName!!) == true
    }


    val shouldHideNavElements = isSearchScreen || isSearchResultScreen || isSettingsScreen || isPlaylistScreen

    val darkTheme by settingsViewModel.darkModeFLow.collectAsState(DEFAULT_DARK_MODE)

    val dynamicColors by settingsViewModel.dynamicColorsFlow.collectAsState(DEFAULT_DYNAMIC_COLORS)


    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) {
            showBottomSheet.value = false
        }
    }

    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColors
    ) {
        Scaffold(
            topBar = {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    TopBarComponent(shouldHideNavElements, navController)
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
                    if (!shouldHideNavElements) {
                        MyNavigationRailComponent(navController)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Navigation(
                            navController = navController,
                            settingsViewModel = settingsViewModel,
                            updateManager = updateManager,
                            updateViewModel = updateViewModel,
                            musicViewModel = musicViewModel,
                            database = database
                        )
                    }
                }



                // This is the PlayerControlBar
                if (showControlBar && !isSearchScreen) {

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
                            viewModel = musicViewModel,
                            database = database
                        )
                    }
                }
            }
        }
    }
}

