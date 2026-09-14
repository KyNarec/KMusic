package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kynarec.kmusic.data.db.entities.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: Artist)

    suspend fun upsertArtist(artist: Artist) {
        val existing = getArtistById(artist.id)
        if (existing == null) {
            // Artist doesn't exist, insert it
            insertArtist(artist)
        } else {
            // Artist exists, update metadata but preserve user data
            updateArtist(artist.copy(
                bookmarkedAt = existing.bookmarkedAt,
                isYoutubeArtist = existing.isYoutubeArtist
            ))
        }
    }

    @Delete
    suspend fun deleteArtist(artist: Artist)

    @Update
    suspend fun updateArtist(artist: Artist)

    @Query("SELECT * FROM Artist")
    fun getAllArtist(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun getArtistByIdFlow(id: String): Flow<Artist?>

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun getArtistById(id: String): Artist?

    @Query("SELECT * FROM Artist WHERE bookmarkedAt > 0")
    fun getFavouritesArtistsFlow(): Flow<List<Artist>>

    @Query("DELETE FROM Artist WHERE bookmarkedAt IS NULL OR bookmarkedAt <= 0")
    suspend fun deleteUnbookmarkedArtists()
}