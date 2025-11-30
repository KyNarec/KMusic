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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    /**
     * Upserts a song into the database.
     * - If the song doesn't exist, it inserts it.
     * - If the song exists, it updates metadata (title, artist, duration, thumbnail)
     *   while preserving user-specific data (likedAt, totalPlayTimeMs).
     *
     * This prevents CASCADE DELETE issues that occur with OnConflictStrategy.REPLACE.
     *
     * @param song The song to upsert.
     */
    suspend fun upsertSong(song: Song) {
        val existing = getSongById(song.id)
        if (existing == null) {
            // Song doesn't exist, insert it
            insertSong(song)
        } else {
            // Song exists, update metadata but preserve user data
            updateSong(song.copy(
                likedAt = existing.likedAt,
                totalPlayTimeMs = existing.totalPlayTimeMs
            ))
        }
    }

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
