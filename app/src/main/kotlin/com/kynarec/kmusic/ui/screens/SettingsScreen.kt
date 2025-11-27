package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.ui.screens.settings.SettingComponentEnumChoice
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.Constants.TRANSITION_EFFECT_KEY


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    prefs: SettingsViewModel
) {
    // var transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Fade)

    Box(Modifier.fillMaxSize()) {
        Column {
            LazyColumn{
                item {
                    SettingComponentEnumChoice(
                        icon = Icons.Default.Animation,
                        title = "ScreenTransitionEffect",
                        description = "Choose how screens transition in the app",
                        prefs = prefs,
                        key = TRANSITION_EFFECT_KEY,
                        enumValues = TransitionEffect.all,
                        default = TransitionEffect.Fade,
                        labelMapper = { it.name }
                    )
                }
            }
        }
    }
}
