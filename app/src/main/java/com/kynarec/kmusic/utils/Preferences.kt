package com.kynarec.kmusic.utils

import android.content.Context
import androidx.core.content.edit




fun Context.getPlayerIsPlaying(): Boolean {
    return getSharedPreferences("preferences", Context.MODE_PRIVATE)
        .getBoolean(PLAYER_IS_PLAYING, false)
}

fun Context.setPlayerIsPlaying(value: Boolean) {
    getSharedPreferences("preferences", Context.MODE_PRIVATE)
        .edit() {
            putBoolean(PLAYER_IS_PLAYING, value)
        }
}

fun Context.getJustStartedUp(): Boolean {
    return getSharedPreferences("preferences", Context.MODE_PRIVATE)
        .getBoolean(JUST_STARTED_UP, false)
}

fun Context.setJustStartedUp(value: Boolean) {
    getSharedPreferences("preferences", Context.MODE_PRIVATE)
        .edit() {
            putBoolean(JUST_STARTED_UP, value)
        }
}

