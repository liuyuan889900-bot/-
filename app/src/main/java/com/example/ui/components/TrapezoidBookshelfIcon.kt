package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Bookshelf Silhouette Icons (上下一样宽 - Rectangular Upright Bookshelf)
 * Replaces generic book icon for "书架" tab in bottom navigation.
 * Straight, elegant twin-upright bookcase with 3 level shelves and book silhouettes.
 */
object TrapezoidBookshelfIcons {
    val Filled: ImageVector by lazy {
        ImageVector.Builder(
            name = "BookshelfFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Left vertical upright beam (上下一样宽, x = 4.0 to 5.6)
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4.0f, 3.5f)
                lineTo(5.6f, 3.5f)
                lineTo(5.6f, 21.5f)
                lineTo(4.0f, 21.5f)
                close()
            }
            // Right vertical upright beam (上下一样宽, x = 18.4 to 20.0)
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(18.4f, 3.5f)
                lineTo(20.0f, 3.5f)
                lineTo(20.0f, 21.5f)
                lineTo(18.4f, 21.5f)
                close()
            }
            // Top shelf board (x: 4.0 to 20.0, y: 5.5 to 7.0)
            path(fill = SolidColor(Color.White)) {
                moveTo(4.0f, 5.5f)
                lineTo(20.0f, 5.5f)
                lineTo(20.0f, 7.0f)
                lineTo(4.0f, 7.0f)
                close()
            }
            // Middle shelf board (x: 4.0 to 20.0, y: 12.0 to 13.5)
            path(fill = SolidColor(Color.White)) {
                moveTo(4.0f, 12.0f)
                lineTo(20.0f, 12.0f)
                lineTo(20.0f, 13.5f)
                lineTo(4.0f, 13.5f)
                close()
            }
            // Bottom shelf board (x: 3.5 to 20.5, y: 18.5 to 20.2)
            path(fill = SolidColor(Color.White)) {
                moveTo(3.5f, 18.5f)
                lineTo(20.5f, 18.5f)
                lineTo(20.5f, 20.2f)
                lineTo(3.5f, 20.2f)
                close()
            }
            // Books on middle shelf
            path(fill = SolidColor(Color.White)) {
                // Book 1 (Upright)
                moveTo(7.0f, 12.0f)
                lineTo(7.0f, 8.2f)
                lineTo(8.8f, 8.2f)
                lineTo(8.8f, 12.0f)
                close()
                // Book 2 (Upright)
                moveTo(9.4f, 12.0f)
                lineTo(9.4f, 8.7f)
                lineTo(11.0f, 8.7f)
                lineTo(11.0f, 12.0f)
                close()
                // Book 3 (Leaning)
                moveTo(12.0f, 12.0f)
                lineTo(14.5f, 8.8f)
                lineTo(15.7f, 9.6f)
                lineTo(13.5f, 12.0f)
                close()
            }
            // Books on bottom shelf
            path(fill = SolidColor(Color.White)) {
                // Book 1
                moveTo(6.8f, 18.5f)
                lineTo(6.8f, 14.5f)
                lineTo(8.5f, 14.5f)
                lineTo(8.5f, 18.5f)
                close()
                // Book 2
                moveTo(9.1f, 18.5f)
                lineTo(9.1f, 15.0f)
                lineTo(10.6f, 15.0f)
                lineTo(10.6f, 18.5f)
                close()
                // Book 3
                moveTo(11.2f, 18.5f)
                lineTo(11.2f, 14.2f)
                lineTo(12.7f, 14.2f)
                lineTo(12.7f, 18.5f)
                close()
                // Book 4 (Leaning)
                moveTo(13.6f, 18.5f)
                lineTo(16.2f, 15.0f)
                lineTo(17.4f, 15.8f)
                lineTo(15.1f, 18.5f)
                close()
            }
        }.build()
    }

    val Outlined: ImageVector by lazy {
        ImageVector.Builder(
            name = "BookshelfOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Main rectangular bookshelf frame (上下一样宽)
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Left upright
                moveTo(4.5f, 3.5f)
                lineTo(4.5f, 21.5f)
                // Right upright
                moveTo(19.5f, 3.5f)
                lineTo(19.5f, 21.5f)
                // Top cap
                moveTo(4.5f, 3.5f)
                lineTo(19.5f, 3.5f)
                // Top shelf
                moveTo(4.5f, 6.5f)
                lineTo(19.5f, 6.5f)
                // Middle shelf
                moveTo(4.5f, 12.5f)
                lineTo(19.5f, 12.5f)
                // Bottom shelf
                moveTo(4.0f, 18.5f)
                lineTo(20.0f, 18.5f)
            }
            // Minimal book silhouettes
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.2f,
                strokeLineCap = StrokeCap.Round
            ) {
                // Middle shelf books
                moveTo(7.5f, 12.5f)
                lineTo(7.5f, 9.2f)
                lineTo(9.5f, 9.2f)
                lineTo(9.5f, 12.5f)

                moveTo(12.5f, 12.5f)
                lineTo(14.8f, 9.5f)

                // Bottom shelf books
                moveTo(7.5f, 18.5f)
                lineTo(7.5f, 14.8f)
                lineTo(9.2f, 14.8f)
                lineTo(9.2f, 18.5f)

                moveTo(10.2f, 18.5f)
                lineTo(10.2f, 15.2f)
                lineTo(11.8f, 15.2f)
                lineTo(11.8f, 18.5f)

                moveTo(13.5f, 18.5f)
                lineTo(15.8f, 15.2f)
            }
        }.build()
    }
}
