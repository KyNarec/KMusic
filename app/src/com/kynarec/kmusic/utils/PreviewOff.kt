package com.kynarec.kmusic.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val preview_off: ImageVector
    get() {
        if (_preview_off != null) {
            return _preview_off!!
        }
        _preview_off =
            ImageVector.Builder(
                name = "preview_off",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(12f, 17f)
                        quadTo(9.95f, 17f, 8.34f, 15.89f)
                        quadTo(6.73f, 14.78f, 6f, 13f)
                        quadTo(6.5f, 11.8f, 7.4f, 10.9f)
                        reflectiveQuadTo(9.5f, 9.5f)
                        lineToRelative(1.18f, 1.17f)
                        quadTo(9.7f, 10.95f, 8.93f, 11.54f)
                        reflectiveQuadTo(7.65f, 13f)
                        quadToRelative(0.65f, 1.17f, 1.8f, 1.84f)
                        reflectiveQuadTo(12f, 15.5f)
                        quadToRelative(0.75f, 0f, 1.45f, -0.2f)
                        reflectiveQuadToRelative(1.28f, -0.57f)
                        lineTo(15.8f, 15.8f)
                        quadTo(15f, 16.38f, 14.04f, 16.69f)
                        reflectiveQuadTo(12f, 17f)
                        close()
                        moveToRelative(5.23f, -2.6f)
                        lineTo(16.15f, 13.33f)
                        quadToRelative(0.05f, -0.07f, 0.1f, -0.16f)
                        reflectiveQuadTo(16.35f, 13f)
                        quadTo(15.9f, 12.18f, 15.18f, 11.59f)
                        reflectiveQuadTo(13.55f, 10.73f)
                        lineTo(11.83f, 9f)
                        quadToRelative(2.05f, 0f, 3.75f, 1.11f)
                        quadTo(17.28f, 11.23f, 18f, 13f)
                        quadToRelative(-0.15f, 0.38f, -0.34f, 0.72f)
                        reflectiveQuadTo(17.23f, 14.4f)
                        close()
                        moveToRelative(2.55f, 8.2f)
                        lineTo(18.18f, 21f)
                        horizontalLineTo(5f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        verticalLineTo(5.82f)
                        lineTo(1.4f, 4.2f)
                        lineTo(2.8f, 2.8f)
                        lineTo(21.2f, 21.2f)
                        lineToRelative(-1.43f, 1.4f)
                        close()
                        moveTo(5f, 19f)
                        horizontalLineTo(16.18f)
                        lineTo(5f, 7.82f)
                        verticalLineTo(19f)
                        close()
                        moveTo(21f, 18.18f)
                        lineToRelative(-2f, -2f)
                        verticalLineTo(8f)
                        horizontalLineTo(10.83f)
                        lineToRelative(-5f, -5f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 5f)
                        verticalLineTo(18.18f)
                        close()
                    }
                }
                .build()
        return _preview_off!!
    }

private var _preview_off: ImageVector? = null