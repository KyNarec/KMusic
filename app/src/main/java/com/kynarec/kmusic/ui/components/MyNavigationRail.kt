package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun MyNavigationRail() {
    Row() {
        NavigationRail(
            content = {
                IconButton(
                    onClick = { /*TODO*/ }
                ) { }
            }
        )
    }
}