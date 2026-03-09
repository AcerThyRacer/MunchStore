package com.sugarmunch.app.widgets.infinite

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Widget Builder - Visual widget creation and customization
 * 
 * Features:
 * - Drag-and-drop widget creation
 * - Property editor
 * - Style customization
 * - Preview mode
 * - Template system
 */
class WidgetBuilder {

    // Current building state
    private val _builderState = MutableStateFlow(BuilderState())
    val builderState: StateFlow<BuilderState> = _builderState.asStateFlow()

    // Available templates
    private val templates = mutableListOf<WidgetTemplate>()

    // Builder configuration
    var builderConfig = BuilderConfig(
        showGrid = true,
        snapToGrid = true,
        showGuides = true,
        livePreview = true,
        autoSave = true
    )

    init {
        initializeTemplates()
    }

    private fun initializeTemplates() {
        templates.addAll(listOf(
            WidgetTemplate(
                id = "minimal_clock",
                name = "Minimal Clock",
                category = WidgetCategory.CLOCK,
                defaultSize = Size(200f, 100f),
                defaultBackgroundColor = Color.Black.copy(alpha = 0.5f),
                defaultCornerRadius = 16f
            ),
            WidgetTemplate(
                id = "weather_card",
                name = "Weather Card",
                category = WidgetCategory.WEATHER,
                defaultSize = Size(150f, 150f),
                defaultBackgroundColor = Color.Blue.copy(alpha = 0.3f),
                defaultCornerRadius = 75f
            ),
            WidgetTemplate(
                id = "note_sticky",
                name = "Sticky Note",
                category = WidgetCategory.NOTE,
                defaultSize = Size(200f, 200f),
                defaultBackgroundColor = Color.Yellow.copy(alpha = 0.5f),
                defaultCornerRadius = 8f
            ),
            WidgetTemplate(
                id = "music_player",
                name = "Music Player",
                category = WidgetCategory.MUSIC,
                defaultSize = Size(300f, 120f),
                defaultBackgroundColor = Color(0xFF2D2D2D),
                defaultCornerRadius = 12f
            ),
            WidgetTemplate(
                id = "battery_status",
                name = "Battery Status",
                category = WidgetCategory.BATTERY,
                defaultSize = Size(100f, 100f),
                defaultBackgroundColor = Color.Green.copy(alpha = 0.3f),
                defaultCornerRadius = 50f
            )
        ))
    }

    // ========== BUILDING OPERATIONS ==========

    fun startBuilding(template: WidgetTemplate, position: Offset): CanvasWidget {
        _builderState.value = BuilderState(
            isBuilding = true,
            currentTemplate = template,
            currentWidget = CanvasWidget(
                id = "building_${System.currentTimeMillis()}",
                type = template.toWidgetType(),
                position = position,
                size = template.defaultSize,
                backgroundColor = template.defaultBackgroundColor,
                cornerRadius = template.defaultCornerRadius
            ),
            history = emptyList(),
            historyIndex = -1
        )

        return _builderState.value.currentWidget!!
    }

    fun updateWidgetProperty(property: String, value: Any) {
        val state = _builderState.value
        val widget = state.currentWidget ?: return

        val updatedWidget = when (property) {
            "position" -> widget.copy(position = value as Offset)
            "size" -> widget.copy(size = value as Size)
            "rotation" -> widget.copy(rotation = value as Float)
            "scale" -> widget.copy(scale = value as Float)
            "backgroundColor" -> widget.copy(backgroundColor = value as Color)
            "borderColor" -> widget.copy(borderColor = value as Color)
            "borderWidth" -> widget.copy(borderWidth = value as Float)
            "cornerRadius" -> widget.copy(cornerRadius = value as Float)
            "opacity" -> widget.copy(opacity = (value as Float).coerceIn(0f, 1f))
            "blendMode" -> widget.copy(blendMode = value as BlendMode)
            else -> widget
        }

        addToHistory(updatedWidget)
        _builderState.value = state.copy(currentWidget = updatedWidget)
    }

    fun applyStyle(style: WidgetStyle) {
        val state = _builderState.value
        val widget = state.currentWidget ?: return

        val styledWidget = widget.copy(
            backgroundColor = style.backgroundColor,
            borderColor = style.borderColor,
            borderWidth = style.borderWidth,
            cornerRadius = style.cornerRadius,
            opacity = style.opacity
        )

        addToHistory(styledWidget)
        _builderState.value = state.copy(currentWidget = styledWidget)
    }

    fun undo() {
        val state = _builderState.value
        if (state.historyIndex > 0) {
            val newIndex = state.historyIndex - 1
            _builderState.value = state.copy(
                currentWidget = state.history[newIndex],
                historyIndex = newIndex
            )
        }
    }

    fun redo() {
        val state = _builderState.value
        if (state.historyIndex < state.history.size - 1) {
            val newIndex = state.historyIndex + 1
            _builderState.value = state.copy(
                currentWidget = state.history[newIndex],
                historyIndex = newIndex
            )
        }
    }

    fun finishBuilding(): CanvasWidget? {
        val state = _builderState.value
        val widget = state.currentWidget

        _builderState.value = BuilderState()

        return widget
    }

    fun cancelBuilding() {
        _builderState.value = BuilderState()
    }

    private fun addToHistory(widget: CanvasWidget) {
        val state = _builderState.value
        val newHistory = state.history.take(state.historyIndex + 1) + widget

        _builderState.value = state.copy(
            history = newHistory,
            historyIndex = newHistory.size - 1,
            currentWidget = widget
        )
    }

    // ========== TEMPLATES ==========

    fun getTemplates(): List<WidgetTemplate> = templates

    fun getTemplatesByCategory(category: WidgetCategory): List<WidgetTemplate> {
        return templates.filter { it.category == category }
    }

    fun createCustomTemplate(
        name: String,
        category: WidgetCategory,
        widget: CanvasWidget
    ): WidgetTemplate {
        val template = WidgetTemplate(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            category = category,
            defaultSize = widget.size,
            defaultBackgroundColor = widget.backgroundColor,
            defaultCornerRadius = widget.cornerRadius,
            isCustom = true
        )

        templates.add(template)
        return template
    }

    // ========== EXPORT/IMPORT ==========

    fun exportWidget(widget: CanvasWidget): String {
        return Json.encodeToString(CanvasWidgetSerializer, widget)
    }

    fun importWidget(json: String): CanvasWidget? {
        return try {
            Json.decodeFromString(CanvasWidgetSerializer, json)
        } catch (e: Exception) {
            null
        }
    }

    fun exportWidgetConfig(widgets: List<CanvasWidget>): String {
        return Json.encodeToString(WidgetConfigSerializer, WidgetConfig(widgets))
    }

    fun importWidgetConfig(json: String): List<CanvasWidget>? {
        return try {
            Json.decodeFromString(WidgetConfigSerializer, json).widgets
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Builder state
 */
data class BuilderState(
    val isBuilding: Boolean = false,
    val currentTemplate: WidgetTemplate? = null,
    val currentWidget: CanvasWidget? = null,
    val history: List<CanvasWidget> = emptyList(),
    val historyIndex: Int = -1,
    val selectedTool: BuilderTool = BuilderTool.SELECT
)

/**
 * Builder tools
 */
enum class BuilderTool {
    SELECT,
    MOVE,
    RESIZE,
    ROTATE,
    DRAW,
    TEXT,
    SHAPE
}

/**
 * Builder configuration
 */
data class BuilderConfig(
    val showGrid: Boolean = true,
    val snapToGrid: Boolean = true,
    val showGuides: Boolean = true,
    val livePreview: Boolean = true,
    val autoSave: Boolean = true
)

/**
 * Widget template
 */
data class WidgetTemplate(
    val id: String,
    val name: String,
    val category: WidgetCategory,
    val defaultSize: Size,
    val defaultBackgroundColor: Color,
    val defaultCornerRadius: Float,
    val description: String = "",
    val previewImage: String? = null,
    val isCustom: Boolean = false
) {
    fun toWidgetType(): WidgetType {
        return when (category) {
            WidgetCategory.CLOCK -> WidgetType.CLOCK
            WidgetCategory.WEATHER -> WidgetType.WEATHER
            WidgetCategory.NOTE -> WidgetType.NOTE
            WidgetCategory.MUSIC -> WidgetType.MUSIC
            WidgetCategory.BATTERY -> WidgetType.BATTERY
            WidgetCategory.CALENDAR -> WidgetType.CALENDAR
            WidgetCategory.PHOTO -> WidgetType.PHOTO
            WidgetCategory.CUSTOM -> WidgetType.CUSTOM
        }
    }
}

/**
 * Widget categories
 */
enum class WidgetCategory {
    CLOCK,
    WEATHER,
    NOTE,
    MUSIC,
    BATTERY,
    CALENDAR,
    PHOTO,
    CUSTOM
}

/**
 * Widget style
 */
data class WidgetStyle(
    val backgroundColor: Color = Color.Transparent,
    val borderColor: Color = Color.Transparent,
    val borderWidth: Float = 0f,
    val cornerRadius: Float = 0f,
    val opacity: Float = 1f,
    val shadowColor: Color = Color.Black.copy(alpha = 0.3f),
    val shadowRadius: Float = 10f,
    val shadowOffset: Offset = Offset(0f, 5f),
    val gradient: GradientStyle? = null
)

/**
 * Gradient style
 */
data class GradientStyle(
    val type: GradientType,
    val colors: List<Color>,
    val startOffset: Offset = Offset.Zero,
    val endOffset: Offset = Offset(1f, 1f)
)

/**
 * Gradient types
 */
enum class GradientType {
    LINEAR,
    RADIAL,
    SWEEP
}

/**
 * Widget config for serialization
 */
@Serializable
data class WidgetConfig(val widgets: List<CanvasWidget>)

// Serializers would be implemented here using kotlinx.serialization
