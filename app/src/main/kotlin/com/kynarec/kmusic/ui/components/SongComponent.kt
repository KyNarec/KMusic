package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import io.ktor.client.content.LocalFileContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongComponent(
    song: Song,
    onClick: () -> Unit,
    onLongClick: () ->  Unit = {}
) {
    // The main Row acts as the container, mimicking the XML's fixed height and clickable behavior.
    val title = song.title
    val artist = song.artist
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
            .background(Color.Transparent) // Equivalent to ?attr/selectableItemBackground
            .padding(vertical = 8.dp), // Add padding to make space for the content
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image View, sized and padded
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

        // Spacer to replicate the margin between the image and the text
        Spacer(modifier = Modifier.width(24.dp))

        // Column for the title and artist, with a weight to take up remaining space
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            // Smart marquee for title - only scrolls if text is too long
            ConditionalMarqueeText(
                text = title,
                fontSize = 24.sp,
                maxLines = 1
            )

            // Smart marquee for artist - only scrolls if text is too long
            ConditionalMarqueeText(
                text = artist,
                fontSize = 14.sp,
                maxLines = 1
            )
        }

        Text(
            text = duration,
            fontSize = 11.sp,
            modifier = Modifier
                .padding(end = 16.dp, bottom = 4.dp)
                .align(Alignment.Bottom)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MediaItemPreview() {
    SongComponent(
        song = Song(
            title = "Song Title",
            artist = "Artist Name",
            duration = "3:45",
            thumbnail = "https://img.youtube.com/vi/0MUeHF1SOm8/maxresdefault.jpg",
            id = "TODO",
            likedAt = 912910293,
            totalPlayTimeMs = 12318239
        ),
        onClick = {}
    )
}
