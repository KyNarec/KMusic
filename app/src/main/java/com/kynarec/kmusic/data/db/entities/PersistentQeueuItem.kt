package com.kynarec.kmusic.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "persisted_playback_queue",
    foreignKeys = [
        ForeignKey(
            entity = Song::class, // Assuming you have a Song entity
            parentColumns = ["id"],
            childColumns = ["mediaId"], // This mediaId should map to your Song.id
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mediaId"])]
)
data class PersistedQueueItem(        @PrimaryKey(autoGenerate = true)
                                      val persistenceId: Long = 0, // Unique ID for the persisted entry in the DB table

                                      val mediaSessionQueueId: Long, // The ID MediaSessionCompat assigned to this item
    // This is crucial if you need to map back to MediaSession's items
    // or if MediaSession generates these and you want to keep them.
    // Alternatively, if you build the MediaSession queue from the DB,
    // you might not strictly need to store this one, as MediaSession
    // will assign new IDs when you call setQueue().

                                      val mediaId: String,          // Corresponds to MediaDescriptionCompat.getMediaId() and your Song.id
                                      val positionInQueue: Int      // The order of this item
)
