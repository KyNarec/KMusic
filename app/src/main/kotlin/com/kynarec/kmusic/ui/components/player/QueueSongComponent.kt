package com.kynarec.kmusic.ui.components.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.entities.Song
import sh.calvin.reorderable.ReorderableCollectionItemScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QueueSongComponent(
    song: Song,
    onClick: () -> Unit,
    onLongClick: () ->  Unit = {},
    isPlaying: Boolean = false,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
) {
    val title = song.title
    val artist = song.artists.joinToString(", ") { it.name }
    val duration = song.duration
    val imageUrl = song.thumbnail

    Box(
        modifier = with(reorderableCollectionItemScope) {
            Modifier
                .fillMaxWidth()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    hapticFeedbackEnabled = true
                )
                .padding(vertical = 8.dp)
                .background(Color.Transparent),
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
                Text(
                    text = title,
                    fontSize = 24.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee(
                            initialDelayMillis = 1000, iterations = Int.MAX_VALUE
                        )
                )

                Text(
                    text = artist,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee(
                            initialDelayMillis = 1000, iterations = Int.MAX_VALUE
                        )
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
        Box(
            modifier = with(reorderableCollectionItemScope) {
                Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .padding(top = 6.dp)
                    .size(width = 40.dp, height = 12.dp)
                    .background(Color.Transparent)
                    .draggableHandle(
                        onDragStarted = {
                           onDragStarted()
                        },
                        onDragStopped = {
                            onDragStopped()
                        },
                    )
            }
        ) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 32.dp, height = 4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }
    }
}