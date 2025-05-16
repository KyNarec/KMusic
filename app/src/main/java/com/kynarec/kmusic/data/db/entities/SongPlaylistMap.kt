package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SongPlaylistMap")
data class SongPlaylistMap (
    @PrimaryKey val songId: String,
    @PrimaryKey val playlistId: String,
    val position: Int,
    val setVideoId: String,
    val dateAdded: Int
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songId)
        parcel.writeString(playlistId)
        parcel.writeInt(position)
        parcel.writeString(setVideoId)
        parcel.writeInt(position)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SongPlaylistMap> {
        override fun createFromParcel(parcel: Parcel): SongPlaylistMap = SongPlaylistMap(parcel)
        override fun newArray(size: Int): Array<SongPlaylistMap?> = arrayOfNulls(size)
    }
}