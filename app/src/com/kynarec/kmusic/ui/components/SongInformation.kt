package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kynarec.kmusic.data.db.entities.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongInformation(
    modifier: Modifier = Modifier,
    song: Song,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Information") },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        icon = { Icon(Icons.Rounded.Info, contentDescription = "Info") },
        text = {
            SelectionContainer {
                Column() {
                    Text("Id: ${song.id}")
                    Text("Title: ${song.title}")
                    Text("Artist: ${song.artists.joinToString(", ") { it.name }}")
                    Text("ArtistId: ${song.artists.joinToString(", ") { it.id }}")
                    Text("AlbumId: ${song.albumId}")
                    Text("Duration: ${song.duration}")
                    Text("LikedAt: ${song.likedAt}")
                    Text("TotalPlayTimeMs: ${song.totalPlayTimeMs}")
                    Text("Thumbnail: ${song.thumbnail}")
                }
            }
        },
    )
}