package com.kynarec.kmusic.ui.components.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader

@Composable
fun TwoByTwoImageGrid(imageUrls: List<String>) {
    val imageUrls = imageUrls.take(4).toMutableList().apply {
        while (size < 4) add("")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the overall grid a square
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),

        ) {
        // --- First Row (Top Half) ---
        Row(
            modifier = Modifier
                .weight(1f) // Takes half the height
                .fillMaxWidth()
        ) {
            // Top-Left Image (Index 0)
            GridImage(url = imageUrls[0], modifier = Modifier.weight(1f))
            // Top-Right Image (Index 1)
            GridImage(url = imageUrls[1], modifier = Modifier.weight(1f))
        }

        // --- Second Row (Bottom Half) ---
        Row(
            modifier = Modifier
                .weight(1f) // Takes half the height
                .fillMaxWidth()
        ) {
            // Bottom-Left Image (Index 2)
            GridImage(url = imageUrls[2], modifier = Modifier.weight(1f))
            // Bottom-Right Image (Index 3)
            GridImage(url = imageUrls[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun GridImage(url: String, modifier: Modifier) {
    Box(
        modifier = modifier // This modifier should define the final 1:1 square size and shape
            .fillMaxHeight()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    )
    {
        AsyncImage(
            model = url, // The image URL or resource ID
            contentDescription = null, // Set proper content description in a real app
            contentScale = ContentScale.Crop, // Scales and crops to fill the container
            modifier = modifier
                .fillMaxSize(),
            imageLoader = LocalContext.current.imageLoader
        )
    }

}