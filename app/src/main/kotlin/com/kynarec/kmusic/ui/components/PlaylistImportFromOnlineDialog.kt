package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistImportFromOnlineDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmation: (url: String) -> Unit,
) {
    var alwaysMinimizeLabel by remember { mutableStateOf(false) }
    val state = rememberTextFieldState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        icon = {
            Icon(
                Icons.Default.CloudDownload,
                contentDescription = null
            )
        },
        title = {
            Text(text = "Import new Playlist")
        },
        text = {
            Column() {
                OutlinedTextField(
                    state = state,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("Playlist Url") },
                    labelPosition = TextFieldLabelPosition.Attached(alwaysMinimize = alwaysMinimizeLabel),
                    modifier = Modifier.focusRequester(focusRequester),

                )
            }
        },
        onDismissRequest = {
            focusManager.clearFocus()
            keyboardController?.hide()
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onConfirmation(state.text.toString())
                }
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}