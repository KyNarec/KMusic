package com.kynarec.kmusic.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.service.update.DownloadStatus
import com.kynarec.kmusic.service.update.UpdateInfo
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography


@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    downloadStatus: DownloadStatus,
    supportsInAppInstallation: Boolean,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    onOpenStore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (downloadStatus !is DownloadStatus.Progress) {
                onDismiss()
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.NewReleases,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Update Available",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Version info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Version ${updateInfo.version}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = updateInfo.releaseDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                // Download progress (if applicable)
                if (downloadStatus is DownloadStatus.Progress && supportsInAppInstallation) {
                    DownloadProgressCard(downloadStatus)
                }


                Markdown(
                    content = updateInfo.releaseNotes,
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
                                fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline
                            ).toSpanStyle()
                        )
                    )
                )

                // Platform notice
                if (!supportsInAppInstallation) {
                    InfoCard(
                        text = "You'll be redirected to the ${getPlatformStoreName()} to install the update."
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (supportsInAppInstallation) {
                        if (downloadStatus !is DownloadStatus.Progress) {
                            onUpdate()
                        }
                    } else {
                        onOpenStore()
                    }
                },
                enabled = downloadStatus !is DownloadStatus.Progress
            ) {
                Text(
                    text = when {
                        downloadStatus is DownloadStatus.Progress -> "${downloadStatus.percent}%"
                        supportsInAppInstallation -> "Download"
                        else -> "Open ${getPlatformStoreName()}"
                    },
                    modifier = Modifier.animateContentSize()
                )
            }
        },
        dismissButton = {
            if (downloadStatus !is DownloadStatus.Progress && !updateInfo.isMandatory) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}

@Composable
private fun DownloadProgressCard(status: DownloadStatus.Progress) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Downloading...",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "${status.percent}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            LinearProgressIndicator(
                progress = { status.percent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )

            if (status.totalBytes > 0) {
                Text(
                    text = "${formatBytes(status.bytesDownloaded)} / ${formatBytes(status.totalBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun getPlatformStoreName(): String = "Play Store"

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}