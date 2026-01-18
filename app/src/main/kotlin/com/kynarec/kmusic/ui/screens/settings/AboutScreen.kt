package com.kynarec.kmusic.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.R
import com.kynarec.kmusic.service.update.getCurrentVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onOpenUrl: (String) -> Unit
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground_scaled),
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("KMusic", style = MaterialTheme.typography.headlineMedium)
            Text("v${getCurrentVersion()}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ListItem(
                        headlineContent = { Text("Author") },
                        supportingContent = { Text("KyNarec") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.clickable { onOpenUrl("https://github.com/KyNarec/") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("GitHub Repository") },
                        supportingContent = { Text(text = "github.com/KyNarec/KMusic") },
                        leadingContent = { Icon(painter = painterResource(id = R.drawable.github), contentDescription = null) },
                        modifier = Modifier.clickable { onOpenUrl("https://github.com/KyNarec/KMusic") }
                    )
                }
            }

//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "KMusic is an open-source music player built with Kotlin and Jetpack Compose, focusing on performance and a clean user experience.",
//                style = MaterialTheme.typography.bodySmall,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(horizontal = 16.dp),
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
        }
    }