package com.kynarec.kmusic.data.db.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

/**
 * Data class used to retrieve a full Playlist object along with all its associated Songs.
 * This is the result object of the @Transaction queries in the PlaylistDao.
 *
 * It combines data from the Playlist, SongPlaylistMap (Junction), and Song tables.
 */
@Parcelize
data class PlaylistWithSongs(
    // 1. EMBEDDED: Holds the main Playlist information
    @Embedded val playlist: Playlist,

    // 2. RELATION: Defines how to retrieve the List of Songs
    @Relation(
        parentColumn = "id",            // Playlist's primary key (id: Long)
        entity = Song::class,           // The target entity for the list (Song)
        entityColumn = "id",            // Song's primary key (id: String)
        associateBy = Junction(
            value = SongPlaylistMap::class, // The cross-reference table used to link them
            parentColumn = "playlistId",      // Column in the junction table pointing to Playlist
            entityColumn = "songId"           // Column in the junction table pointing to Song
        )
    )
    val songs: List<Song>
) : Parcelable