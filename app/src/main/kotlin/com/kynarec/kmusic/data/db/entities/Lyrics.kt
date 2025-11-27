package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Lyrics")
data class Lyrics (
    @PrimaryKey val songId: String,
    val fixed: String,
    val synced: String,
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songId)
        parcel.writeString(fixed)
        parcel.writeString(synced)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Lyrics> {
        override fun createFromParcel(parcel: Parcel): Lyrics = Lyrics(parcel)
        override fun newArray(size: Int): Array<Lyrics?> = arrayOfNulls(size)
    }
}