package com.kynarec.kmusic.data.repository

import com.kynarec.kmusic.data.db.entities.Song
import kotlin.random.Random

data class PlaylistItem(
    val id: Long = Random.nextLong(),
    val song: Song
)

fun Song.toPlaylistItem(): PlaylistItem {
    return PlaylistItem(song = this)
}

data class PlayerState(
    val songsList: List<PlaylistItem> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val currentDurationLong: Long = 0,
    val timeLeftMillis: Long = 0,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: Int = 0 // Player.REPEAT_MODE_OFF -> 0, etc.
)
