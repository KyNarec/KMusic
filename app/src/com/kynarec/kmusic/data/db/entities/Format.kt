package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Format")
data class Format (
    @PrimaryKey val songId: String,
    val iTag: Int,
    val mimeType: String,
    val bitrate: Int,
    val contentLength: Int,
    val lastModified: Int,
    // in SQL should be "REAL"
    val loudnessDb: Int
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songId)
        parcel.writeInt(iTag)
        parcel.writeString(mimeType)
        parcel.writeInt(bitrate)
        parcel.writeInt(contentLength)
        parcel.writeInt(lastModified)
        parcel.writeInt(loudnessDb)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Format> {
        override fun createFromParcel(parcel: Parcel): Format = Format(parcel)
        override fun newArray(size: Int): Array<Format?> = arrayOfNulls(size)
    }
}