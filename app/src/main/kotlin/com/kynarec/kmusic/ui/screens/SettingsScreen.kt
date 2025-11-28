package com.kynarec.kmusic.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.service.update.UpdateManager
import com.kynarec.kmusic.ui.screens.settings.SettingComponentEnumChoice
import com.kynarec.kmusic.ui.screens.settings.SettingComponentSwitch
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import com.kynarec.kmusic.utils.Constants.DARK_MODE_KEY
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import com.kynarec.kmusic.utils.Constants.DYNAMIC_COLORS_KEY
import com.kynarec.kmusic.utils.Constants.TRANSITION_EFFECT_KEY
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

    if (updateViewModel.showDialog && updateViewModel.updateInfo != null) {
        Log.i("UpdateChecker", "Now showing update dialog")
        UpdateDialog(
            updateInfo = updateViewModel.updateInfo!!,
            downloadStatus = updateViewModel.downloadStatus,
            supportsInAppInstallation = updateManager.supportsInAppInstallation(),
            onDismiss = { updateViewModel.dismissDialog() },
            onUpdate = { updateViewModel.startDownload() },
            onOpenStore = { updateViewModel.openStore() }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column {
            LazyColumn{
                item {
                    SettingComponentEnumChoice(
                        icon = Icons.Default.Animation,
                        title = "Screen Transition Effect",
                        description = "Choose how screens transition in the app",
                        prefs = prefs,
                        key = TRANSITION_EFFECT_KEY,
                        enumValues = TransitionEffect.all,
                        default = TransitionEffect.Fade,
                        labelMapper = { it.name }
                    )
                }
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch{
                                    updateViewModel.checkForUpdates()
                                }
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
        }
    }
}
