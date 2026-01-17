package com.kynarec.kmusic.ui.screens.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.ArtistDetailScreen
import com.kynarec.kmusic.ui.components.song.SongOptionsBottomSheet
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.material.WavySlider

/**
 * The full-screen music player composable.
 *
 * @param onClose A callback function to be invoked when the user requests to close the screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerScreen(
    onClose: () -> Unit,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showQueueBottomSheet = remember { mutableStateOf(false) }
    val showOptionsBottomSheet = remember { mutableStateOf(false) }

    BackHandler {
        onClose()
    }

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(true, onClick = {
                        showQueueBottomSheet.value = true
                    })
            ) {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Show Lyrics",
                    )
                }
                Spacer(
                    Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        showQueueBottomSheet.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Show queue",
                    )
                }
                Spacer(
                    Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        showOptionsBottomSheet.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Show more options",
                    )
                }

            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(contentPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section with back button and app icon


            Spacer(modifier = Modifier.height(24.dp))

            // Album art
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .multiLayersShadow(
                        elevation = 15.dp,
                        transparencyMultiplier = 0.2f,
                        layers = 10,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                AsyncImage(
                    model = uiState.currentSong?.thumbnail,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(300.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    imageLoader = LocalContext.current.imageLoader
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Song title
            val isLiked = database.songDao().getSongFlowById(uiState.currentSong!!.id)
                .collectAsStateWithLifecycle(null).value?.isLiked ?: false
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(

                    onClick = {
                        if (uiState.currentSong?.albumId != "") {
                            onClose()
                            navController.navigate(AlbumDetailScreen(uiState.currentSong?.albumId!!))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp)
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            // Left fade
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0f to Color.Transparent, 0.15f to Color.Black,
                                    startX = 0f, endX = 80f
                                ),
                                blendMode = BlendMode.DstIn
                            )
                            // Right fade
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0.85f to Color.Black, 1f to Color.Transparent,
                                    startX = size.width - 80f, endX = size.width
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                ) {
                    Text(
                        text = uiState.currentSong?.title ?: "NA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 2000
                        )
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleFavoriteSong(uiState.currentSong!!) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "Toggle Liked"
                    )
                }
            }

                val artists = uiState.currentSong?.artists ?: emptyList()

                val annotatedString = buildAnnotatedString {
                    artists.forEachIndexed { index, artist ->
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "artistId",
                                linkInteractionListener = {
                                    Log.i("PlayerScreen", "Clicked Artist ID: ${artist.id}")
                                    onClose()
                                    navController.navigate(ArtistDetailScreen(artist.id))
                                }
                            )
                        ) {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textDecoration = TextDecoration.None // Explicitly remove underline
                                )
                            ) {
                                append(artist.name)
                            }
                        }

                        if (index < artists.lastIndex) {
                            append(", ")
                        }
                    }
                }

                Text(
                    text = annotatedString,
                    fontSize = 16.sp,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Seek bar and time display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {

                    val customSliderColors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.inversePrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondary,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent,
                        thumbColor = MaterialTheme.colorScheme.inversePrimary
                    )


                    val newSliderValue = remember { mutableFloatStateOf(0f) }
                    val isDragging = remember { mutableStateOf(false) }

                    val displayValue = when {
                        isDragging.value -> newSliderValue.floatValue
                        uiState.currentDurationLong > 0 -> uiState.currentPosition.toFloat() / uiState.currentDurationLong.toFloat()
                        else -> 0f
                    }

                    WavySlider(
                        value = displayValue.coerceIn(0f,1f),
                        onValueChange = { newValue ->
                            isDragging.value = true
                            newSliderValue.floatValue = newValue
                        },
                        onValueChangeFinished = {
                            viewModel.seekTo((newSliderValue.floatValue * uiState.currentDurationLong).toLong())
                            isDragging.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        waveHeight = if (uiState.isPlaying) 4.dp else 0.dp,
                        waveLength = 32.dp,
                        waveVelocity = 12.dp to WaveDirection.TAIL,
                        waveThickness = 2.dp,
                        trackThickness = 2.dp,
                        incremental = false,
                        colors = customSliderColors,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isDragging.value) {
                            Text(
                                text = "${(newSliderValue.floatValue * uiState.currentDurationLong).toLong() / 1000 / 60}:${
                                    ((newSliderValue.floatValue * uiState.currentDurationLong).toLong() / 1000 % 60).toString().padStart(2, '0')
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = "${uiState.currentPosition / 1000 / 60}:${
                                    (uiState.currentPosition / 1000 % 60).toString().padStart(2, '0')
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        Text(
//                    text = "${uiState.currentDuration / 1000 / 60}:${(uiState.totalDuration / 1000 % 60).toString().padStart(2, '0')}",
//                    text = parseMillisToDuration(uiState.currentDurationLong),
                            text = uiState.currentSong?.duration ?: "NA",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Player controls
                Row(
                    modifier = Modifier
                        .width(300.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.skipToPrevious() },
                        modifier = Modifier.size(50.dp),
                        shape = IconButtonDefaults.mediumSquareShape,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Skip back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
                        },
                        modifier = Modifier.size(70.dp),
                        shape = IconButtonDefaults.mediumSquareShape,
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.skipToNext() },
                        modifier = Modifier.size(50.dp),
                        shape = IconButtonDefaults.mediumSquareShape,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Skip forward",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }

//        Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.weight(1f))


                if (showQueueBottomSheet.value) {
                    QueueScreen(
                        viewModel = viewModel,
                        onClose = { showQueueBottomSheet.value = false },
                        sheetState = sheetState,
                        showBottomSheet = showQueueBottomSheet,
                        database = database,
                        navController = navController
                    )
                }

                if (showOptionsBottomSheet.value) {
                    SongOptionsBottomSheet(
                        song = uiState.currentSong!!,
                        onDismiss = { showOptionsBottomSheet.value = false },
                        viewModel = viewModel,
                        database = database,
                        navController = navController
                    )
                }
            }
        }


    }

    /**
     * Code from https://medium.com/@yuriyskul/different-approaches-to-create-android-style-shadows-with-transparent-containers-in-jetpack-compose-e299a215557e
     */
    fun Modifier.multiLayersShadow(
        elevation: Dp,
        transparencyMultiplier: Float = 0.1f,
        color: Color = Color.Black,
        layers: Int = 20,
        shape: Shape = RoundedCornerShape(8.dp)
    ): Modifier = this.drawWithCache {

        // Set the shadow size based on the elevation
        val shadowSize =
            elevation.toPx() * 1.2f  // tweak the multiplier for proper shadow size
        val layerSize = shadowSize / layers

        // Create the outline based on the shape and size
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path().apply { addOutline(outline) }

        onDrawWithContent {
            // Draw each shadow layer with decreasing opacity and expanding size
            repeat(layers) { layer ->
                val layerAlpha = 1f - (1 / layers.toFloat()) * layer
                val reducedLayerAlpha = layerAlpha * transparencyMultiplier

                // Adjust the scale factor based on the layer
                val scaleFactorX = 1f + (layer * layerSize) / size.width
                val scaleFactorY = 1f + (layer * layerSize) / size.height

                drawIntoCanvas { canvas ->
                    // Save the current state of the canvas
                    canvas.save()

                    // Move the canvas to the center
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    canvas.translate(centerX, centerY)

                    // Apply scale transformation, scaling differently in X and Y directions
                    canvas.scale(scaleFactorX, scaleFactorY)

                    // Translate back to the original position
                    canvas.translate(-centerX, -centerY)

                    // Draw the outline using the path and apply transparency for the shadow effect
                    drawPath(
                        path = path,
                        color = color.copy(alpha = reducedLayerAlpha),
                        style = Stroke(width = layerSize)  // Set stroke width for each layer
                    )

                    // Restore the canvas to its original state
                    canvas.restore()
                }
            }

            drawContent()
        }
    }