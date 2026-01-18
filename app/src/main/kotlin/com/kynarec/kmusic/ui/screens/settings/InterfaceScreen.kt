package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.enums.StartDestination
import com.kynarec.kmusic.ui.components.settings.SettingComponentEnumChoice
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun InterfaceScreen(
    prefs: SettingsViewModel,
    musicViewModel: MusicViewModel
) {
    val scope = rememberCoroutineScope()

    val startDestinationFlow by prefs.startDestinationFlow.collectAsStateWithLifecycle(prefs.startDestination)

    val showControlBar = musicViewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp
    LazyColumn(
        Modifier.fillMaxSize()
            .padding(
                horizontal = 16.dp
            )
    ) {
        item {
            ElevatedCard() {
                SettingComponentEnumChoice(
                    icon = Icons.Default.PinDrop,
                    title = "Start Destination",
                    description = "Choose on which screen the app starts",
                    enumValues = StartDestination.entries,
                    selected = startDestinationFlow,
                    onValueSelected = {
                        scope.launch { prefs.putStartDestination(it) }
                    },
                    labelMapper = { it.label }
                )
            }
        }

        if (showControlBar) {
            item {
                Spacer(Modifier.height(bottomPadding))
            }
        }
    }
}