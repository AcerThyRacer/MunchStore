package com.sugarmunch.app.ui.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress

enum class HomeSection(val displayName: String, val icon: String) {
    FEATURED("Featured", "⭐"),
    CATEGORIES("Categories", "📂"),
    RECENT("Recent", "🕐"),
    RECOMMENDED("For You", "💡"),
    TRENDING("Trending", "🔥"),
    NEW_RELEASES("New Releases", "🆕"),
    FAVORITES("Favorites", "❤️")
}

data class HomeSectionConfig(
    val section: HomeSection,
    val enabled: Boolean = true,
    val order: Int = 0
)

data class HomeLayoutConfig(
    val sections: List<HomeSectionConfig> = HomeSection.entries.mapIndexed { i, s ->
        HomeSectionConfig(s, true, i)
    }
)

@Composable
fun HomeLayoutEditor(
    config: HomeLayoutConfig,
    onConfigChanged: (HomeLayoutConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val sortedSections = remember(config) {
        config.sections.sortedWith(compareBy({ !it.enabled }, { it.order })).toMutableStateList()
    }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val dragAnimatable = remember { Animatable(0f) }
    val listState = rememberLazyListState()

    val itemHeight = 72.dp

    Column(modifier = modifier.fillMaxWidth().padding(SugarDimens.Spacing.md)) {
        Text("Home Layout", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(SugarDimens.Spacing.xs))
        Text(
            "Long-press and drag to reorder sections",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(SugarDimens.Spacing.md))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
        ) {
            itemsIndexed(sortedSections, key = { _, item -> item.section.name }) { index, sectionConfig ->
                val isDragging = dragIndex == index
                val elevation = if (isDragging) SugarDimens.Elevation.high else SugarDimens.Elevation.low
                val scaleVal = if (isDragging) 1.02f else 1f
                val yOffset = if (isDragging) dragOffset else 0f

                SectionRow(
                    sectionConfig = sectionConfig,
                    isDragging = isDragging,
                    elevation = elevation,
                    scale = scaleVal,
                    yOffset = yOffset,
                    onToggle = { enabled ->
                        val updated = sortedSections.toMutableList()
                        updated[index] = sectionConfig.copy(enabled = enabled)
                        val reordered = updated
                            .sortedWith(compareBy({ !it.enabled }, { it.order }))
                            .mapIndexed { i, s -> s.copy(order = i) }
                        sortedSections.clear()
                        sortedSections.addAll(reordered)
                        onConfigChanged(HomeLayoutConfig(reordered))
                    },
                    onDragStart = {
                        dragIndex = index
                        dragOffset = 0f
                    },
                    onDrag = { change ->
                        dragOffset += change
                        val itemHeightPx = itemHeight.value * 2.5f
                        val targetIndex = (index + (dragOffset / itemHeightPx).roundToInt())
                            .coerceIn(0, sortedSections.lastIndex)

                        if (targetIndex != index && sortedSections[targetIndex].enabled) {
                            val item = sortedSections.removeAt(index)
                            sortedSections.add(targetIndex, item)
                            dragIndex = targetIndex
                            dragOffset = 0f
                        }
                    },
                    onDragEnd = {
                        dragIndex = -1
                        scope.launch {
                            dragAnimatable.snapTo(dragOffset)
                            dragAnimatable.animateTo(0f, spring())
                        }
                        dragOffset = 0f
                        val reordered = sortedSections.mapIndexed { i, s -> s.copy(order = i) }
                        sortedSections.clear()
                        sortedSections.addAll(reordered)
                        onConfigChanged(HomeLayoutConfig(reordered))
                    }
                )
            }
        }

        Spacer(Modifier.height(SugarDimens.Spacing.md))
        Button(
            onClick = {
                val defaults = HomeLayoutConfig()
                sortedSections.clear()
                sortedSections.addAll(defaults.sections)
                onConfigChanged(defaults)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(SugarDimens.IconSize.sm))
            Spacer(Modifier.width(SugarDimens.Spacing.xs))
            Text("Reset to Default")
        }
    }
}

@Composable
private fun SectionRow(
    sectionConfig: HomeSectionConfig,
    isDragging: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    scale: Float,
    yOffset: Float,
    onToggle: (Boolean) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val alpha = if (sectionConfig.enabled) 1f else 0.45f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(0, yOffset.roundToInt()) }
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(SugarDimens.Radius.sm))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(SugarDimens.Radius.sm))
            .graphicsLayer { this.alpha = alpha }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, offset ->
                        change.consume()
                        onDrag(offset.y)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .padding(horizontal = SugarDimens.Spacing.md, vertical = SugarDimens.Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SugarDimens.IconSize.md)
            )
            Text(sectionConfig.section.icon, fontSize = MaterialTheme.typography.titleLarge.fontSize)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sectionConfig.section.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = sectionConfig.enabled,
                onCheckedChange = onToggle
            )
        }
    }
}
