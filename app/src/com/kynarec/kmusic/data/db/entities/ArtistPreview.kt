package com.kynarec.kmusic.data.db.entities

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ArtistPreview")
data class ArtistPreview(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val monthlyListeners : String,
) : Parcelable
