package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.AlbumWithSongs
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongAlbumMap
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Delete
    suspend fun deleteAlbum(album: Album)

    @Update
    suspend fun updateAlbum(album: Album)

    @Query("SELECT * FROM Album")
    fun getAllAlbum(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE id = :id")
    fun getAlbumByIdFlow(id: String): Flow<Album?>

    @Transaction // Required when using @Relation annotation
    @Query("SELECT * FROM Album WHERE id = :albumId")
    fun getAlbumWithSongs(albumId: String): Flow<AlbumWithSongs?>

    @Transaction
    @Query("SELECT * FROM Album")
    fun getAllAlbumsWithSongs(): Flow<List<AlbumWithSongs>>

    @Query("SELECT * FROM SongAlbumMap WHERE albumId = :albumId ORDER BY position ASC")
    suspend fun getSongMapByAlbumId(albumId: String): List<SongAlbumMap>

    @Query("""
        SELECT Song.* FROM Song
        INNER JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId
        WHERE SongAlbumMap.albumId = :albumId
        ORDER BY SongAlbumMap.position ASC
    """)
    fun getSongsForAlbum(albumId: String): Flow<List<Song>>
}