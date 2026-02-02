package com.kynarec.lrclib

import com.kynarec.lrclib.model.Lyrics

class LyricsRepository(private val lrcLib: LrcLib) {
    suspend fun getLyrics(title: String, artist: String, album: String? = null, duration: Int? = null): List<Lyrics> {
        return try {
            lrcLib.searchParsed(title, artist, album)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLyricsString(title: String, artist: String, album: String? = null, duration: Int? = null): String {
        return try {
            lrcLib.search(title, artist, album)
        } catch (e: Exception) {
            ""
        }
    }
}