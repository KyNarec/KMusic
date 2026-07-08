package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.enums.PlayerRepeatMode
import com.kynarec.kmusic.enums.StartDestination
import com.kynarec.kmusic.enums.TransitionEffect
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.PlayerScreenAction
import com.kynarec.kmusic.ui.viewModels.PlayerScreenViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.singleSegmentedShape
import com.kynarec.kmusic.utils.transition_chop
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InterfaceScreen(
    prefs: SettingsViewModel = koinActivityViewModel(),
    appViewModel: AppViewModel = koinActivityViewModel(),
    playerScreenViewModel: PlayerScreenViewModel = koinViewModel(),
) {

    val startDestinationFlow by prefs.startDestinationFlow.collectAsStateWithLifecycle(prefs.startDestination)
    val playerRepeatModeFlow by prefs.playerRepeatModeFlow.collectAsStateWithLifecycle(prefs.playerRepeatMode)
    val transitionEffectFlow by prefs.transitionEffectFlow.collectAsStateWithLifecycle(prefs.transitionEffect)


    val showControlBar = appViewModel.state.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        item {
            var expanded by rememberSaveable { mutableStateOf(false) }
            val count = StartDestination.entries.size
            val itemCount = 1 + if (expanded) count else 0
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                SegmentedListItem(
                    onClick = { expanded = !expanded },
                    shapes = if (expanded) singleSegmentedShape()
                    else ListItemDefaults.segmentedShapes(0, 3),
                    colors = colors,
                    leadingContent = { Icon(Icons.Default.PinDrop, null) },
                    content = { Text("Start Destination") },
                    supportingContent = { Text("Choose on which screen the app starts") },
                    overlineContent = {
                        Text(
                            startDestinationFlow.label,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .semantics {
                                stateDescription = if (expanded) "Expanded" else "Collapsed"
                            }
//                            .height(itemHeight + 16.dp),
                )

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                    exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                        StartDestination.entries.forEachIndexed { index, destination ->
                            SegmentedListItem(
                                checked = startDestinationFlow == destination,
                                onCheckedChange = { prefs.putStartDestination(destination) },
                                colors = colors,
                                shapes =
                                    ListItemDefaults.segmentedShapes(
                                        index = index + 1,
                                        count = itemCount
                                    ),
                                trailingContent = {
                                    RadioButton(
                                        selected = startDestinationFlow == destination,
                                        onClick = null
                                    )
                                },
                                content = { Text(destination.label) }
                            )
                        }
                    }
                }
            }
        }

        item {
            var expanded by rememberSaveable { mutableStateOf(false) }
            val count = TransitionEffect.entries.size
            val itemCount = 1 + if (expanded) count else 0
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                SegmentedListItem(
                    onClick = { expanded = !expanded },
                    shapes = if (expanded) singleSegmentedShape()
                    else ListItemDefaults.segmentedShapes(1, 3),
                    colors = colors,
                    leadingContent = { Icon(transition_chop, null) },
                    content = { Text("Screen Transition Effect") },
                    supportingContent = { Text("Choose how screens transition in the app") },
                    overlineContent = {
                        Text(
                            transitionEffectFlow.label,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .semantics {
                                stateDescription = if (expanded) "Expanded" else "Collapsed"
                            }
//                            .height(itemHeight + 16.dp),
                )

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                    exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                        TransitionEffect.entries.forEachIndexed { index, effect ->
                            SegmentedListItem(
                                checked = transitionEffectFlow == effect,
                                onCheckedChange = { prefs.putTransitionEffect(effect) },
                                colors = colors,
                                shapes =
                                    ListItemDefaults.segmentedShapes(
                                        index = index + 1,
                                        count = itemCount
                                    ),
                                trailingContent = {
                                    RadioButton(
                                        selected = transitionEffectFlow == effect,
                                        onClick = null
                                    )
                                },
                                content = { Text(effect.label) })
                        }
                    }
                }
            }
        }
        item {
            var expanded by rememberSaveable { mutableStateOf(false) }
            val count = PlayerRepeatMode.entries.size
            val itemCount = 1 + if (expanded) count else 0
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                SegmentedListItem(
                    onClick = { expanded = !expanded },
                    shapes = if (expanded) singleSegmentedShape()
                    else ListItemDefaults.segmentedShapes(2, 3),
                    colors = colors,
                    leadingContent = { Icon(Icons.Default.Repeat, null) },
                    content = { Text("Player repeat mode") },
                    supportingContent = { Text("Changes how the player acts at the end of the queue") },
                    overlineContent = {
                        Text(
                            playerRepeatModeFlow.toString(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .semantics {
                                stateDescription = if (expanded) "Expanded" else "Collapsed"
                            }
//                            .height(itemHeight + 32.dp),
                )

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                    exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                        PlayerRepeatMode.entries.forEachIndexed { index, repeatMode ->
                            SegmentedListItem(
                                checked = playerRepeatModeFlow == repeatMode,
                                onCheckedChange = {
                                    prefs.putPlayerRepeatMode(repeatMode)
                                    playerScreenViewModel.onAction(
                                        PlayerScreenAction.ToggleRepeatMode(
                                            repeatMode
                                        )
                                    )
                                },
                                colors = colors,
                                shapes =
                                    ListItemDefaults.segmentedShapes(
                                        index = index + 1,
                                        count = itemCount
                                    ),
                                trailingContent = {
                                    RadioButton(
                                        selected = playerRepeatModeFlow == repeatMode,
                                        onClick = null
                                    )
                                },
                                content = { Text(repeatMode.toString()) })
                        }
                    }
                }
            }
        }

        if (showControlBar) {
            item {
                Spacer(Modifier.height(bottomPadding))
            }
        }
    }
}