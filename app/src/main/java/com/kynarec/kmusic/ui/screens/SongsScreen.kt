package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SongComponent

@Composable
fun SongsScreen(
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(songs.size) { index ->
            val song = songs[index]
            SongComponent(song = song, onClick = { /*TODO*/ })
        }
    }
}