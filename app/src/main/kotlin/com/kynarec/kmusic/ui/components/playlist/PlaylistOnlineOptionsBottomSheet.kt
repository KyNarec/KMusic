package com.kynarec.kmusic.ui.components.playlist

import android.util.Log
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.room.withTransaction
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.components.song.BottomSheetItem
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistOnlineOptionsBottomSheet(
    modifier: Modifier = Modifier,
//    playlistId: String,
    playlist: Playlist,
    thumbnail: String,
    songs: List<Song>,
    onDismiss: () -> Unit,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    val iconAndTextSize = 14.sp
    val density = LocalDensity.current
    val iconSizeDp = with(density) {
        iconAndTextSize.toDp()
    }

    val keyboardController = LocalSoftwareKeyboardController.current


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(100.dp)
                    .height(100.dp)
                ) {
                    AsyncImage(
                        model = thumbnail,
                        contentDescription = "Playlist art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        imageLoader = LocalContext.current.imageLoader
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    Modifier.weight(1f)
                ) {
                    MarqueeBox(
                        text = playlist.name,
                        fontSize = iconAndTextSize,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = "Music Icon",
                            modifier = Modifier.size(iconSizeDp)
                        )
                        Spacer(Modifier.width(8.dp))
                        MarqueeBox(
                            text = songs.size.toString(),
                            fontSize = iconAndTextSize,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            BottomSheetItem(
                icon = Icons.Default.BookmarkAdd,
                text = "Import",
                onClick = {
                    scope.launch {
                        database.withTransaction {
                            val databasePlaylistId =
                                database.playlistDao().insertPlaylist(playlist)
                            Log.i("PlaylistScreen", "databasePlaylistId = $databasePlaylistId")

                            songs.forEach { song ->

                                Log.i("PlaylistScreen", "adding song to db ${song.title}")
                                database.songDao().upsertSong(song)
                                database.playlistDao()
                                    .insertSongAtEndOfPlaylist(song.id, databasePlaylistId)
                            }
                        }
                        onDismiss()
                    }
                }
            )

            BottomSheetItem(
                icon = Icons.Default.SkipNext,
                text = "Play next",
                onClick = {
                    viewModel.playNextList(songs)
                    SmartMessage("Playing ${playlist.name} next", context = context)
                    onDismiss()
                }
            )

            BottomSheetItem(
                icon = Icons.Default.Queue,
                text = "Enqueue",
                onClick = {
                    viewModel.enqueueSongList(songs)
                    SmartMessage("Added ${playlist.name} to queue", context = context)
                    onDismiss()
                }
            )
        }
    }
}