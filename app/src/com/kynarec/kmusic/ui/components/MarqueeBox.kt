package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit


@Composable
fun MarqueeBox(
    boxModifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current
) {
    var isMarqueeActive by remember(text) { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = boxModifier.fillMaxWidth(),
        contentAlignment = contentAlignment
    ) {
        val containerWidth = constraints.maxWidth

        Box(
            contentAlignment = contentAlignment,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    if (isMarqueeActive) {
                        val fadeWidth = 10f
                        // Left fade
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent, 1f to Color.Black,
                                startX = 0f, endX = fadeWidth
                            ),
                            blendMode = BlendMode.DstIn
                        )
                        // Right fade
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Black, 1f to Color.Transparent,
                                startX = size.width - fadeWidth, endX = size.width
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
                }
        ) {
            Text(
                text = text,
                modifier = modifier.basicMarquee(
                    initialDelayMillis = 1000,
                    iterations = Int.MAX_VALUE
                ),
                color = color,
                autoSize = autoSize,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                style = style,
                onTextLayout = { textLayoutResult ->
                    isMarqueeActive = textLayoutResult.size.width > containerWidth
                },
                softWrap = softWrap
            )
        }
    }
}