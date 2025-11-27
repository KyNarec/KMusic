package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Song")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "artistsText") val artist: String,
    @ColumnInfo(name = "durationText") val duration: String,
    @ColumnInfo(name = "thumbnailUrl") val thumbnail: String,

    // Optional fields for Room but not needed in UI/Parcelable
    @ColumnInfo(name = "likedAt") val likedAt: Long? = null,
    @ColumnInfo(name = "totalPlayTimeMs") val totalPlayTimeMs: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(thumbnail)
        parcel.writeString(duration)
        parcel.writeValue(likedAt)
        parcel.writeLong(totalPlayTimeMs)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song = Song(parcel)
        override fun newArray(size: Int): Array<Song?> = arrayOfNulls(size)
    }
}

