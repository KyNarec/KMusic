package com.kynarec.kmusic.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SearchQuery")
data class SearchQuery (
    @PrimaryKey val id: String,
    val query: String,
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(query)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SearchQuery> {
        override fun createFromParcel(parcel: Parcel): SearchQuery = SearchQuery(parcel)
        override fun newArray(size: Int): Array<SearchQuery?> = arrayOfNulls(size)
    }
}