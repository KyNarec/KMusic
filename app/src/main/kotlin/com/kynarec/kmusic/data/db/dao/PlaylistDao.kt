package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.PlaylistWithSongs
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongPlaylistMap
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("SELECT * FROM Playlist")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM Playlist WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun getPlaylistByIdFlow(id: Long): Flow<Playlist?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(crossRef: SongPlaylistMap)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM SongPlaylistMap WHERE playlistId = :playlistId")
    suspend fun getNextPositionForPlaylist(playlistId: Long): Int

    @Transaction
    suspend fun insertSongAtEndOfPlaylist(songId: String, playlistId: Long) {
        val nextPosition = getNextPositionForPlaylist(playlistId)
        val newEntry = SongPlaylistMap(
            songId = songId,
            playlistId = playlistId,
            position = nextPosition
        )
        insertSongToPlaylist(newEntry)
    }

    @Query("DELETE FROM SongPlaylistMap WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)

    /**
     * Retrieves a single Playlist along with all its associated Songs, using the
     * Many-to-Many relationship defined in the PlaylistWithSongs data class.
     *
     * @param playlistId The ID of the playlist to retrieve.
     * @return A PlaylistWithSongs object, wrapped in a Flow for continuous updates.
     */
    @Transaction // Required when using @Relation annotation
    @Query("SELECT * FROM Playlist WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    /**
     * Retrieves all Playlists along with all their associated Songs.
     *
     * @return A list of PlaylistWithSongs objects, wrapped in a Flow.
     */
    @Transaction
    @Query("SELECT * FROM Playlist")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>


    /**
     * Helper to get the SongPlaylistMap entries for a specific playlist.
     * Useful for mapping and sorting the songs list retrieved via @Relation.
     */
    @Query("SELECT * FROM SongPlaylistMap WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getSongMapByPlaylistId(playlistId: Long): List<SongPlaylistMap>

    @Query("""
        SELECT Song.* FROM Song
        INNER JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId
        WHERE SongPlaylistMap.playlistId = :playlistId
        ORDER BY SongPlaylistMap.position ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>

    @Query(
        """
        SELECT * FROM Song
            INNER JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId
            WHERE SongPlaylistMap.playlistId = :playlistId
            ORDER BY SongPlaylistMap.position ASC
            LIMIT 4
        """
    )
    fun getFirstFourSongsForPlaylistFlow(playlistId: Long): Flow<List<Song>>

    @Query("UPDATE Playlist SET isEditable = NOT isEditable WHERE id = :playlistId")
    suspend fun toggleIsEditable(playlistId: Long)
}