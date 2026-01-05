package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.shareUrl
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun SongOptionsBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
    onInformation: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController,
    isInPlaylistDetailScreen: Boolean = false,
    playlistId: Long? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val dbSong by database.songDao()
        .getSongFlowById(song.id)
        .collectAsStateWithLifecycle(null)

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.maybeAddSongToDB(song)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        if (dbSong == null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularWavyProgressIndicator()
            }
            return@ModalBottomSheet
        } else {
            val isLiked = dbSong!!.isLiked
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Song Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Art
                    AsyncImage(
                        model = dbSong!!.thumbnail,
                        contentDescription = "Album art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        imageLoader = LocalContext.current.imageLoader
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Song Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        ConditionalMarqueeText(
                            text = dbSong!!.title,
                            fontSize = 18.sp,
                            maxLines = 1,
                            //overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        ConditionalMarqueeText(
                            text = dbSong!!.artists.joinToString(", ") { it.name },
                            fontSize = 14.sp,
                            maxLines = 1,
                            //overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Duration and Share
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = dbSong!!.duration,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    shareUrl(
                                        context,
                                        url = "https://music.youtube.com/watch?v=${dbSong!!.id}"
                                    )
                                },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Items
                BottomSheetItem(
                    icon = Icons.Default.Info,
                    text = "Information",
                    onClick = {
                        onInformation()
                        onDismiss()
                    }
                )

                BottomSheetItem(
                    icon = Icons.Default.Radio,
                    text = "Start radio",
                    onClick = {
                        viewModel.playSongByIdWithRadio(dbSong!!)
                        onDismiss()
                    }
                )

                BottomSheetItem(
                    icon = Icons.Default.SkipNext,
                    text = "Play next",
                    onClick = {
                        viewModel.playNext(dbSong!!)
                        SmartMessage("Playing ${dbSong!!.title} next", context = context)
                        onDismiss()
                    }
                )

                BottomSheetItem(
                    icon = Icons.Default.Queue,
                    text = "Enqueue",
                    onClick = {
                        viewModel.enqueueSong(dbSong!!)
                        SmartMessage("Added ${dbSong!!.title} to queue", context = context)
                        onDismiss()
                    }
                )

                BottomSheetItem(
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    text = if (isLiked) "Remove from favorites" else "Add to favorites",
                    onClick = {
                        viewModel.toggleFavoriteSong(dbSong!!)
                    }
                )

                BottomSheetItem(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    text = "Add to playlist",
                    onClick = {
                        showAddToPlaylistDialog = true
//                        onDismiss()
                    }
                )

                if (isInPlaylistDetailScreen && playlistId != null) {
                    BottomSheetItem(
                        icon = Icons.Default.Delete,
                        text = "Delete from playlist",
                        onClick = {
                            scope.launch {
                                database.playlistDao().removeSongFromPlaylist(playlistId, dbSong!!.id)
                            }
                            onDismiss()
                        }
                    )
                }
            }
        }
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            song = dbSong!!,
            database = database,
            onDismissRequest = { showAddToPlaylistDialog = false },
            navController = navController
        )
    }
}

@Composable
fun BottomSheetItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
