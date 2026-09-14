package com.kynarec.kmusic.ui.components.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun PlaylistRenameDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
    playlistName: String
) {
    var alwaysMinimizeLabel by remember { mutableStateOf(false) }
    val state = rememberTextFieldState()

    LaunchedEffect(Unit) {
        state.setTextAndPlaceCursorAtEnd(playlistName)
    }

    AlertDialog(
        icon = {
            Icon(Icons.Default.Edit, contentDescription = null)
        },
        title = {
            Text(text = "Rename Playlist")
        },
        text = {
            Column() {
                OutlinedTextField(
                    state = state,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("New Playlist Name") },
                    labelPosition = TextFieldLabelPosition.Attached(alwaysMinimize = alwaysMinimizeLabel),
                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(state.text.toString())
                }
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}