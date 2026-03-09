package com.sugarmunch.app.ui.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.CardStyle
import com.sugarmunch.app.ui.design.SugarCard
import com.sugarmunch.app.ui.design.SugarDimens

enum class GridColumns(val count: Int, val label: String) {
    TWO(2, "Cozy"),
    THREE(3, "Default"),
    FOUR(4, "Compact"),
    FIVE(5, "Dense")
}

enum class CardSize(val label: String) {
    COMPACT("Compact"),
    NORMAL("Normal"),
    LARGE("Large")
}

enum class InfoDensity(val label: String) {
    MINIMAL("Minimal"),
    STANDARD("Standard"),
    DETAILED("Detailed")
}

data class LayoutConfig(
    val gridColumns: GridColumns = GridColumns.THREE,
    val cardSize: CardSize = CardSize.NORMAL,
    val cardStyle: CardStyle = CardStyle.ELEVATED,
    val infoDensity: InfoDensity = InfoDensity.STANDARD,
    val showCategoryHeaders: Boolean = true,
    val showAppIcons: Boolean = true,
    val compactMode: Boolean = false
)

val LocalLayoutConfig = staticCompositionLocalOf { LayoutConfig() }

@Composable
fun ProvideLayoutConfig(config: LayoutConfig, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutConfig provides config) {
        content()
    }
}

@Composable
fun LayoutCustomizerPanel(
    config: LayoutConfig,
    onConfigChanged: (LayoutConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(SugarDimens.Spacing.md)
    ) {
        Text("Layout Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(SugarDimens.Spacing.md))

        // Live preview
        SugarCard(style = config.cardStyle) {
            Text("Preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(SugarDimens.Spacing.xs))
            LivePreviewGrid(config)
        }

        Spacer(Modifier.height(SugarDimens.Spacing.lg))

        SectionLabel("Grid Columns")
        SegmentedRow(
            options = GridColumns.entries.map { it.label },
            selectedIndex = GridColumns.entries.indexOf(config.gridColumns),
            onSelected = { onConfigChanged(config.copy(gridColumns = GridColumns.entries[it])) }
        )

        Spacer(Modifier.height(SugarDimens.Spacing.md))
        SectionLabel("Card Size")
        SegmentedRow(
            options = CardSize.entries.map { it.label },
            selectedIndex = CardSize.entries.indexOf(config.cardSize),
            onSelected = { onConfigChanged(config.copy(cardSize = CardSize.entries[it])) }
        )

        Spacer(Modifier.height(SugarDimens.Spacing.md))
        SectionLabel("Card Style")
        CardStyleRow(
            selected = config.cardStyle,
            onSelected = { onConfigChanged(config.copy(cardStyle = it)) }
        )

        Spacer(Modifier.height(SugarDimens.Spacing.md))
        SectionLabel("Info Density")
        SegmentedRow(
            options = InfoDensity.entries.map { it.label },
            selectedIndex = InfoDensity.entries.indexOf(config.infoDensity),
            onSelected = { onConfigChanged(config.copy(infoDensity = InfoDensity.entries[it])) }
        )

        Spacer(Modifier.height(SugarDimens.Spacing.md))
        ToggleRow("Category Headers", config.showCategoryHeaders) {
            onConfigChanged(config.copy(showCategoryHeaders = it))
        }
        ToggleRow("App Icons", config.showAppIcons) {
            onConfigChanged(config.copy(showAppIcons = it))
        }
        ToggleRow("Compact Mode", config.compactMode) {
            onConfigChanged(config.copy(compactMode = it))
        }
    }
}

@Composable
private fun LivePreviewGrid(config: LayoutConfig) {
    val cols = 2
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(cols) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (config.cardSize == CardSize.COMPACT) 32.dp else if (config.cardSize == CardSize.LARGE) 52.dp else 42.dp)
                            .clip(RoundedCornerShape(SugarDimens.Radius.xs))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (config.showAppIcons) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            if (config.infoDensity != InfoDensity.MINIMAL) {
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    Modifier
                                        .width(24.dp)
                                        .height(3.dp)
                                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = SugarDimens.Spacing.xs)
    )
}

@Composable
private fun SegmentedRow(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SugarDimens.Radius.sm))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val bgColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = spring(), label = "seg_bg"
            )
            val textColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(), label = "seg_txt"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(index) }
                    .background(bgColor)
                    .padding(vertical = SugarDimens.Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = textColor, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun CardStyleRow(selected: CardStyle, onSelected: (CardStyle) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        CardStyle.entries.forEach { style ->
            val isSelected = style == selected
            val borderColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "card_border"
            )
            SugarCard(
                modifier = Modifier
                    .width(80.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(SugarDimens.Radius.md)),
                style = style,
                onClick = { onSelected(style) }
            ) {
                Text(
                    text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SugarDimens.Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
