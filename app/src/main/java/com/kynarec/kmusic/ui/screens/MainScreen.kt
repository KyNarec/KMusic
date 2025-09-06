package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kynarec.kmusic.ui.Navigation
import com.kynarec.kmusic.ui.components.MyNavigationRailComponent
import com.kynarec.kmusic.ui.components.TopBarComponent
import com.kynarec.kmusic.ui.theme.KMusicTheme

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    KMusicTheme {
        val navController = rememberNavController()
        Scaffold(
            topBar = {
                TopBarComponent(navController)
            }
        ) { contentPadding ->
            // contentPadding automatically accounts for the TopBar height
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                MyNavigationRailComponent(navController)
                // The Box for app screens goes here
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // This is where app screens will be placed
                    Navigation(navController)
                }
            }
        }
    }
}