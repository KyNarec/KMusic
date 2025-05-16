package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Artist")
data class Artist (
    @PrimaryKey val id: String,
    val name: String,
    val thumbnailUrl: String,
    val timestamp: Int,
    val bookmarkedAt: Int,
    val isYoutubeArtist: Int
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(thumbnailUrl)
        parcel.writeInt(timestamp)
        parcel.writeInt(bookmarkedAt)
        parcel.writeInt(isYoutubeArtist)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Artist> {
        override fun createFromParcel(parcel: Parcel): Artist = Artist(parcel)
        override fun newArray(size: Int): Array<Artist?> = arrayOfNulls(size)
    }
}