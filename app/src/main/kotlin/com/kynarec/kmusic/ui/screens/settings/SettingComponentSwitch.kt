package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingComponentSwitch(
    icon: ImageVector,
    title: String,
    description: String,
    prefs: SettingsViewModel,
    switchId: String
) {
    var checked by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        checked = prefs.getBoolean(switchId, true)
//        checked = secureStorage.getBoolean(switchId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Visible
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                scope.launch {
                    prefs.putBoolean(switchId, it)
                }
            },
            modifier = Modifier.padding(8.dp)
        )
    }
}
