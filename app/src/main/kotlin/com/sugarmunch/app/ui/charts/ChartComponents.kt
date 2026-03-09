package com.sugarmunch.app.ui.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * EXTREME Data Visualization Suite for SugarMunch
 * 8 chart types with animated entry effects and SugarMunch styling
 */

// ═══════════════════════════════════════════════════════════════
// BAR CHART - Vertical/Horizontal bars with gradient fills
// ═══════════════════════════════════════════════════════════════

@Composable
fun BarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    axisConfig: AxisConfig = AxisConfig(),
    horizontal: Boolean = false,
    onBarClick: ((ChartDataPoint) -> Unit)? = null,
    showTooltip: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val animationProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(
                1f,
                animationSpec = tween(config.animationDuration)
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(config.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium)
    ) {
        Box(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 40f
                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2
                
                // Calculate max value
                val maxValue = data.maxOfOrNull { it.value } ?: 1f
                val barWidth = if (horizontal) {
                    (chartHeight / data.size).coerceAtMost(config.barWidth)
                } else {
                    (chartWidth / data.size).coerceAtMost(config.barWidth)
                }
                val barSpacing = if (horizontal) {
                    (chartHeight - barWidth * data.size) / (data.size + 1)
                } else {
                    (chartWidth - barWidth * data.size) / (data.size + 1)
                }
                
                // Draw grid
                if (config.showGrid) {
                    drawGrid(
                        width = chartWidth,
                        height = chartHeight,
                        offsetX = padding,
                        offsetY = padding,
                        config = config,
                        axisConfig = axisConfig,
                        maxValue = maxValue,
                        textMeasurer = textMeasurer
                    )
                }
                
                // Draw bars
                data.forEachIndexed { index, point ->
                    val barProgress = animationProgress.value
                    val normalizedValue = point.value / maxValue
                    
                    val (barRect, barColor) = if (horizontal) {
                        val left = padding
                        val top = padding + barSpacing + index * (barWidth + barSpacing)
                        val right = padding + normalizedValue * chartWidth * barProgress
                        val bottom = top + barWidth
                        
                        Rect(Offset(left, top), Offset(right, bottom)) to
                            (point.color ?: ChartStyles.getDefaultGradientColors(config.style)[index % 3])
                    } else {
                        val left = padding + barSpacing + index * (barWidth + barSpacing)
                        val top = padding + chartHeight - normalizedValue * chartHeight * barProgress
                        val right = left + barWidth
                        val bottom = padding + chartHeight
                        
                        Rect(Offset(left, top), Offset(right, bottom)) to
                            (point.color ?: ChartStyles.getDefaultGradientColors(config.style)[index % 3])
                    }
                    
                    // Draw bar with gradient
                    val brush = ChartStyles.getGradientBrush(
                        style = config.style,
                        start = Offset(barRect.left, barRect.bottom),
                        end = Offset(barRect.right, barRect.top),
                        colors = listOf(barColor, barColor.copy(alpha = 0.7f))
                    )
                    
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(barRect.left, barRect.top),
                        size = barRect.size,
                        cornerRadius = CornerRadius(config.cornerRadius, config.cornerRadius)
                    )
                    
                    // Draw glow effect
                    if (config.style == ChartStyle.NEON || config.style == ChartStyle.SUGARRUSH) {
                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(barRect.left, barRect.top),
                            size = barRect.size,
                            cornerRadius = CornerRadius(config.cornerRadius, config.cornerRadius),
                            style = Stroke(
                                width = 2f,
                                pathEffect = android.graphics.PathEffect()
                            )
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LINE CHART - Smooth bezier curves with glow effects
// ═══════════════════════════════════════════════════════════════

@Composable
fun LineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    axisConfig: AxisConfig = AxisConfig(),
    onPointClick: ((ChartDataPoint) -> Unit)? = null
) {
    val animationProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(
                1f,
                animationSpec = tween(config.animationDuration)
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(config.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Box(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 40f
                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2
                
                val maxValue = data.maxOfOrNull { it.value } ?: 1f
                val minValue = data.minOfOrNull { it.value } ?: 0f
                val valueRange = maxValue - minValue
                
                // Draw grid
                if (config.showGrid) {
                    drawGrid(
                        width = chartWidth,
                        height = chartHeight,
                        offsetX = padding,
                        offsetY = padding,
                        config = config,
                        axisConfig = axisConfig,
                        maxValue = maxValue,
                        textMeasurer = textMeasurer
                    )
                }
                
                if (data.size < 2) return@Canvas
                
                // Calculate points
                val points = data.mapIndexed { index, point ->
                    val x = padding + (index.toFloat() / (data.size - 1)) * chartWidth
                    val y = padding + chartHeight - ((point.value - minValue) / valueRange) * chartHeight
                    Offset(x, y)
                }
                
                // Draw gradient fill
                if (config.gradientFill) {
                    val fillPath = Path().apply {
                        moveTo(padding, padding + chartHeight)
                        lineTo(points[0].x, points[0].y)
                        
                        // Draw smooth curve through points
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val controlPoint1 = Offset(
                                p1.x + (p2.x - p1.x) * config.lineSmoothness,
                                p1.y
                            )
                            val controlPoint2 = Offset(
                                p2.x - (p2.x - p1.x) * config.lineSmoothness,
                                p2.y
                            )
                            cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p2.x, p2.y
                            )
                        }
                        
                        lineTo(points.last().x, padding + chartHeight)
                        close()
                    }
                    
                    val gradientBrush = ChartStyles.getGradientBrush(
                        style = config.style,
                        start = Offset(padding, padding),
                        end = Offset(padding + chartWidth, padding + chartHeight)
                    )
                    
                    drawPath(fillPath, gradientBrush, alpha = 0.3f * animationProgress.value)
                }
                
                // Draw line
                val linePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlPoint1 = Offset(
                            p1.x + (p2.x - p1.x) * config.lineSmoothness,
                            p1.y
                        )
                        val controlPoint2 = Offset(
                            p2.x - (p2.x - p1.x) * config.lineSmoothness,
                            p2.y
                        )
                        cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p2.x, p2.y
                        )
                    }
                }
                
                val lineColor = ChartStyles.getDefaultGradientColors(config.style).first()
                drawPath(
                    linePath,
                    lineColor,
                    style = Stroke(
                        width = 4f * animationProgress.value,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                
                // Draw glow
                if (config.style == ChartStyle.NEON) {
                    drawPath(
                        linePath,
                        lineColor.copy(alpha = 0.3f),
                        style = Stroke(width = 12f * animationProgress.value)
                    )
                }
                
                // Draw data points
                if (config.showDataPoints) {
                    points.forEachIndexed { index, point ->
                        val pointProgress = animationProgress.value
                        drawCircle(
                            color = ChartStyles.getDefaultGradientColors(config.style)[index % 3],
                            radius = 8f * pointProgress,
                            center = point
                        )
                        
                        // Outer ring
                        drawCircle(
                            color = ChartStyles.getDefaultGradientColors(config.style)[index % 3].copy(alpha = 0.3f),
                            radius = 16f * pointProgress,
                            center = point,
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PIE CHART - 3D extruded pie with holographic effects
// ═══════════════════════════════════════════════════════════════

@Composable
fun PieChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    showLabels: Boolean = true,
    show3D: Boolean = true
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(
                1f,
                animationSpec = tween(config.animationDuration)
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }
    
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val colors = remember { data.map { it.color ?: ChartStyles.getDefaultGradientColors(config.style)[data.indexOf(it) % 3] } }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(config.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(250.dp)
            ) {
                val size = this.size
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.width / 2).coerceAtMost(size.height / 2) * 0.9f
                
                var currentAngle = -90f
                
                // Draw 3D extrusion
                if (show3D) {
                    val extrusionDepth = 20f * animationProgress.value
                    data.forEachIndexed { index, point ->
                        val sweepAngle = (point.value / total) * 360f
                        
                        val extrusionColor = colors[index].copy(alpha = 0.3f)
                        drawArc(
                            color = extrusionColor,
                            startAngle = currentAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius + extrusionDepth),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = extrusionDepth)
                        )
                        
                        currentAngle += sweepAngle
                    }
                }
                
                // Reset angle for main pie
                currentAngle = -90f
                
                // Draw pie slices
                data.forEachIndexed { index, point ->
                    val sweepAngle = (point.value / total) * 360f * animationProgress.value
                    
                    val sliceBrush = ChartStyles.getGradientBrush(
                        style = config.style,
                        start = Offset(center.x - radius, center.y - radius),
                        end = Offset(center.x + radius, center.y + radius),
                        colors = listOf(colors[index], colors[index].copy(alpha = 0.7f))
                    )
                    
                    drawArc(
                        brush = sliceBrush,
                        startAngle = currentAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    // Draw slice border
                    drawArc(
                        color = colors[index].copy(alpha = 0.5f),
                        startAngle = currentAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 2f)
                    )
                    
                    currentAngle += (point.value / total) * 360f * animationProgress.value
                }
                
                // Draw center circle (donut effect)
                if (config.style == ChartStyle.HOLOGRAPHIC) {
                    drawCircle(
                        color = MaterialTheme.colorScheme.surface,
                        radius = radius * 0.3f,
                        center = center
                    )
                }
            }
            
            // Legend
            if (config.showLegend) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(SugarDimens.Spacing.sm)
                ) {
                    data.forEachIndexed { index, point ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = colors[index],
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = point.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// AREA CHART - Gradient-filled areas with wave animation
// ═══════════════════════════════════════════════════════════════

@Composable
fun AreaChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    axisConfig: AxisConfig = AxisConfig()
) {
    val animationProgress = remember { Animatable(0f) }
    val waveOffset = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(1f, animationSpec = tween(config.animationDuration))
        } else {
            animationProgress.snapTo(1f)
        }
        
        // Animate wave
        while (true) {
            waveOffset.animateTo(
                360f,
                animationSpec = tween(3000)
            )
            waveOffset.snapTo(0f)
        }
    }
    
    LineChart(
        data = data,
        modifier = modifier,
        config = config.copy(gradientFill = true),
        axisConfig = axisConfig
    )
}

// ═══════════════════════════════════════════════════════════════
// RADAR CHART - Spider/radar charts for multi-metric comparison
// ═══════════════════════════════════════════════════════════════

@Composable
fun RadarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    maxValue: Float? = null
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(
                1f,
                animationSpec = tween(config.animationDuration)
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }
    
    val chartMax = maxValue ?: data.maxOfOrNull { it.value } ?: 1f
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(config.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(250.dp)
            ) {
                val size = this.size
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.width / 2).coerceAtMost(size.height / 2) * 0.8f
                val angleStep = 360f / data.size
                
                // Draw grid (concentric polygons)
                if (config.showGrid) {
                    for (level in 1..5) {
                        val levelRadius = radius * (level / 5f)
                        val path = Path()
                        
                        data.forEachIndexed { index, _ ->
                            val angle = (angleStep * index - 90) * PI / 180
                            val x = center.x + levelRadius * cos(angle).toFloat()
                            val y = center.y + levelRadius * sin(angle).toFloat()
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        path.close()
                        
                        drawPath(
                            path,
                            Color.Gray.copy(alpha = 0.2f),
                            style = Stroke(width = 1f)
                        )
                    }
                }
                
                // Draw axes
                data.forEachIndexed { index, _ ->
                    val angle = (angleStep * index - 90) * PI / 180
                    val x = center.x + radius * cos(angle).toFloat()
                    val y = center.y + radius * sin(angle).toFloat()
                    
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 1f
                    )
                }
                
                // Draw data polygon
                val dataPath = Path()
                val dataPoints = data.mapIndexed { index, point ->
                    val angle = (angleStep * index - 90) * PI / 180
                    val normalizedValue = (point.value / chartMax) * animationProgress.value
                    val x = center.x + radius * normalizedValue * cos(angle).toFloat()
                    val y = center.y + radius * normalizedValue * sin(angle).toFloat()
                    Offset(x, y)
                }
                
                dataPoints.forEachIndexed { index, point ->
                    if (index == 0) {
                        dataPath.moveTo(point.x, point.y)
                    } else {
                        dataPath.lineTo(point.x, point.y)
                    }
                }
                dataPath.close()
                
                // Fill polygon
                val brush = ChartStyles.getGradientBrush(
                    style = config.style,
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius)
                )
                drawPath(dataPath, brush, alpha = 0.5f)
                
                // Draw border
                drawPath(
                    dataPath,
                    ChartStyles.getDefaultGradientColors(config.style).first(),
                    style = Stroke(width = 3f)
                )
                
                // Draw points
                dataPoints.forEachIndexed { index, point ->
                    drawCircle(
                        color = ChartStyles.getDefaultGradientColors(config.style)[index % 3],
                        radius = 6f,
                        center = point
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// GAUGE CHART - Semi-circular gauges for progress/scores
// ═══════════════════════════════════════════════════════════════

@Composable
fun GaugeChart(
    value: Float,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    maxValue: Float = 100f,
    showValue: Boolean = true,
    label: String = ""
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(
                1f,
                animationSpec = tween(config.animationDuration)
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }
    
    val animatedValue = value * animationProgress.value
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(config.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(
                    modifier = Modifier.size(200.dp)
                ) {
                    val size = this.size
                    val center = Offset(size.width / 2, size.height / 2 + 20f)
                    val radius = (size.width / 2).coerceAtMost(size.height / 2) * 0.9f
                    
                    // Draw background arc
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 20f, cap = StrokeCap.Round)
                    )
                    
                    // Draw value arc
                    val sweepAngle = (animatedValue / maxValue) * 270f
                    val gradientBrush = ChartStyles.getGradientBrush(
                        style = config.style,
                        start = Offset(center.x - radius, center.y),
                        end = Offset(center.x + radius, center.y)
                    )
                    
                    drawArc(
                        brush = gradientBrush,
                        startAngle = 135f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 20f, cap = StrokeCap.Round)
                    )
                    
                    // Draw glow
                    if (config.style == ChartStyle.NEON) {
                        drawArc(
                            brush = gradientBrush,
                            startAngle = 135f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 30f, cap = StrokeCap.Round)
                        )
                    }
                }
                
                if (showValue) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${animatedValue.toInt()}/${maxValue.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ChartStyles.getDefaultGradientColors(config.style).first()
                    )
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// HEAT MAP - Color intensity grids for activity tracking
// ═══════════════════════════════════════════════════════════════

@Composable
fun HeatMap(
    data: List<List<Float>>,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    rowLabels: List<String> = emptyList(),
    columnLabels: List<String> = emptyList()
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(
                1f,
                animationSpec = tween(config.animationDuration)
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }
    
    val maxValue = data.flatten().maxOrNull() ?: 1f
    val rows = data.size
    val cols = data.firstOrNull()?.size ?: 0
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(config.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg)
        ) {
            // Column labels
            if (columnLabels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp)
                ) {
                    columnLabels.forEach { label ->
                        Text(
                            modifier = Modifier.weight(1f),
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
            
            // Heat map grid
            data.forEachIndexed { rowIndex, row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Row label
                    if (rowLabels.isNotEmpty() && rowIndex < rowLabels.size) {
                        Text(
                            modifier = Modifier
                                .width(40.dp)
                                .padding(end = SugarDimens.Spacing.xs),
                            text = rowLabels[rowIndex],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Cells
                    row.forEach { value ->
                        val intensity = (value / maxValue) * animationProgress.value
                        val cellColor = lerpColor(
                            ChartStyles.getDefaultGradientColors(config.style).first(),
                            ChartStyles.getDefaultGradientColors(config.style).last(),
                            intensity
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = cellColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// BUBBLE CHART - Animated bubbles with physics
// ═══════════════════════════════════════════════════════════════

@Composable
fun BubbleChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    xAxisMax: Float = 100f,
    yAxisMax: Float = 100f
) {
    val animationProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    
    LaunchedEffect(config.animate) {
        if (config.animate) {
            animationProgress.animateTo(
                1f,
                animationSpec = tween(config.animationDuration)
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(config.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Box(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 40f
                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2
                
                // Draw grid
                if (config.showGrid) {
                    drawGrid(
                        width = chartWidth,
                        height = chartHeight,
                        offsetX = padding,
                        offsetY = padding,
                        config = config,
                        axisConfig = AxisConfig(),
                        maxValue = yAxisMax,
                        textMeasurer = textMeasurer
                    )
                }
                
                // Draw bubbles
                data.forEachIndexed { index, point ->
                    val x = padding + (point.value / xAxisMax) * chartWidth
                    val y = padding + chartHeight - ((point.value / yAxisMax) * chartHeight)
                    val bubbleRadius = (20f + point.value / 10) * animationProgress.value
                    
                    val bubbleColor = point.color ?: ChartStyles.getDefaultGradientColors(config.style)[index % 3]
                    
                    // Bubble gradient
                    val gradientBrush = Brush.radialGradient(
                        colors = listOf(
                            bubbleColor.copy(alpha = 0.8f),
                            bubbleColor.copy(alpha = 0.3f)
                        ),
                        center = Offset(x, y),
                        radius = bubbleRadius
                    )
                    
                    drawCircle(
                        brush = gradientBrush,
                        radius = bubbleRadius,
                        center = Offset(x, y)
                    )
                    
                    // Bubble border
                    drawCircle(
                        color = bubbleColor,
                        radius = bubbleRadius,
                        center = Offset(x, y),
                        style = Stroke(width = 2f)
                    )
                    
                    // Highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = bubbleRadius * 0.3f,
                        center = Offset(x - bubbleRadius * 0.3f, y - bubbleRadius * 0.3f)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════

private fun DrawScope.drawGrid(
    width: Float,
    height: Float,
    offsetX: Float,
    offsetY: Float,
    config: ChartConfig,
    axisConfig: AxisConfig,
    maxValue: Float,
    textMeasurer: TextMeasurer
) {
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    
    // Horizontal grid lines
    for (i in 0..axisConfig.gridLines) {
        val y = offsetY + (height / axisConfig.gridLines) * i
        drawLine(
            color = gridColor,
            start = Offset(offsetX, y),
            end = Offset(offsetX + width, y),
            strokeWidth = 1f
        )
        
        // Y-axis labels
        if (config.showLabels && axisConfig.showYAxis) {
            val value = maxValue * (1 - i.toFloat() / axisConfig.gridLines)
            val textLayout = textMeasurer.measure(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.labelSmall.toSpanStyle()
            )
            drawText(
                textLayout = textLayout,
                topLeft = Offset(offsetX - textLayout.size.width - 8f, y - textLayout.size.height / 2f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
    
    // Vertical grid lines
    if (axisConfig.showXAxis) {
        for (i in 0..axisConfig.gridLines) {
            val x = offsetX + (width / axisConfig.gridLines) * i
            drawLine(
                color = gridColor,
                start = Offset(x, offsetY),
                end = Offset(x, offsetY + height),
                strokeWidth = 1f
            )
        }
    }
}

private fun lerpColor(color1: Color, color2: Color, fraction: Float): Color {
    return Color(
        red = color1.red + (color2.red - color1.red) * fraction,
        green = color1.green + (color2.green - color1.green) * fraction,
        blue = color1.blue + (color2.blue - color1.blue) * fraction,
        alpha = color1.alpha + (color2.alpha - color1.alpha) * fraction
    )
}
