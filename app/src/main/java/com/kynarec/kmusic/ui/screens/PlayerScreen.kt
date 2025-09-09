package com.kynarec.kmusic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kynarec.kmusic.R
import com.kynarec.kmusic.ui.viewModels.PlayerViewModel

/**
 * The full-screen music player composable.
 *
 * @param onClose A callback function to be invoked when the user requests to close the screen.
 */
@Composable
fun PlayerScreen(
    onClose: () -> Unit,
    viewModel: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(LocalContext.current))
) {
    val playerViewModel = viewModel
    val uiState by playerViewModel.uiState.collectAsState()

    // Handle system back button press
    BackHandler {
        onClose()
    }

    // This DraggableState handles the swipe-down-to-dismiss gesture
    val draggableState = rememberDraggableState(onDelta = { delta ->
        if (delta > 50) { // A small threshold to prevent accidental dismissal
            onClose()
        }
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStarted = { /* Optional: can be used to add visual feedback */ },
                onDragStopped = { /* Optional: can be used to add a 'snap to close' animation */ }
            )
            .padding(16.dp)
    ) {
        // Top section with back button and app icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_down),
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App icon",
                modifier = Modifier.size(30.dp)
            )
        }

        // Main content column, centered
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Album art
            AsyncImage(
                model = uiState.albumArtUri,
                contentDescription = "Album art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(250.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Song title
            Button(
                onClick = { /* TODO: Navigate to song details */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = uiState.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Song artist
            Button(
                onClick = { /* TODO: Navigate to artist details */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = uiState.artist,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Player controls
            Row(
                modifier = Modifier
                    .width(260.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { playerViewModel.skipToPrevious() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Skip back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = {
                        if (uiState.isPlaying) viewModel.pause() else viewModel.play()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { playerViewModel.skipToNext() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Skip forward",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
