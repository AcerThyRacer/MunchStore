package com.sugarmunch.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object CandyIcons {

    // ========================================
    // Navigation Icons
    // ========================================

    /** House shape with candy chimney */
    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Roof triangle
                moveTo(12f, 3f)
                lineTo(2f, 12f)
                lineTo(5f, 12f)
                lineTo(5f, 20f)
                lineTo(10f, 20f)
                lineTo(10f, 15f)
                lineTo(14f, 15f)
                lineTo(14f, 20f)
                lineTo(19f, 20f)
                lineTo(19f, 12f)
                lineTo(22f, 12f)
                close()
                // Candy chimney (small rounded rectangle on roof)
                moveTo(16f, 4f)
                lineTo(16f, 8.5f)
                lineTo(18.5f, 8.5f)
                lineTo(18.5f, 4f)
                curveTo(18.5f, 3.2f, 17.8f, 2.5f, 17.25f, 2.5f)
                curveTo(16.7f, 2.5f, 16f, 3.2f, 16f, 4f)
                close()
            }
        }.build()
    }

    /** Magic wand with sparkle at tip */
    val Effects: ImageVector by lazy {
        ImageVector.Builder(
            name = "Effects",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Wand body (diagonal line with thickness)
                moveTo(3.5f, 21.5f)
                lineTo(2.5f, 20.5f)
                lineTo(14.5f, 8.5f)
                lineTo(15.5f, 9.5f)
                close()
                // Sparkle at tip — 4-point star
                moveTo(18f, 3f)
                lineTo(19f, 6f)
                lineTo(22f, 7f)
                lineTo(19f, 8f)
                lineTo(18f, 11f)
                lineTo(17f, 8f)
                lineTo(14f, 7f)
                lineTo(17f, 6f)
                close()
            }
        }.build()
    }

    /** Painter's palette with color holes */
    val Themes: ImageVector by lazy {
        ImageVector.Builder(
            name = "Themes",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Palette outline (bean-like oval)
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(12.83f, 22f, 13.5f, 21.33f, 13.5f, 20.5f)
                curveTo(13.5f, 20.11f, 13.35f, 19.76f, 13.11f, 19.49f)
                curveTo(12.88f, 19.23f, 12.73f, 18.88f, 12.73f, 18.5f)
                curveTo(12.73f, 17.67f, 13.4f, 17f, 14.23f, 17f)
                lineTo(16f, 17f)
                curveTo(19.31f, 17f, 22f, 14.31f, 22f, 11f)
                curveTo(22f, 6.04f, 17.52f, 2f, 12f, 2f)
                close()
                // Color hole 1
                moveTo(6.5f, 13f)
                curveTo(5.67f, 13f, 5f, 12.33f, 5f, 11.5f)
                curveTo(5f, 10.67f, 5.67f, 10f, 6.5f, 10f)
                curveTo(7.33f, 10f, 8f, 10.67f, 8f, 11.5f)
                curveTo(8f, 12.33f, 7.33f, 13f, 6.5f, 13f)
                close()
                // Color hole 2
                moveTo(9.5f, 9f)
                curveTo(8.67f, 9f, 8f, 8.33f, 8f, 7.5f)
                curveTo(8f, 6.67f, 8.67f, 6f, 9.5f, 6f)
                curveTo(10.33f, 6f, 11f, 6.67f, 11f, 7.5f)
                curveTo(11f, 8.33f, 10.33f, 9f, 9.5f, 9f)
                close()
                // Color hole 3
                moveTo(14.5f, 9f)
                curveTo(13.67f, 9f, 13f, 8.33f, 13f, 7.5f)
                curveTo(13f, 6.67f, 13.67f, 6f, 14.5f, 6f)
                curveTo(15.33f, 6f, 16f, 6.67f, 16f, 7.5f)
                curveTo(16f, 8.33f, 15.33f, 9f, 14.5f, 9f)
                close()
                // Color hole 4
                moveTo(18f, 13f)
                curveTo(17.17f, 13f, 16.5f, 12.33f, 16.5f, 11.5f)
                curveTo(16.5f, 10.67f, 17.17f, 10f, 18f, 10f)
                curveTo(18.83f, 10f, 19.5f, 10.67f, 19.5f, 11.5f)
                curveTo(19.5f, 12.33f, 18.83f, 13f, 18f, 13f)
                close()
            }
        }.build()
    }

    /** Gear/cog with teeth around edge */
    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Gear outer shape with 8 teeth
                moveTo(10.5f, 2f)
                lineTo(13.5f, 2f)
                lineTo(14f, 4.5f)
                lineTo(16.1f, 5.3f)
                lineTo(18.2f, 3.8f)
                lineTo(20.2f, 5.8f)
                lineTo(18.7f, 7.9f)
                lineTo(19.5f, 10f)
                lineTo(22f, 10.5f)
                lineTo(22f, 13.5f)
                lineTo(19.5f, 14f)
                lineTo(18.7f, 16.1f)
                lineTo(20.2f, 18.2f)
                lineTo(18.2f, 20.2f)
                lineTo(16.1f, 18.7f)
                lineTo(14f, 19.5f)
                lineTo(13.5f, 22f)
                lineTo(10.5f, 22f)
                lineTo(10f, 19.5f)
                lineTo(7.9f, 18.7f)
                lineTo(5.8f, 20.2f)
                lineTo(3.8f, 18.2f)
                lineTo(5.3f, 16.1f)
                lineTo(4.5f, 14f)
                lineTo(2f, 13.5f)
                lineTo(2f, 10.5f)
                lineTo(4.5f, 10f)
                lineTo(5.3f, 7.9f)
                lineTo(3.8f, 5.8f)
                lineTo(5.8f, 3.8f)
                lineTo(7.9f, 5.3f)
                lineTo(10f, 4.5f)
                close()
                // Center hole
                moveTo(12f, 8f)
                curveTo(9.79f, 8f, 8f, 9.79f, 8f, 12f)
                curveTo(8f, 14.21f, 9.79f, 16f, 12f, 16f)
                curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f)
                curveTo(16f, 9.79f, 14.21f, 8f, 12f, 8f)
                close()
            }
        }.build()
    }

    /** Left-pointing chevron arrow */
    val Back: ImageVector by lazy {
        ImageVector.Builder(
            name = "Back",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(15.41f, 4.59f)
                lineTo(13.83f, 3f)
                lineTo(4f, 12f)
                lineTo(13.83f, 21f)
                lineTo(15.41f, 19.41f)
                lineTo(7.17f, 12f)
                close()
            }
        }.build()
    }

    // ========================================
    // Effect Icons
    // ========================================

    /** Lightning bolt zigzag */
    val SugarRush: ImageVector by lazy {
        ImageVector.Builder(
            name = "SugarRush",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13f, 2f)
                lineTo(6f, 13f)
                lineTo(11f, 13f)
                lineTo(10f, 22f)
                lineTo(18f, 10f)
                lineTo(13f, 10f)
                lineTo(15f, 2f)
                close()
            }
        }.build()
    }

    /** Rainbow arc with 3 concentric bands */
    val Rainbow: ImageVector by lazy {
        ImageVector.Builder(
            name = "Rainbow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Outer arc
                moveTo(2f, 18f)
                lineTo(2f, 16f)
                arcTo(10f, 10f, 0f, false, true, 22f, 16f)
                lineTo(22f, 18f)
                arcTo(11f, 11f, 0f, false, false, 2f, 18f)
                close()
                // Middle gap
                moveTo(4.5f, 18f)
                lineTo(4.5f, 17f)
                arcTo(7.5f, 7.5f, 0f, false, true, 19.5f, 17f)
                lineTo(19.5f, 18f)
                arcTo(8.5f, 8.5f, 0f, false, false, 4.5f, 18f)
                close()
                // Inner arc
                moveTo(7f, 18f)
                lineTo(7f, 17.5f)
                arcTo(5f, 5f, 0f, false, true, 17f, 17.5f)
                lineTo(17f, 18f)
                arcTo(6f, 6f, 0f, false, false, 7f, 18f)
                close()
                // Inner gap
                moveTo(9.5f, 18f)
                lineTo(9.5f, 17.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 14.5f, 17.5f)
                lineTo(14.5f, 18f)
                arcTo(3.5f, 3.5f, 0f, false, false, 9.5f, 18f)
                close()
            }
        }.build()
    }

    /** Leaf shape from two symmetric curves */
    val MintWash: ImageVector by lazy {
        ImageVector.Builder(
            name = "MintWash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Leaf body
                moveTo(17f, 3f)
                curveTo(17f, 3f, 21f, 7f, 21f, 12f)
                curveTo(21f, 17f, 17f, 21f, 12f, 21f)
                curveTo(7f, 21f, 3f, 17f, 3f, 12f)
                lineTo(3f, 12f)
                curveTo(3f, 12f, 7f, 12f, 12f, 7f)
                curveTo(14.5f, 4.5f, 17f, 3f, 17f, 3f)
                close()
                // Center vein
                moveTo(5f, 19f)
                lineTo(6f, 18f)
                lineTo(19f, 5f)
                lineTo(18f, 6f)
                curveTo(15f, 9f, 9f, 15f, 6f, 18f)
                close()
            }
        }.build()
    }

    /** Crescent moon shape */
    val CaramelDim: ImageVector by lazy {
        ImageVector.Builder(
            name = "CaramelDim",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(9.37f, 2f)
                curveTo(7.16f, 3.69f, 5.75f, 6.32f, 5.75f, 9.3f)
                curveTo(5.75f, 14.58f, 10.02f, 18.85f, 15.3f, 18.85f)
                curveTo(17.08f, 18.85f, 18.74f, 18.36f, 20.17f, 17.51f)
                curveTo(18.45f, 20.22f, 15.43f, 22f, 12f, 22f)
                curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
                curveTo(2f, 7.71f, 4.97f, 4.09f, 9.37f, 2f)
                close()
            }
        }.build()
    }

    /** Scattered small diamond shapes */
    val Confetti: ImageVector by lazy {
        ImageVector.Builder(
            name = "Confetti",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Diamond 1 (top-left)
                moveTo(5f, 3f)
                lineTo(6.5f, 5f)
                lineTo(5f, 7f)
                lineTo(3.5f, 5f)
                close()
                // Diamond 2 (top-right)
                moveTo(17f, 2f)
                lineTo(18.5f, 4f)
                lineTo(17f, 6f)
                lineTo(15.5f, 4f)
                close()
                // Square 3 (mid-left, rotated)
                moveTo(3f, 12f)
                lineTo(5f, 11f)
                lineTo(6f, 13f)
                lineTo(4f, 14f)
                close()
                // Diamond 4 (center)
                moveTo(12f, 8f)
                lineTo(13.5f, 10.5f)
                lineTo(12f, 13f)
                lineTo(10.5f, 10.5f)
                close()
                // Square 5 (mid-right, rotated)
                moveTo(19f, 10f)
                lineTo(21f, 9f)
                lineTo(22f, 11f)
                lineTo(20f, 12f)
                close()
                // Diamond 6 (bottom-left)
                moveTo(7f, 17f)
                lineTo(8.5f, 19f)
                lineTo(7f, 21f)
                lineTo(5.5f, 19f)
                close()
                // Square 7 (bottom-center, rotated)
                moveTo(13f, 16f)
                lineTo(15f, 15f)
                lineTo(16f, 17f)
                lineTo(14f, 18f)
                close()
                // Diamond 8 (bottom-right)
                moveTo(19f, 17f)
                lineTo(20.5f, 19f)
                lineTo(19f, 21f)
                lineTo(17.5f, 19f)
                close()
            }
        }.build()
    }

    /** Heart with a pulse/EKG line through center */
    val Heartbeat: ImageVector by lazy {
        ImageVector.Builder(
            name = "Heartbeat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Heart left lobe
                moveTo(12f, 21f)
                lineTo(2.1f, 11.5f)
                curveTo(0.5f, 9.5f, 0.5f, 6.5f, 2.1f, 4.5f)
                curveTo(3.7f, 2.5f, 6.5f, 2f, 8.5f, 3.5f)
                lineTo(12f, 6.5f)
                lineTo(15.5f, 3.5f)
                curveTo(17.5f, 2f, 20.3f, 2.5f, 21.9f, 4.5f)
                curveTo(23.5f, 6.5f, 23.5f, 9.5f, 21.9f, 11.5f)
                close()
                // Pulse line cutout (horizontal EKG shape)
                moveTo(2f, 11.5f)
                lineTo(8f, 11.5f)
                lineTo(9.5f, 9f)
                lineTo(11f, 14f)
                lineTo(12.5f, 10f)
                lineTo(14f, 11.5f)
                lineTo(22f, 11.5f)
                lineTo(22f, 12.5f)
                lineTo(13.5f, 12.5f)
                lineTo(12.5f, 11f)
                lineTo(11f, 15f)
                lineTo(9.5f, 10f)
                lineTo(8.5f, 12.5f)
                lineTo(2f, 12.5f)
                close()
            }
        }.build()
    }

    /** 4-point star with pinched waist */
    val Sparkle: ImageVector by lazy {
        ImageVector.Builder(
            name = "Sparkle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Large 4-point star
                moveTo(12f, 2f)
                curveTo(12f, 2f, 13.5f, 9f, 14f, 10f)
                curveTo(14.5f, 11f, 22f, 12f, 22f, 12f)
                curveTo(22f, 12f, 14.5f, 13f, 14f, 14f)
                curveTo(13.5f, 15f, 12f, 22f, 12f, 22f)
                curveTo(12f, 22f, 10.5f, 15f, 10f, 14f)
                curveTo(9.5f, 13f, 2f, 12f, 2f, 12f)
                curveTo(2f, 12f, 9.5f, 11f, 10f, 10f)
                curveTo(10.5f, 9f, 12f, 2f, 12f, 2f)
                close()
            }
        }.build()
    }

    /** Wand with small stars around tip */
    val Magic: ImageVector by lazy {
        ImageVector.Builder(
            name = "Magic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Wand body
                moveTo(3f, 21.5f)
                lineTo(2.5f, 21f)
                lineTo(14f, 9.5f)
                lineTo(14.5f, 10f)
                close()
                // Star 1 (at wand tip)
                moveTo(17f, 4f)
                lineTo(17.7f, 6f)
                lineTo(19.7f, 6.7f)
                lineTo(17.7f, 7.4f)
                lineTo(17f, 9.4f)
                lineTo(16.3f, 7.4f)
                lineTo(14.3f, 6.7f)
                lineTo(16.3f, 6f)
                close()
                // Star 2 (upper right)
                moveTo(21f, 2f)
                lineTo(21.5f, 3.2f)
                lineTo(22.7f, 3.7f)
                lineTo(21.5f, 4.2f)
                lineTo(21f, 5.4f)
                lineTo(20.5f, 4.2f)
                lineTo(19.3f, 3.7f)
                lineTo(20.5f, 3.2f)
                close()
                // Star 3 (small, right side)
                moveTo(20f, 9f)
                lineTo(20.4f, 10f)
                lineTo(21.4f, 10.4f)
                lineTo(20.4f, 10.8f)
                lineTo(20f, 11.8f)
                lineTo(19.6f, 10.8f)
                lineTo(18.6f, 10.4f)
                lineTo(19.6f, 10f)
                close()
            }
        }.build()
    }

    // ========================================
    // Category Icons
    // ========================================

    /** Play triangle inside circle */
    val VideoMusic: ImageVector by lazy {
        ImageVector.Builder(
            name = "VideoMusic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Outer circle
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                close()
                // Inner circle cutout (donut)
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, true, false, 12f, 20f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // Play triangle
                moveTo(9.5f, 7f)
                lineTo(9.5f, 17f)
                lineTo(18f, 12f)
                close()
            }
        }.build()
    }

    /** Wrench shape */
    val Tools: ImageVector by lazy {
        ImageVector.Builder(
            name = "Tools",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(22.7f, 19f)
                lineTo(13.6f, 9.9f)
                curveTo(14.5f, 7.6f, 14f, 4.9f, 12.1f, 3.1f)
                curveTo(10f, 1f, 7f, 0.6f, 4.6f, 1.8f)
                lineTo(8.5f, 5.7f)
                lineTo(5.7f, 8.5f)
                lineTo(1.7f, 4.6f)
                curveTo(0.5f, 7.1f, 1f, 10.1f, 3.1f, 12.1f)
                curveTo(4.9f, 14f, 7.6f, 14.5f, 9.9f, 13.6f)
                lineTo(19f, 22.7f)
                curveTo(19.5f, 23.1f, 20.2f, 23.1f, 20.6f, 22.7f)
                lineTo(22.7f, 20.6f)
                curveTo(23.1f, 20.1f, 23.1f, 19.5f, 22.7f, 19f)
                close()
            }
        }.build()
    }

    /** Two overlapping person silhouettes */
    val Social: ImageVector by lazy {
        ImageVector.Builder(
            name = "Social",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Front person head
                moveTo(9f, 4f)
                arcTo(3f, 3f, 0f, true, true, 9f, 10f)
                arcTo(3f, 3f, 0f, true, true, 9f, 4f)
                close()
                // Front person body
                moveTo(2f, 18f)
                curveTo(2f, 15.33f, 7.33f, 14f, 9f, 14f)
                curveTo(10.67f, 14f, 16f, 15.33f, 16f, 18f)
                lineTo(16f, 20f)
                lineTo(2f, 20f)
                close()
                // Back person head
                moveTo(15f, 4f)
                arcTo(2.5f, 2.5f, 0f, true, true, 15f, 9f)
                arcTo(2.5f, 2.5f, 0f, true, true, 15f, 4f)
                close()
                // Back person body
                moveTo(17.5f, 14.2f)
                curveTo(19f, 15.1f, 22f, 16f, 22f, 18f)
                lineTo(22f, 20f)
                lineTo(17f, 20f)
                lineTo(17f, 18f)
                curveTo(17f, 16.3f, 16.5f, 15.1f, 17.5f, 14.2f)
                close()
            }
        }.build()
    }

    /** Game controller shape */
    val Games: ImageVector by lazy {
        ImageVector.Builder(
            name = "Games",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Controller body
                moveTo(7f, 6f)
                lineTo(17f, 6f)
                curveTo(20f, 6f, 22f, 8f, 22f, 11f)
                curveTo(22f, 14f, 21f, 17f, 19f, 18f)
                curveTo(18f, 18.5f, 17f, 17.5f, 16f, 16f)
                lineTo(14f, 13f)
                lineTo(10f, 13f)
                lineTo(8f, 16f)
                curveTo(7f, 17.5f, 6f, 18.5f, 5f, 18f)
                curveTo(3f, 17f, 2f, 14f, 2f, 11f)
                curveTo(2f, 8f, 4f, 6f, 7f, 6f)
                close()
                // D-pad horizontal
                moveTo(5f, 9.5f)
                lineTo(9f, 9.5f)
                lineTo(9f, 10.5f)
                lineTo(5f, 10.5f)
                close()
                // D-pad vertical
                moveTo(6.5f, 8f)
                lineTo(7.5f, 8f)
                lineTo(7.5f, 12f)
                lineTo(6.5f, 12f)
                close()
                // Button top
                moveTo(16.5f, 8f)
                arcTo(1f, 1f, 0f, true, true, 16.5f, 10f)
                arcTo(1f, 1f, 0f, true, true, 16.5f, 8f)
                close()
                // Button bottom
                moveTo(16.5f, 10.5f)
                arcTo(1f, 1f, 0f, true, true, 16.5f, 12.5f)
                arcTo(1f, 1f, 0f, true, true, 16.5f, 10.5f)
                close()
                // Button left
                moveTo(14.5f, 9.5f)
                arcTo(1f, 1f, 0f, true, true, 14.5f, 11.5f)
                arcTo(1f, 1f, 0f, true, true, 14.5f, 9.5f)
                close()
                // Button right
                moveTo(18.5f, 9.5f)
                arcTo(1f, 1f, 0f, true, true, 18.5f, 11.5f)
                arcTo(1f, 1f, 0f, true, true, 18.5f, 9.5f)
                close()
            }
        }.build()
    }

    /** Checkmark inside a circle */
    val Productivity: ImageVector by lazy {
        ImageVector.Builder(
            name = "Productivity",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Outer circle
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                close()
                // Inner circle (hollow)
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, true, false, 12f, 20f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // Checkmark
                moveTo(10f, 14.5f)
                lineTo(7f, 11.5f)
                lineTo(5.6f, 12.9f)
                lineTo(10f, 17.3f)
                lineTo(18.4f, 8.9f)
                lineTo(17f, 7.5f)
                close()
            }
        }.build()
    }

    /** 3x3 grid of dots */
    val Other: ImageVector by lazy {
        ImageVector.Builder(
            name = "Other",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Row 1
                moveTo(5f, 3.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 6.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 3.5f)
                close()
                moveTo(12f, 3.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 6.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 3.5f)
                close()
                moveTo(19f, 3.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 19f, 6.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 19f, 3.5f)
                close()
                // Row 2
                moveTo(5f, 10.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 13.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 10.5f)
                close()
                moveTo(12f, 10.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 13.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 10.5f)
                close()
                moveTo(19f, 10.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 19f, 13.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 19f, 10.5f)
                close()
                // Row 3
                moveTo(5f, 17.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 20.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 17.5f)
                close()
                moveTo(12f, 17.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 20.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 17.5f)
                close()
                moveTo(19f, 17.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 19f, 20.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 19f, 17.5f)
                close()
            }
        }.build()
    }

    // ========================================
    // Shop & Rewards Icons
    // ========================================

    /** Coin with candy swirl inside */
    val SugarPoints: ImageVector by lazy {
        ImageVector.Builder(
            name = "SugarPoints",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Outer circle
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                close()
                // Inner ring cutout
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, true, false, 12f, 20f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // S shape inside
                moveTo(15f, 8f)
                curveTo(15f, 8f, 15f, 6.5f, 12f, 6.5f)
                curveTo(9.5f, 6.5f, 8.5f, 8f, 8.5f, 9.5f)
                curveTo(8.5f, 11f, 10f, 11.5f, 12f, 12f)
                curveTo(14f, 12.5f, 15.5f, 13f, 15.5f, 14.5f)
                curveTo(15.5f, 16f, 14.5f, 17.5f, 12f, 17.5f)
                curveTo(9f, 17.5f, 9f, 16f, 9f, 16f)
                lineTo(8f, 16f)
                curveTo(8f, 17f, 9f, 18.5f, 12f, 18.5f)
                curveTo(15f, 18.5f, 16.5f, 16.5f, 16.5f, 14.5f)
                curveTo(16.5f, 12.5f, 15f, 11.7f, 12.5f, 11f)
                curveTo(10.5f, 10.5f, 9.5f, 10f, 9.5f, 9.5f)
                curveTo(9.5f, 8.5f, 10.5f, 7.5f, 12f, 7.5f)
                curveTo(14f, 7.5f, 14f, 8f, 14f, 8f)
                close()
            }
        }.build()
    }

    /** Shopping bag with handle */
    val Shop: ImageVector by lazy {
        ImageVector.Builder(
            name = "Shop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Bag body
                moveTo(5f, 8f)
                lineTo(4f, 21f)
                lineTo(20f, 21f)
                lineTo(19f, 8f)
                close()
                // Handle (arch cutout - outer)
                moveTo(8f, 8f)
                curveTo(8f, 4.69f, 9.79f, 3f, 12f, 3f)
                curveTo(14.21f, 3f, 16f, 4.69f, 16f, 8f)
                lineTo(14.5f, 8f)
                curveTo(14.5f, 5.51f, 13.38f, 4.5f, 12f, 4.5f)
                curveTo(10.62f, 4.5f, 9.5f, 5.51f, 9.5f, 8f)
                close()
            }
        }.build()
    }

    /** Gift box with ribbon and bow */
    val DailyReward: ImageVector by lazy {
        ImageVector.Builder(
            name = "DailyReward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Box bottom
                moveTo(3f, 11f)
                lineTo(3f, 21f)
                lineTo(21f, 21f)
                lineTo(21f, 11f)
                close()
                // Vertical ribbon gap
                moveTo(11f, 11f)
                lineTo(13f, 11f)
                lineTo(13f, 21f)
                lineTo(11f, 21f)
                close()
                // Box lid
                moveTo(2f, 8f)
                lineTo(2f, 11f)
                lineTo(22f, 11f)
                lineTo(22f, 8f)
                close()
                // Horizontal ribbon gap on lid
                moveTo(11f, 8f)
                lineTo(13f, 8f)
                lineTo(13f, 11f)
                lineTo(11f, 11f)
                close()
                // Bow left loop
                moveTo(12f, 8f)
                curveTo(12f, 8f, 8f, 7f, 7f, 5f)
                curveTo(6f, 3f, 8f, 2f, 9.5f, 2.5f)
                curveTo(11f, 3f, 12f, 5f, 12f, 8f)
                close()
                // Bow right loop
                moveTo(12f, 8f)
                curveTo(12f, 5f, 13f, 3f, 14.5f, 2.5f)
                curveTo(16f, 2f, 18f, 3f, 17f, 5f)
                curveTo(16f, 7f, 12f, 8f, 12f, 8f)
                close()
            }
        }.build()
    }

    /** Trophy with handles */
    val Achievement: ImageVector by lazy {
        ImageVector.Builder(
            name = "Achievement",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Cup body
                moveTo(7f, 3f)
                lineTo(17f, 3f)
                lineTo(17f, 5f)
                curveTo(17f, 10f, 15f, 13f, 12f, 14f)
                curveTo(9f, 13f, 7f, 10f, 7f, 5f)
                close()
                // Left handle
                moveTo(7f, 4f)
                lineTo(5f, 4f)
                curveTo(3.5f, 4f, 2.5f, 5f, 2.5f, 6.5f)
                curveTo(2.5f, 8f, 3.5f, 9f, 5f, 9f)
                lineTo(7f, 9f)
                lineTo(7f, 7.5f)
                lineTo(5f, 7.5f)
                curveTo(4.5f, 7.5f, 4f, 7f, 4f, 6.5f)
                curveTo(4f, 6f, 4.5f, 5.5f, 5f, 5.5f)
                lineTo(7f, 5.5f)
                close()
                // Right handle
                moveTo(17f, 4f)
                lineTo(19f, 4f)
                curveTo(20.5f, 4f, 21.5f, 5f, 21.5f, 6.5f)
                curveTo(21.5f, 8f, 20.5f, 9f, 19f, 9f)
                lineTo(17f, 9f)
                lineTo(17f, 7.5f)
                lineTo(19f, 7.5f)
                curveTo(19.5f, 7.5f, 20f, 7f, 20f, 6.5f)
                curveTo(20f, 6f, 19.5f, 5.5f, 19f, 5.5f)
                lineTo(17f, 5.5f)
                close()
                // Stem
                moveTo(11f, 14f)
                lineTo(13f, 14f)
                lineTo(13f, 18f)
                lineTo(11f, 18f)
                close()
                // Base
                moveTo(8f, 18f)
                lineTo(16f, 18f)
                lineTo(16f, 20f)
                lineTo(8f, 20f)
                close()
            }
        }.build()
    }

    /** Flame/fire shape */
    val Streak: ImageVector by lazy {
        ImageVector.Builder(
            name = "Streak",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 1f)
                curveTo(12f, 1f, 7f, 7f, 7f, 13f)
                curveTo(7f, 15.5f, 8f, 17.5f, 9.5f, 19f)
                curveTo(9.5f, 19f, 9f, 17f, 10f, 15f)
                curveTo(11f, 13f, 12f, 12f, 12f, 12f)
                curveTo(12f, 12f, 13f, 13f, 14f, 15f)
                curveTo(15f, 17f, 14.5f, 19f, 14.5f, 19f)
                curveTo(16f, 17.5f, 17f, 15.5f, 17f, 13f)
                curveTo(17f, 7f, 12f, 1f, 12f, 1f)
                close()
                // Inner flame highlight
                moveTo(12f, 16f)
                curveTo(11f, 16f, 10f, 17f, 10f, 18.5f)
                curveTo(10f, 20f, 11f, 21f, 12f, 21f)
                curveTo(13f, 21f, 14f, 20f, 14f, 18.5f)
                curveTo(14f, 17f, 13f, 16f, 12f, 16f)
                close()
            }
        }.build()
    }

    /** Rocket ship */
    val Boost: ImageVector by lazy {
        ImageVector.Builder(
            name = "Boost",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Rocket body
                moveTo(12f, 2f)
                curveTo(12f, 2f, 15f, 5f, 16f, 10f)
                lineTo(16f, 16f)
                lineTo(14f, 18f)
                lineTo(10f, 18f)
                lineTo(8f, 16f)
                lineTo(8f, 10f)
                curveTo(9f, 5f, 12f, 2f, 12f, 2f)
                close()
                // Left fin
                moveTo(8f, 12f)
                lineTo(5f, 16f)
                lineTo(5f, 19f)
                lineTo(8f, 16f)
                close()
                // Right fin
                moveTo(16f, 12f)
                lineTo(19f, 16f)
                lineTo(19f, 19f)
                lineTo(16f, 16f)
                close()
                // Window
                moveTo(12f, 8f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 11f)
                arcTo(1.5f, 1.5f, 0f, true, true, 12f, 8f)
                close()
                // Exhaust flame
                moveTo(10f, 18f)
                lineTo(11f, 22f)
                lineTo(12f, 20f)
                lineTo(13f, 22f)
                lineTo(14f, 18f)
                close()
            }
        }.build()
    }

    /** Backpack shape */
    val Inventory: ImageVector by lazy {
        ImageVector.Builder(
            name = "Inventory",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Top handle
                moveTo(9f, 3f)
                curveTo(9f, 1.5f, 10f, 1f, 12f, 1f)
                curveTo(14f, 1f, 15f, 1.5f, 15f, 3f)
                lineTo(15f, 5f)
                lineTo(13.5f, 5f)
                lineTo(13.5f, 3f)
                curveTo(13.5f, 2.5f, 13f, 2.5f, 12f, 2.5f)
                curveTo(11f, 2.5f, 10.5f, 2.5f, 10.5f, 3f)
                lineTo(10.5f, 5f)
                lineTo(9f, 5f)
                close()
                // Main bag body
                moveTo(6f, 5f)
                curveTo(5f, 5f, 4f, 6f, 4f, 7f)
                lineTo(4f, 20f)
                curveTo(4f, 21f, 5f, 22f, 6f, 22f)
                lineTo(18f, 22f)
                curveTo(19f, 22f, 20f, 21f, 20f, 20f)
                lineTo(20f, 7f)
                curveTo(20f, 6f, 19f, 5f, 18f, 5f)
                close()
                // Front pocket
                moveTo(7f, 14f)
                lineTo(17f, 14f)
                lineTo(17f, 19f)
                curveTo(17f, 19.5f, 16.5f, 20f, 16f, 20f)
                lineTo(8f, 20f)
                curveTo(7.5f, 20f, 7f, 19.5f, 7f, 19f)
                close()
                // Pocket flap
                moveTo(9f, 14f)
                lineTo(15f, 14f)
                lineTo(15f, 15.5f)
                lineTo(9f, 15.5f)
                close()
            }
        }.build()
    }

    /** Diamond/gem with facets */
    val Rare: ImageVector by lazy {
        ImageVector.Builder(
            name = "Rare",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Top facet row
                moveTo(5f, 4f)
                lineTo(19f, 4f)
                lineTo(22f, 9f)
                lineTo(2f, 9f)
                close()
                // Bottom facet (main triangle)
                moveTo(2f, 9f)
                lineTo(22f, 9f)
                lineTo(12f, 22f)
                close()
                // Internal facet lines (left facet)
                moveTo(8f, 4f)
                lineTo(6f, 9f)
                lineTo(12f, 22f)
                lineTo(10f, 9f)
                close()
                // Internal facet lines (right facet)
                moveTo(16f, 4f)
                lineTo(18f, 9f)
                lineTo(12f, 22f)
                lineTo(14f, 9f)
                close()
            }
        }.build()
    }

    // ========================================
    // Action Icons
    // ========================================

    /** Downward arrow into a tray */
    val Download: ImageVector by lazy {
        ImageVector.Builder(
            name = "Download",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Arrow shaft
                moveTo(11f, 3f)
                lineTo(13f, 3f)
                lineTo(13f, 13f)
                lineTo(11f, 13f)
                close()
                // Arrow head
                moveTo(12f, 17f)
                lineTo(7f, 12f)
                lineTo(8.4f, 10.6f)
                lineTo(12f, 14.2f)
                lineTo(15.6f, 10.6f)
                lineTo(17f, 12f)
                close()
                // Tray
                moveTo(4f, 17f)
                lineTo(4f, 20f)
                lineTo(20f, 20f)
                lineTo(20f, 17f)
                lineTo(18f, 17f)
                lineTo(18f, 18f)
                lineTo(6f, 18f)
                lineTo(6f, 17f)
                close()
            }
        }.build()
    }

    /** Plus sign inside a circle */
    val Install: ImageVector by lazy {
        ImageVector.Builder(
            name = "Install",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Outer circle
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                close()
                // Inner circle cutout
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, true, false, 12f, 20f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // Plus horizontal
                moveTo(7f, 11f)
                lineTo(17f, 11f)
                lineTo(17f, 13f)
                lineTo(7f, 13f)
                close()
                // Plus vertical
                moveTo(11f, 7f)
                lineTo(13f, 7f)
                lineTo(13f, 17f)
                lineTo(11f, 17f)
                close()
            }
        }.build()
    }

    /** Two curved arrows forming a refresh cycle */
    val Update: ImageVector by lazy {
        ImageVector.Builder(
            name = "Update",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Top arrow (clockwise, right-pointing)
                moveTo(17.65f, 6.35f)
                curveTo(16.2f, 4.9f, 14.2f, 4f, 12f, 4f)
                curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f)
                lineTo(2f, 12f)
                curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
                curveTo(14.76f, 2f, 17.26f, 3.12f, 19.07f, 4.93f)
                lineTo(21f, 3f)
                lineTo(21f, 9f)
                lineTo(15f, 9f)
                close()
                // Bottom arrow (counter-clockwise, left-pointing)
                moveTo(6.35f, 17.65f)
                curveTo(7.8f, 19.1f, 9.8f, 20f, 12f, 20f)
                curveTo(16.42f, 20f, 19.99f, 16.42f, 19.99f, 12f)
                lineTo(22f, 12f)
                curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
                curveTo(9.24f, 22f, 6.74f, 20.88f, 4.93f, 19.07f)
                lineTo(3f, 21f)
                lineTo(3f, 15f)
                lineTo(9f, 15f)
                close()
            }
        }.build()
    }

    /** Trash can with lid */
    val Delete: ImageVector by lazy {
        ImageVector.Builder(
            name = "Delete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Lid
                moveTo(3f, 5f)
                lineTo(21f, 5f)
                lineTo(21f, 7f)
                lineTo(3f, 7f)
                close()
                // Handle on lid
                moveTo(9f, 3f)
                lineTo(15f, 3f)
                lineTo(15f, 5f)
                lineTo(9f, 5f)
                close()
                // Can body
                moveTo(5f, 7f)
                lineTo(19f, 7f)
                lineTo(18f, 21f)
                lineTo(6f, 21f)
                close()
                // Left groove
                moveTo(9f, 9f)
                lineTo(9.5f, 19f)
                lineTo(10.5f, 19f)
                lineTo(10f, 9f)
                close()
                // Right groove
                moveTo(14f, 9f)
                lineTo(13.5f, 19f)
                lineTo(14.5f, 19f)
                lineTo(15f, 9f)
                close()
            }
        }.build()
    }

    /** Branching share node with three circles connected by lines */
    val Share: ImageVector by lazy {
        ImageVector.Builder(
            name = "Share",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Top-right node
                moveTo(18f, 2f)
                arcTo(3f, 3f, 0f, true, true, 18f, 8f)
                arcTo(3f, 3f, 0f, true, true, 18f, 2f)
                close()
                // Middle-left node
                moveTo(6f, 9f)
                arcTo(3f, 3f, 0f, true, true, 6f, 15f)
                arcTo(3f, 3f, 0f, true, true, 6f, 9f)
                close()
                // Bottom-right node
                moveTo(18f, 16f)
                arcTo(3f, 3f, 0f, true, true, 18f, 22f)
                arcTo(3f, 3f, 0f, true, true, 18f, 16f)
                close()
                // Line from left to top-right
                moveTo(8.59f, 10.51f)
                lineTo(15.42f, 6.49f)
                lineTo(16.42f, 8.22f)
                lineTo(9.59f, 12.24f)
                close()
                // Line from left to bottom-right
                moveTo(8.59f, 13.49f)
                lineTo(15.42f, 17.51f)
                lineTo(16.42f, 15.78f)
                lineTo(9.59f, 11.76f)
                close()
            }
        }.build()
    }

    /** Five-pointed star */
    val Favorite: ImageVector by lazy {
        ImageVector.Builder(
            name = "Favorite",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // 5-point star computed from center (12,12), outer r=10, inner r=4.5
                // Points at 5 outer vertices and 5 inner vertices, starting from top
                moveTo(12f, 2f)          // top outer
                lineTo(13.76f, 8.29f)    // inner right-upper
                lineTo(21.51f, 8.29f)    // outer right-upper
                lineTo(15.36f, 12.71f)   // inner right-lower
                lineTo(17.63f, 20.71f)   // outer right-lower
                lineTo(12f, 16.18f)      // inner bottom
                lineTo(6.37f, 20.71f)    // outer left-lower
                lineTo(8.64f, 12.71f)    // inner left-lower
                lineTo(2.49f, 8.29f)     // outer left-upper
                lineTo(10.24f, 8.29f)    // inner left-upper
                close()
            }
        }.build()
    }

    /** Magnifying glass */
    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Glass circle (outer)
                moveTo(10f, 2f)
                arcTo(8f, 8f, 0f, true, true, 10f, 18f)
                arcTo(8f, 8f, 0f, true, true, 10f, 2f)
                close()
                // Glass circle (inner cutout)
                moveTo(10f, 4f)
                arcTo(6f, 6f, 0f, true, false, 10f, 16f)
                arcTo(6f, 6f, 0f, true, false, 10f, 4f)
                close()
                // Handle
                moveTo(15.5f, 14f)
                lineTo(22f, 20.5f)
                lineTo(20.5f, 22f)
                lineTo(14f, 15.5f)
                close()
            }
        }.build()
    }

    /** Funnel shape narrowing downward */
    val Filter: ImageVector by lazy {
        ImageVector.Builder(
            name = "Filter",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(2f, 4f)
                lineTo(22f, 4f)
                lineTo(14f, 13f)
                lineTo(14f, 20f)
                lineTo(10f, 22f)
                lineTo(10f, 13f)
                close()
            }
        }.build()
    }

    // ========================================
    // Status Icons
    // ========================================

    /** Checkmark inside circle */
    val Success: ImageVector by lazy {
        ImageVector.Builder(
            name = "Success",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Circle
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                close()
                // Inner cutout
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, true, false, 12f, 20f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // Checkmark
                moveTo(10f, 15.17f)
                lineTo(6.83f, 12f)
                lineTo(5.41f, 13.41f)
                lineTo(10f, 18f)
                lineTo(19f, 9f)
                lineTo(17.59f, 7.59f)
                close()
            }
        }.build()
    }

    /** X inside circle */
    val Error: ImageVector by lazy {
        ImageVector.Builder(
            name = "Error",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Circle
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                close()
                // Inner cutout
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, true, false, 12f, 20f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // X mark (two crossing bars)
                moveTo(8.46f, 7.05f)
                lineTo(7.05f, 8.46f)
                lineTo(10.59f, 12f)
                lineTo(7.05f, 15.54f)
                lineTo(8.46f, 16.95f)
                lineTo(12f, 13.41f)
                lineTo(15.54f, 16.95f)
                lineTo(16.95f, 15.54f)
                lineTo(13.41f, 12f)
                lineTo(16.95f, 8.46f)
                lineTo(15.54f, 7.05f)
                lineTo(12f, 10.59f)
                close()
            }
        }.build()
    }

    /** Exclamation mark inside triangle */
    val Warning: ImageVector by lazy {
        ImageVector.Builder(
            name = "Warning",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Triangle
                moveTo(12f, 2f)
                lineTo(1f, 21f)
                lineTo(23f, 21f)
                close()
                // Inner triangle cutout
                moveTo(12f, 5.5f)
                lineTo(4f, 19.5f)
                lineTo(20f, 19.5f)
                close()
                // Exclamation bar
                moveTo(11f, 10f)
                lineTo(13f, 10f)
                lineTo(13f, 15f)
                lineTo(11f, 15f)
                close()
                // Exclamation dot
                moveTo(11f, 16.5f)
                lineTo(13f, 16.5f)
                lineTo(13f, 18.5f)
                lineTo(11f, 18.5f)
                close()
            }
        }.build()
    }

    /** Letter 'i' inside circle */
    val Info: ImageVector by lazy {
        ImageVector.Builder(
            name = "Info",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Circle
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                close()
                // Inner cutout
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, true, false, 12f, 20f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // 'i' dot
                moveTo(11f, 6.5f)
                lineTo(13f, 6.5f)
                lineTo(13f, 8.5f)
                lineTo(11f, 8.5f)
                close()
                // 'i' body
                moveTo(11f, 10f)
                lineTo(13f, 10f)
                lineTo(13f, 17f)
                lineTo(11f, 17f)
                close()
            }
        }.build()
    }

    /** Circular arc suggesting a spinner */
    val Loading: ImageVector by lazy {
        ImageVector.Builder(
            name = "Loading",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Partial circle arc (about 270 degrees) — outer
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, true, 2f, 12f)
                lineTo(4f, 12f)
                arcTo(8f, 8f, 0f, true, false, 12f, 4f)
                close()
                // Gap at top-left makes it look like a spinner
            }
        }.build()
    }
}
