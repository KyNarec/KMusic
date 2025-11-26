package com.kynarec.kmusic.ui.screens

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.ui.Navigation
import com.kynarec.kmusic.ui.SearchResultScreen
import com.kynarec.kmusic.ui.SearchScreen
import com.kynarec.kmusic.ui.components.MyNavigationRailComponent
import com.kynarec.kmusic.ui.components.PlayerControlBar
import com.kynarec.kmusic.ui.components.TopBarComponent
import com.kynarec.kmusic.ui.theme.AppTheme
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()

    val application = LocalContext.current.applicationContext as Application
    val musicViewModel: MusicViewModel = viewModel(
        factory = MusicViewModel.Factory(
            (application as MyApp).database.songDao(),
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

    val shouldHideNavElements = isSearchScreen || isSearchResultScreen

    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) {
            showBottomSheet.value = false
        }
    }

    AppTheme {
        // The top-level Box for layering
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//                color = MaterialTheme.colorScheme.background
//            ) {
        // Main UI Scaffold, including the NavHost
        Scaffold(
            topBar = {
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    TopBarComponent(shouldHideNavElements, navController)
                }
            },
        ) { contentPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(top = 8.dp)
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
                        Navigation(navController)
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
                            viewModel = musicViewModel
                        )
                    }
                }
            }
        }
    }
}

