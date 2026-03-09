package com.sugarmunch.app.widgets.infinite

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Widget Stack Manager - Layer and stack widgets with blend modes
 * 
 * Features:
 * - Widget stacking with z-order
 * - Blend mode compositing
 * - Layer groups
 * - Clipping and masking
 * - Stack operations
 */
class WidgetStackManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Widget stacks
    private val _stacks = MutableStateFlow<List<WidgetStack>>(emptyList())
    val stacks: StateFlow<List<WidgetStack>> = _stacks.asStateFlow()

    // Active layers
    private val _layers = MutableStateFlow<List<WidgetLayer>>(emptyList())
    val layers: StateFlow<List<WidgetLayer>> = _layers.asStateFlow()

    // Stack configuration
    var stackConfig = StackConfig(
        maxStackDepth = 10,
        enableBlendModes = true,
        enableClipping = true,
        enableMasks = true,
        compositeOperation = CompositeOperation.NORMAL
    )

    // ========== STACK MANAGEMENT ==========

    fun createStack(position: Offset, size: Size): WidgetStack {
        val stack = WidgetStack(
            id = "stack_${System.currentTimeMillis()}",
            position = position,
            size = size,
            widgets = emptyList(),
            blendMode = BlendMode.NORMAL,
            opacity = 1f
        )

        _stacks.value = _stacks.value + stack
        return stack
    }

    fun addWidgetToStack(stackId: String, widget: CanvasWidget) {
        val stacks = _stacks.value.toMutableList()
        val index = stacks.indexOfFirst { it.id == stackId }

        if (index != -1) {
            val stack = stacks[index]
            if (stack.widgets.size < stackConfig.maxStackDepth) {
                stacks[index] = stack.copy(
                    widgets = stack.widgets + widget.copy(
                        zIndex = stack.widgets.size
                    )
                )
                _stacks.value = stacks
            }
        }
    }

    fun removeWidgetFromStack(stackId: String, widgetId: String) {
        val stacks = _stacks.value.toMutableList()
        val index = stacks.indexOfFirst { it.id == stackId }

        if (index != -1) {
            val stack = stacks[index]
            stacks[index] = stack.copy(
                widgets = stack.widgets.filter { it.id != widgetId }
            )
            _stacks.value = stacks
        }
    }

    fun updateStackBlendMode(stackId: String, blendMode: BlendMode) {
        val stacks = _stacks.value.toMutableList()
        val index = stacks.indexOfFirst { it.id == stackId }

        if (index != -1) {
            stacks[index] = stacks[index].copy(blendMode = blendMode)
            _stacks.value = stacks
        }
    }

    fun updateStackOpacity(stackId: String, opacity: Float) {
        val stacks = _stacks.value.toMutableList()
        val index = stacks.indexOfFirst { it.id == stackId }

        if (index != -1) {
            stacks[index] = stacks[index].copy(opacity = opacity.coerceIn(0f, 1f))
            _stacks.value = stacks
        }
    }

    // ========== LAYER MANAGEMENT ==========

    fun createLayer(name: String): WidgetLayer {
        val layer = WidgetLayer(
            id = "layer_${System.currentTimeMillis()}",
            name = name,
            widgetIds = emptySet(),
            isVisible = true,
            isLocked = false,
            opacity = 1f,
            blendMode = BlendMode.NORMAL
        )

        _layers.value = _layers.value + layer
        return layer
    }

    fun addWidgetToLayer(layerId: String, widgetId: String) {
        val layers = _layers.value.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }

        if (index != -1) {
            val layer = layers[index]
            layers[index] = layer.copy(
                widgetIds = layer.widgetIds + widgetId
            )
            _layers.value = layers
        }
    }

    fun removeWidgetFromLayer(layerId: String, widgetId: String) {
        val layers = _layers.value.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }

        if (index != -1) {
            val layer = layers[index]
            layers[index] = layer.copy(
                widgetIds = layer.widgetIds - widgetId
            )
            _layers.value = layers
        }
    }

    fun setLayerVisibility(layerId: String, isVisible: Boolean) {
        val layers = _layers.value.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }

        if (index != -1) {
            layers[index] = layers[index].copy(isVisible = isVisible)
            _layers.value = layers
        }
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        val layers = _layers.value.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }

        if (index != -1) {
            layers[index] = layers[index].copy(opacity = opacity.coerceIn(0f, 1f))
            _layers.value = layers
        }
    }

    // ========== BLEND MODE OPERATIONS ==========

    fun applyBlendMode(
        baseColor: Color,
        blendColor: Color,
        blendMode: BlendMode
    ): Color {
        if (!stackConfig.enableBlendModes) return blendColor

        return when (blendMode) {
            BlendMode.NORMAL -> blendColor
            BlendMode.MULTIPLY -> multiplyColors(baseColor, blendColor)
            BlendMode.SCREEN -> screenColors(baseColor, blendColor)
            BlendMode.OVERLAY -> overlayColors(baseColor, blendColor)
            BlendMode.DARKEN -> darkenColors(baseColor, blendColor)
            BlendMode.LIGHTEN -> lightenColors(baseColor, blendColor)
            BlendMode.ADD -> addColors(baseColor, blendColor)
            BlendMode.SUBTRACT -> subtractColors(baseColor, blendColor)
            BlendMode.DIFFERENCE -> differenceColors(baseColor, blendColor)
            BlendMode.EXCLUSION -> exclusionColors(baseColor, blendColor)
        }
    }

    private fun multiplyColors(base: Color, blend: Color): Color {
        return Color(
            red = base.red * blend.red,
            green = base.green * blend.green,
            blue = base.blue * blend.blue,
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun screenColors(base: Color, blend: Color): Color {
        return Color(
            red = 1f - (1f - base.red) * (1f - blend.red),
            green = 1f - (1f - base.green) * (1f - blend.green),
            blue = 1f - (1f - base.blue) * (1f - blend.blue),
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun overlayColors(base: Color, blend: Color): Color {
        return Color(
            red = if (base.red < 0.5f) 2 * base.red * blend.red else 1f - 2 * (1f - base.red) * (1f - blend.red),
            green = if (base.green < 0.5f) 2 * base.green * blend.green else 1f - 2 * (1f - base.green) * (1f - blend.green),
            blue = if (base.blue < 0.5f) 2 * base.blue * blend.blue else 1f - 2 * (1f - base.blue) * (1f - blend.blue),
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun darkenColors(base: Color, blend: Color): Color {
        return Color(
            red = minOf(base.red, blend.red),
            green = minOf(base.green, blend.green),
            blue = minOf(base.blue, blend.blue),
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun lightenColors(base: Color, blend: Color): Color {
        return Color(
            red = maxOf(base.red, blend.red),
            green = maxOf(base.green, blend.green),
            blue = maxOf(base.blue, blend.blue),
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun addColors(base: Color, blend: Color): Color {
        return Color(
            red = (base.red + blend.red).coerceIn(0f, 1f),
            green = (base.green + blend.green).coerceIn(0f, 1f),
            blue = (base.blue + blend.blue).coerceIn(0f, 1f),
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun subtractColors(base: Color, blend: Color): Color {
        return Color(
            red = (base.red - blend.red).coerceIn(0f, 1f),
            green = (base.green - blend.green).coerceIn(0f, 1f),
            blue = (base.blue - blend.blue).coerceIn(0f, 1f),
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun differenceColors(base: Color, blend: Color): Color {
        return Color(
            red = abs(base.red - blend.red),
            green = abs(base.green - blend.green),
            blue = abs(base.blue - blend.blue),
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    private fun exclusionColors(base: Color, blend: Color): Color {
        return Color(
            red = base.red + blend.red - 2 * base.red * blend.red,
            green = base.green + blend.green - 2 * base.green * blend.green,
            blue = base.blue + blend.blue - 2 * base.blue * blend.blue,
            alpha = maxOf(base.alpha, blend.alpha)
        )
    }

    // ========== CLIPPING AND MASKING ==========

    fun createClippingMask(widgetId: String, clipPath: List<Offset>): ClippingMask {
        return ClippingMask(
            id = "clip_${System.currentTimeMillis()}",
            targetWidgetId = widgetId,
            path = clipPath
        )
    }

    fun createAlphaMask(widgetId: String, maskColor: Color): AlphaMask {
        return AlphaMask(
            id = "mask_${System.currentTimeMillis()}",
            targetWidgetId = widgetId,
            color = maskColor
        )
    }

    // ========== STACK OPERATIONS ==========

    fun mergeStacks(stackIds: List<String>): WidgetStack? {
        if (stackIds.size < 2) return null

        val stacksToMerge = _stacks.value.filter { it.id in stackIds }
        if (stacksToMerge.size != stackIds.size) return null

        val allWidgets = stacksToMerge.flatMap { it.widgets }
        val boundingBox = calculateBoundingBox(allWidgets)

        val mergedStack = WidgetStack(
            id = "merged_${System.currentTimeMillis()}",
            position = boundingBox.topLeft,
            size = boundingBox.size,
            widgets = allWidgets,
            blendMode = BlendMode.NORMAL,
            opacity = 1f
        )

        _stacks.value = _stacks.value.filter { it.id !in stackIds } + mergedStack

        // Remove old stacks
        stackIds.forEach { removeStack(it) }

        return mergedStack
    }

    fun removeStack(stackId: String) {
        _stacks.value = _stacks.value.filter { it.id != stackId }
    }

    fun clearStack(stackId: String) {
        val stacks = _stacks.value.toMutableList()
        val index = stacks.indexOfFirst { it.id == stackId }

        if (index != -1) {
            stacks[index] = stacks[index].copy(widgets = emptyList())
            _stacks.value = stacks
        }
    }

    private fun calculateBoundingBox(widgets: List<CanvasWidget>): Rect {
        if (widgets.isEmpty()) return Rect.Zero

        var left = Float.MAX_VALUE
        var top = Float.MAX_VALUE
        var right = Float.MIN_VALUE
        var bottom = Float.MIN_VALUE

        widgets.forEach { widget ->
            left = minOf(left, widget.position.x)
            top = minOf(top, widget.position.y)
            right = maxOf(right, widget.position.x + widget.size.width)
            bottom = maxOf(bottom, widget.position.y + widget.size.height)
        }

        return Rect(Offset(left, top), Offset(right, bottom))
    }
}

/**
 * Widget stack
 */
data class WidgetStack(
    val id: String,
    val position: Offset,
    val size: Size,
    val widgets: List<CanvasWidget>,
    val blendMode: BlendMode,
    val opacity: Float
)

/**
 * Widget layer
 */
data class WidgetLayer(
    val id: String,
    val name: String,
    val widgetIds: Set<String>,
    val isVisible: Boolean,
    val isLocked: Boolean,
    val opacity: Float,
    val blendMode: BlendMode
)

/**
 * Stack configuration
 */
data class StackConfig(
    val maxStackDepth: Int = 10,
    val enableBlendModes: Boolean = true,
    val enableClipping: Boolean = true,
    val enableMasks: Boolean = true,
    val compositeOperation: CompositeOperation = CompositeOperation.NORMAL
)

/**
 * Composite operations
 */
enum class CompositeOperation {
    NORMAL,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    COLOR_DODGE,
    COLOR_BURN,
    HARD_LIGHT,
    SOFT_LIGHT,
    DIFFERENCE,
    EXCLUSION
}

/**
 * Clipping mask
 */
data class ClippingMask(
    val id: String,
    val targetWidgetId: String,
    val path: List<Offset>
)

/**
 * Alpha mask
 */
data class AlphaMask(
    val id: String,
    val targetWidgetId: String,
    val color: Color
)
