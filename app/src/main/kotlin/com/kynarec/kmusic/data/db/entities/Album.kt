package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Album")
data class Album (
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val year: String,
    val authorsText: String,
    val copyright: String,
    val shareUrl: String,
    val timestamp: Long? = null,
    val bookmarkedAt: Long? = null,
    val isYoutubeAlbum: Boolean = false,
) : Parcelable {
    fun toggleBookmark(): Album {
        return copy(
            bookmarkedAt = if (bookmarkedAt == null) System.currentTimeMillis() else null
        )
    }

    val isLiked: Boolean
        get() = bookmarkedAt != null
}