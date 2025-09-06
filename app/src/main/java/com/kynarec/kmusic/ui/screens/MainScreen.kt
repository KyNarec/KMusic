package com.kynarec.kmusic.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.kynarec.kmusic.ui.components.ThemedNavigationRail
import com.kynarec.kmusic.ui.components.TopBar
import com.kynarec.kmusic.ui.theme.KMusicTheme

@Preview
@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    KMusicTheme {
        Row {
            Column {
                TopBar()
                ThemedNavigationRail()
            }
        }
    }

}