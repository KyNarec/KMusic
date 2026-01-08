package com.kynarec.kmusic.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kynarec.kmusic.enums.StartDestination
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.service.update.UpdateManager
import com.kynarec.kmusic.service.update.getCurrentVersion
import com.kynarec.kmusic.ui.components.UpdateDialog
import com.kynarec.kmusic.ui.components.settings.SettingComponentEnumChoice
import com.kynarec.kmusic.ui.components.settings.SettingComponentSwitch
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import com.kynarec.kmusic.utils.Constants.DARK_MODE_KEY
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import com.kynarec.kmusic.utils.Constants.DYNAMIC_COLORS_KEY
import com.kynarec.kmusic.utils.SmartMessage
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    prefs: SettingsViewModel,
    navController: NavHostController,
    updateManager: UpdateManager,
    updateViewModel: UpdateViewModel,
) {
    // var transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Fade)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val transitionEffectFlow by prefs.transitionEffectFlow.collectAsStateWithLifecycle(prefs.transitionEffect)
    val startDestinationFlow by prefs.startDestinationFlow.collectAsStateWithLifecycle(prefs.startDestination)



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
            LazyColumn{
                item {
                    SettingComponentSwitch(
                        icon = Icons.Default.DarkMode,
                        title = "Dark theme",
                        description = "Use dark theme",
                        prefs = prefs,
                        switchId = DARK_MODE_KEY,
                        defaultValue = DEFAULT_DARK_MODE
                    )
                }
                item {
                    SettingComponentSwitch(
                        icon = Icons.Default.FormatColorFill,
                        title = "Dynamic Colors",
                        description = "Use dynamic android native colors",
                        prefs = prefs,
                        switchId = DYNAMIC_COLORS_KEY,
                        defaultValue = DEFAULT_DYNAMIC_COLORS
                    )
                }
                item {
                    SettingComponentEnumChoice(
                        icon = Icons.Default.Animation,
                        title = "Screen Transition Effect",
                        description = "Choose how screens transition in the app",
                        enumValues = TransitionEffect.entries, // Now correctly seen as List<TransitionEffect>
                        selected = transitionEffectFlow,
                        onValueSelected = {
                            scope.launch { prefs.putTransitionEffect(it) }
                        },
                        labelMapper = { it.label }
                    )
                }

                item {
                    SettingComponentEnumChoice(
                        icon = Icons.Default.PinDrop,
                        title = "Start Destination",
                        description = "Choose on which screen the app starts",
                        enumValues = StartDestination.entries,
                        selected = startDestinationFlow,
                        onValueSelected = {
                            scope.launch { prefs.putStartDestination(it) }
                        },
                        labelMapper = { it.label }
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch{
                                    updateViewModel.checkForUpdates()
                                }
                                SmartMessage("Checking for Updates...", context = context)
                            }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Update, contentDescription = "update", modifier = Modifier.size(48.dp).padding(8.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Check Updates", style = MaterialTheme.typography.titleMedium)
                                Text("Check whether there is a new update available", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
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
                Text("v$version",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
