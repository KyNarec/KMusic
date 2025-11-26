package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kynarec.kmusic.data.db.entities.Song

@Dao
interface SongDao {
    @Query("SELECT * FROM Song")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM Song WHERE id = :id")
    suspend fun getSongById(id: String): Song?

    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0")
    suspend fun getSongsWithPlaytime(): List<Song>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    /**
     * Deletes a song from the database by its unique ID.
     * This uses a custom SQL query with the @Query annotation, as
     * the @Delete annotation requires an entire entity object.
     *
     * @param id The unique ID of the song to delete.
     */
    @Query("DELETE FROM Song WHERE id = :id")
    suspend fun deleteSongById(id: String)
}
