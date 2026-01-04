package com.kynarec.kmusic.ui.viewModels

import android.content.Context
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.utils.Constants.DARK_MODE_KEY
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import com.kynarec.kmusic.utils.Constants.DEFAULT_TRANSITION_EFFECT
import com.kynarec.kmusic.utils.Constants.DYNAMIC_COLORS_KEY
import com.kynarec.kmusic.utils.Constants.TRANSITION_EFFECT_KEY
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.compose.mutableStateOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    val ksafe: KSafe,
    context: Context
): ViewModel() {

    var transitionEffect by ksafe.mutableStateOf(DEFAULT_TRANSITION_EFFECT, TRANSITION_EFFECT_KEY)
    val transitionEffectFlow: StateFlow<TransitionEffect> = snapshotFlow { transitionEffect }
        .stateIn(viewModelScope, SharingStarted.Eagerly, transitionEffect)

//    val darkMode by ksafe.mutableStateOf(DEFAULT_DARK_MODE, DARK_MODE_KEY)
    val darkModeFLow = ksafe.getFlow(DARK_MODE_KEY, DEFAULT_DARK_MODE)
    val dynamicColorsFlow = ksafe.getFlow(DYNAMIC_COLORS_KEY, DEFAULT_DYNAMIC_COLORS)




    suspend inline fun <reified T : Enum<T>> getEnum(key: String, default: T): T {
        return ksafe.get(key, default)
    }

    suspend inline fun <reified T : Enum<T>> putEnum(key: String, value: T) {
        when (key) {
            TRANSITION_EFFECT_KEY -> {
                transitionEffect = value as TransitionEffect
            }
        }

        ksafe.put(key, value)
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