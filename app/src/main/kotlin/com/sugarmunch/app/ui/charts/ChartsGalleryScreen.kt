package com.sugarmunch.app.ui.charts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * Gallery screen showcasing all chart types with live previews
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsGalleryScreen(
    onNavigateBack: () -> Unit
) {
    val chartStyles = ChartStyle.entries
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charts Gallery") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xl)
        ) {
            // Bar Chart
            item {
                Text(
                    text = "Bar Chart",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.hotPink
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                BarChart(
                    data = listOf(
                        ChartDataPoint("A", 45f, Color(0xFFFF69B4)),
                        ChartDataPoint("B", 72f, Color(0xFF00FFA3)),
                        ChartDataPoint("C", 38f, Color(0xFFFFD700)),
                        ChartDataPoint("D", 91f, Color(0xFF1A1A2E)),
                        ChartDataPoint("E", 63f, Color(0xFFFFA500))
                    ),
                    config = ChartConfig(
                        style = ChartStyle.NEON,
                        animate = true,
                        animationDuration = 1200
                    )
                )
            }
            
            // Line Chart
            item {
                Text(
                    text = "Line Chart",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.mint
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                LineChart(
                    data = listOf(
                        ChartDataPoint("Mon", 30f, Color(0xFF00BFFF)),
                        ChartDataPoint("Tue", 45f, Color(0xFF00BFFF)),
                        ChartDataPoint("Wed", 38f, Color(0xFF00BFFF)),
                        ChartDataPoint("Thu", 65f, Color(0xFF00BFFF)),
                        ChartDataPoint("Fri", 52f, Color(0xFF00BFFF)),
                        ChartDataPoint("Sat", 78f, Color(0xFF00BFFF)),
                        ChartDataPoint("Sun", 85f, Color(0xFF00BFFF))
                    ),
                    config = ChartConfig(
                        style = ChartStyle.GLASS,
                        animate = true,
                        gradientFill = true,
                        lineSmoothness = 0.4f
                    )
                )
            }
            
            // Pie Chart
            item {
                Text(
                    text = "Pie Chart",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.yellow
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                PieChart(
                    data = listOf(
                        ChartDataPoint("Category A", 35f, Color(0xFFFF69B4)),
                        ChartDataPoint("Category B", 25f, Color(0xFF00FFA3)),
                        ChartDataPoint("Category C", 20f, Color(0xFFFFD700)),
                        ChartDataPoint("Category D", 15f, Color(0xFF00BFFF)),
                        ChartDataPoint("Category E", 5f, Color(0xFF1A1A2E))
                    ),
                    config = ChartConfig(
                        style = ChartStyle.HOLOGRAPHIC,
                        showLegend = true,
                        animate = true
                    ),
                    show3D = true
                )
            }
            
            // Radar Chart
            item {
                Text(
                    text = "Radar Chart",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.candyOrange
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                RadarChart(
                    data = listOf(
                        ChartDataPoint("Speed", 85f, Color(0xFFFF69B4)),
                        ChartDataPoint("Strength", 72f, Color(0xFF00FFA3)),
                        ChartDataPoint("Agility", 91f, Color(0xFFFFD700)),
                        ChartDataPoint("Intelligence", 68f, Color(0xFF00BFFF)),
                        ChartDataPoint("Charisma", 79f, Color(0xFF1A1A2E))
                    ),
                    config = ChartConfig(
                        style = ChartStyle.NEON,
                        showGrid = true,
                        animate = true
                    )
                )
            }
            
            // Gauge Chart
            item {
                Text(
                    text = "Gauge Chart",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.deepPurple
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
                ) {
                    GaugeChart(
                        value = 75f,
                        modifier = Modifier.weight(1f),
                        config = ChartConfig(style = ChartStyle.SUGARRUSH, animate = true),
                        label = "Progress"
                    )
                    GaugeChart(
                        value = 92f,
                        modifier = Modifier.weight(1f),
                        config = ChartConfig(style = ChartStyle.NEON, animate = true),
                        label = "Score"
                    )
                }
            }
            
            // Heat Map
            item {
                Text(
                    text = "Heat Map",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.bubblegumBlue
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                HeatMap(
                    data = listOf(
                        listOf(12f, 45f, 78f, 34f, 56f),
                        listOf(23f, 67f, 89f, 45f, 67f),
                        listOf(34f, 78f, 91f, 56f, 78f),
                        listOf(45f, 89f, 95f, 67f, 89f),
                        listOf(56f, 91f, 98f, 78f, 91f)
                    ),
                    config = ChartConfig(
                        style = ChartStyle.LIQUID,
                        animate = true
                    ),
                    rowLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
                    columnLabels = listOf("9AM", "11AM", "1PM", "3PM", "5PM")
                )
            }
            
            // Bubble Chart
            item {
                Text(
                    text = "Bubble Chart",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.hotPink
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                BubbleChart(
                    data = listOf(
                        ChartDataPoint("A", 30f, Color(0xFFFF69B4)),
                        ChartDataPoint("B", 50f, Color(0xFF00FFA3)),
                        ChartDataPoint("C", 70f, Color(0xFFFFD700)),
                        ChartDataPoint("D", 40f, Color(0xFF00BFFF)),
                        ChartDataPoint("E", 60f, Color(0xFF1A1A2E)),
                        ChartDataPoint("F", 80f, Color(0xFFFFA500))
                    ),
                    config = ChartConfig(
                        style = ChartStyle.CRYSTAL,
                        animate = true,
                        showDataPoints = true
                    ),
                    xAxisMax = 100f,
                    yAxisMax = 100f
                )
            }
            
            // Area Chart
            item {
                Text(
                    text = "Area Chart",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.mint
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                AreaChart(
                    data = listOf(
                        ChartDataPoint("Jan", 40f, Color(0xFF00BFFF)),
                        ChartDataPoint("Feb", 55f, Color(0xFF00BFFF)),
                        ChartDataPoint("Mar", 48f, Color(0xFF00BFFF)),
                        ChartDataPoint("Apr", 72f, Color(0xFF00BFFF)),
                        ChartDataPoint("May", 65f, Color(0xFF00BFFF)),
                        ChartDataPoint("Jun", 88f, Color(0xFF00BFFF))
                    ),
                    config = ChartConfig(
                        style = ChartStyle.SUGARRUSH,
                        animate = true,
                        gradientFill = true
                    )
                )
            }
            
            // Style comparison
            item {
                Text(
                    text = "Style Comparison",
                    style = MaterialTheme.typography.titleLarge,
                    color = SugarDimens.Brand.yellow
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
                ) {
                    items(chartStyles) { style ->
                        GaugeChart(
                            value = 75f,
                            modifier = Modifier.width(150.dp),
                            config = ChartConfig(
                                style = style,
                                animate = false,
                                showValue = false
                            ),
                            showValue = false
                        )
                    }
                }
            }
        }
    }
}
