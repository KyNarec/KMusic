package com.kynarec.kmusic.utils

import com.kynarec.kmusic.enums.StartDestination
import com.kynarec.kmusic.enums.TransitionEffect

object Constants {
    const val ACTION_RESUME = "ACTION_RESUME"
    const val ACTION_PLAY = "ACTION_PLAY"
    const val ACTION_PAUSE = "ACTION_PAUSE"
    const val ACTION_NEXT = "ACTION_NEXT"
    const val ACTION_PREV = "ACTION_PREV"
    const val ACTION_SEEK = "ACTION_SEEK"
    const val ACTION_STOP_UPDATES = "ACTION_STOP_UPDATES"
    const val ACTION_RESUME_UPDATES = "ACTION_RESUME_UPDATES"

    const val PLAYER_PROGRESS_UPDATE = "PLAYER_PROGRESS_UPDATE"
    const val PLAYBACK_STATE_CHANGED = "PLAYBACK_STATE_CHANGED"
    const val IS_PLAYING = "IS_PLAYING"
    const val NOTIFICATION_ID = 1596
    const val PLAYER_IS_PLAYING = "playerIsPlaying"
    const val JUST_STARTED_UP = "justStartedUp"
    const val PLAYER_IS_OPEN = "playerIsOpen"
    const val THUMBNAIL_ROUNDNESS = 30
    const val MARQUEE_DELAY = 1000L
    const val TRANSITION_EFFECT_KEY = "transitionEffect"
    val DEFAULT_TRANSITION_EFFECT = TransitionEffect.Fade
    const val START_DESTINATION_KEY = "startDestination"
    val DEFAULT_START_DESTINATION = StartDestination.HomeScreen
    const val DARK_MODE_KEY = "darkMode"
    // is changed to isSystemInDarkTheme() in mainScreen
    var DEFAULT_DARK_MODE = false
    const val DYNAMIC_COLORS_KEY = "dynamicColors"
    const val DEFAULT_DYNAMIC_COLORS = false
}

