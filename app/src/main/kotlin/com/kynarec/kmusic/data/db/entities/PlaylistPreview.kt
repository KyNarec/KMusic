package com.kynarec.kmusic.data.db.entities

import com.kynarec.kmusic.ui.PlaylistOnlineDetailScreen

data class PlaylistPreview(
    val id: String,
    val title: String,
    val author: String,
    val thumbnail: String,
    val views : String,
)

fun PlaylistPreview.toPlaylistOnlineDetailScreen() : PlaylistOnlineDetailScreen {
    return PlaylistOnlineDetailScreen(
        id = id,
        title = title,
        author = author,
        thumbnail = thumbnail,
        views = views
    )
}

fun PlaylistOnlineDetailScreen.toPlaylistPreview() : PlaylistPreview {
    return PlaylistPreview(
        id = id,
        title = title,
        author = author,
        thumbnail = thumbnail,
        views = views
    )
}
