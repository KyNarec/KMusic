package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SongArtistMap")
data class SongArtistMap (
    @PrimaryKey val songId: String,
    @PrimaryKey val artistId: String,
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songId)
        parcel.writeString(artistId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SongArtistMap> {
        override fun createFromParcel(parcel: Parcel): SongArtistMap = SongArtistMap(parcel)
        override fun newArray(size: Int): Array<SongArtistMap?> = arrayOfNulls(size)
    }
}