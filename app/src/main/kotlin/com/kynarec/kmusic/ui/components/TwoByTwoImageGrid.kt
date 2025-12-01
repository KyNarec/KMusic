package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun TwoByTwoImageGrid(imageUrls: List<String>) {
    if (imageUrls.size < 4) return // Safety check

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the overall grid a square
            .padding(4.dp)
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
    AsyncImage(
        model = url, // The image URL or resource ID
        contentDescription = null, // Set proper content description in a real app
        contentScale = ContentScale.Crop, // Scales and crops to fill the container
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f) // Makes each individual image a square
    )
}