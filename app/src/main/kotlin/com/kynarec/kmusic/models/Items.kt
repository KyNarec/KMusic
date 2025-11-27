package com.kynarec.kmusic.models

import android.os.Parcel
import android.os.Parcelable

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val duration: String
    // other properties
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        // Read other properties
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(thumbnail)
        parcel.writeString(duration)
        // Write other properties
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}

data class Artist(
    val name: String,
    val id: String?,
)