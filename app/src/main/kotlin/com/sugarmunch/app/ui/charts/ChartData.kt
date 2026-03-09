package com.sugarmunch.app.ui.charts

import androidx.compose.ui.graphics.Color

/**
 * Data models for chart components
 */

/**
 * Single data point for charts
 */
data class ChartDataPoint(
    val label: String,
    val value: Float,
    val color: Color? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Series of data points for multi-series charts
 */
data class ChartSeries(
    val name: String,
    val data: List<ChartDataPoint>,
    val color: Color? = null
)

/**
 * Chart configuration
 */
data class ChartConfig(
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val showLegend: Boolean = true,
    val animate: Boolean = true,
    val animationDuration: Int = 1000,
    val cornerRadius: Float = 8f,
    val barWidth: Float = 48f,
    val lineSmoothness: Float = 0.3f,
    val showDataPoints: Boolean = true,
    val gradientFill: Boolean = true,
    val style: ChartStyle = ChartStyle.NEON
)

/**
 * Chart visual styles matching SugarMunch theme
 */
enum class ChartStyle {
    NEON,           // Glowing neon borders
    GLASS,          // Glassmorphic with transparency
    CRYSTAL,        // Crystal with refraction
    LIQUID,         // Fluid gradient fills
    HOLOGRAPHIC,    // Rainbow holographic effect
    SUGARRUSH       // Animated SugarMunch gradient
}

/**
 * Tooltip data for interactive charts
 */
data class ChartTooltip(
    val label: String,
    val value: String,
    val color: Color,
    val extraInfo: List<String> = emptyList()
)

/**
 * Axis configuration
 */
data class AxisConfig(
    val showXAxis: Boolean = true,
    val showYAxis: Boolean = true,
    val xAxisLabel: String = "",
    val yAxisLabel: String = "",
    val yMin: Float? = null,
    val yMax: Float? = null,
    val gridLines: Int = 5,
    val labelRotation: Float = 0f
)

/**
 * Legend item
 */
data class LegendItem(
    val label: String,
    val color: Color,
    val isSelected: Boolean = false
)
