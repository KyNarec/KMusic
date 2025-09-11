package com.kynarec.kmusic.ui.screens

import android.app.Application
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.R
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.PlayerViewModel
import com.kynarec.kmusic.utils.parseMillisToDuration
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.material.WavySlider

/**
 * The full-screen music player composable.
 *
 * @param onClose A callback function to be invoked when the user requests to close the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onClose: () -> Unit,
    viewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory((LocalContext.current.applicationContext as Application as MyApp).database.songDao(),LocalContext.current))
) {
    //val playerViewModel = viewModel
    //val uiState by playerViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStarted = { /* Optional: can be used to add visual feedback */ },
                onDragStopped = { /* Optional: can be used to add a 'snap to close' animation */ }
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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

        Spacer(modifier = Modifier.height(24.dp))

        // Album art
        AsyncImage(
            model = uiState.currentSong?.thumbnail,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Song title
        Button(
            onClick = { /* TODO: Navigate to song details */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = uiState.currentSong?.title ?: "NA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
        }

        // Song artist
        Button(
            onClick = { /* TODO: Navigate to artist details */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = uiState.currentSong?.artist ?: "NA",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seek bar and time display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
//            Slider(
//                value = if (uiState.totalDuration > 0) uiState.currentPosition.toFloat() / uiState.totalDuration.toFloat() else 0f,
//                onValueChange = { newValue ->
//                    playerViewModel.seekTo((newValue * uiState.totalDuration).toLong())
//                },
//                modifier = Modifier.fillMaxWidth()
//            )

            val customSliderColors  = SliderDefaults.colors(
            activeTrackColor = Color(0xFFD4B67C),
            inactiveTrackColor = Color(0xFF8E8A81),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
            thumbColor = Color(0xFFD4B67C)
            )

//            Log.i("PlayerScreen", "total duration: ${uiState.totalDuration}, currentDuration: ${uiState.currentDuration}")

            WavySlider(
                value = if (uiState.totalDuration > 0 || uiState.totalDuration < 0) uiState.currentPosition.toFloat() / uiState.currentDuration.toFloat() else 0f,
                onValueChange = { newValue ->
                    viewModel.seekTo((newValue * uiState.currentDuration).toLong())
                },
                modifier = Modifier.fillMaxWidth(),
                waveHeight = if (uiState.isPlaying) 4.dp else 0.dp,
                waveLength = 32.dp,
                waveVelocity = 12.dp to WaveDirection.TAIL,
                waveThickness = 2.dp,
                trackThickness = 2.dp,
                incremental = false,
                colors = customSliderColors,
//                thumb = {
//                    SliderDefaults.Thumb(
//                        interactionSource = remember { MutableInteractionSource() },
//                        colors = customSliderColors,
//                        thumbSize = DpSize(12.dp, 12.dp),
//                        modifier = Modifier
//                            .align(Alignment.CenterHorizontally)
//                            .shadow(1.dp, CircleShape, clip = false)
//                            .indication(
//                                interactionSource = remember { MutableInteractionSource() },
//                                indication = ripple(bounded = false, radius = 12.dp)
////                        .align(Arrangement.Center as Alignment.Horizontal)
//                            )
//                    )
//                },
//                track = {
//                    SliderDefaults.Track(
//                        sliderState = it,
////                        modifier = Modifier.height(trackHeight),
//                        thumbTrackGapSize = 0.dp,
//                        trackInsideCornerSize = 0.dp,
//                        drawStopIndicator = null,
//                    )
//                }
            )
//            WavySlider(
//                value = if (uiState.totalDuration > 0) uiState.currentPosition.toFloat() / uiState.totalDuration.toFloat() else 0f,
//                onValueChange = { newValue ->
//                    playerViewModel.seekTo((newValue * uiState.totalDuration).toLong())
//                },
//                modifier = Modifier.fillMaxWidth(),
//                waveHeight = if (uiState.isPlaying) 8.dp else 0.dp,
//                waveLength = 32.dp,
//                waveVelocity = 12.dp to WaveDirection.TAIL,
//                waveThickness = 2.dp,
//                trackThickness = 2.dp,
//                incremental = false,
//                thumbRadius = 8.dp, // <--- Add this line and adjust the value
//            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${uiState.currentPosition / 1000 / 60}:${(uiState.currentPosition / 1000 % 60).toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
//                    text = "${uiState.currentDuration / 1000 / 60}:${(uiState.totalDuration / 1000 % 60).toString().padStart(2, '0')}",
                    text = parseMillisToDuration(uiState.currentDuration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
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
                onClick = { viewModel.skipToPrevious() },
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
                    if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
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
                onClick = { viewModel.skipToNext() },
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
