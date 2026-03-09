package com.sugarmunch.app.ui.layout

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

enum class NavigationStyle(val displayName: String, val description: String) {
    CLASSIC_TABS("Classic Tabs", "Standard bottom navigation bar"),
    FLOATING_PILL("Floating Pill", "Floating rounded pill above content"),
    DOCK_STYLE("Dock", "macOS-style dock with scale on hover"),
    SIDE_RAIL("Side Rail", "Vertical rail for tablets and landscape")
}

data class NavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun NavigationStylePicker(
    currentStyle: NavigationStyle,
    onStyleSelected: (NavigationStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(SugarDimens.Spacing.md)) {
        Text("Navigation Style", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(SugarDimens.Spacing.md))

        val styles = NavigationStyle.entries
        Column(verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)) {
            for (row in styles.chunked(2)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { style ->
                        val isSelected = style == currentStyle
                        val scale by animateFloatAsState(
                            if (isSelected) 1f else 0.96f,
                            animationSpec = spring(dampingRatio = 0.7f),
                            label = "nav_scale"
                        )
                        StylePreviewCard(
                            style = style,
                            isSelected = isSelected,
                            onClick = { onStyleSelected(style) },
                            modifier = Modifier.weight(1f).scale(scale)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StylePreviewCard(
    style: NavigationStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .border(borderWidth, borderColor, RoundedCornerShape(SugarDimens.Radius.md))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) SugarDimens.Elevation.medium else SugarDimens.Elevation.none
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phone silhouette
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                PhoneMockup(style)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(SugarDimens.Spacing.xs))
            Text(style.displayName, style = MaterialTheme.typography.labelMedium)
            Text(
                style.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhoneMockup(style: NavigationStyle) {
    Box(Modifier.fillMaxSize()) {
        // Content placeholder lines
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(bottom = if (style != NavigationStyle.SIDE_RAIL) 20.dp else 0.dp)
                .padding(start = if (style == NavigationStyle.SIDE_RAIL) 16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) {
                Box(
                    Modifier
                        .fillMaxWidth(if (it == 2) 0.6f else 0.85f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                )
            }
        }

        // Navigation mockup
        when (style) {
            NavigationStyle.CLASSIC_TABS -> {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                        repeat(4) { i ->
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .background(
                                        if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
            NavigationStyle.FLOATING_PILL -> {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .fillMaxWidth(0.75f)
                        .height(14.dp)
                        .shadow(2.dp, RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(50))
                ) {
                    Row(Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                        repeat(4) { i ->
                            Box(
                                Modifier
                                    .size(5.dp)
                                    .background(
                                        if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
            NavigationStyle.DOCK_STYLE -> {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                        .fillMaxWidth(0.8f)
                        .height(18.dp)
                        .background(
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))),
                            RoundedCornerShape(6.dp)
                        )
                ) {
                    Row(Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                        repeat(4) { i ->
                            val size = if (i == 0) 10.dp else 7.dp
                            Box(
                                Modifier
                                    .size(size)
                                    .background(
                                        if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
            NavigationStyle.SIDE_RAIL -> {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .width(14.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.fillMaxHeight().padding(vertical = 8.dp), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                        repeat(4) { i ->
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .background(
                                        if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SugarBottomNav(
    style: NavigationStyle,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    items: List<NavItem>,
    modifier: Modifier = Modifier
) {
    when (style) {
        NavigationStyle.CLASSIC_TABS -> ClassicTabsNav(selectedIndex, onItemSelected, items, modifier)
        NavigationStyle.FLOATING_PILL -> FloatingPillNav(selectedIndex, onItemSelected, items, modifier)
        NavigationStyle.DOCK_STYLE -> DockStyleNav(selectedIndex, onItemSelected, items, modifier)
        NavigationStyle.SIDE_RAIL -> SideRailNav(selectedIndex, onItemSelected, items, modifier)
    }
}

@Composable
private fun ClassicTabsNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    items: List<NavItem>,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

@Composable
private fun FloatingPillNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    items: List<NavItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = SugarDimens.Spacing.lg),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .offset(y = (-8).dp)
                .shadow(SugarDimens.Elevation.medium, RoundedCornerShape(50)),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = SugarDimens.Elevation.medium
        ) {
            Row(
                modifier = Modifier.padding(vertical = SugarDimens.Spacing.sm, horizontal = SugarDimens.Spacing.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val iconScale by animateFloatAsState(
                        if (isSelected) 1.15f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f),
                        label = "pill_icon"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onItemSelected(index) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.scale(iconScale)
                        )
                        if (isSelected) {
                            Spacer(Modifier.height(2.dp))
                            Box(
                                Modifier
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockStyleNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    items: List<NavItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(bottom = SugarDimens.Spacing.xs),
            shape = RoundedCornerShape(SugarDimens.Radius.lg),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            tonalElevation = SugarDimens.Elevation.low
        ) {
            Box(
                Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SugarDimens.Spacing.sm, horizontal = SugarDimens.Spacing.md),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = index == selectedIndex
                        val iconScale by animateFloatAsState(
                            if (isSelected) 1.3f else 1f,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
                            label = "dock_scale"
                        )
                        Column(
                            modifier = Modifier
                                .clickable { onItemSelected(index) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .scale(iconScale)
                                    .graphicsLayer {
                                        // Subtle reflection via vertical flip below
                                        translationY = if (isSelected) -4.dp.toPx() else 0f
                                    }
                            )
                            if (isSelected) {
                                // Reflection
                                Icon(
                                    item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .size(SugarDimens.IconSize.sm)
                                        .graphicsLayer { scaleY = -0.5f }
                                )
                            } else {
                                Spacer(Modifier.height(SugarDimens.IconSize.sm))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SideRailNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    items: List<NavItem>,
    modifier: Modifier = Modifier
) {
    NavigationRail(modifier = modifier) {
        Spacer(Modifier.weight(1f))
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) }
            )
        }
        Spacer(Modifier.weight(1f))
    }
}
