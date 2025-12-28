package com.kynarec.kmusic.data.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Artist")
data class Artist (
    @PrimaryKey val id: String,
    val name: String,
    val thumbnailUrl: String,
    val subscriber: String?,
    val description: String? = null,
    val timestamp: Long? = null,
    val bookmarkedAt: Long? = null,
    val isYoutubeArtist: Boolean = false
)  : Parcelable