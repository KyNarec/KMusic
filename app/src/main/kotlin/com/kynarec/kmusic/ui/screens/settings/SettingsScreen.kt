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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.kynarec.kmusic.ui.components.settings.SettingsFolder
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import com.kynarec.kmusic.utils.SmartMessage
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen(
    prefs: SettingsViewModel,
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    updateManager: UpdateManager,
    updateViewModel: UpdateViewModel,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showControlBar = musicViewModel.uiState.collectAsStateWithLifecycle().value.showControlBar
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
        Column() {
            LazyColumn(
                Modifier.padding(
                    horizontal = 16.dp
                )
            ) {
                item {
                    ElevatedCard(
                        Modifier.fillMaxWidth()
                    ) {
                        SettingsFolder(
                            title = { Text("Appearance") },
                            icon = { Icon(Icons.Rounded.Palette, null) },
                            onClick = { navController.navigate(Settings.Appearance) }
                        )
                        SettingsFolder(
                            title = { Text("Interface") },
                            icon = { Icon(Icons.Rounded.Interests, null) },
                            onClick = { navController.navigate(Settings.Interface) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ElevatedCard(
                        Modifier.fillMaxWidth()
                    ) {
                        SettingsFolder(
                            title = { Text("Check Updates") },
                            icon = { Icon(Icons.Rounded.Update, null) },
                            onClick = {
                                scope.launch {
                                    updateViewModel.checkForUpdates()
                                }
                                SmartMessage("Checking for Updates...", context = context)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ElevatedCard(
                        Modifier.fillMaxWidth()
                    ) {
                        SettingsFolder(
                            title = { Text("About") },
                            icon = { Icon(Icons.Rounded.Info, null) },
                            onClick = { navController.navigate(Settings.AboutScreen) }
                        )
                    }
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
