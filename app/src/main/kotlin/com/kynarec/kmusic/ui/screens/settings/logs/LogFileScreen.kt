package com.kynarec.kmusic.ui.screens.settings.logs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.WrapText
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.scrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.ui.components.settings.logs.DeleteLogDialog
import com.kynarec.kmusic.ui.viewModels.LogFileActions
import com.kynarec.kmusic.ui.viewModels.LogFileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun LogFileScreen(
    filename: String,
    navBack: () -> Unit,
    logFileViewModel: LogFileViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var content by remember(filename) { mutableStateOf<String?>(null) }
    var error by remember(filename) { mutableStateOf<String?>(null) }

    val state by logFileViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(filename) {
        content = try {
            withContext(Dispatchers.IO) {
                File(context.filesDir, filename).readText()
            }
        } catch (e: Exception) {
            error = e.message ?: "Unknown error"
            null
        }
    }
    val scrollBehavior = enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = filename, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navBack
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when {
                error != null -> {
                    Text(
                        "Error reading file: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                content == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    val verticalScrollState = rememberScrollState()
                    val horizontalScrollState = rememberScrollState()
                    val coroutineScope = rememberCoroutineScope()

                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalFloatingToolbar(
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = -ScreenOffset)
                                    .zIndex(1f),
                            expanded = true,
                            leadingContent = {
                                TooltipBox(
                                    positionProvider =
                                        TooltipDefaults.rememberTooltipPositionProvider(
                                            TooltipAnchorPosition.Above
                                        ),
                                    tooltip = {
                                        PlainTooltip(
                                            modifier =
                                                Modifier.semantics {
                                                    // TODO(b/496338253): Remove this modifier once bug
                                                    //  where tooltip text is not announced by a11y screen
                                                    //  readers is resolved.
                                                    liveRegion = LiveRegionMode.Assertive
                                                    paneTitle = "Wrap Text"
                                                }
                                        ) {
                                            Text("Wrap Text")
                                        }
                                    },
                                    state = rememberTooltipState(),
                                ) {
                                    IconButton(onClick = {
                                        logFileViewModel.onAction(LogFileActions.ToggleWrapLines)
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.WrapText,
                                            null
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                AppBarRow {
                                    clickableItem(
                                        onClick = {
                                            logFileViewModel.onAction(LogFileActions.ShowDeleteDialog(File(context.filesDir, filename)))
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = null,
                                            )
                                        },
                                        label = "Delete",
                                    )
                                    clickableItem(
                                        onClick = {
                                            coroutineScope.launch {
                                                verticalScrollState.animateScrollTo(0)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Filled.ArrowUpward,
                                                contentDescription = null,
                                            )
                                        },
                                        label = "Scroll Up",
                                    )
                                    clickableItem(
                                        onClick = {
                                            coroutineScope.launch {
                                                verticalScrollState.animateScrollTo(value = verticalScrollState.maxValue)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Filled.ArrowDownward,
                                                contentDescription = null,
                                            )
                                        },
                                        label = "Scroll Down",
                                    )
                                }
                            },
                            content = {
                                TooltipBox(
                                    positionProvider =
                                        TooltipDefaults.rememberTooltipPositionProvider(
                                            TooltipAnchorPosition.Above
                                        ),
                                    tooltip = {
                                        PlainTooltip(
                                            modifier =
                                                Modifier.semantics {
                                                    // TODO(b/496338253): Remove this modifier once bug
                                                    //  where tooltip text is not announced by a11y screen
                                                    //  readers is resolved.
                                                    liveRegion = LiveRegionMode.Assertive
                                                    paneTitle = "Share file"
                                                }
                                        ) {
                                            Text("Share file")
                                        }
                                    },
                                    state = rememberTooltipState(),
                                ) {
                                    FilledIconButton(
                                        modifier = Modifier.width(64.dp),
                                        onClick = {
                                            logFileViewModel.onAction(LogFileActions.Share(File(context.filesDir, filename), context))
                                        },
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = null)
                                    }
                                }
                            },
                        )

                        val textModifier = Modifier
                            .fillMaxSize()
                            .padding(end = 12.dp)
                            .verticalScroll(verticalScrollState)
                            .let {
                                if (!state.wrapLines) it
                                    .scrollbar(
                                        state = horizontalScrollState.scrollIndicatorState,
                                        orientation = Orientation.Horizontal,
                                        isFadeEnabled = false
                                    )
                                    .horizontalScroll(horizontalScrollState)
                                else it
                            }
                            .padding(8.dp)
                            // Explicit mouse-wheel handling (safety net; verticalScroll usually
                            // already responds to wheel events, but this guarantees it works).
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == PointerEventType.Scroll) {
                                            val scrollDelta = event.changes.first().scrollDelta.y
                                            coroutineScope.launch {
                                                // scrollDelta.y is typically ~1 per notch; scale it up
                                                verticalScrollState.scrollBy(scrollDelta * 50f)
                                            }
                                            event.changes.first().consume()
                                        }
                                    }
                                }
                            }

                        SelectionContainer {
                            Text(
                                text = content!!,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                softWrap = state.wrapLines,
                                overflow = if (state.wrapLines) TextOverflow.Clip else TextOverflow.Visible,
                                modifier = textModifier
                            )
                        }

                        VerticalScrollbar(
                            scrollState = verticalScrollState,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(12.dp)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (state.showDeleteDialog) {
            DeleteLogDialog(
                file = File(context.filesDir, filename),
                onDismissRequest = { logFileViewModel.onAction(LogFileActions.HideDeleteDialog) },
                onConfirm = {
                    logFileViewModel.onAction(LogFileActions.Delete(File(context.filesDir, filename)))
                    navBack()
                }
            )
        }
    }

}

@Composable
private fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val thumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val coroutineScope = rememberCoroutineScope()

    // Track the last-measured viewport height so the drag handler can
    // compute the scroll-to-pixel ratio.
    var viewportHeightPx by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val maxValue = scrollState.maxValue
                    if (maxValue > 0 && viewportHeightPx > 0f) {
                        val thumbHeightFraction = (viewportHeightPx / (viewportHeightPx + maxValue))
                            .coerceIn(0.05f, 1f)
                        val thumbHeightPx = viewportHeightPx * thumbHeightFraction
                        val trackRange = viewportHeightPx - thumbHeightPx
                        if (trackRange > 0f) {
                            // Convert the drag's pixel movement on the track
                            // into the equivalent scroll movement in content.
                            val scrollPerPixel = maxValue / trackRange
                            coroutineScope.launch {
                                scrollState.scrollBy(dragAmount.y * scrollPerPixel)
                            }
                        }
                    }
                }
            }
    ) {
        viewportHeightPx = size.height
        val maxValue = scrollState.maxValue
        val radius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

        drawRoundRect(color = trackColor, cornerRadius = radius)

        if (maxValue > 0) {
            val viewportHeight = size.height
            val thumbHeightFraction = (viewportHeight / (viewportHeight + maxValue))
                .coerceIn(0.05f, 1f)
            val thumbHeight = viewportHeight * thumbHeightFraction
            val scrollFraction = scrollState.value.toFloat() / maxValue
            val thumbOffset = (viewportHeight - thumbHeight) * scrollFraction

            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(0f, thumbOffset),
                size = Size(size.width, thumbHeight),
                cornerRadius = radius
            )
        }
    }
}