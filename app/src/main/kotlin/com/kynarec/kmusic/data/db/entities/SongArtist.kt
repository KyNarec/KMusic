package com.kynarec.kmusic.data.db.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SongArtist(
    val id: String,
    val name: String
) : Parcelable
