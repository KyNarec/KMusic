package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "QueuedMediaItem")
data class QueuedMediaItem(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val songId: String,  // Foreign key to Song table
    val position: Int,
    val queueTimestamp: Long = System.currentTimeMillis(),
    val isCurrentlyPlaying: Boolean = false
)
    : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readLong(),
        parcel.readBoolean()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(queueId)
        parcel.writeString(songId)
        parcel.writeInt(position)
        parcel.writeLong(queueTimestamp)
        parcel.writeBoolean(isCurrentlyPlaying)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<QueuedMediaItem> {
        override fun createFromParcel(parcel: Parcel): QueuedMediaItem = QueuedMediaItem(parcel)
        override fun newArray(size: Int): Array<QueuedMediaItem?> = arrayOfNulls(size)
    }
}