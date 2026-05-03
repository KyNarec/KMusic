package com.kynarec.kmusic.ui.screens.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.ui.components.SegmentedColumn
import com.kynarec.kmusic.ui.components.SongInformation
import com.kynarec.kmusic.ui.components.player.SleepTimerDialog
import com.kynarec.kmusic.ui.components.playlist.AddToPlaylistDialog
import com.kynarec.kmusic.ui.viewModels.AppAction
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.LibraryAction
import com.kynarec.kmusic.ui.viewModels.LibraryViewModel
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.shareUrl
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerOptionsScreen(
    song: Song,
    onDismiss: () -> Unit,
    onInformation: () -> Unit = {},
    libraryViewModel: LibraryViewModel = koinActivityViewModel(),
    dataViewModel: DataViewModel = koinActivityViewModel(),
    appViewModel: AppViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController,
    isInPlaylistDetailScreen: Boolean = false,
    playlistIdLong: Long? = null,
    playlistIdString: String? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val appState by appViewModel.state.collectAsStateWithLifecycle()
    val sleepTimerTimeLeft = appState.timeLeftMillis
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    val dbSong by database.songDao()
        .getSongFlowById(song.id)
        .collectAsStateWithLifecycle(null)

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showInformationDialog by remember { mutableStateOf(false) }

    val downloadingSongs by dataViewModel.downloadingSongs.collectAsStateWithLifecycle()
    val completedIds by dataViewModel.completedDownloadIds.collectAsStateWithLifecycle()
    val isDownloading = downloadingSongs.containsKey(song.id)
    val isDownloaded = completedIds.contains(song.id)

    BackHandler {
        onDismiss()
    }

    LaunchedEffect(Unit) {
        libraryViewModel.onAction(LibraryAction.MaybeAddSongToDB(song))
    }
    if (dbSong == null) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularWavyProgressIndicator()
        }
        return
    } else {
        val isLiked = dbSong!!.isLiked
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp)
                .padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            val listItems: MutableList<@Composable () -> Unit> = mutableListOf(
                {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Information") },
                        modifier = Modifier.clickable { showInformationDialog = true }
                    )
                },
                {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Share") },
                        modifier = Modifier.clickable {
                            shareUrl(
                                context,
                                url = "https://music.youtube.com/watch?v=${dbSong!!.id}"
                            )
                        }
                    )
                },
                {
                    when {
                        isDownloading -> {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = {
                                    Icon(
                                        painterResource(id = R.drawable.rounded_downloading_24),
                                        contentDescription = null
                                    )
                                },
                                headlineContent = { Text("Downloading") },
                            )
                        }

                        isDownloaded -> {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = {
                                    Icon(
                                        painterResource(R.drawable.rounded_download_done_24),
                                        contentDescription = null
                                    )
                                },
                                headlineContent = { Text("Downloaded") },
                                modifier = Modifier.clickable {
                                    dataViewModel.removeDownload(
                                        song
                                    )
                                }
                            )
                        }

                        else -> {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = {
                                    Icon(
                                        painterResource(R.drawable.rounded_download_24),
                                        contentDescription = null
                                    )
                                },
                                headlineContent = { Text("Download") },
                                modifier = Modifier.clickable { dataViewModel.addDownload(song) }
                            )
                        }
                    }
                },
                {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(if (isLiked) "Remove from favorites" else "Add to favorites") },
                        modifier = Modifier.clickable {
                            libraryViewModel.onAction(LibraryAction.ToggleFavoriteSong(dbSong!!))
                        }
                    )
                },
                {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                Icons.Default.Radio,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Start radio") },
                        modifier = Modifier.clickable {
                            libraryViewModel.onAction(LibraryAction.PlaySong(dbSong!!, withRadio = true))
                            onDismiss()
                        }
                    )
                },
                {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Play next") },
                        modifier = Modifier.clickable {
                            libraryViewModel.onAction(LibraryAction.PlayNext(dbSong!!))
                            SmartMessage("Playing ${dbSong!!.title} next", context = context)
                            onDismiss()
                        }
                    )
                },
                {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                Icons.Default.LowPriority,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Enqueue") },
                        modifier = Modifier.clickable {
                            libraryViewModel.onAction(LibraryAction.EnqueueSong(dbSong!!))
                            SmartMessage("Added ${dbSong!!.title} to queue", context = context)
                            onDismiss()
                        }
                    )
                },
                {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Add to playlist") },
                        modifier = Modifier.clickable {
                            showAddToPlaylistDialog = true
                        }
                    )
                },
            )

            if (isInPlaylistDetailScreen && playlistIdLong != null) {
                listItems.add(
                    {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null
                                )
                            },
                            headlineContent = { Text("Delete from playlist") },
                            modifier = Modifier.clickable {
                                scope.launch {
                                    database.playlistDao()
                                        .removeSongFromPlaylist(playlistIdLong, dbSong!!.id)
                                }
                                onDismiss()
                            }
                        )
                    }
                )
            }
            if ((appState.currentSong?.id ?: "") == dbSong!!.id) {
                listItems.add(
                    {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Icon(
                                    Icons.Default.Bedtime,
                                    contentDescription = null
                                )
                            },
                            headlineContent = { Text(if (sleepTimerTimeLeft > 0) "Timer: ${sleepTimerTimeLeft / 1000 / 60}m remaining" else "Sleep Timer") },
                            modifier = Modifier.clickable {
                                showSleepTimerDialog = true
                            }
                        )
                    }
                )
            }

            SegmentedColumn(
                items = listItems
            )
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

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { showSleepTimerDialog = false },
            onTimerSelected = { minutes ->
                appViewModel.onAction(AppAction.StartSleepTimer(minutes))
                showSleepTimerDialog = false
                onDismiss() // Close bottom sheet
            }
        )
    }

    if (showInformationDialog && dbSong != null) {
        SongInformation(
            song = dbSong!!,
            onDismiss = { showInformationDialog = false },
            modifier = Modifier.padding(8.dp)
        )
    }
}