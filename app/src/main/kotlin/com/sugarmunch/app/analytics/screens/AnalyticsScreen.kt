package com.sugarmunch.app.analytics.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.analytics.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val analyticsManager = remember { AnalyticsManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val userStats by analyticsManager.userStats.collectAsState(initial = UserStats(0, 0, 0, 0, 0, 0, 0))
    val categoryBreakdown by analyticsManager.categoryBreakdown.collectAsState(initial = emptyMap())
    val hourlyActivity by analyticsManager.hourlyActivity.collectAsState(initial = emptyMap())
    val dailyActivity by analyticsManager.dailyActivity.collectAsState(initial = emptyMap())
    val mostActiveHour by analyticsManager.mostActiveHour.collectAsState(initial = 12)
    val mostActiveDay by analyticsManager.mostActiveDay.collectAsState(initial = "Unknown")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Your Stats",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Summary Cards
                StatsSummaryRow(userStats = userStats, colors = colors)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Time Stats
                TimeStatsCard(
                    totalHours = userStats.totalTimeHours,
                    avgSession = userStats.averageSessionMinutes,
                    mostActiveDay = mostActiveDay,
                    mostActiveHour = mostActiveHour,
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category Breakdown
                if (categoryBreakdown.isNotEmpty()) {
                    CategoryBreakdownCard(
                        breakdown = categoryBreakdown,
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Hourly Activity Chart
                if (hourlyActivity.isNotEmpty()) {
                    HourlyActivityChart(
                        activity = hourlyActivity,
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Daily Activity Chart
                if (dailyActivity.isNotEmpty()) {
                    DailyActivityChart(
                        activity = dailyActivity,
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Feature Usage
                FeatureUsageCard(
                    themesTried = userStats.themesTried,
                    effectsUsed = userStats.effectsUsed,
                    shopPurchases = userStats.shopPurchases,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun StatsSummaryRow(
    userStats: UserStats,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = "${userStats.totalInstalls}",
            label = "Apps Installed",
            icon = "📱",
            modifier = Modifier.weight(1f),
            colors = colors
        )
        StatCard(
            value = "${userStats.totalSessions}",
            label = "Sessions",
            icon = "🎯",
            modifier = Modifier.weight(1f),
            colors = colors
        )
        StatCard(
            value = "${userStats.achievementsUnlocked}",
            label = "Achievements",
            icon = "🏆",
            modifier = Modifier.weight(1f),
            colors = colors
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: String,
    modifier: Modifier = Modifier,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimeStatsCard(
    totalHours: Long,
    avgSession: Long,
    mostActiveDay: String,
    mostActiveHour: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Time Stats",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeStatItem(
                    value = "$totalHours",
                    unit = "hours",
                    label = "Total Time",
                    colors = colors
                )
                Divider(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp),
                    color = colors.onSurface.copy(alpha = 0.2f)
                )
                TimeStatItem(
                    value = "$avgSession",
                    unit = "min",
                    label = "Avg Session",
                    colors = colors
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Most active times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActiveTimePill(
                    icon = "📅",
                    label = "Most Active Day",
                    value = mostActiveDay,
                    colors = colors
                )
                ActiveTimePill(
                    icon = "🕐",
                    label = "Peak Hour",
                    value = "$mostActiveHour:00",
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun TimeStatItem(
    value: String,
    unit: String,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                unit,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ActiveTimePill(
    icon: String,
    label: String,
    value: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    breakdown: Map<String, Int>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val total = breakdown.values.sum()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Apps by Category",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            breakdown.entries.sortedByDescending { it.value }.forEach { (category, count) ->
                val percentage = (count.toFloat() / total * 100).toInt()
                CategoryBar(
                    category = category,
                    count = count,
                    percentage = percentage,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryBar(
    category: String,
    count: Int,
    percentage: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            category,
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colors.primary,
                                colors.secondary
                            )
                        )
                    )
            )
            
            Text(
                "$count",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$percentage%",
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun HourlyActivityChart(
    activity: Map<Int, Int>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val maxValue = activity.values.maxOrNull() ?: 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Activity by Hour",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(0, 6, 12, 18, 22).forEach { hour ->
                    val value = activity[hour] ?: 0
                    val height = (value.toFloat() / maxValue).coerceIn(0.1f, 1f)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(80.dp * height)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hour == 12 || hour == 18) 
                                        colors.primary 
                                    else 
                                        colors.primary.copy(alpha = 0.5f)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$hour:00",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyActivityChart(
    activity: Map<String, Long>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxValue = activity.values.maxOrNull()?.toFloat() ?: 1f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Activity by Day",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    val value = activity[day] ?: 0
                    val height = (value / maxValue).coerceIn(0.1f, 1f)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(60.dp * height)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (value == maxValue.toLong())
                                        colors.primary
                                    else
                                        colors.primary.copy(alpha = 0.5f)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            day,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureUsageCard(
    themesTried: Int,
    effectsUsed: Int,
    shopPurchases: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Feature Usage",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FeatureUsageRow(
                icon = "🎨",
                label = "Themes Tried",
                value = themesTried,
                colors = colors
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureUsageRow(
                icon = "✨",
                label = "Effects Used",
                value = effectsUsed,
                colors = colors
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureUsageRow(
                icon = "🛒",
                label = "Shop Purchases",
                value = shopPurchases,
                colors = colors
            )
        }
    }
}

@Composable
private fun FeatureUsageRow(
    icon: String,
    label: String,
    value: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.primary.copy(alpha = 0.2f)
        ) {
            Text(
                "$value",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleSmall,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
