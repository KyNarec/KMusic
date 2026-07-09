package com.kynarec.kmusic.ui.components.settings.logs

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kynarec.kmusic.ui.theme.KMusicTheme
import java.io.File

@Composable
fun DeleteLogDialog(
    modifier: Modifier = Modifier,
    file: File,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null
            )
        },
        title = {
            Text(text = "Delete log file?")
        },
        text = {
            Text(text = "Are you sure you want to delete the log file \"${file.name}\"?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest,
                shape = ButtonDefaults.outlinedShape,
                colors = ButtonDefaults.outlinedButtonColors(),
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showSystemUi = true)
@Composable
fun PreviewStopAllDownloadingDialog() {
    KMusicTheme(darkTheme = false, dynamicColor = false) {
        Scaffold() { paddingValues ->
            DeleteLogDialog(
                modifier = Modifier.padding(paddingValues),
                onDismissRequest = {  },
                onConfirm = {  },
                file = File("", "kmusic-logs-2026-07-08-21-38-07.logcat")
            )
        }
    }
}