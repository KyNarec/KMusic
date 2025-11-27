package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingFolderComponent(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(Modifier.clickable(onClick = onClick).fillMaxWidth().height(80.dp).padding(16.dp)) {
        Box(Modifier.size(48.dp).align(Alignment.CenterVertically).padding(8.dp)){
            Icon(icon, contentDescription = title, Modifier.fillMaxSize())
        }
//                        Spacer(Modifier.width(8.dp))
        Column(Modifier.height(48.dp).align(Alignment.CenterVertically)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}