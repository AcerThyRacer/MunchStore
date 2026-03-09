package com.sugarmunch.wear.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.dialog.Alert
import com.google.android.horologist.compose.layout.fadeAway
import com.sugarmunch.wear.data.WearDataLayer
import kotlinx.coroutines.launch

/**
 * Theme Switcher Screen - Browse and apply themes from watch
 */
@Composable
fun ThemeSwitcherScreen(
    navController: NavHostController,
    wearDataLayer: WearDataLayer
) {
    val currentTheme by wearDataLayer.currentTheme.collectAsState()
    val listState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    var selectedTheme by remember { mutableStateOf<String?>(null) }

    val themes = remember {
        listOf(
            // Classic
            ThemeInfo("classic_candy", "Classic Candy", "🍬", "Classic", 
                listOf(Color(0xFFFFB6C1), Color(0xFF98FF98), Color(0xFFFFFACD))),
            ThemeInfo("cotton_candy", "Cotton Candy", "🍥", "Classic",
                listOf(Color(0xFFFF9ECD), Color(0xFFB5DEFF), Color(0xFFE8C5FF))),
            ThemeInfo("sour_patch", "Sour Patch", "🍋", "Classic",
                listOf(Color(0xFF7FFF00), Color(0xFFFFEA00), Color(0xFF00FF7F))),
            
            // SugarRush
            ThemeInfo("sugarrush_classic", "SugarRush", "⚡", "SugarRush",
                listOf(Color(0xFFFF1493), Color(0xFF00CED1), Color(0xFFFFD700))),
            ThemeInfo("sugarrush_nuclear", "Nuclear", "☢️", "SugarRush",
                listOf(Color(0xFF39FF14), Color(0xFF00FFFF), Color(0xFFFF00FF))),
            ThemeInfo("sugarrush_volcano", "Volcano", "🌋", "SugarRush",
                listOf(Color(0xFFFF2400), Color(0xFFFF8C00), Color(0xFFFFD700))),
            
            // Trippy
            ThemeInfo("trippy_rainbow", "Rainbow", "🌈", "Trippy",
                listOf(Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00), 
                       Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF9400D3))),
            ThemeInfo("trippy_liquid", "Liquid", "💧", "Trippy",
                listOf(Color(0xFF00CED1), Color(0xFFFF69B4), Color(0xFF7B68EE))),
            ThemeInfo("trippy_galaxy", "Galaxy", "🌌", "Trippy",
                listOf(Color(0xFFE0B0FF), Color(0xFF87CEEB), Color(0xFF4B0082))),
            ThemeInfo("trippy_acid", "Acid", "🧪", "Trippy",
                listOf(Color(0xFFCCFF00), Color(0xFF00FFCC), Color(0xFFFF00CC))),
            
            // Chill
            ThemeInfo("chill_mint", "Cool Mint", "🌿", "Chill",
                listOf(Color(0xFF98D8C8), Color(0xFFB8E0D2), Color(0xFFD4F1F4))),
            ThemeInfo("chill_chocolate", "Chocolate", "🍫", "Chill",
                listOf(Color(0xFFD2691E), Color(0xFF8B4513), Color(0xFFCD853F))),
            
            // Dark
            ThemeInfo("dark_berry", "Berry", "🫐", "Dark",
                listOf(Color(0xFF9932CC), Color(0xFF8A2BE2), Color(0xFFDA70D6))),
            ThemeInfo("dark_cocoa", "Cocoa", "☕", "Dark",
                listOf(Color(0xFFA0522D), Color(0xFF8B7355), Color(0xFFBC8F8F))),
            
            // Seasonal
            ThemeInfo("halloween_candy", "Halloween", "🎃", "Seasonal",
                listOf(Color(0xFFFF6600), Color(0xFF660099), Color(0xFF00FF00))),
            ThemeInfo("christmas_peppermint", "Christmas", "🎄", "Seasonal",
                listOf(Color(0xFFDC143C), Color(0xFF228B22), Color(0xFFFFFFFF)))
        )
    }

    // Group themes by category
    val themesByCategory = themes.groupBy { it.category }

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        timeText = { 
            TimeText(
                modifier = Modifier.fadeAway { listState }
            ) 
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            anchorType = ScalingLazyListAnchorType.ItemStart
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎨",
                        style = MaterialTheme.typography.display3
                    )
                    Text(
                        text = "Themes",
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.primary
                    )
                    currentTheme?.let { themeId ->
                        val themeName = themes.find { it.id == themeId }?.name ?: themeId
                        Text(
                            text = "Current: $themeName",
                            style = MaterialTheme.typography.caption3,
                            color = MaterialTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Current theme preview (if any)
            currentTheme?.let { themeId ->
                val theme = themes.find { it.id == themeId }
                theme?.let {
                    item {
                        CurrentThemeCard(theme)
                    }
                }
            }

            // Category sections
            themesByCategory.forEach { (category, categoryThemes) ->
                item {
                    CategoryHeader(category)
                }

                items(categoryThemes, key = { it.id }) { theme ->
                    val isActive = currentTheme == theme.id
                    ThemeCard(
                        theme = theme,
                        isActive = isActive,
                        onClick = {
                            if (!isActive) {
                                selectedTheme = theme.id
                            }
                        }
                    )
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Apply theme confirmation dialog
    selectedTheme?.let { themeId ->
        val theme = themes.find { it.id == themeId }
        theme?.let {
            Alert(
                title = { Text("Apply Theme?") },
                message = { 
                    Column {
                        Text("Apply ${theme.name}?")
                        Spacer(modifier = Modifier.height(8.dp))
                        ThemePreview(theme)
                    }
                },
                onDismiss = { selectedTheme = null }
            ) {
                item {
                    Button(
                        onClick = {
                            scope.launch {
                                wearDataLayer.sendSetTheme(themeId)
                            }
                            selectedTheme = null
                        },
                        colors = ButtonDefaults.primaryButtonColors()
                    ) {
                        Text("Apply")
                    }
                }
                item {
                    Button(
                        onClick = { selectedTheme = null },
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

/**
 * Theme info data class
 */
data class ThemeInfo(
    val id: String,
    val name: String,
    val icon: String,
    val category: String,
    val colors: List<Color>
)

/**
 * Category header with icon
 */
@Composable
fun CategoryHeader(category: String) {
    val (icon, color) = when (category) {
        "Classic" -> "🍬" to Color(0xFFFFB6C1)
        "SugarRush" -> "⚡" to Color(0xFFFF1493)
        "Trippy" -> "🌈" to Color(0xFF00FF80)
        "Chill" -> "❄️" to Color(0xFF98D8C8)
        "Dark" -> "🌙" to Color(0xFF9932CC)
        "Seasonal" -> "🎉" to Color(0xFFFF6600)
        else -> "🎨" to MaterialTheme.colors.primary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.button,
            color = color
        )
    }
}

/**
 * Current active theme card
 */
@Composable
fun CurrentThemeCard(theme: ThemeInfo) {
    Card(
        onClick = { },
        backgroundPainter = androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(
            startBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f),
            endBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.05f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = theme.icon,
                    style = MaterialTheme.typography.title2
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Theme",
                        style = MaterialTheme.typography.caption3,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.button
                    )
                }
                Text(
                    text = "✓",
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.title2
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ThemeColorStrip(theme.colors)
        }
    }
}

/**
 * Theme selection card
 */
@Composable
fun ThemeCard(
    theme: ThemeInfo,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        backgroundPainter = if (isActive) {
            androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(
                startBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                endBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.05f)
            )
        } else {
            androidx.wear.compose.material.CardDefaults.cardBackgroundPainter()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = theme.icon,
                style = MaterialTheme.typography.title3
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.button
                )
            }
            ThemeColorStrip(
                colors = theme.colors.take(3),
                size = 16.dp
            )
        }
    }
}

/**
 * Color strip preview for theme
 */
@Composable
fun ThemeColorStrip(
    colors: List<Color>,
    size: androidx.compose.ui.unit.Dp = 20.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * Theme preview in dialog
 */
@Composable
fun ThemePreview(theme: ThemeInfo) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(theme.colors.take(3))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = theme.icon,
                style = MaterialTheme.typography.display3
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ThemeColorStrip(theme.colors)
    }
}

/**
 * Quick theme selector (for use in other screens)
 */
@Composable
fun QuickThemeChip(
    theme: ThemeInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Chip(
        onClick = onClick,
        label = {
            Text(
                text = theme.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = theme.icon)
            }
        },
        colors = if (isSelected) {
            ChipDefaults.primaryChipColors()
        } else {
            ChipDefaults.secondaryChipColors()
        },
        modifier = Modifier.fillMaxWidth()
    )
}
