package com.sugarmunch.app.ui.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * Visual style definitions for charts
 */
object ChartStyles {
    
    /**
     * Get gradient brush for a chart style
     */
    fun getGradientBrush(
        style: ChartStyle,
        start: Offset = Offset.Zero,
        end: Offset = Offset.Infinity,
        colors: List<Color>? = null
    ): Brush {
        val gradientColors = colors ?: getDefaultGradientColors(style)
        
        return when (style) {
            ChartStyle.NEON -> Brush.linearGradient(
                colors = gradientColors,
                start = start,
                end = end
            )
            ChartStyle.GLASS -> Brush.verticalGradient(
                colors = gradientColors.map { it.copy(alpha = 0.6f) }
            )
            ChartStyle.CRYSTAL -> Brush.radialGradient(
                colors = gradientColors,
                center = Offset(end.x / 2, end.y / 2)
            )
            ChartStyle.LIQUID -> Brush.sweepGradient(
                colors = gradientColors,
                center = Offset(end.x / 2, end.y / 2)
            )
            ChartStyle.HOLOGRAPHIC -> Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFFF0080),
                    Color(0xFF00FFFF),
                    Color(0xFF80FF00),
                    Color(0xFFFF0080)
                ),
                center = Offset(end.x / 2, end.y / 2)
            )
            ChartStyle.SUGARRUSH -> Brush.linearGradient(
                colors = listOf(
                    SugarDimens.Brand.hotPink,
                    SugarDimens.Brand.mint,
                    SugarDimens.Brand.yellow,
                    SugarDimens.Brand.candyOrange
                ),
                start = start,
                end = end
            )
        }
    }
    
    /**
     * Get default gradient colors for a style
     */
    fun getDefaultGradientColors(style: ChartStyle): List<Color> {
        return when (style) {
            ChartStyle.NEON -> listOf(
                Color(0xFFFF00FF),
                Color(0xFF00FFFF),
                Color(0xFFFF00FF)
            )
            ChartStyle.GLASS -> listOf(
                Color(0xFF8EC5FC),
                Color(0xFFE0C3FC),
                Color(0xFF8EC5FC)
            )
            ChartStyle.CRYSTAL -> listOf(
                Color(0xFFF8F9FA),
                Color(0xFF8EC5FC),
                Color(0xFFE0C3FC)
            )
            ChartStyle.LIQUID -> listOf(
                Color(0xFF00C9FF),
                Color(0xFF92FE9D),
                Color(0xFF00C9FF)
            )
            ChartStyle.HOLOGRAPHIC -> listOf(
                Color(0xFFFF0080),
                Color(0xFF00FFFF),
                Color(0xFF80FF00)
            )
            ChartStyle.SUGARRUSH -> listOf(
                SugarDimens.Brand.hotPink,
                SugarDimens.Brand.mint,
                SugarDimens.Brand.yellow
            )
        }
    }
    
    /**
     * Get glow color for chart elements
     */
    fun getGlowColor(style: ChartStyle, baseColor: Color): Color {
        return when (style) {
            ChartStyle.NEON -> baseColor.copy(alpha = 0.6f)
            ChartStyle.GLASS -> baseColor.copy(alpha = 0.3f)
            ChartStyle.CRYSTAL -> Color(0xFF8EC5FC).copy(alpha = 0.4f)
            ChartStyle.LIQUID -> baseColor.copy(alpha = 0.5f)
            ChartStyle.HOLOGRAPHIC -> Color(0xFFFF0080).copy(alpha = 0.5f)
            ChartStyle.SUGARRUSH -> SugarDimens.Brand.hotPink.copy(alpha = 0.6f)
        }
    }
    
    /**
     * Get shadow configuration for style
     */
    fun getShadow(style: ChartStyle, color: Color): Shadow {
        return when (style) {
            ChartStyle.NEON -> Shadow(
                color = getGlowColor(style, color),
                blurRadius = 12f
            )
            ChartStyle.GLASS -> Shadow(
                color = Color.Black.copy(alpha = 0.1f),
                blurRadius = 8f
            )
            ChartStyle.CRYSTAL -> Shadow(
                color = Color(0xFF8EC5FC).copy(alpha = 0.3f),
                blurRadius = 16f
            )
            ChartStyle.LIQUID -> Shadow(
                color = getGlowColor(style, color),
                blurRadius = 10f
            )
            ChartStyle.HOLOGRAPHIC -> Shadow(
                color = Color(0xFFFF0080).copy(alpha = 0.4f),
                blurRadius = 14f
            )
            ChartStyle.SUGARRUSH -> Shadow(
                color = SugarDimens.Brand.mint.copy(alpha = 0.5f),
                blurRadius = 12f
            )
        }
    }
    
    /**
     * Get border width for style
     */
    fun getBorderWidth(style: ChartStyle): Float {
        return when (style) {
            ChartStyle.NEON -> 3f
            ChartStyle.GLASS -> 1f
            ChartStyle.CRYSTAL -> 2f
            ChartStyle.LIQUID -> 0f // No border for liquid
            ChartStyle.HOLOGRAPHIC -> 2f
            ChartStyle.SUGARRUSH -> 3f
        }
    }
}

/**
 * Animation specs for chart transitions
 */
object ChartAnimations {
    const val DEFAULT_DURATION = 1000
    const val FAST_DURATION = 500
    const val SLOW_DURATION = 2000
    
    val EASING_OUT_BACK = androidx.compose.animation.core.BackOut
    val EASING_IN_OUT = androidx.compose.animation.core.FastOutSlowInEasing
    val EASING_LINEAR = androidx.compose.animation.core.LinearEasing
}
