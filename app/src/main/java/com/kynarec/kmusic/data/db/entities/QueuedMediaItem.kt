package com.kynarec.kmusic.data.db.entities

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "QueuedMediaItem")
data class QueuedMediaItem(
    @PrimaryKey val id: String,
    // something is weird here in sql lite this should be a Blob but It can't, idk why TODO??
    val mediaItem: ByteArray?,
    val position: Int
)  : Parcelable {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readBlob(),
        parcel.readInt()
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeBlob(mediaItem)
        parcel.writeInt(position)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<QueuedMediaItem> {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun createFromParcel(parcel: Parcel): QueuedMediaItem = QueuedMediaItem(parcel)
        override fun newArray(size: Int): Array<QueuedMediaItem?> = arrayOfNulls(size)
    }
}