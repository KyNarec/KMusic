package com.kynarec.kmusic.ui.components.song

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

@Composable
fun StopDownloadingDialog(
    modifier: Modifier = Modifier,
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
            Text(text = "Stop Downloading?")
        },
        text = {
            Text(text = "Are you sure you want to stop downloading all songs?")
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
fun PreviewStopDownloadingDialog() {
    KMusicTheme(darkTheme = false, dynamicColor = false) {
        Scaffold() { paddingValues ->
            StopDownloadingDialog(
                modifier = Modifier.padding(paddingValues),
                onDismissRequest = {  },
                onConfirm = {  }
            )
        }
    }
}