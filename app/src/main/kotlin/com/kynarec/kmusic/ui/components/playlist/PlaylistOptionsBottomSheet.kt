package com.kynarec.kmusic.ui.components.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.ui.PlaylistsScreen
import com.kynarec.kmusic.ui.components.song.BottomSheetItem
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.ConditionalMarqueeText
import com.kynarec.kmusic.utils.SmartMessage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistOptionsBottomSheet(
    playlistId: Long,
    onDismiss: () -> Unit,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val playlist by database.playlistDao()
        .getPlaylistByIdFlow(playlistId)
        .collectAsStateWithLifecycle(null)

    val songsThumbnailList = remember { mutableStateListOf<String>() }
    val songsFlow = remember(playlistId) {
        database.playlistDao().getSongsForPlaylist(playlistId)
    }
    val songs by songsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(Unit) {
        database.playlistDao()
            .getFirstFourSongsForPlaylistFlow(playlistId)
            .collect { songsList ->
                songsThumbnailList.clear()
                songsList.forEach { song ->
                    songsThumbnailList.add(song.thumbnail)
                }
            }
    }

    val iconAndTextSize = 14.sp
    val density = LocalDensity.current
    val iconSizeDp = with(density) {
        iconAndTextSize.toDp() // Convert 14.sp to the corresponding dp value
    }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        if (playlist == null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularWavyProgressIndicator()
            }
            return@ModalBottomSheet
        } else {
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
                        TwoByTwoImageGrid(songsThumbnailList)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        Modifier.weight(1f)
                    ) {
                        ConditionalMarqueeText(
                            text = playlist!!.name,
                            fontSize = iconAndTextSize,
                            maxLines = 1,
                            //overflow = TextOverflow.Ellipsis,
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
                            ConditionalMarqueeText(
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
                    icon = Icons.Default.Delete,
                    text = "Delete",
                    onClick = {
                        viewModel.deletePlaylist(playlist!!)
                        navController.navigate(PlaylistsScreen)
                        SmartMessage(
                            "Playlist deleted successfully",
                            type = PopupType.Success,
                            context = context
                        )
                        onDismiss()
                    }
                )
            }
        }
    }
}