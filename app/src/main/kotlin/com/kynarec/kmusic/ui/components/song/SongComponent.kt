package com.kynarec.kmusic.ui.components.song

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.MarqueeBox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongComponent(
    song: Song,
    onClick: () -> Unit,
    onLongClick: () ->  Unit = {},
    isPlaying: Boolean = false
) {
    val title = song.title
    val artist = song.artists.joinToString(", ") { it.name }
    val duration = song.duration
    val imageUrl = song.thumbnail
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(Color.Transparent)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(62.dp)
                .padding(start = 16.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            imageLoader = LocalContext.current.imageLoader

        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {

            MarqueeBox(
                text = title,
                fontSize = 24.sp,
                maxLines = 1
            )

            MarqueeBox(
                text = artist,
                fontSize = 14.sp,
                maxLines = 1
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 16.dp, start = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            if (isPlaying) {
                Icon(
                    Icons.Default.Equalizer,
                    contentDescription = "Equalizer",
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = duration,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
        }
    }
}