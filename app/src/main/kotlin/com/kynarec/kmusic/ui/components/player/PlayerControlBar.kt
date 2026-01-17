package com.kynarec.kmusic.ui.components.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.kynarec.kmusic.R
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.Constants.THUMBNAIL_ROUNDNESS

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerControlBar(
    onBarClick: () -> Unit,
    viewModel: MusicViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val progress = if (uiState.currentDurationLong != 0L) {
        uiState.currentPosition.toFloat() / uiState.currentDurationLong.toFloat()
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "ProgressAnimation"
    )

    val fillColor = MaterialTheme.colorScheme.onSecondary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
//            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(THUMBNAIL_ROUNDNESS.toFloat()))

            .background(MaterialTheme.colorScheme.secondaryContainer)
            .drawBehind {
                drawRect(
                    color = fillColor,
                    size = size.copy(width = size.width * animatedProgress)
                )
            }
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
                imageLoader = LocalContext.current.imageLoader,
                error = painterResource(id = R.drawable.album) // Use a local placeholder
            )
        }

        // Song and Artist Info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        ) {
            MarqueeBox(
                text = uiState.currentSong?.title ?: "NA",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            MarqueeBox(
                text = uiState.currentSong?.artists?.joinToString(", ") { it.name } ?: "NA",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
            )
        }

        // Playback Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.skipToPrevious() },
                shape = IconButtonDefaults.mediumSquareShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Skip Previous",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(
                onClick = {
                    if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
                },
                shape = IconButtonDefaults.mediumSquareShape
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(
                onClick = { viewModel.skipToNext() },
                shape = IconButtonDefaults.mediumSquareShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Skip Forward",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
