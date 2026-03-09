package com.sugarmunch.app.widgets.infinite

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Infinite Canvas - Unlimited widget positioning and sizing
 * 
 * Features:
 * - Freeform widget placement
 * - Infinite canvas space
 * - Zoom and pan support
 * - Widget snapping
 * - Grid system
 */
class InfiniteCanvas(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Widgets on canvas
    private val _widgets = MutableStateFlow<List<CanvasWidget>>(emptyList())
    val widgets: StateFlow<List<CanvasWidget>> = _widgets.asStateFlow()

    // Canvas viewport
    private val _viewport = MutableStateFlow(ViewportState())
    val viewport: StateFlow<ViewportState> = _viewport.asStateFlow()

    // Selected widget
    private val _selectedWidgetId = MutableStateFlow<String?>(null)
    val selectedWidgetId: StateFlow<String?> = _selectedWidgetId.asStateFlow()

    // Canvas configuration
    var canvasConfig = CanvasConfig(
        gridSize = 20f,
        snapToGrid = true,
        showGrid = false,
        minZoom = 0.5f,
        maxZoom = 3.0f,
        infiniteBounds = true,
        canvasWidth = 10000f,
        canvasHeight = 10000f
    )

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
    }

    fun stop() {
        isRunning = false
    }

    // ========== WIDGET MANAGEMENT ==========

    fun addWidget(widget: CanvasWidget) {
        _widgets.value = _widgets.value + widget
    }

    fun removeWidget(id: String) {
        _widgets.value = _widgets.value.filter { it.id != id }
        if (_selectedWidgetId.value == id) {
            _selectedWidgetId.value = null
        }
    }

    fun updateWidget(id: String, update: CanvasWidget.() -> CanvasWidget) {
        _widgets.value = _widgets.value.map { widget ->
            if (widget.id == id) widget.update() else widget
        }
    }

    fun moveWidget(id: String, newPosition: Offset) {
        val position = if (canvasConfig.snapToGrid) {
            snapToGrid(newPosition)
        } else {
            newPosition
        }

        updateWidget(id) { copy(position = position) }
    }

    fun resizeWidget(id: String, newSize: Size) {
        val size = if (canvasConfig.snapToGrid) {
            Size(
                (newSize.width / canvasConfig.gridSize).roundToInt() * canvasConfig.gridSize,
                (newSize.height / canvasConfig.gridSize).roundToInt() * canvasConfig.gridSize
            )
        } else {
            newSize
        }

        updateWidget(id) { copy(size = size.coerceAtLeast(Size(50f, 50f))) }
    }

    fun selectWidget(id: String?) {
        _selectedWidgetId.value = id
    }

    fun clearWidgets() {
        _widgets.value = emptyList()
        _selectedWidgetId.value = null
    }

    // ========== VIEWPORT CONTROL ==========

    fun setViewportOffset(offset: Offset) {
        _viewport.value = _viewport.value.copy(offset = offset)
    }

    fun setViewportZoom(zoom: Float) {
        _viewport.value = _viewport.value.copy(
            zoom = zoom.coerceIn(canvasConfig.minZoom, canvasConfig.maxZoom)
        )
    }

    fun zoomIn() {
        setViewportZoom(_viewport.value.zoom * 1.2f)
    }

    fun zoomOut() {
        setViewportZoom(_viewport.value.zoom / 1.2f)
    }

    fun resetViewport() {
        _viewport.value = ViewportState()
    }

    fun screenToCanvas(screenPosition: Offset): Offset {
        val viewport = _viewport.value
        return Offset(
            (screenPosition.x / viewport.zoom) - viewport.offset.x,
            (screenPosition.y / viewport.zoom) - viewport.offset.y
        )
    }

    fun canvasToScreen(canvasPosition: Offset): Offset {
        val viewport = _viewport.value
        return Offset(
            (canvasPosition.x + viewport.offset.x) * viewport.zoom,
            (canvasPosition.y + viewport.offset.y) * viewport.zoom
        )
    }

    // ========== GRID SYSTEM ==========

    private fun snapToGrid(position: Offset): Offset {
        return Offset(
            (position.x / canvasConfig.gridSize).roundToInt() * canvasConfig.gridSize,
            (position.y / canvasConfig.gridSize).roundToInt() * canvasConfig.gridSize
        )
    }

    fun isPointOnGrid(position: Offset): Boolean {
        return position.x % canvasConfig.gridSize == 0f &&
               position.y % canvasConfig.gridSize == 0f
    }

    fun getNearbyGridPoints(position: Offset, radius: Float): List<Offset> {
        val points = mutableListOf<Offset>()
        val startX = ((position.x - radius) / canvasConfig.gridSize).roundToInt()
        val endX = ((position.x + radius) / canvasConfig.gridSize).roundToInt()
        val startY = ((position.y - radius) / canvasConfig.gridSize).roundToInt()
        val endY = ((position.y + radius) / canvasConfig.gridSize).roundToInt()

        for (x in startX..endX) {
            for (y in startY..endY) {
                val point = Offset(x * canvasConfig.gridSize, y * canvasConfig.gridSize)
                if ((point - position).getDistance() <= radius) {
                    points.add(point)
                }
            }
        }

        return points
    }

    // ========== WIDGET STACKING ==========

    fun bringWidgetToFront(id: String) {
        val widgets = _widgets.value.toMutableList()
        val index = widgets.indexOfFirst { it.id == id }
        if (index != -1) {
            val widget = widgets.removeAt(index)
            widgets.add(widget)
            _widgets.value = widgets
        }
    }

    fun sendWidgetToBack(id: String) {
        val widgets = _widgets.value.toMutableList()
        val index = widgets.indexOfFirst { it.id == id }
        if (index != -1) {
            val widget = widgets.removeAt(index)
            widgets.add(0, widget)
            _widgets.value = widgets
        }
    }

    fun getWidgetsAtPosition(position: Offset): List<CanvasWidget> {
        return _widgets.value.filter { widget ->
            val widgetRect = Rect(widget.position, widget.size)
            widgetRect.contains(position)
        }.sortedByDescending { it.zIndex }
    }

    fun getTopWidgetAtPosition(position: Offset): CanvasWidget? {
        return getWidgetsAtPosition(position).firstOrNull()
    }

    // ========== COLLISION DETECTION ==========

    fun checkWidgetCollision(widget1: CanvasWidget, widget2: CanvasWidget): Boolean {
        val rect1 = Rect(widget1.position, widget1.size)
        val rect2 = Rect(widget2.position, widget2.size)

        return Rect.intersects(rect1, rect2)
    }

    fun getCollidingWidgets(widget: CanvasWidget): List<CanvasWidget> {
        return _widgets.value.filter { other ->
            other.id != widget.id && checkWidgetCollision(widget, other)
        }
    }

    // ========== WIDGET GROUPS ==========

    fun createWidgetGroup(widgetIds: List<String>, groupName: String): WidgetGroup {
        val group = WidgetGroup(
            id = "group_${System.currentTimeMillis()}",
            name = groupName,
            widgetIds = widgetIds.toSet()
        )

        // Update widgets with group ID
        widgetIds.forEach { id ->
            updateWidget(id) { copy(groupId = group.id) }
        }

        return group
    }

    fun moveWidgetGroup(groupId: String, offset: Offset) {
        val groupWidgets = _widgets.value.filter { it.groupId == groupId }
        groupWidgets.forEach { widget ->
            moveWidget(widget.id, widget.position + offset)
        }
    }

    // ========== PRESET WIDGETS ==========

    fun createClockWidget(position: Offset): CanvasWidget {
        return CanvasWidget(
            id = "clock_${System.currentTimeMillis()}",
            type = WidgetType.CLOCK,
            position = position,
            size = Size(200f, 100f),
            backgroundColor = Color.Black.copy(alpha = 0.5f),
            cornerRadius = 16f,
            zIndex = 0
        )
    }

    fun createWeatherWidget(position: Offset): CanvasWidget {
        return CanvasWidget(
            id = "weather_${System.currentTimeMillis()}",
            type = WidgetType.WEATHER,
            position = position,
            size = Size(150f, 150f),
            backgroundColor = Color.Blue.copy(alpha = 0.3f),
            cornerRadius = 75f,
            zIndex = 0
        )
    }

    fun createNoteWidget(position: Offset): CanvasWidget {
        return CanvasWidget(
            id = "note_${System.currentTimeMillis()}",
            type = WidgetType.NOTE,
            position = position,
            size = Size(200f, 200f),
            backgroundColor = Color.Yellow.copy(alpha = 0.5f),
            cornerRadius = 8f,
            zIndex = 0
        )
    }

    companion object {
        @Volatile
        private var instance: InfiniteCanvas? = null

        fun getInstance(context: Context): InfiniteCanvas {
            return instance ?: synchronized(this) {
                instance ?: InfiniteCanvas(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Widget on infinite canvas
 */
data class CanvasWidget(
    val id: String,
    val type: WidgetType,
    val position: Offset,
    val size: Size,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val backgroundColor: Color = Color.Transparent,
    val borderColor: Color = Color.Transparent,
    val borderWidth: Float = 0f,
    val cornerRadius: Float = 0f,
    val zIndex: Int = 0,
    val groupId: String? = null,
    val isLocked: Boolean = false,
    val isVisible: Boolean = true,
    val opacity: Float = 1f,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val content: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Widget types
 */
enum class WidgetType {
    CLOCK,
    WEATHER,
    NOTE,
    CALENDAR,
    BATTERY,
    MUSIC,
    PHOTO,
    CUSTOM,
    CONTAINER
}

/**
 * Blend modes for widgets
 */
enum class BlendMode {
    NORMAL,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    ADD,
    SUBTRACT,
    DIFFERENCE,
    EXCLUSION
}

/**
 * Viewport state
 */
data class ViewportState(
    val offset: Offset = Offset.Zero,
    val zoom: Float = 1f,
    val rotation: Float = 0f
)

/**
 * Canvas configuration
 */
data class CanvasConfig(
    val gridSize: Float = 20f,
    val snapToGrid: Boolean = true,
    val showGrid: Boolean = false,
    val minZoom: Float = 0.5f,
    val maxZoom: Float = 3.0f,
    val infiniteBounds: Boolean = true,
    val canvasWidth: Float = 10000f,
    val canvasHeight: Float = 10000f,
    val backgroundColor: Color = Color(0xFF1A1A2E),
    val gridColor: Color = Color.White.copy(alpha = 0.1f)
)

/**
 * Widget group
 */
data class WidgetGroup(
    val id: String,
    val name: String,
    val widgetIds: Set<String>,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Canvas presets
 */
object CanvasPresets {

    val GRID_8 = CanvasConfig(
        gridSize = 8f,
        snapToGrid = true,
        showGrid = true
    )

    val GRID_16 = CanvasConfig(
        gridSize = 16f,
        snapToGrid = true,
        showGrid = true
    )

    val GRID_32 = CanvasConfig(
        gridSize = 32f,
        snapToGrid = true,
        showGrid = false
    )

    val FREEFORM = CanvasConfig(
        gridSize = 1f,
        snapToGrid = false,
        showGrid = false
    )

    val ZOOMED_OUT = ViewportState(
        offset = Offset.Zero,
        zoom = 0.5f
    )

    val ZOOMED_IN = ViewportState(
        offset = Offset.Zero,
        zoom = 2.0f
    )
}
