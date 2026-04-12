package com.kynarec.kmusic.data.repository

import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.kynarec.kmusic.data.db.dao.AlbumDao
import com.kynarec.kmusic.data.db.dao.ArtistDao
import com.kynarec.kmusic.data.db.dao.PlaylistDao
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.Artist
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongAlbumMap

class LibraryRepository(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao
) {
    private val tag = "LibraryRepository"

    suspend fun removeNotLikedAlbums() {
        albumDao.deleteUnbookmarkedAlbums()
    }

    suspend fun removeNotLikedArtists() {
        artistDao.deleteUnbookmarkedArtists()
    }

    suspend fun maybeAddSongToDB(song: Song) {
        if (songDao.getSongById(song.id) != null) return
        songDao.insertSong(song)
    }

    suspend fun toggleFavoriteSong(song: Song): Song {
        val updated = song.toggleLike()
        songDao.updateSong(updated)
        return updated
    }

    @OptIn(UnstableApi::class)
    suspend fun toggleFavoriteAlbum(album: Album, albumSongs: List<Song>) {
        val updated = album.toggleBookmark()
        albumDao.upsertAlbum(updated)

        // it was bookmarked before
        if (album.bookmarkedAt != null) {
            albumDao.getSongsForAlbum(album.id).forEach { song ->
                albumDao.removeSongFromAlbum(album.id, song.id)
            }
        } else {
            Log.i(tag, "Adding ${albumSongs.size} songs to album ${album.title}")
            albumSongs.forEachIndexed { index, it ->
                Log.i(tag, "Upserting song: $it")
                songDao.upsertSong(it)
                Log.i(tag, "inserting song to album")
                albumDao.insertSongToAlbum(
                    SongAlbumMap(
                        it.id,
                        album.id,
                        index
                    )
                )
            }
        }
    }

    suspend fun toggleFavoriteArtist(artist: Artist) {
        val updated = artist.toggleBookmark()
        artistDao.updateArtist(updated)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }
}
