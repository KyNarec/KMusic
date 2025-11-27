package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SongAlbumMap")
data class SongAlbumMap (
    @PrimaryKey val songId: String,
    @PrimaryKey val albumId: String,
    val position: Int,
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songId)
        parcel.writeString(albumId)
        parcel.writeInt(position)

    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SongAlbumMap> {
        override fun createFromParcel(parcel: Parcel): SongAlbumMap = SongAlbumMap(parcel)
        override fun newArray(size: Int): Array<SongAlbumMap?> = arrayOfNulls(size)
    }
}