package com.kynarec.kmusic.ui.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.ui.components.MyNavigationRailComponent
import com.kynarec.kmusic.ui.components.TopBarComponent
import com.kynarec.kmusic.ui.components.player.PlayerControlBar
import com.kynarec.kmusic.ui.screens.player.PlayerScreen
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithContent(
    database: KmusicDatabase,
    navController: NavHostController,
    currentRoute: String?,
    isSearchScreen: Boolean,
    musicViewModel: MusicViewModel,
    hideVertNavElements: Boolean,
    isSettingsScreen: Boolean = false,
    content: @Composable () -> Unit,
    ) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember { mutableStateOf(false) }

    val showControlBar = musicViewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) {
            showBottomSheet.value = false
        }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(top = 16.dp)) {
                TopBarComponent(
                    showBackButton = hideVertNavElements,
                    navController = navController,
                    isSettingsScreen = isSettingsScreen)
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
                if (!hideVertNavElements) {
                    MyNavigationRailComponent(navController, currentRoute)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    content()
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
                        database = database,
                        navController = navController
                    )
                }
            }
        }
    }
}