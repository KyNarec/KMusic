package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.ui.Navigation
import com.kynarec.kmusic.ui.theme.KMusicTheme
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    DEFAULT_DARK_MODE = isSystemInDarkTheme()
    val navController = rememberNavController()

    val settingsViewModel: SettingsViewModel = koinActivityViewModel()
    val darkTheme by settingsViewModel.darkModeFLow.collectAsStateWithLifecycle(DEFAULT_DARK_MODE)

    val dynamicColors by settingsViewModel.dynamicColorsFlow.collectAsStateWithLifecycle(DEFAULT_DYNAMIC_COLORS)

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            focusManager.clearFocus()
            keyboardController?.hide()

        }
    }
    KMusicTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColors
    ) {
        Navigation(
            navController = navController,
        )
    }
}

