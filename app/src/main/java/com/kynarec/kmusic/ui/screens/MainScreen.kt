package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.kynarec.kmusic.ui.components.ThemedNavigationRail
import com.kynarec.kmusic.ui.components.TopBar
import com.kynarec.kmusic.ui.theme.KMusicTheme

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    KMusicTheme {
        Scaffold(
            topBar = {
                TopBar()
            }
        ) { contentPadding ->
            // contentPadding automatically accounts for the TopBar height
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                ThemedNavigationRail()
                // The Box for app screens goes here
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // This is where app screens will be placed
                }
            }
        }
    }
}