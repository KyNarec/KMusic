package com.kynarec.kmusic.data.db.entities

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "AlbumPreview")
data class AlbumPreview(
    val id: String,
    val title: String,
    val artist: String,
    val year: String,
    val thumbnail: String
) : Parcelable
