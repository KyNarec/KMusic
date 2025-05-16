package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Year

@Entity(tableName = "Album")
data class Album (
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String,
    val year: String,
    val authorsText: String,
    val shareUrl: String,
    val timestamp: Int,
    val bookmarkedAt: Int,
    val isYoutubeAlbum: Int
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(year)
        parcel.writeString(authorsText)
        parcel.writeString(thumbnailUrl)
        parcel.writeInt(timestamp)
        parcel.writeInt(bookmarkedAt)
        parcel.writeInt(isYoutubeAlbum)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album = Album(parcel)
        override fun newArray(size: Int): Array<Album?> = arrayOfNulls(size)
    }
}