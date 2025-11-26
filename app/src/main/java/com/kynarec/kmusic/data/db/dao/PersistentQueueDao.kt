package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kynarec.kmusic.data.db.entities.PersistedQueueItem

@Dao
interface PersistedQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace because we clear and re-insert the whole queue
    suspend fun saveQueue(items: List<PersistedQueueItem>)

    @Query("SELECT * FROM persisted_playback_queue ORDER BY positionInQueue ASC")
    suspend fun getPersistedQueue(): List<PersistedQueueItem> // Use suspend List for one-shot load

    @Query("DELETE FROM persisted_playback_queue")
    suspend fun clearPersistedQueue()

    // Optional: If you want to get Song objects directly joined with their queue position
//    @Transaction
//    @Query("""
//        SELECT s.*, pq.positionInQueue, pq.mediaSessionQueueId
//        FROM songs s
//        INNER JOIN persisted_playback_queue pq ON s.id = pq.mediaId
//        ORDER BY pq.positionInQueue ASC
//    """)
//    fun getSongsInPersistedQueue(): Flow<List<SongWithPersistedQueueInfo>>
// Define SongWithPersistedQueueInfo
}

// Data class for the combined query (optional)
//data class SongWithPersistedQueueInfo(
//    @Embedded val song: Song,
//    val positionInQueue: Int,
//    val mediaSessionQueueId: Long
//)