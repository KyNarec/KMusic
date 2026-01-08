package com.kynarec.kmusic.ui.components.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit,
    onTimerSelected: (Long) -> Unit
) {
    val options = listOf(15, 30, 45, 60)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Sleep Timer") },
        text = {
            Column {
                options.forEach { minutes ->
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onTimerSelected(minutes.toLong()) }
                    ) {
                        Text("$minutes minutes")
                    }
                }
            }
        },
        confirmButton = {}
    )
}