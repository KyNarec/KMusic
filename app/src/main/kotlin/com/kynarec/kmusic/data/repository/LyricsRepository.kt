package com.kynarec.kmusic.data.repository

import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.utils.parseMillisToDuration
import com.kynarec.kmusic.utils.toSeconds
import com.kynarec.lrclib.LrcLibRepository
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.parser.EnhancedLrcParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class LyricsDataState {
    object Idle : LyricsDataState()
    object Loading : LyricsDataState()
    data class Success(val lyrics: SyncedLyrics) : LyricsDataState()
    data class Error(val message: String) : LyricsDataState()
}

class LyricsRepository(
    private val lrcLibRepository: LrcLibRepository,
    private val playerRepository: PlayerRepository,
    private val externalScope: CoroutineScope
) {
    private val tag = "Lyrics Repository"

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    val currentLyricsState: StateFlow<LyricsDataState> = playerRepository.playerState
        .map { it.currentSong }
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .flatMapLatest { song ->
            flow {
                if (song == null) {
                    emit(LyricsDataState.Idle)
                    return@flow
                }

                emit(LyricsDataState.Loading)
                try {
                    val lyrics = getSyncedLyrics(song, playerRepository.playerState.value.currentPosition)
                    if (lyrics == null) {
                        emit(LyricsDataState.Error("Failed to load lyrics"))
                        return@flow
                    } else {
                        emit(LyricsDataState.Success(lyrics))
                    }
                } catch (_: Exception) {
                    emit(LyricsDataState.Error("Failed to load lyrics"))
                }
            }
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = LyricsDataState.Idle
        )

    @OptIn(UnstableApi::class)
    suspend fun getSyncedLyrics(song: Song, currentDuration: Long): SyncedLyrics? {
        return try {
            val duration = if (song.duration.isBlank() || song.duration.isEmpty()) {
                currentDuration.parseMillisToDuration()
            } else {
                song.duration
            }
            Log.i(tag, "getSyncedLyrics duration: $duration")
            EnhancedLrcParser.parse(
                lrcLibRepository.getLyrics(
                    song.title,
                    artist = song.artists.joinToString(", ") { it.name },
                    duration = duration.toSeconds()
                ).first().syncedLyrics ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}