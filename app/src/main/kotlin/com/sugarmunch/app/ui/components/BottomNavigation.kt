package com.sugarmunch.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.engine.ThemeManager

enum class BottomNavItem(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    CATALOG("catalog", "Catalog", Icons.Outlined.Home, Icons.Filled.Home),
    EFFECTS("effects", "Effects", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome),
    THEME("theme", "Theme", Icons.Outlined.Palette, Icons.Filled.Palette),
    SETTINGS("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

@Composable
fun SugarMunchBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Glassmorphism background
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(64.dp)
                    .shadow(8.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = colors.surface.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem.values().forEach { item ->
                        val isSelected = currentRoute.startsWith(item.route)
                        NavItem(
                            item = item,
                            isSelected = isSelected,
                            colors = colors,
                            onClick = { onNavigate(item.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon with animation
            Box(
                modifier = Modifier
                    .scale(iconScale)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            Brush.radialGradient(
                                listOf(
                                    colors.primary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        } else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.icon,
                    contentDescription = item.title,
                    tint = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Label
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary
                )
            }
            
            // Indicator dot
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                )
            }
        }
    }
}
