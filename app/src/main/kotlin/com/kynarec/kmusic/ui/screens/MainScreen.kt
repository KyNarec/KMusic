package com.kynarec.kmusic.ui.screens

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.service.update.PlatformUpdateManager
import com.kynarec.kmusic.ui.Navigation
import com.kynarec.kmusic.ui.theme.AppTheme
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val updateManager = remember { PlatformUpdateManager() }
    val updateViewModel = remember { UpdateViewModel(updateManager) }

    val application = LocalContext.current.applicationContext as Application
    val database = remember { (application as KMusic).database }

    val musicViewModel: MusicViewModel = viewModel(
        factory = MusicViewModel.Factory(
            application,
            database.songDao(),
            database.playlistDao(),
            database.albumDao(),
            database.artistDao(),
        )
    )
    val ksafeInstance = remember { (application as KMusic).ksafe }
    DEFAULT_DARK_MODE = isSystemInDarkTheme()
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            ksafeInstance, // Use the remembered, stable KSafe instance
        )
    )
    val navController = rememberNavController()


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
    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColors
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

