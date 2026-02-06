package com.kynarec.kmusic.ui.screens.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.klyrics.Lyrics
import com.kynarec.klyrics.LyricsDefaults
import com.kynarec.klyrics.LyricsLine
import com.kynarec.klyrics.LyricsState
import com.kynarec.klyrics.UiLyrics
import com.kynarec.klyrics.rememberLyricsState
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.utils.Constants.DEFAULT_WAVY_LYRICS_IDLE_INDICATOR
import com.kynarec.kmusic.utils.toSeconds
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LyricsScreen(
    onDismiss: () -> Unit,
    musicViewModel: MusicViewModel = koinActivityViewModel(),
    settingsViewModel: SettingsViewModel = koinActivityViewModel()
) {
    val uiState by musicViewModel.uiState.collectAsStateWithLifecycle()
    val wavyLyricsIdleIndicator by settingsViewModel.wavyLyricsIdleIndicatorFlow.collectAsStateWithLifecycle(DEFAULT_WAVY_LYRICS_IDLE_INDICATOR)

    val currentUiLyrics = remember(uiState.currentSong?.id, uiState.currentLyrics) {
        uiState.currentLyrics?.toUiLyrics(uiState.currentDurationLong.toSeconds())?.lines?.forEach {
            println("LyricsScreen $it")
        }
        uiState.currentLyrics?.toUiLyrics(uiState.currentDurationLong.toSeconds())
    }

    BackHandler {
        onDismiss()
    }

    Scaffold(
        Modifier.fillMaxSize()
    )
    { contentPadding ->
        if (uiState.isLoadingLyrics) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularWavyProgressIndicator()
            }
        }
        if (currentUiLyrics != null && !uiState.isLoadingLyrics) {
            val lyricsState = rememberLyricsState(
                uiLyrics = currentUiLyrics,
                isPlaying = { uiState.isPlaying },
                playbackTime = { uiState.currentPosition.toInt() }
            )
            val focusedColor = LocalContentColor.current

            // the same as LocalContentColor.current.copy(alpha = .5f) but alpha blending is buggy on Android
            val unfocusedColor =
                lerp(LocalContentColor.current, MaterialTheme.colorScheme.background, .5f)

            val lastLaneStyle = MaterialTheme.typography.titleLarge

            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Lyrics(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = lyricsState,
                    textStyle = {
                        when {
                            it == currentUiLyrics.lines.lastIndex -> lastLaneStyle
                            currentUiLyrics.lines[it].alignment == Alignment.End -> LyricsDefaults.TextStyleEndAligned
                            else -> LyricsDefaults.TextStyle
                        }
                    },
                    lineModifier = { idx ->
                        val line = currentUiLyrics.lines[idx]

                        Modifier
                            .appleMusicLane(
                                state = lyricsState,
                                idx = idx,
                                isAnnotation = idx == currentUiLyrics.lines.lastIndex,
                                constraints = constraints,
                                singleArtist = currentUiLyrics.lines.all { it.alignment == Alignment.Start },
                                onClick = {
                                    musicViewModel.seekTo(line.start.toLong())
                                    if (!uiState.isPlaying) musicViewModel.resume()
                                }
                            )
                    },
                    focusedColor = focusedColor,
                    unfocusedColor = unfocusedColor,
//                    contentPadding = PaddingValues(
//                        top = 42.dp + contentPadding.calculateTopPadding(),
//                        bottom = 20.dp + contentPadding.calculateBottomPadding()
//                    ),
                    idleIndicator = {
                        if (wavyLyricsIdleIndicator)
                            WavyLyricsIdleIndicator(
                                index = it,
                                state = lyricsState,
                                focusedColor = focusedColor,
                                unfocusedColor = unfocusedColor,
                                waveSpeed = if (uiState.isPlaying) 10.dp else 0.dp,
                                isPlaying = { uiState.isPlaying }
                            )
                        else
                            LinearLyricsIdleIndicator(
                                index = it,
                                state = lyricsState,
                                focusedColor = focusedColor,
                                unfocusedColor = unfocusedColor,
                                isPlaying = { uiState.isPlaying }
                            )
                    }
                )
            }
        } else if(currentUiLyrics == null && !uiState.isLoadingLyrics) {
            ElevatedCard(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unable to load lyrics", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

fun SyncedLyrics.toUiLyrics(duration: Int): UiLyrics {
    return UiLyrics(
        duration = duration,
        lines = this.lines.map {
            LyricsLine.Default(
                start = it.start,
                end = it.end,
                content = (it as SyncedLine).content,
                alignment = Alignment.Start
            )
        }
                + LyricsLine.Default(
            start = this.lines.last().end,
            end = duration * 1000,
            content = "Source: LrcLib",
            alignment = Alignment.Start
        )
    )
}

private val HorizontalPadding = 32.dp
private val VerticalPadding = 6.dp

private fun Modifier.appleMusicLane(
    state: LyricsState,
    idx: Int,
    isAnnotation: Boolean,
    singleArtist: Boolean,
    constraints: Constraints,
    onClick: () -> Unit
) = composed {
    val density = LocalDensity.current


    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()


    val blurRadius by animateDpAsState(
        when {
            hovered || !state.isAutoScrolling -> 0.dp
            state.lastFocusedLine < idx -> 1.5.dp * (idx - state.lastFocusedLine).coerceAtMost(4)
            else -> 3.dp * (state.firstFocusedLine - idx).coerceAtMost(4)
        }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    val isFocused by remember(state) {
        derivedStateOf {
            idx in state.firstFocusedLine..state.lastFocusedLine
        }
    }

    val scale by animateFloatAsState(
        when {
            pressed -> .975f
            isFocused -> 1.025f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )


    val alpha by animateFloatAsState(
        if (!isAnnotation || isFocused) 1f else 0f
    )

    widthIn(max = density.run { constraints.maxWidth.toDp() * if (singleArtist) 1f else 5 / 6f })
        .padding(
            vertical = 8.dp,
            horizontal = HorizontalPadding / 2
        )
        .clip(RoundedCornerShape(HorizontalPadding / 2))
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick,
            enabled = !isAnnotation
        )
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .padding(
            vertical = VerticalPadding,
            horizontal = HorizontalPadding / 2
        )
        .blur(
            radius = blurRadius,
            edgeTreatment = BlurredEdgeTreatment.Unbounded
        )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WavyLyricsIdleIndicator(
    index: Int,
    state: LyricsState,
    focusedColor: Color,
    unfocusedColor: Color,
    modifier: Modifier = Modifier,
    waveSpeed: Dp = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
    isPlaying: () -> Boolean
) {
    val startTime = if (index == 0) 0 else state.uiLyrics.lines[index - 1].end
    val endTime = state.uiLyrics.lines[index].start

    val smoothTime = remember { Animatable(state.playbackTime().toFloat()) }

    LaunchedEffect(state.playbackTime(), isPlaying()) {
        if (isPlaying()) {
            smoothTime.animateTo(
                targetValue = state.playbackTime().toFloat(),
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        } else {
            smoothTime.snapTo(state.playbackTime().toFloat())
        }
    }

    val visible by remember(state) {
        derivedStateOf {
            val t = state.playbackTime()
            t in startTime until endTime &&
                    index == state.firstFocusedLine &&
                    (endTime - startTime) > 1000
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = HorizontalPadding)
                .fillMaxWidth()
        ) {
            LinearWavyProgressIndicator(
                progress = {
                    val time = smoothTime.value
                    val total = (endTime - startTime).toFloat()
                    if (total > 0) ((time - startTime) / total).coerceIn(0f, 1f) else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = focusedColor,
                trackColor = unfocusedColor.copy(alpha = 0.2f),
                amplitude = { p -> WavyProgressIndicatorDefaults.indicatorAmplitude(p) * 1.5f },
                wavelength = 40.dp,
                waveSpeed = waveSpeed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LinearLyricsIdleIndicator(
    index: Int,
    state: LyricsState,
    focusedColor: Color,
    unfocusedColor: Color,
    modifier: Modifier = Modifier,
    isPlaying: () -> Boolean
) {
    val startTime = if (index == 0) 0 else state.uiLyrics.lines[index - 1].end
    val endTime = state.uiLyrics.lines[index].start

    val smoothTime = remember { Animatable(state.playbackTime().toFloat()) }

    LaunchedEffect(state.playbackTime(), isPlaying()) {
        if (isPlaying()) {
            smoothTime.animateTo(
                targetValue = state.playbackTime().toFloat(),
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        } else {
            smoothTime.snapTo(state.playbackTime().toFloat())
        }
    }

    val visible by remember(state) {
        derivedStateOf {
            val t = state.playbackTime()
            t in startTime until endTime &&
                    index == state.firstFocusedLine &&
                    (endTime - startTime) > 1000
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = HorizontalPadding)
                .fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = {
                    // 3. Use the smoothTime value for the progress lambda
                    val time = smoothTime.value
                    val total = (endTime - startTime).toFloat()
                    if (total > 0) ((time - startTime) / total).coerceIn(0f, 1f) else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = focusedColor,
                trackColor = unfocusedColor.copy(alpha = 0.2f),
            )
        }
    }
}