package com.kynarec.kmusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

@Composable
fun rememberColumnCount(): Int {
    val density = LocalDensity.current
    val containerWidthPx = LocalWindowInfo.current.containerSize.width

    val screenWidthDp = with(density) { containerWidthPx.toDp() }

    return remember(screenWidthDp) {
        when {
            screenWidthDp < 600.dp -> 2 // Compact (Phones)
            screenWidthDp < 840.dp -> 4 // Medium (Tablets/Foldables)
            else -> 6                   // Expanded (Large Tablets/Desktop)
        }
    }
}