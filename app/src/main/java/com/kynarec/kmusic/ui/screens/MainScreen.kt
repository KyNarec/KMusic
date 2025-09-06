package com.kynarec.kmusic.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.kynarec.kmusic.ui.components.TopBar

@Preview
@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()

    TopBar()
}