package com.kynarec.kmusic.ui.components

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.R
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.Constants.THUMBNAIL_ROUNDNESS

@Composable
fun PlayerControlBar(
    onBarClick: () -> Unit,
    viewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory((LocalContext.current.applicationContext as Application as KMusic).database.songDao(),LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
//            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(THUMBNAIL_ROUNDNESS.toFloat()))

            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onBarClick)
//            .padding()
            .padding(8.dp, 8.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiState.currentSong?.thumbnail)
                    .crossfade(true)
                    .transformations(RoundedCornersTransformation(THUMBNAIL_ROUNDNESS.toFloat()))
                    .build(),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .clip(RoundedCornerShape(8.dp)),
                error = painterResource(id = R.drawable.album) // Use a local placeholder
            )
        }

        // Song and Artist Info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        ) {
            Text(
                text = uiState.currentSong?.title ?: "NA",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(
                    repeatDelayMillis = 1000,
                    initialDelayMillis = 1000,
                )
            )
            Text(
                text = uiState.currentSong?.artist ?: "NA",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(
                    repeatDelayMillis = 1000,
                    initialDelayMillis = 1000,
                )
            )
        }

        // Playback Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.skipToPrevious() }) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Skip Previous",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = {
                if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
            }) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = { viewModel.skipToNext() }) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Skip Forward",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerControlBarPreview() {
    MaterialTheme {
        PlayerControlBar(
            onBarClick = {},
        )
    }
}
