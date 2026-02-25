package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import com.kynarec.kmusic.enums.SortBy
import com.kynarec.kmusic.enums.SortOrder
import com.kynarec.kmusic.enums.StartDestination
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.utils.Constants.COLORED_DOWNLOAD_INDICATOR_KEY
import com.kynarec.kmusic.utils.Constants.DARK_MODE_KEY
import com.kynarec.kmusic.utils.Constants.DEFAULT_COLORED_DOWNLOAD_INDICATOR
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import com.kynarec.kmusic.utils.Constants.DEFAULT_PLAYLIST_SORT_BY
import com.kynarec.kmusic.utils.Constants.DEFAULT_PLAYLIST_SORT_ORDER
import com.kynarec.kmusic.utils.Constants.DEFAULT_START_DESTINATION
import com.kynarec.kmusic.utils.Constants.DEFAULT_TRANSITION_EFFECT
import com.kynarec.kmusic.utils.Constants.DEFAULT_WAVY_LYRICS_IDLE_INDICATOR
import com.kynarec.kmusic.utils.Constants.DYNAMIC_COLORS_KEY
import com.kynarec.kmusic.utils.Constants.PLAYLIST_SORT_BY_KEY
import com.kynarec.kmusic.utils.Constants.PLAYLIST_SORT_ORDER_KEY
import com.kynarec.kmusic.utils.Constants.START_DESTINATION_KEY
import com.kynarec.kmusic.utils.Constants.TRANSITION_EFFECT_KEY
import com.kynarec.kmusic.utils.Constants.WAVY_LYRICS_IDLE_INDICATOR_KEY
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.compose.mutableStateOf

class SettingsViewModel(
    val ksafe: KSafe,
): ViewModel() {

    val transitionEffect by ksafe.mutableStateOf(DEFAULT_TRANSITION_EFFECT, TRANSITION_EFFECT_KEY)
//    val transitionEffectFlow: StateFlow<TransitionEffect> = snapshotFlow { transitionEffect }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, transitionEffect)
    val transitionEffectFlow = ksafe.getFlow(TRANSITION_EFFECT_KEY, DEFAULT_TRANSITION_EFFECT)


    val startDestination by ksafe.mutableStateOf(DEFAULT_START_DESTINATION, START_DESTINATION_KEY)
    val startDestinationFlow = ksafe.getFlow(START_DESTINATION_KEY, DEFAULT_START_DESTINATION)


//    val darkMode by ksafe.mutableStateOf(DEFAULT_DARK_MODE, DARK_MODE_KEY)
    val darkModeFLow = ksafe.getFlow(DARK_MODE_KEY, DEFAULT_DARK_MODE)
    val dynamicColorsFlow = ksafe.getFlow(DYNAMIC_COLORS_KEY, DEFAULT_DYNAMIC_COLORS)

    val wavyLyricsIdleIndicatorFlow = ksafe.getFlow(WAVY_LYRICS_IDLE_INDICATOR_KEY, DEFAULT_WAVY_LYRICS_IDLE_INDICATOR)
    var coloredDownloadIndicator by ksafe.mutableStateOf(DEFAULT_COLORED_DOWNLOAD_INDICATOR, key = COLORED_DOWNLOAD_INDICATOR_KEY)

    val playlistSortByFlow = ksafe.getFlow(PLAYLIST_SORT_BY_KEY, DEFAULT_PLAYLIST_SORT_BY)
    val playlistSortOrderFlow = ksafe.getFlow(PLAYLIST_SORT_ORDER_KEY, DEFAULT_PLAYLIST_SORT_ORDER)


    fun putTransitionEffect(value: TransitionEffect) {
        ksafe.putDirect(TRANSITION_EFFECT_KEY, value)
    }

    fun putStartDestination(value: StartDestination) {
        ksafe.putDirect(START_DESTINATION_KEY, value)
    }

    fun putPlaylistSortBy(value: SortBy) {
        ksafe.putDirect(PLAYLIST_SORT_BY_KEY, value)
    }

    fun putPlaylistSortOrder(value: SortOrder) {
        ksafe.putDirect(PLAYLIST_SORT_ORDER_KEY, value)
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return ksafe.getDirect(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        ksafe.putDirect(key, value)
    }
}