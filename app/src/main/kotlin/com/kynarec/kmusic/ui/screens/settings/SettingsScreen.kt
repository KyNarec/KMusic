package com.kynarec.kmusic.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.service.update.UpdateManager
import com.kynarec.kmusic.service.update.getCurrentVersion
import com.kynarec.kmusic.ui.Settings
import com.kynarec.kmusic.ui.components.UpdateDialog
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import com.kynarec.kmusic.utils.SmartMessage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    appViewModel: AppViewModel = koinActivityViewModel(),
    updateManager: UpdateManager = koinInject(),
    updateViewModel: UpdateViewModel = koinActivityViewModel(),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showControlBar = appViewModel.state.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    if (updateViewModel.showDialog && updateViewModel.updateInfo != null) {
        Log.i("UpdateChecker", "Now showing update dialog")
        UpdateDialog(
            updateInfo = updateViewModel.updateInfo!!,
            downloadStatus = updateViewModel.downloadStatus,
            supportsInAppInstallation = updateManager.supportsInAppInstallation(),
            onDismiss = { updateViewModel.dismissDialog() },
            onUpdate = { updateViewModel.startDownload() },
            onOpenStore = { updateViewModel.openStore() })
    }

    Box(Modifier.fillMaxSize()) {
        Column {
            val itemHeight = 72.dp
            val colors =
                ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            LazyColumn(
                Modifier.padding(
                    horizontal = 16.dp
                )
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                        SegmentedListItem(
                            onClick = { navController.navigate(Settings.Appearance) },
                            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                            colors = colors,
                            leadingContent = { Icon(Icons.Rounded.Palette, null) },
                            content = { Text("Appearance") },
                            modifier = Modifier.height(itemHeight)
                        )
                        SegmentedListItem(
                            onClick = { navController.navigate(Settings.Interface) },
                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                            colors = colors,
                            leadingContent = { Icon(Icons.Rounded.Interests, null) },
                            content = { Text("Interface") },
                            modifier = Modifier.height(itemHeight)

                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    val count = 4
                    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                        SegmentedListItem(
                            onClick = { navController.navigate(Settings.DataScreen) },
                            shapes = ListItemDefaults.segmentedShapes(index = 0, count = count),
                            colors = colors,
                            leadingContent = { Icon(Icons.Rounded.Storage, null) },
                            content = { Text("Data") },
                            modifier = Modifier.height(itemHeight)

                        )
                        SegmentedListItem(
                            onClick = {
                                scope.launch {
                                    updateViewModel.checkForUpdates()
                                }
                                SmartMessage("Checking for Updates...", context = context)
                            },
                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = count),
                            colors = colors,
                            leadingContent = { Icon(Icons.Rounded.Update, null) },
                            content = { Text("Check Updates") },
                            modifier = Modifier.height(itemHeight)
                        )

                        SegmentedListItem(
                            onClick = { navController.navigate(Settings.Logs.LogsScreen) },
                            shapes = ListItemDefaults.segmentedShapes(index = 2, count = count),
                            colors = colors,
                            leadingContent = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.InsertDriveFile,
                                    null
                                )
                            },
                            content = { Text("Logs") },
                            modifier = Modifier.height(itemHeight)
                        )

                        SegmentedListItem(
                            onClick = { navController.navigate(Settings.AboutScreen) },
                            shapes = ListItemDefaults.segmentedShapes(index = 3, count = count),
                            colors = colors,
                            leadingContent = { Icon(Icons.Rounded.Info, null) },
                            content = { Text("About") },
                            modifier = Modifier.height(itemHeight)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val version = getCurrentVersion()
                Text(
                    "v$version",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (showControlBar) {
                Spacer(Modifier.height(bottomPadding))
            }
        }
    }
}