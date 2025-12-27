package com.kynarec.kmusic.data.db

import androidx.room.TypeConverter
import com.kynarec.kmusic.data.db.entities.SongArtist
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromArtistList(value: List<SongArtist>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toArtistList(value: String): List<SongArtist> {
        return try {
            Json.decodeFromString<List<SongArtist>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}