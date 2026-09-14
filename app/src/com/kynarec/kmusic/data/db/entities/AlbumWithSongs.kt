package com.kynarec.kmusic.data.db.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlbumWithSongs(
    @Embedded val album: Album,
    @Relation(
        parentColumn = "id",            // Playlist's primary key (id: Long)
        entity = Song::class,           // The target entity for the list (Song)
        entityColumn = "id",            // Song's primary key (id: String)
        associateBy = Junction(
            value = SongAlbumMap::class, // The cross-reference table used to link them
            parentColumn = "albumId",      // Column in the junction table pointing to Playlist
            entityColumn = "songId"           // Column in the junction table pointing to Song
        )
    )
    val songs: List<Song>
) : Parcelable
