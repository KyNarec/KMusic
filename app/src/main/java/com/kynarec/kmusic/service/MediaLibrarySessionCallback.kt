package com.kynarec.kmusic.service

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture

class MediaLibrarySessionCallback : MediaLibrarySession.Callback {

    // A simple, hardcoded data source for demonstration.
    private val rootMediaId = "root"
    private val playlistId = "playlist_1"
    private val allSongsMediaId = "all_songs"

    private val dummyMediaItems = mutableMapOf<String, List<MediaItem>>().apply {
        // Dummy songs
        val song1 = MediaItem.Builder()
            .setMediaId("song_1")
            .setUri("https://example.com/song1.mp3")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Song 1")
                    .setIsPlayable(true)
                    .build()
            )
            .build()
        val song2 = MediaItem.Builder()
            .setMediaId("song_2")
            .setUri("https://example.com/song2.mp3")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Song 2")
                    .setIsPlayable(true)
                    .build()
            )
            .build()
        val song3 = MediaItem.Builder()
            .setMediaId("song_3")
            .setUri("https://example.com/song3.mp3")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Song 3")
                    .setIsPlayable(true)
                    .build()
            )
            .build()

        // Dummy playlist
        val playlist = MediaItem.Builder()
            .setMediaId(playlistId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("My Playlist")
                    .setIsBrowsable(true)
                    .build()
            )
            .build()

        // Dummy "All Songs" folder
        val allSongsFolder = MediaItem.Builder()
            .setMediaId(allSongsMediaId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("All Songs")
                    .setIsBrowsable(true)
                    .build()
            )
            .build()

        // Map the root to browsable children
        put(rootMediaId, listOf(playlist, allSongsFolder))
        // Map the playlist to playable children (songs)
        put(playlistId, listOf(song1, song2))
        // Map the all songs folder to all playable songs
        put(allSongsMediaId, listOf(song1, song2, song3))
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val root = MediaItem.Builder()
            .setMediaId(rootMediaId)
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Media Root").setIsBrowsable(true).build())
            .build()
        return SettableFuture.create<LibraryResult<MediaItem>>().apply {
            set(LibraryResult.ofItem(root, params))
        }
    }

    @OptIn(UnstableApi::class)
    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val settableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        // This is where you provide the child nodes for a given parent.
        // For a playlist parent, you would return its child songs, which is exactly
        // what this example does by looking up the parentId in the dummyMediaItems map.
        val children = dummyMediaItems[parentId]
        if (children!= null) {
            val list = ImmutableList.copyOf(children)
            settableFuture.set(LibraryResult.ofItemList(list, params))
        } else {
            settableFuture.set(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
        }

        return settableFuture
    }

    @OptIn(UnstableApi::class)
    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val future = SettableFuture.create<LibraryResult<MediaItem>>()
        // Find the item by iterating through the map or a more efficient data structure
        val item = dummyMediaItems.values.flatten().find { it.mediaId == mediaId }
        return if (item!= null) {
            future.apply { set(LibraryResult.ofItem(item, null)) }
        } else {
            future.apply { set(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)) }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        // Implement search logic here
        return SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>().apply {
            set(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
        }
    }
}