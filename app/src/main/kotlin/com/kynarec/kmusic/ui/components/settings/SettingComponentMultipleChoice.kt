package com.kynarec.kmusic.ui.components.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
inline fun <reified T : Enum<T>> SettingComponentEnumChoice(
    icon: ImageVector,
    title: String,
    description: String,
    prefs: SettingsViewModel,
    key: String,
    enumValues: List<T>,
    default: T,
    crossinline labelMapper: (T) -> String = { it.name }
) {
    var selected by remember { mutableStateOf(default) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    // Load stored value
    LaunchedEffect(Unit) {
        selected = prefs.getEnum(key, default)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(48.dp).padding(8.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            //Text(labelMapper(selected), style = MaterialTheme.typography.bodyMedium)
        }
    }

    // M3 Dialog Popup
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    enumValues.forEach { value ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selected = value
                                    scope.launch {
                                        prefs.putEnum(key, value)
                                    }
                                    showDialog = false
                                }
                        ) {
                            RadioButton(
                                selected = selected == value,
                                onClick = {
                                    selected = value
                                    showDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(labelMapper(value))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}