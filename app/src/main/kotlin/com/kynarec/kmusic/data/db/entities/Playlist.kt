package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val browseId: String? = null
): Parcelable



//@Entity(tableName = "Playlist")
//data class Playlist(
//    @PrimaryKey val id: String,
//    val name: String,
//    val browseId: String,
//    val isEditable: Int,
//    val isYoutubePlaylist: Int
//) : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readInt(),
//        parcel.readInt()
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(id)
//        parcel.writeString(name)
//        parcel.writeString(browseId)
//        parcel.writeInt(isEditable)
//        parcel.writeInt(isYoutubePlaylist)
//    }
//
//    override fun describeContents(): Int = 0
//
//    companion object CREATOR : Parcelable.Creator<Playlist> {
//        override fun createFromParcel(parcel: Parcel): Playlist = Playlist(parcel)
//        override fun newArray(size: Int): Array<Playlist?> = arrayOfNulls(size)
//    }
//}

