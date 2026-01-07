package com.kynarec.kmusic.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kynarec.kmusic.enums.StartDestination
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.utils.Constants.DARK_MODE_KEY
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import com.kynarec.kmusic.utils.Constants.DEFAULT_START_DESTINATION
import com.kynarec.kmusic.utils.Constants.DEFAULT_TRANSITION_EFFECT
import com.kynarec.kmusic.utils.Constants.DYNAMIC_COLORS_KEY
import com.kynarec.kmusic.utils.Constants.START_DESTINATION_KEY
import com.kynarec.kmusic.utils.Constants.TRANSITION_EFFECT_KEY
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.compose.mutableStateOf

class SettingsViewModel(
    val ksafe: KSafe,
    context: Context
): ViewModel() {

    var transitionEffect by ksafe.mutableStateOf(DEFAULT_TRANSITION_EFFECT, TRANSITION_EFFECT_KEY)
//    val transitionEffectFlow: StateFlow<TransitionEffect> = snapshotFlow { transitionEffect }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, transitionEffect)
    val transitionEffectFlow = ksafe.getFlow(TRANSITION_EFFECT_KEY, DEFAULT_TRANSITION_EFFECT)


    var startDestination by ksafe.mutableStateOf(DEFAULT_START_DESTINATION, START_DESTINATION_KEY)
    val startDestinationFlow = ksafe.getFlow(START_DESTINATION_KEY, DEFAULT_START_DESTINATION)


//    val darkMode by ksafe.mutableStateOf(DEFAULT_DARK_MODE, DARK_MODE_KEY)
    val darkModeFLow = ksafe.getFlow(DARK_MODE_KEY, DEFAULT_DARK_MODE)
    val dynamicColorsFlow = ksafe.getFlow(DYNAMIC_COLORS_KEY, DEFAULT_DYNAMIC_COLORS)



    suspend fun putTransitionEffect(value: TransitionEffect) {
        ksafe.put(TRANSITION_EFFECT_KEY, value)
    }
    suspend fun getTransitionEffect(): TransitionEffect {
        return ksafe.get(TRANSITION_EFFECT_KEY, DEFAULT_TRANSITION_EFFECT)
    }

    suspend fun putStartDestination(value: StartDestination) {
        ksafe.put(START_DESTINATION_KEY, value)
    }
    suspend fun getStartDestination(): StartDestination {
        return ksafe.get(START_DESTINATION_KEY, DEFAULT_START_DESTINATION)
    }

    suspend fun getBoolean(key: String, default: Boolean): Boolean {
        return ksafe.get(key, default)
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        ksafe.put(key, value)
    }


    /**
     * Factory for creating the ViewModel with dependencies.
     */
    class Factory(
        private val ksafe: KSafe,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(ksafe, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


//object SettingsPreferences {
//    private val prefs get() = (KMusic.instance).ksafe
//
//    var transitionEffect by prefs(
//        key = "transition_effect",
//        defaultValue = TransitionEffect.Fade
//    )
//}