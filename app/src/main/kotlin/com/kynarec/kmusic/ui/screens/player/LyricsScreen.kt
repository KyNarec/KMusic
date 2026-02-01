package com.kynarec.kmusic.ui.screens.player

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.klyrics.Lyrics
import com.kynarec.klyrics.LyricsDefaults
import com.kynarec.klyrics.LyricsLine
import com.kynarec.klyrics.LyricsState
import com.kynarec.klyrics.UiLyrics
import com.kynarec.klyrics.rememberLyricsState
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.toSeconds
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LyricsScreen(
    musicViewModel: MusicViewModel = koinViewModel()
) {
    val uiState by musicViewModel.uiState.collectAsStateWithLifecycle()

    val currentUiLyrics = remember(uiState.currentSong?.id, uiState.currentLyrics) {
        uiState.currentLyrics?.toUiLyrics(uiState.currentDurationLong.toSeconds())
    }

    LaunchedEffect( uiState.currentSong?.id) {
        if (uiState.currentLyrics == null) {
            uiState.currentSong?.let { song ->
                val syncedLyrics = musicViewModel.getSyncedLyrics(song)
                Log.i("LyricsScreen", "Synced Lyrics: ${syncedLyrics?.lines}")
                Log.i("LyricsScreen", "Synced Lyrics: ${syncedLyrics?.title}")
                syncedLyrics?.let { musicViewModel.setCurrentLyrics(it) }
            }
        }
    }
    Scaffold(
        Modifier.fillMaxSize()
    )
    { contentPadding ->
        if (currentUiLyrics != null) {
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
                Modifier.fillMaxWidth().padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Lyrics(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
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

                        Modifier.appleMusicLane(
                            state = lyricsState,
                            idx = idx,
                            isAnnotation = idx == currentUiLyrics.lines.lastIndex,
                            constraints = constraints,
                            singleArtist = currentUiLyrics.lines.all { it.alignment == Alignment.Start },
                            onClick = {
                                musicViewModel.seekTo(line.start.toLong())
                                if (!uiState.isPlaying) musicViewModel.resume()
                            }
                        ).align(Alignment.Center)
                    },
                    focusedColor = focusedColor,
                    unfocusedColor = unfocusedColor,
//                    contentPadding = PaddingValues(
//                        top = 42.dp + contentPadding.calculateTopPadding(),
//                        bottom = 20.dp + contentPadding.calculateBottomPadding()
//                    ),
                    idleIndicator = {
                        LyricsDefaults.IdleIndicator(
                            index = it,
                            state = lyricsState,
                            focusedColor = focusedColor,
                            unfocusedColor = unfocusedColor,
                            modifier = Modifier
//                                .padding(
//                                    horizontal = HorizontalPadding / 2,
//                                    vertical = VerticalPadding
//                                )
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(
                                    onClick = {
                                        musicViewModel.seekTo(
                                            (if (it == 0) 0 else lyricsState.uiLyrics.lines[it - 1].end + 1).toLong()
                                        )
                                    }
                                )
//                                .padding(
//                                    horizontal = HorizontalPadding / 2,
//                                    vertical = HorizontalPadding / 2
//                                )
                        )
                    }
                )
            }
        }
    }
}

fun SyncedLyrics.toUiLyrics(duration : Int) : UiLyrics {
    return UiLyrics(
        duration = duration,
        lines = this.lines.map {
            LyricsLine.Default(
                start = it.start,
                end = it.end,
                content = (it as SyncedLine).content,
                alignment = Alignment.CenterHorizontally
            )
        }
    )
}

private val HorizontalPadding = 32.dp
private val VerticalPadding = 6.dp

private fun Modifier.appleMusicLane(
    state: LyricsState,
    idx : Int,
    isAnnotation : Boolean,
    singleArtist : Boolean,
    constraints : Constraints,
    onClick : () -> Unit
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