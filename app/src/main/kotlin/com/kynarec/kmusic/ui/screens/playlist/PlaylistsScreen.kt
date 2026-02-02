package com.kynarec.kmusic.ui.screens.playlist

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.room.withTransaction
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.innertube.getPlaylistAndSongs
import com.kynarec.kmusic.ui.PlaylistOfflineDetailScreen
import com.kynarec.kmusic.ui.components.playlist.PlaylistComponent
import com.kynarec.kmusic.ui.components.playlist.PlaylistCreateNewDialog
import com.kynarec.kmusic.ui.components.playlist.PlaylistImportFromOnlineDialog
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.importPlaylistFromCsv
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@ExperimentalMaterial3ExpressiveApi
@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    database: KmusicDatabase = koinInject(),
    viewModel: MusicViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val playlists: List<Playlist> by remember(database) {
        database.playlistDao().getAllPlaylists()
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val isLoading = playlists.isEmpty()

    val totalLines = remember { mutableIntStateOf(0) }
    val currentLine = remember { mutableIntStateOf(0) }
    val isImportingPlaylist = remember { mutableStateOf(false) }

    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    // Used for dimming the screen, not yet implemented
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportFromOnlineDialog by remember { mutableStateOf(false) }
    var showFAB by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Playlists",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        },
        floatingActionButton = {
            if (showFAB) {
                FloatingActionButtonMenu(
                    modifier = Modifier.padding(bottom = if (showControlBar) 70.dp else 0.dp),
                    expanded = fabMenuExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            modifier = Modifier.semantics {
                                stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                                contentDescription = "Toggle menu"
                            },
                            checked = fabMenuExpanded,
                            onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                        ) {
                            val imageVector by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                                }
                            }
                            Icon(
                                painter = rememberVectorPainter(imageVector),
                                contentDescription = null,
                                modifier = Modifier.animateIcon({ checkedProgress }),
                            )
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            showCreateDialog = true
                        },
                        icon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                        text = { Text(text = "Create new") },
                    )

                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            showImportFromOnlineDialog = true
                        },
                        icon = { Icon(Icons.Default.CloudDownload, contentDescription = null) },
                        text = { Text(text = "Import from online") },
                    )

                    // 3. Import from File (Your existing logic)
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            scope.launch {
                                val file = FileKit.openFilePicker(mode = FileKitMode.Single)
                                if (file?.extension == "csv") {
                                    SmartMessage(
                                        "Importing...",
                                        context = context,
                                        durationLong = true
                                    )
                                    val csvContent = file.readString()
                                    totalLines.intValue =
                                        csvContent.lines().drop(1).filter { it.isNotBlank() }.size
                                    currentLine.intValue = 0
                                    isImportingPlaylist.value = true
                                    try {
                                        importPlaylistFromCsv(csvContent, context, database)
                                            .collect { currentIndex ->
                                                currentLine.intValue = currentIndex + 1
                                            }
                                    } catch (e: Exception) {
                                        SmartMessage(
                                            "Import failed: ${e.message}",
                                            context = context,
                                            type = PopupType.Error
                                        )
                                    } finally {
                                        isImportingPlaylist.value = false
                                    }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                        text = { Text(text = "Import from file") },
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (showCreateDialog) {
            PlaylistCreateNewDialog(
                onDismissRequest = {
                    showCreateDialog = false
                },
                onConfirmation = { text ->
                    showCreateDialog = false
                    scope.launch {
                        database.playlistDao().insertPlaylist(Playlist(name = text))
                    }
                }
            )
        }
        if (showImportFromOnlineDialog) {
            PlaylistImportFromOnlineDialog(
                onDismissRequest = {
                    showImportFromOnlineDialog = false
                },
                onConfirmation = { url ->
                    focusManager.clearFocus(true)
                    keyboardController?.hide()

                    showImportFromOnlineDialog = false
                    val uri = url.toUri()
                    val listParam = uri.getQueryParameter("list") ?: ""

                    val playlistId = if (listParam.length == 34) {
                        "VL$listParam"
                    } else {
                        listParam
                    }

                    Log.i("PlaylistScreen", "playlistId = $playlistId")

                    scope.launch {
                        isImportingPlaylist.value = true
                        totalLines.intValue = 1
                        showFAB = false
                        val playlistAndSongs = getPlaylistAndSongs(playlistId)
                        if (playlistAndSongs != null) {
                            withContext(Dispatchers.Main) {
//                                isImportingPlaylist.value = true
                                totalLines.intValue = playlistAndSongs.songs.size
                            }
                            database.withTransaction {
                                val databasePlaylistId =
                                    database.playlistDao().insertPlaylist(playlistAndSongs.playlist)
                                Log.i("PlaylistScreen", "databasePlaylistId = $databasePlaylistId")

                                playlistAndSongs.songs.forEachIndexed { index, song ->

                                    Log.i("PlaylistScreen", "adding song to db ${song.title}")
                                    database.songDao().upsertSong(song)
                                    database.playlistDao()
                                        .insertSongAtEndOfPlaylist(song.id, databasePlaylistId)

                                    withContext(Dispatchers.Main) {
                                        currentLine.intValue = index + 1
                                    }
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            isImportingPlaylist.value = false
                            totalLines.intValue = 0
                            currentLine.intValue = 0
                            showFAB = true
                        }
                    }
                }
            )
        }
        if (!isImportingPlaylist.value && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator()
            }
        } else {

            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = if (showControlBar) 70.dp else 0.dp // 70.dp (bar) + 16.dp (margin)
                ),
                columns = GridCells.Adaptive(minSize = 100.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistComponent(
                        playlist = playlist, navController, onRemove = {
                            scope.launch {
                                database.playlistDao().deletePlaylist(it)
                            }
                        },
                        database = database,
                        onClick = { navController.navigate(PlaylistOfflineDetailScreen(playlist.id)) }
                    )
                }
                if (isImportingPlaylist.value && totalLines.intValue > 0) {
                    item {
                        // https://proandroiddev.com/cheatsheet-for-centering-items-in-jetpack-compose-1e3534415237
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularWavyProgressIndicator(
                                // Use .intValue for both the numerator and denominator
                                progress = { currentLine.intValue.toFloat() / totalLines.intValue.toFloat() },
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}