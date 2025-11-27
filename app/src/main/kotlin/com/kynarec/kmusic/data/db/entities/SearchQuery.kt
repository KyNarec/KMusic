package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "SearchQuery")
data class SearchQuery (
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(query)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SearchQuery> {
        override fun createFromParcel(parcel: Parcel): SearchQuery = SearchQuery(parcel)
        override fun newArray(size: Int): Array<SearchQuery?> = arrayOfNulls(size)
    }
}