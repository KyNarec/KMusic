package com.kynarec.kmusic.ui.components.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LowPriority
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.song.BottomSheetItem
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumOptionsBottomSheet(
    album: Album?,
    albumSongs: List<Song>,
    onDismiss: () -> Unit,
    viewModel: MusicViewModel,
    database: KmusicDatabase = koinInject()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val localAlbumState by album?.let {
        database.albumDao().getAlbumByIdFlow(it.id).collectAsStateWithLifecycle(initialValue = null)
    } ?: retain { mutableStateOf(null) }

    val activeAlbum = localAlbumState ?: album

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        if (activeAlbum == null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularWavyProgressIndicator()
            }
            return@ModalBottomSheet
        } else {
            val isLiked = activeAlbum.isLiked
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                BottomSheetItem(
                    icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    text = if (isLiked) "Remove from favorites" else "Add to favorites",
                    onClick = {
//                        viewModel.deletePlaylist(playlist!!)
//                        navController.navigate(PlaylistsScreen)
                        viewModel.toggleFavoriteAlbum(activeAlbum, albumSongs)
//                        onDismiss()
                    }
                )
                BottomSheetItem(
                    icon = Icons.Rounded.SkipNext,
                    text = "Play next",
                    onClick = {
                        viewModel.playNextList(albumSongs)
                        SmartMessage("Playing ${activeAlbum.title} next", context = context)
                        onDismiss()
                    }
                )

                BottomSheetItem(
                    icon = Icons.Rounded.LowPriority,
                    text = "Enqueue",
                    onClick = {
                        viewModel.enqueueSongList(albumSongs)
                        SmartMessage("Added ${activeAlbum.title} to queue", context = context)
                        onDismiss()
                    }
                )
            }
        }
    }
}