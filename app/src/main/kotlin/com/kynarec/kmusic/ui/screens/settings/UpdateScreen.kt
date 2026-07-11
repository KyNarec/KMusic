package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationImportant
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.enums.ReleaseNotificationType
import com.kynarec.kmusic.service.update.DownloadStatus
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateAction
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import com.kynarec.kmusic.utils.Constants.SHOW_PRE_RELEASES_KEY
import com.kynarec.kmusic.utils.formatDate
import com.kynarec.kmusic.utils.singleSegmentedShape
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun UpdateScreen(
    modifier: Modifier = Modifier,
    updateViewModel: UpdateViewModel = koinActivityViewModel(),
    appViewModel: AppViewModel = koinActivityViewModel(),
    prefs: SettingsViewModel = koinActivityViewModel()
) {
    val context = LocalContext.current
    val showControlBar = appViewModel.state.collectAsStateWithLifecycle().value.showControlBar
    val bottomPadding = if (showControlBar) 70.dp else 0.dp

    val state by updateViewModel.state.collectAsStateWithLifecycle()
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    val latestNonPreRelease by remember(state.releases) { mutableStateOf(state.releases.find { !it.preRelease }) }
    PullToRefreshBox(
        isRefreshing = state.fetchingLoadingState != 0f,
        onRefresh = { updateViewModel.onAction(UpdateAction.Refresh) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )

                    Spacer(Modifier.height(4.dp))

                    SegmentedListItem(
                        onClick = {
                            prefs.putBoolean(SHOW_PRE_RELEASES_KEY, !state.showPreReleases)
                        },
                        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                        colors = colors,
                        leadingContent = { Icon(Icons.Rounded.Preview, null) },
                        content = { Text("Show Pre-Releases") },
                        supportingContent = {
                            Text("Choose whether to show pre-releases in the list below")
                        },
                        trailingContent = {
                            Switch(
                                checked = state.showPreReleases,
                                onCheckedChange = {
                                    prefs.putBoolean(SHOW_PRE_RELEASES_KEY, it)
                                },
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    )

                    var expanded by rememberSaveable { mutableStateOf(false) }
                    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                        SegmentedListItem(
                            onClick = { expanded = !expanded },
                            shapes = if (expanded) singleSegmentedShape()
                            else ListItemDefaults.segmentedShapes(1, 2),
                            colors = colors,
                            leadingContent = { Icon(Icons.Rounded.NotificationImportant, null) },
                            content = { Text("Notification") },
                            supportingContent = { Text("Choose on which new release type you will be notified") },
                            overlineContent = {
                                Text(
                                    state.releaseNotificationType.toString(),
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
                        )

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                            exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                                ReleaseNotificationType.entries.forEachIndexed { index, releaseNotificationType ->
                                    SegmentedListItem(
                                        checked = state.releaseNotificationType == releaseNotificationType,
                                        onCheckedChange = {
                                            prefs.putReleaseNotification(
                                                releaseNotificationType
                                            )
                                        },
                                        colors = colors,
                                        shapes =
                                            ListItemDefaults.segmentedShapes(
                                                index = index,
                                                count = ReleaseNotificationType.entries.size
                                            ),
                                        trailingContent = {
                                            RadioButton(
                                                selected = state.releaseNotificationType == releaseNotificationType,
                                                onClick = null
                                            )
                                        },
                                        content = { Text(releaseNotificationType.toString()) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Releases",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp)
                )
                Spacer(Modifier.height(4.dp))
            }
            item {
                AnimatedVisibility(
                    visible = state.fetchingLoadingState != 0f,
                    modifier = Modifier.semantics {
                        stateDescription = "Loading"
                    }
                ) {
                    LinearWavyProgressIndicator(
                        progress = { state.fetchingLoadingState },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            itemsIndexed(
                state.releases.filter { state.showPreReleases || !it.preRelease || state.currentInstalledVersion == it.tagName.removePrefix("v") },
                key = { _, release -> release.id }) { index, release ->
                var expanded by retain { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SegmentedListItem(
                        onClick = { expanded = !expanded },
                        shapes = if (expanded) singleSegmentedShape()
                        else ListItemDefaults.segmentedShapes(index, state.releases.size),
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    release.name ?: "Unknown Release Title",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (release.preRelease) {
                                    Box(
                                        Modifier
                                            .clip(
                                                RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.tertiaryContainer
                                                ),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(
                                                start = 2.dp,
                                                top = 4.dp,
                                                bottom = 4.dp,
                                                end = 6.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Pre-Release",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }
                                }
                                if (latestNonPreRelease?.id == release.id) {
                                    Box(
                                        Modifier
                                            .clip(
                                                RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                ),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(
                                                start = 2.dp,
                                                top = 4.dp,
                                                bottom = 4.dp,
                                                end = 6.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Latest",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }
                                }
                                if (state.currentInstalledVersion == release.tagName.removePrefix("v")) {
                                    Box(
                                        Modifier
                                            .clip(
                                                RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.primaryContainer
                                                ),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(
                                                start = 2.dp,
                                                top = 4.dp,
                                                bottom = 4.dp,
                                                end = 6.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Installed",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = colors,
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
                    )
                    val optionsCount = 2
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                        exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                            SegmentedListItem(
                                colors = colors,
                                shapes =
                                    ListItemDefaults.segmentedShapes(
                                        index = 0,
                                        count = optionsCount
                                    ),
                                leadingContent = {
                                    Icon(Icons.Rounded.Info, null)
                                },
                                content = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    )
                                    {
                                        Text("Release Notes")
                                        Text(
                                            formatDate(release.publishedAt),
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.align(Alignment.Bottom)
                                        )
                                    }
                                },
                                supportingContent = {
                                    Markdown(
                                        content = release.body,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = markdownColor(
                                            text = MaterialTheme.colorScheme.onSurface,
                                        ),
                                        typography = markdownTypography(
                                            h1 = MaterialTheme.typography.titleMedium,
                                            h2 = MaterialTheme.typography.titleMedium,
                                            h3 = MaterialTheme.typography.titleMedium,
                                            h4 = MaterialTheme.typography.titleMedium,
                                            h5 = MaterialTheme.typography.titleMedium,
                                            h6 = MaterialTheme.typography.titleMedium,
                                            text = MaterialTheme.typography.bodyMedium,
                                            paragraph = MaterialTheme.typography.bodyMedium,
                                            ordered = MaterialTheme.typography.bodyMedium,
                                            bullet = MaterialTheme.typography.bodyMedium,
                                            textLink = TextLinkStyles(
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = TextDecoration.Underline
                                                ).toSpanStyle()
                                            )
                                        )
                                    )
                                }
                            )
                            SegmentedListItem(
                                onClick = {
                                    updateViewModel.onAction(
                                        UpdateAction.Download(
                                            release,
                                            context
                                        )
                                    )
                                },
                                colors = colors,
                                shapes =
                                    ListItemDefaults.segmentedShapes(
                                        index = 1,
                                        count = optionsCount
                                    ),
                                leadingContent = { Icon(Icons.Rounded.Download, null) },
                                trailingContent = {

                                    val thisDownloadStatue =
                                        state.downloadStatus.filter { it.key == release.id }.values.firstOrNull()
                                    when (thisDownloadStatue) {
                                        is DownloadStatus.Progress -> {
                                            LinearWavyProgressIndicator(
                                                progress = { thisDownloadStatue.percent.toFloat() / 100f },
                                                modifier = Modifier.fillMaxWidth(0.5f)
                                            )
                                        }

                                        is DownloadStatus.Completed -> {
                                            Icon(
                                                Icons.Rounded.DownloadDone,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        is DownloadStatus.Error -> {
                                            IconButton(onClick = {
                                                updateViewModel.onAction(
                                                    UpdateAction.DownloadErrorDialog(
                                                        thisDownloadStatue.message
                                                    )
                                                )
                                            }) {
                                                Icon(
                                                    Icons.Rounded.Error,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        else -> {}
                                    }
                                },
                                content = { Text("Download") }
                            )
                        }
                    }
                }

            }

            if (showControlBar) {
                item {
                    Spacer(Modifier.height(bottomPadding + 16.dp))
                }
            }
        }
    }

    state.fetchingError?.let { error ->
        if (error.startsWith("API rate limit")) {
            val dismiss = { updateViewModel.onAction(UpdateAction.CloseErrorDialog) }
            AlertDialog(
                onDismissRequest = dismiss,
                icon = { Icon(Icons.Rounded.Error, null) },
                title = { Text("API rate limit reached") },
                text = {
                    Markdown(
                        content = error,
                        modifier = Modifier.fillMaxWidth(),
                        colors = markdownColor(
                            text = MaterialTheme.colorScheme.onSurface,
                        ),
                        typography = markdownTypography(
                            h1 = MaterialTheme.typography.titleMedium,
                            h2 = MaterialTheme.typography.titleMedium,
                            h3 = MaterialTheme.typography.titleMedium,
                            h4 = MaterialTheme.typography.titleMedium,
                            h5 = MaterialTheme.typography.titleMedium,
                            h6 = MaterialTheme.typography.titleMedium,
                            text = MaterialTheme.typography.bodyMedium,
                            paragraph = MaterialTheme.typography.bodyMedium,
                            ordered = MaterialTheme.typography.bodyMedium,
                            bullet = MaterialTheme.typography.bodyMedium,
                            textLink = TextLinkStyles(
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                ).toSpanStyle()
                            )
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = dismiss,
                        shape = ButtonDefaults.outlinedShape,
                        colors = ButtonDefaults.outlinedButtonColors(),
                    ) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        } else {
            val dismiss = { updateViewModel.onAction(UpdateAction.CloseErrorDialog) }
            AlertDialog(
                onDismissRequest = dismiss,
                icon = { Icon(Icons.Rounded.Error, null) },
                title = { Text("Error fetching releases") },
                text = { Text(error) },
                confirmButton = {
                    Button(
                        onClick = dismiss,
                        shape = ButtonDefaults.outlinedShape,
                        colors = ButtonDefaults.outlinedButtonColors(),
                    ) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        }
    }

    state.downloadErrorDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { updateViewModel.onAction(UpdateAction.DownloadErrorDialog(null)) },
            icon = { Icon(Icons.Rounded.Error, null) },
            title = { Text("Error downloading releases") },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = { updateViewModel.onAction(UpdateAction.DownloadErrorDialog(null)) },
                    shape = ButtonDefaults.outlinedShape,
                    colors = ButtonDefaults.outlinedButtonColors(),
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
        )
    }
}