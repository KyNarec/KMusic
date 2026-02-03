package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.ui.components.settings.SettingComponentEnumChoice
import com.kynarec.kmusic.ui.components.settings.SettingComponentSwitch
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.Constants.DARK_MODE_KEY
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import com.kynarec.kmusic.utils.Constants.DYNAMIC_COLORS_KEY
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinActivityViewModel


@Composable
fun AppearanceScreen(
    prefs: SettingsViewModel = koinActivityViewModel(),
    musicViewModel: MusicViewModel = koinActivityViewModel()

) {
    val scope = rememberCoroutineScope()

    val transitionEffectFlow by prefs.transitionEffectFlow.collectAsStateWithLifecycle(prefs.transitionEffect)

    val showControlBar = musicViewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp
            )
    ) {
        item {
            ElevatedCard() {
                SettingComponentSwitch(
                    icon = Icons.Default.DarkMode,
                    title = "Dark theme",
                    description = "Use dark theme",
                    prefs = prefs,
                    switchId = DARK_MODE_KEY,
                    defaultValue = DEFAULT_DARK_MODE
                )
                SettingComponentSwitch(
                    icon = Icons.Default.FormatColorFill,
                    title = "Dynamic Colors",
                    description = "Use dynamic android native colors",
                    prefs = prefs,
                    switchId = DYNAMIC_COLORS_KEY,
                    defaultValue = DEFAULT_DYNAMIC_COLORS
                )
            }

        }
        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            ElevatedCard() {
                SettingComponentEnumChoice(
                    icon = Icons.Default.Animation,
                    title = "Screen Transition Effect",
                    description = "Choose how screens transition in the app",
                    enumValues = TransitionEffect.entries, // Now correctly seen as List<TransitionEffect>
                    selected = transitionEffectFlow,
                    onValueSelected = {
                        scope.launch { prefs.putTransitionEffect(it) }
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