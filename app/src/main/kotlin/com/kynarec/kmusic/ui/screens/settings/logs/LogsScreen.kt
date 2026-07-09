package com.kynarec.kmusic.ui.screens.settings.logs

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.data.repository.logs.LogsRepository
import com.kynarec.kmusic.ui.Settings
import com.kynarec.kmusic.ui.components.settings.logs.DeleteLogDialog
import com.kynarec.kmusic.ui.components.settings.logs.LogTimespan
import com.kynarec.kmusic.ui.theme.KMusicTheme
import com.kynarec.kmusic.ui.viewModels.LogsActions
import com.kynarec.kmusic.ui.viewModels.LogsViewModel
import com.kynarec.kmusic.utils.singleSegmentedShape
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    logsViewModel: LogsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by logsViewModel.state.collectAsStateWithLifecycle()

    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val focusManager = LocalFocusManager.current
    val imeVisible = WindowInsets.isImeVisible

    LaunchedEffect(imeVisible) {
        if (!imeVisible) {
            focusManager.clearFocus()
        }
    }
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { logsViewModel.onAction(LogsActions.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        "Capture Logs",
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )

                    Spacer(Modifier.height(6.dp))

                    SegmentedListItem(
                        shapes = singleSegmentedShape(),
                        colors = colors
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.filenameValue ?: "",
                            onValueChange = { logsViewModel.onAction(LogsActions.SetFilenameValue(it)) },
                            label = { Text("Filename") },
                            supportingText = { Text("File extension is always .logcat") },
                            placeholder = {
                                Text(
                                    state.filenamePlaceholder,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
//                        trailingIcon = { if (state.isError) Icon(Icons.Filled.Error, null) },
                            singleLine = true,
//                        isError = state.isError,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LogTimespan.entries.forEachIndexed { index, logTimespan ->
                            SegmentedButton(
                                onClick = { logsViewModel.onAction(LogsActions.SetSelectedTimespan(logTimespan)) },
                                selected = state.selectedTimespan == logTimespan,
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = LogTimespan.entries.size),
                            ) {
                                Text(logTimespan.toString())
                            }
                        }
                    }

                    Spacer(Modifier.height(ListItemDefaults.SegmentedGap))

                    Button(
                        onClick = { logsViewModel.onAction(LogsActions.Capture) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isError
                    ) {
                        Text("Capture")
                    }
                }
            }


            item {
                Text(
                    "Saved Logs",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp)
                )
                Spacer(Modifier.height(6.dp))

            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    state.savedLogs.forEachIndexed { index, pair ->
                        val onClick = {
                            navController.navigate(Settings.Logs.LogFileScreen(pair.first.name))
                        }
                        SegmentedListItem(
                            onClick = onClick,
                            shapes = if (pair.second) singleSegmentedShape()
                            else ListItemDefaults.segmentedShapes(index, state.savedLogs.size),
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = onClick,
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.OpenInNew,
                                            contentDescription = "Open Item",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { logsViewModel.onAction(LogsActions.ToggleExpansion(index)) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (pair.second) Icons.Default.ExpandLess else Icons.Default.MoreHoriz,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                            colors = colors,
                            modifier = Modifier.semantics {
                                stateDescription = if (pair.second) "Expanded" else "Collapsed"
                            }
                        ) {
                            Text(pair.first.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        AnimatedVisibility(
                            visible = pair.second,
                            enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                            exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                                SegmentedListItem(
                                    onClick = { logsViewModel.onAction(LogsActions.ShowDeleteDialog(pair.first)) },
                                    colors = colors,
                                    shapes =
                                        ListItemDefaults.segmentedShapes(index = 1, count = 3),
                                    leadingContent = {
                                        Icon(Icons.Rounded.DeleteOutline, null)
                                    },
                                    content = { Text("Delete") }
                                )
                                SegmentedListItem(
                                    onClick = { logsViewModel.onAction(LogsActions.Share(pair.first, context)) },
                                    colors = colors,
                                    shapes =
                                        ListItemDefaults.segmentedShapes(index = 2, count = 3),
                                    leadingContent = {
                                        Icon(Icons.Rounded.Share, null)
                                    },
                                    content = { Text("Share") }
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    if (state.showDeleteDialog.first) {
        DeleteLogDialog(
            file = state.showDeleteDialog.second,
            onDismissRequest = { logsViewModel.onAction(LogsActions.HideDeleteDialog) },
            onConfirm = {
                logsViewModel.onAction(LogsActions.Delete(state.showDeleteDialog.second))
            }
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun PreviewLogsScreen() {
    KMusicTheme(dynamicColor = false) {
        Scaffold() { paddingValues ->
            LogsScreen(
                modifier = Modifier.padding(paddingValues),
                logsViewModel = LogsViewModel(logsRepository = LogsRepository(LocalContext.current)),
                navController = rememberNavController()
            )
        }
    }
}