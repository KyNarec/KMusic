package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.Constants.DARK_MODE_KEY
import com.kynarec.kmusic.utils.Constants.DEFAULT_DARK_MODE
import com.kynarec.kmusic.utils.Constants.DEFAULT_DYNAMIC_COLORS
import com.kynarec.kmusic.utils.Constants.DEFAULT_WAVY_LYRICS_IDLE_INDICATOR
import com.kynarec.kmusic.utils.Constants.DYNAMIC_COLORS_KEY
import com.kynarec.kmusic.utils.Constants.WAVY_LYRICS_IDLE_INDICATOR_KEY
import org.koin.compose.viewmodel.koinActivityViewModel


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceScreen(
    prefs: SettingsViewModel = koinActivityViewModel(),
    appViewModel: AppViewModel = koinActivityViewModel(),
) {

    val showControlBar = appViewModel.state.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp
            )
    ) {
        item {
            val count = 4
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                var darkModeChecked by retain {
                    mutableStateOf(prefs.getBoolean(DARK_MODE_KEY, DEFAULT_DARK_MODE))
                }
                SegmentedListItem(
                    onClick = {
                        val newValue = !darkModeChecked
                        darkModeChecked = newValue
                        prefs.putBoolean(DARK_MODE_KEY, newValue)
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = count),
                    colors = colors,
                    leadingContent = { Icon(Icons.Default.DarkMode, null) },
                    content = { Text("Dark theme") },
                    supportingContent = {
                        Text("Use dark theme")
                    },
                    trailingContent = {
                        Switch(
                            checked = darkModeChecked,
                            onCheckedChange = {
                                darkModeChecked = it
                                prefs.putBoolean(DARK_MODE_KEY, it)
                            },
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                )

                var dynamicColorsChecked by retain {
                    mutableStateOf(prefs.getBoolean(DYNAMIC_COLORS_KEY, DEFAULT_DYNAMIC_COLORS))
                }
                SegmentedListItem(
                    onClick = {
                        val newValue = !dynamicColorsChecked
                        dynamicColorsChecked = newValue
                        prefs.putBoolean(DYNAMIC_COLORS_KEY, newValue)
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = count),
                    colors = colors,
                    leadingContent = { Icon(Icons.Default.FormatColorFill, null) },
                    content = { Text("Dynamic Colors") },
                    supportingContent = {
                        Text("Use dynamic android native colors")
                    },
                    trailingContent = {
                        Switch(
                            checked = dynamicColorsChecked,
                            onCheckedChange = {
                                dynamicColorsChecked = it
                                prefs.putBoolean(DYNAMIC_COLORS_KEY, it)
                            },
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                )

                var wavyLyricsIdleIndicatorChecked by retain {
                    mutableStateOf(prefs.getBoolean(WAVY_LYRICS_IDLE_INDICATOR_KEY, DEFAULT_WAVY_LYRICS_IDLE_INDICATOR))
                }
                SegmentedListItem(
                    onClick = {
                        val newValue = !wavyLyricsIdleIndicatorChecked
                        wavyLyricsIdleIndicatorChecked = newValue
                        prefs.putBoolean(WAVY_LYRICS_IDLE_INDICATOR_KEY, newValue)
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = count),
                    colors = colors,
                    leadingContent = { Icon(Icons.Default.GraphicEq, null) },
                    content = { Text("Wavy lyrics idle indicator") },
                    supportingContent = {
                        Text("Show a rhythmic wavy progress bar during instrumental breaks")
                    },
                    trailingContent = {
                        Switch(
                            checked = wavyLyricsIdleIndicatorChecked,
                            onCheckedChange = {
                                wavyLyricsIdleIndicatorChecked = it
                                prefs.putBoolean(WAVY_LYRICS_IDLE_INDICATOR_KEY, it)
                            },
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                )

                var coloredDownloadIndicatorChecked by retain {
                    mutableStateOf(prefs.coloredDownloadIndicator)
                }
                SegmentedListItem(
                    onClick = {
                        val newValue = !coloredDownloadIndicatorChecked
                        coloredDownloadIndicatorChecked = newValue
                        prefs.coloredDownloadIndicator = newValue
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 3, count = count),
                    colors = colors,
                    leadingContent = { Icon(Icons.Default.FormatColorFill, null) },
                    content = { Text("Colored download indicator") },
                    supportingContent = {
                        Text("Show a colored indicator when song is downloaded")
                    },
                    trailingContent = {

                        Switch(
                            checked = coloredDownloadIndicatorChecked,
                            onCheckedChange = {
                                coloredDownloadIndicatorChecked = it
                                prefs.coloredDownloadIndicator = it },
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                )
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
        }


        if (showControlBar) {
            item {
                Spacer(Modifier.height(bottomPadding))
            }
        }
    }
}