package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kynarec.kmusic.data.db.entities.QueuedMediaItem
import com.kynarec.kmusic.data.db.entities.Song

@Dao
interface QueuedMediaItemDao {
    @Query("SELECT * FROM QueuedMediaItem")
    suspend fun getAllQueuedMediaItems(): List<QueuedMediaItem>

    @Query("SELECT * FROM QueuedMediaItem WHERE songId = :id")
    suspend fun getSongById(id: String): Song?

    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0")
    suspend fun getSongsWithPlaytime(): List<Song>

    @Delete
    suspend fun clearQueuedMediaItems()



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(queuedMediaItem: QueuedMediaItem)

    @Update
    suspend fun updateSong(queuedMediaItem: QueuedMediaItem)

    @Delete
    suspend fun deleteSong(queuedMediaItem: QueuedMediaItem)
}