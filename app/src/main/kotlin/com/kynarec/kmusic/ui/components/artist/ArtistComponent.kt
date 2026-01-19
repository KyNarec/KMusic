package com.kynarec.kmusic.ui.components.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.data.db.entities.ArtistPreview
import com.kynarec.kmusic.ui.components.MarqueeBox

@Composable
fun ArtistComponent(
    modifier: Modifier = Modifier,
    artistPreview: ArtistPreview,
    onClick: () -> Unit,
    imageWith: Int = 100,
    imageHeight: Int = 100
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .background(Color.Transparent),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .width(imageWith.dp)
                    .height(imageHeight.dp)
                    .align(Alignment.CenterHorizontally)
            )
            {
                AsyncImage(
                    model = artistPreview.thumbnailUrl,
                    contentDescription = "Album art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    imageLoader = LocalContext.current.imageLoader
                )
            }
            MarqueeBox(
                contentAlignment = Alignment.Center,
                text = artistPreview.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            MarqueeBox(
                contentAlignment = Alignment.Center,
                text = artistPreview.monthlyListeners,
            )
        }
    }
}