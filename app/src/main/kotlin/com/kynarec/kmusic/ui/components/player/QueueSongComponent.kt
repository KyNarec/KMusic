package com.kynarec.kmusic.ui.components.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.imageLoader
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongArtist
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.theme.KMusicTheme
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    draggingEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val title = song.title
    val artist = song.artists.joinToString(", ") { it.name }
    val duration = song.duration
    val imageUrl = song.thumbnail

    ElevatedCard(
        colors = if (isPlaying) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else CardDefaults.elevatedCardColors(),
        modifier = modifier
    ) {
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
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.playing))
                        val progress by animateLottieCompositionAsState(
                            composition,
                            iterations = LottieConstants.IterateForever,
                            speed = 0.5f
                        )
                        val dynamicProperties = rememberLottieDynamicProperties(
                            rememberLottieDynamicProperty(
                                property = LottieProperty.COLOR,
                                value = MaterialTheme.colorScheme.onSecondaryContainer.toArgb(),
                                keyPath = arrayOf("**")
                            )
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            dynamicProperties = dynamicProperties,
                            modifier = Modifier.size(34.dp)
                        )
//                        Icon(
//                            Icons.Default.Equalizer,
//                            contentDescription = "Equalizer",
//                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = duration,
                        fontSize = 14.sp,
                        modifier = Modifier
//                            .padding(bottom = 4.dp)
                    )
                }
            }
            if (draggingEnabled) {
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
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun QueueSongComponentPreview() {
    KMusicTheme(
        darkTheme = true,
        dynamicColor = false
    ) {
        Scaffold(Modifier.fillMaxSize()) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                val lazyListState = rememberLazyListState()
                val reorderableLazyListState =
                    rememberReorderableLazyListState(lazyListState) { from, to ->

                    }
                val song = Song(
                    id = "",
                    title = "Numb",
                    artists = listOf(SongArtist("", "LinkinPark")),
                    albumId = "",
                    duration = "3:07",
                    thumbnail = "",
                    likedAt = 0,
                    totalPlayTimeMs = 1
                )
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                ) {
                    item {
                        ReorderableItem(reorderableLazyListState, key = song.id) { isDragging ->
                            QueueSongComponent(
                                song = song,
                                onClick = {},
                                onLongClick = {},
                                isPlaying = true,
                                onDragStarted = {},
                                onDragStopped = {},
                                reorderableCollectionItemScope = this@ReorderableItem,
                                draggingEnabled = true,
                            )
                        }
                    }
                }
            }
        }
    }
}