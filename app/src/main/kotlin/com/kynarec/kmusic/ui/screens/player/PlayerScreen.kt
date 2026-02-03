package com.kynarec.kmusic.ui.screens.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
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
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.ui.AlbumDetailScreen
import com.kynarec.kmusic.ui.ArtistDetailScreen
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.SmartMessage
import com.mocharealm.accompanist.lyrics.ui.composable.lyrics.KaraokeBreathingDotsDefaults
import com.mocharealm.accompanist.lyrics.ui.composable.lyrics.KaraokeLyricsView
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.material.WavySlider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerScreen(
    onLyricsClick: () -> Unit,
    onQueueClick: () -> Unit,
    onMoreClick: () -> Unit,
    viewModel: MusicViewModel = koinActivityViewModel(),
    database: KmusicDatabase = koinInject(),
    navController: NavHostController,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isLoadingLyrics = remember { mutableStateOf(false) }
    val lyricsToggled = remember { mutableStateOf(false) }

    BackHandler {
        onClose()
    }

    LaunchedEffect(lyricsToggled) {
        if (uiState.currentLyrics == null && lyricsToggled.value) SmartMessage("Loading Lyrics", PopupType.Info, false, context)
    }

    Scaffold(
        Modifier.fillMaxSize()
//            .then(
//                if (showQueueBottomSheet.value) Modifier.blur(32.dp)
//                else Modifier
//            )
        ,
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
                        onQueueClick()
                    })
            ) {
                IconButton(
                    onClick = onLyricsClick
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
                    onClick = onQueueClick
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
                    onClick = onMoreClick
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
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (lyricsToggled.value && !isLoadingLyrics.value) {
                                Modifier.blur(48.dp, BlurredEdgeTreatment(RoundedCornerShape(16.dp)))
                            } else {
                                Modifier
                            }
                        )
                    ,
                    contentScale = ContentScale.Crop,
                    imageLoader = LocalContext.current.imageLoader
                )
                if (lyricsToggled.value && !isLoadingLyrics.value)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        if (uiState.currentLyrics != null) {
                            KaraokeLyricsView(
                                listState = rememberLazyListState(),
                                lyrics = uiState.currentLyrics!!,
                                currentPosition = { uiState.currentPosition.toInt() },
                                onLineClicked = { line ->
                                    viewModel.seekTo(line.start.toLong())
                                },
                                onLinePressed = {},
                                normalLineTextStyle = LocalTextStyle.current.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textMotion = TextMotion.Animated,
                                    textAlign = TextAlign.Center
                                ),
                                // not used
                                accompanimentLineTextStyle = LocalTextStyle.current.copy(
                                    fontSize = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                    textMotion = TextMotion.Animated,
                                    textAlign = TextAlign.Center
                                ),
                                breathingDotsDefaults = KaraokeBreathingDotsDefaults(
                                    number = 3,
                                    size = 10.dp,
                                    margin = 6.dp,
                                    enterDurationMs = 100,
                                    preExitStillDuration = 0,
                                    preExitDipAndRiseDuration = 0,
                                    exitDurationMs = 100,
                                    breathingDotsColor = MaterialTheme.colorScheme.onBackground,
                                ),
                                modifier = Modifier.graphicsLayer {
                                    blendMode = BlendMode.SrcOver
                                    compositingStrategy = CompositingStrategy.Offscreen
                                },
                                textColor = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
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
                        if (uiState.currentSong?.albumId != "" && uiState.currentSong?.albumId != null) {
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
                    AnimatedContent(
                        targetState = uiState.isPlaying,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.8f))
                                .togetherWith(fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.8f))
                        }
                    ) { isPlaying ->
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(60.dp)
                        )
                    }
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

        }
    }
}