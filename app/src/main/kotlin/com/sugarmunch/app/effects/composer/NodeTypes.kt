package com.sugarmunch.app.effects.composer

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Phase 2: Node-Based Effect Composer
 * 
 * A visual scripting system for creating custom effects by connecting nodes.
 * Each node represents an operation (input, processing, or output).
 * Nodes can be connected to create complex effect chains.
 */

// ─────────────────────────────────────────────────────────────────────────────────
// NODE PORT TYPES
// ─────────────────────────────────────────────────────────────────────────────────

enum class PortType {
    NUMBER,      // Float values
    COLOR,       // Color values
    VECTOR2,     // 2D coordinates
    GRADIENT,    // Color gradients
    PARTICLE,    // Particle data
    AUDIO,       // Audio analysis data
    TRIGGER,     // Event triggers
    BOOLEAN      // Boolean values
}

enum class PortDirection {
    INPUT, OUTPUT
}

/**
 * Represents a connection point on a node
 */
data class NodePort(
    val id: String,
    val name: String,
    val type: PortType,
    val direction: PortDirection,
    val defaultValue: Any? = null,
    val minValue: Float? = null,
    val maxValue: Float? = null
)

// ─────────────────────────────────────────────────────────────────────────────────
// NODE CATEGORIES
// ─────────────────────────────────────────────────────────────────────────────────

enum class NodeCategory {
    INPUT,       // Data sources (time, audio, touch, etc.)
    COLOR,       // Color operations
    MATH,        // Mathematical operations
    PARTICLE,    // Particle system nodes
    SHADER,      // Shader effects
    OUTPUT,      // Final output nodes
    LOGIC,       // Conditional logic
    TRANSFORM    // Transformations
}

// ─────────────────────────────────────────────────────────────────────────────────
// BASE NODE INTERFACE
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Base interface for all effect nodes
 */
interface EffectNode {
    val id: String
    val name: String
    val category: NodeCategory
    val description: String
    val inputs: List<NodePort>
    val outputs: List<NodePort>
    
    /**
     * Process this node given the input values
     * Returns a map of output port IDs to values
     */
    fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?>
    
    /**
     * Create a copy of this node with a new ID
     */
    fun clone(newId: String): EffectNode
}

/**
 * Context passed to nodes during processing
 */
data class NodeContext(
    val time: Float,
    val deltaTime: Float,
    val screenWidth: Float,
    val screenHeight: Float,
    val touchPosition: Pair<Float, Float>?,
    val audioData: AudioAnalysisData?,
    val random: Random = Random.Default
)

/**
 * Audio analysis data passed to nodes
 */
data class AudioAnalysisData(
    val amplitude: Float,
    val bass: Float,
    val mid: Float,
    val treble: Float,
    val frequencyBands: FloatArray,
    val beatDetected: Boolean,
    val bpm: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioAnalysisData) return false
        return frequencyBands.contentEquals(other.frequencyBands)
    }
    
    override fun hashCode(): Int = frequencyBands.contentHashCode()
}

// ─────────────────────────────────────────────────────────────────────────────────
// INPUT NODES
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Time input node - outputs current time and delta time
 */
data class TimeInputNode(
    override val id: String,
    val speedMultiplier: Float = 1f
) : EffectNode {
    override val name = "Time"
    override val category = NodeCategory.INPUT
    override val description = "Outputs current time values"
    override val inputs = emptyList<NodePort>()
    override val outputs = listOf(
        NodePort("time", "Time", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("deltaTime", "Delta Time", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("sin", "Sin(Time)", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("cos", "Cos(Time)", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val t = context.time * speedMultiplier
        return mapOf(
            "time" to t,
            "deltaTime" to context.deltaTime,
            "sin" to kotlin.math.sin(t.toDouble()).toFloat(),
            "cos" to kotlin.math.cos(t.toDouble()).toFloat()
        )
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Touch input node - outputs touch position
 */
data class TouchInputNode(
    override val id: String
) : EffectNode {
    override val name = "Touch"
    override val category = NodeCategory.INPUT
    override val description = "Outputs touch position"
    override val inputs = emptyList<NodePort>()
    override val outputs = listOf(
        NodePort("position", "Position", PortType.VECTOR2, PortDirection.OUTPUT),
        NodePort("x", "X", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("y", "Y", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("isPressed", "Is Pressed", PortType.BOOLEAN, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val pos = context.touchPosition
        return mapOf(
            "position" to pos,
            "x" to pos?.first,
            "y" to pos?.second,
            "isPressed" to (pos != null)
        )
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Audio input node - outputs audio analysis data
 */
data class AudioInputNode(
    override val id: String
) : EffectNode {
    override val name = "Audio"
    override val category = NodeCategory.INPUT
    override val description = "Outputs audio analysis data"
    override val inputs = emptyList<NodePort>()
    override val outputs = listOf(
        NodePort("amplitude", "Amplitude", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("bass", "Bass", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("mid", "Mid", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("treble", "Treble", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("beat", "Beat Detected", PortType.BOOLEAN, PortDirection.OUTPUT),
        NodePort("bpm", "BPM", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val audio = context.audioData
        return mapOf(
            "amplitude" to (audio?.amplitude ?: 0f),
            "bass" to (audio?.bass ?: 0f),
            "mid" to (audio?.mid ?: 0f),
            "treble" to (audio?.treble ?: 0f),
            "beat" to (audio?.beatDetected ?: false),
            "bpm" to (audio?.bpm ?: 0f)
        )
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Random number input node
 */
data class RandomInputNode(
    override val id: String,
    val min: Float = 0f,
    val max: Float = 1f,
    val seed: Long? = null
) : EffectNode {
    override val name = "Random"
    override val category = NodeCategory.INPUT
    override val description = "Outputs random values"
    override val inputs = listOf(
        NodePort("min", "Min", PortType.NUMBER, PortDirection.INPUT, min),
        NodePort("max", "Max", PortType.NUMBER, PortDirection.INPUT, max)
    )
    override val outputs = listOf(
        NodePort("value", "Value", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("int", "Integer", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    private val random = seed?.let { Random(it) } ?: Random
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val minVal = (inputs["min"] as? Float) ?: min
        val maxVal = (inputs["max"] as? Float) ?: max
        val value = minVal + random.nextFloat() * (maxVal - minVal)
        return mapOf(
            "value" to value,
            "int" to value.toInt().toFloat()
        )
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Screen size input node
 */
data class ScreenInputNode(
    override val id: String
) : EffectNode {
    override val name = "Screen"
    override val category = NodeCategory.INPUT
    override val description = "Outputs screen dimensions"
    override val inputs = emptyList<NodePort>()
    override val outputs = listOf(
        NodePort("size", "Size", PortType.VECTOR2, PortDirection.OUTPUT),
        NodePort("width", "Width", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("height", "Height", PortType.NUMBER, PortDirection.OUTPUT),
        NodePort("center", "Center", PortType.VECTOR2, PortDirection.OUTPUT),
        NodePort("aspect", "Aspect Ratio", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val w = context.screenWidth
        val h = context.screenHeight
        return mapOf(
            "size" to Pair(w, h),
            "width" to w,
            "height" to h,
            "center" to Pair(w / 2f, h / 2f),
            "aspect" to (w / h)
        )
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

// ─────────────────────────────────────────────────────────────────────────────────
// COLOR NODES
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Solid color node
 */
data class ColorNode(
    override val id: String,
    val color: Color = Color.White
) : EffectNode {
    override val name = "Color"
    override val category = NodeCategory.COLOR
    override val description = "Outputs a solid color"
    override val inputs = listOf(
        NodePort("red", "Red", PortType.NUMBER, PortDirection.INPUT, color.red, 0f, 1f),
        NodePort("green", "Green", PortType.NUMBER, PortDirection.INPUT, color.green, 0f, 1f),
        NodePort("blue", "Blue", PortType.NUMBER, PortDirection.INPUT, color.blue, 0f, 1f),
        NodePort("alpha", "Alpha", PortType.NUMBER, PortDirection.INPUT, color.alpha, 0f, 1f)
    )
    override val outputs = listOf(
        NodePort("color", "Color", PortType.COLOR, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val r = (inputs["red"] as? Float) ?: color.red
        val g = (inputs["green"] as? Float) ?: color.green
        val b = (inputs["blue"] as? Float) ?: color.blue
        val a = (inputs["alpha"] as? Float) ?: color.alpha
        return mapOf("color" to Color(r, g, b, a))
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Color blend node
 */
data class ColorBlendNode(
    override val id: String,
    val blendMode: ColorBlendMode = ColorBlendMode.NORMAL
) : EffectNode {
    override val name = "Color Blend"
    override val category = NodeCategory.COLOR
    override val description = "Blends two colors together"
    override val inputs = listOf(
        NodePort("colorA", "Color A", PortType.COLOR, PortDirection.INPUT),
        NodePort("colorB", "Color B", PortType.COLOR, PortDirection.INPUT),
        NodePort("factor", "Factor", PortType.NUMBER, PortDirection.INPUT, 0.5f, 0f, 1f)
    )
    override val outputs = listOf(
        NodePort("color", "Result", PortType.COLOR, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val a = inputs["colorA"] as? Color ?: Color.White
        val b = inputs["colorB"] as? Color ?: Color.Black
        val f = (inputs["factor"] as? Float)?.coerceIn(0f, 1f) ?: 0.5f
        
        val result = when (blendMode) {
            ColorBlendMode.NORMAL -> Color(
                a.red * (1 - f) + b.red * f,
                a.green * (1 - f) + b.green * f,
                a.blue * (1 - f) + b.blue * f,
                a.alpha * (1 - f) + b.alpha * f
            )
            ColorBlendMode.MULTIPLY -> Color(
                a.red * b.red,
                a.green * b.green,
                a.blue * b.blue,
                a.alpha * (1 - f) + b.alpha * f
            )
            ColorBlendMode.SCREEN -> Color(
                1 - (1 - a.red) * (1 - b.red),
                1 - (1 - a.green) * (1 - b.green),
                1 - (1 - a.blue) * (1 - b.blue),
                a.alpha * (1 - f) + b.alpha * f
            )
            ColorBlendMode.OVERLAY -> {
                val overlay = { ca: Float, cb: Float ->
                    if (ca < 0.5f) 2 * ca * cb else 1 - 2 * (1 - ca) * (1 - cb)
                }
                Color(
                    overlay(a.red, b.red),
                    overlay(a.green, b.green),
                    overlay(a.blue, b.blue),
                    a.alpha * (1 - f) + b.alpha * f
                )
            }
            ColorBlendMode.ADD -> Color(
                (a.red + b.red * f).coerceIn(0f, 1f),
                (a.green + b.green * f).coerceIn(0f, 1f),
                (a.blue + b.blue * f).coerceIn(0f, 1f),
                a.alpha
            )
        }
        return mapOf("color" to result)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

enum class ColorBlendMode {
    NORMAL, MULTIPLY, SCREEN, OVERLAY, ADD
}

/**
 * HSV to RGB color node
 */
data class HSVColorNode(
    override val id: String
) : EffectNode {
    override val name = "HSV Color"
    override val category = NodeCategory.COLOR
    override val description = "Creates color from HSV values"
    override val inputs = listOf(
        NodePort("hue", "Hue", PortType.NUMBER, PortDirection.INPUT, 0f, 0f, 360f),
        NodePort("saturation", "Saturation", PortType.NUMBER, PortDirection.INPUT, 1f, 0f, 1f),
        NodePort("value", "Value", PortType.NUMBER, PortDirection.INPUT, 1f, 0f, 1f),
        NodePort("alpha", "Alpha", PortType.NUMBER, PortDirection.INPUT, 1f, 0f, 1f)
    )
    override val outputs = listOf(
        NodePort("color", "Color", PortType.COLOR, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val h = (inputs["hue"] as? Float) ?: 0f
        val s = (inputs["saturation"] as? Float) ?: 1f
        val v = (inputs["value"] as? Float) ?: 1f
        val a = (inputs["alpha"] as? Float) ?: 1f
        
        val color = hsvToRgb(h, s, v, a)
        return mapOf("color" to color)
    }
    
    private fun hsvToRgb(h: Float, s: Float, v: Float, a: Float): Color {
        val hNorm = (h % 360f) / 60f
        val i = hNorm.toInt()
        val f = hNorm - i
        val p = v * (1 - s)
        val q = v * (1 - s * f)
        val t = v * (1 - s * (1 - f))
        
        return when (i) {
            0 -> Color(v, t, p, a)
            1 -> Color(q, v, p, a)
            2 -> Color(p, v, t, a)
            3 -> Color(p, q, v, a)
            4 -> Color(t, p, v, a)
            else -> Color(v, p, q, a)
        }
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Gradient node
 */
data class GradientNode(
    override val id: String,
    val colors: List<Color> = listOf(Color.Red, Color.Blue)
) : EffectNode {
    override val name = "Gradient"
    override val category = NodeCategory.COLOR
    override val description = "Creates a color gradient"
    override val inputs = listOf(
        NodePort("position", "Position", PortType.NUMBER, PortDirection.INPUT, 0f, 0f, 1f),
        NodePort("angle", "Angle", PortType.NUMBER, PortDirection.INPUT, 0f, 0f, 360f)
    )
    override val outputs = listOf(
        NodePort("color", "Color", PortType.COLOR, PortDirection.OUTPUT),
        NodePort("gradient", "Gradient", PortType.GRADIENT, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val pos = ((inputs["position"] as? Float) ?: 0f).coerceIn(0f, 1f)
        
        val color = if (colors.size == 1) {
            colors[0]
        } else {
            val scaledPos = pos * (colors.size - 1)
            val lowerIndex = scaledPos.toInt().coerceIn(0, colors.size - 2)
            val upperIndex = (lowerIndex + 1).coerceIn(0, colors.size - 1)
            val t = scaledPos - lowerIndex
            
            val c1 = colors[lowerIndex]
            val c2 = colors[upperIndex]
            Color(
                c1.red * (1 - t) + c2.red * t,
                c1.green * (1 - t) + c2.green * t,
                c1.blue * (1 - t) + c2.blue * t,
                c1.alpha * (1 - t) + c2.alpha * t
            )
        }
        
        return mapOf(
            "color" to color,
            "gradient" to colors
        )
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

// ─────────────────────────────────────────────────────────────────────────────────
// MATH NODES
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Math operation node
 */
data class MathNode(
    override val id: String,
    val operation: MathOperation = MathOperation.ADD
) : EffectNode {
    override val name = "Math"
    override val category = NodeCategory.MATH
    override val description = "Performs mathematical operations"
    override val inputs = listOf(
        NodePort("a", "A", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("b", "B", PortType.NUMBER, PortDirection.INPUT, 0f)
    )
    override val outputs = listOf(
        NodePort("result", "Result", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val a = (inputs["a"] as? Float) ?: 0f
        val b = (inputs["b"] as? Float) ?: 0f
        
        val result = when (operation) {
            MathOperation.ADD -> a + b
            MathOperation.SUBTRACT -> a - b
            MathOperation.MULTIPLY -> a * b
            MathOperation.DIVIDE -> if (b != 0f) a / b else 0f
            MathOperation.POWER -> kotlin.math.pow(a.toDouble(), b.toDouble()).toFloat()
            MathOperation.MODULO -> if (b != 0f) a % b else 0f
            MathOperation.MIN -> kotlin.math.min(a, b)
            MathOperation.MAX -> kotlin.math.max(a, b)
            MathOperation.ABS -> kotlin.math.abs(a)
            MathOperation.FLOOR -> kotlin.math.floor(a.toDouble()).toFloat()
            MathOperation.CEIL -> kotlin.math.ceil(a.toDouble()).toFloat()
            MathOperation.ROUND -> kotlin.math.round(a)
            MathOperation.SQRT -> kotlin.math.sqrt(a.toDouble()).toFloat()
            MathOperation.LERP -> a // Lerp needs special handling
        }
        
        return mapOf("result" to result)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

enum class MathOperation {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, MODULO,
    MIN, MAX, ABS, FLOOR, CEIL, ROUND, SQRT, LERP
}

/**
 * Wave function node
 */
data class WaveNode(
    override val id: String,
    val waveType: WaveType = WaveType.SINE
) : EffectNode {
    override val name = "Wave"
    override val category = NodeCategory.MATH
    override val description = "Generates wave patterns"
    override val inputs = listOf(
        NodePort("input", "Input", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("frequency", "Frequency", PortType.NUMBER, PortDirection.INPUT, 1f),
        NodePort("amplitude", "Amplitude", PortType.NUMBER, PortDirection.INPUT, 1f),
        NodePort("phase", "Phase", PortType.NUMBER, PortDirection.INPUT, 0f)
    )
    override val outputs = listOf(
        NodePort("output", "Output", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val x = (inputs["input"] as? Float) ?: 0f
        val freq = (inputs["frequency"] as? Float) ?: 1f
        val amp = (inputs["amplitude"] as? Float) ?: 1f
        val phase = (inputs["phase"] as? Float) ?: 0f
        
        val t = x * freq + phase
        val result = when (waveType) {
            WaveType.SINE -> kotlin.math.sin(t.toDouble()).toFloat()
            WaveType.COSINE -> kotlin.math.cos(t.toDouble()).toFloat()
            WaveType.TRIANGLE -> {
                val normalized = (t % (2 * kotlin.math.PI)).toFloat()
                if (normalized < kotlin.math.PI) {
                    -1 + 2 * normalized / kotlin.math.PI.toFloat()
                } else {
                    3 - 2 * normalized / kotlin.math.PI.toFloat()
                }
            }
            WaveType.SAWTOOTH -> ((t % (2 * kotlin.math.PI)) / kotlin.math.PI - 1).toFloat()
            WaveType.SQUARE -> if ((t % (2 * kotlin.math.PI)) < kotlin.math.PI) 1f else -1f
            WaveType.NOISE -> context.random.nextFloat() * 2 - 1
        }
        
        return mapOf("output" to result * amp)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

enum class WaveType {
    SINE, COSINE, TRIANGLE, SAWTOOTH, SQUARE, NOISE
}

/**
 * Clamp node
 */
data class ClampNode(
    override val id: String
) : EffectNode {
    override val name = "Clamp"
    override val category = NodeCategory.MATH
    override val description = "Clamps a value between min and max"
    override val inputs = listOf(
        NodePort("value", "Value", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("min", "Min", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("max", "Max", PortType.NUMBER, PortDirection.INPUT, 1f)
    )
    override val outputs = listOf(
        NodePort("result", "Result", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val value = (inputs["value"] as? Float) ?: 0f
        val min = (inputs["min"] as? Float) ?: 0f
        val max = (inputs["max"] as? Float) ?: 1f
        return mapOf("result" to value.coerceIn(min, max))
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Smooth step node
 */
data class SmoothStepNode(
    override val id: String
) : EffectNode {
    override val name = "Smooth Step"
    override val category = NodeCategory.MATH
    override val description = "Smooth interpolation between edges"
    override val inputs = listOf(
        NodePort("value", "Value", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("edge0", "Edge 0", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("edge1", "Edge 1", PortType.NUMBER, PortDirection.INPUT, 1f)
    )
    override val outputs = listOf(
        NodePort("result", "Result", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val value = (inputs["value"] as? Float) ?: 0f
        val edge0 = (inputs["edge0"] as? Float) ?: 0f
        val edge1 = (inputs["edge1"] as? Float) ?: 1f
        
        val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        val result = t * t * (3 - 2 * t)
        
        return mapOf("result" to result)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

// ─────────────────────────────────────────────────────────────────────────────────
// PARTICLE NODES
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Particle emitter node
 */
data class ParticleEmitterNode(
    override val id: String
) : EffectNode {
    override val name = "Particle Emitter"
    override val category = NodeCategory.PARTICLE
    override val description = "Emits particles"
    override val inputs = listOf(
        NodePort("position", "Position", PortType.VECTOR2, PortDirection.INPUT),
        NodePort("velocity", "Velocity", PortType.VECTOR2, PortDirection.INPUT),
        NodePort("color", "Color", PortType.COLOR, PortDirection.INPUT),
        NodePort("size", "Size", PortType.NUMBER, PortDirection.INPUT, 10f),
        NodePort("lifetime", "Lifetime", PortType.NUMBER, PortDirection.INPUT, 2f),
        NodePort("rate", "Spawn Rate", PortType.NUMBER, PortDirection.INPUT, 10f)
    )
    override val outputs = listOf(
        NodePort("particles", "Particles", PortType.PARTICLE, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        // Particle data is passed through for the renderer
        return mapOf("particles" to inputs)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Particle force node
 */
data class ParticleForceNode(
    override val id: String
) : EffectNode {
    override val name = "Particle Force"
    override val category = NodeCategory.PARTICLE
    override val description = "Applies forces to particles"
    override val inputs = listOf(
        NodePort("particles", "Particles", PortType.PARTICLE, PortDirection.INPUT),
        NodePort("gravity", "Gravity", PortType.NUMBER, PortDirection.INPUT, 98f),
        NodePort("wind", "Wind", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("turbulence", "Turbulence", PortType.NUMBER, PortDirection.INPUT, 0f)
    )
    override val outputs = listOf(
        NodePort("particles", "Particles", PortType.PARTICLE, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        return mapOf("particles" to inputs)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

// ─────────────────────────────────────────────────────────────────────────────────
// SHADER NODES
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Shader effect node
 */
data class ShaderEffectNode(
    override val id: String,
    val effectType: ShaderEffectType = ShaderEffectType.BLOOM
) : EffectNode {
    override val name = "Shader Effect"
    override val category = NodeCategory.SHADER
    override val description = "Applies shader effects"
    override val inputs = listOf(
        NodePort("intensity", "Intensity", PortType.NUMBER, PortDirection.INPUT, 1f, 0f, 2f),
        NodePort("color", "Color", PortType.COLOR, PortDirection.INPUT),
        NodePort("radius", "Radius", PortType.NUMBER, PortDirection.INPUT, 10f),
        NodePort("threshold", "Threshold", PortType.NUMBER, PortDirection.INPUT, 0.5f, 0f, 1f)
    )
    override val outputs = listOf(
        NodePort("effect", "Effect", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        return mapOf("effect" to mapOf(
            "type" to effectType,
            "params" to inputs
        ))
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

enum class ShaderEffectType {
    BLOOM, VIGNETTE, PIXELATE, KALEIDOSCOPE, CHROMATIC,
    GLITCH, SCANLINES, NOISE, BLUR, SHARPEN,
    EMBOSS, EDGE_DETECT, POSTERIZE, SOLARIZE, THERMAL
}

// ─────────────────────────────────────────────────────────────────────────────────
// LOGIC NODES
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Compare node
 */
data class CompareNode(
    override val id: String,
    val comparison: ComparisonType = ComparisonType.GREATER_THAN
) : EffectNode {
    override val name = "Compare"
    override val category = NodeCategory.LOGIC
    override val description = "Compares two values"
    override val inputs = listOf(
        NodePort("a", "A", PortType.NUMBER, PortDirection.INPUT, 0f),
        NodePort("b", "B", PortType.NUMBER, PortDirection.INPUT, 0f)
    )
    override val outputs = listOf(
        NodePort("result", "Result", PortType.BOOLEAN, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val a = (inputs["a"] as? Float) ?: 0f
        val b = (inputs["b"] as? Float) ?: 0f
        
        val result = when (comparison) {
            ComparisonType.EQUAL -> a == b
            ComparisonType.NOT_EQUAL -> a != b
            ComparisonType.GREATER_THAN -> a > b
            ComparisonType.LESS_THAN -> a < b
            ComparisonType.GREATER_OR_EQUAL -> a >= b
            ComparisonType.LESS_OR_EQUAL -> a <= b
        }
        
        return mapOf("result" to result)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

enum class ComparisonType {
    EQUAL, NOT_EQUAL, GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL
}

/**
 * Branch node (if/else)
 */
data class BranchNode(
    override val id: String
) : EffectNode {
    override val name = "Branch"
    override val category = NodeCategory.LOGIC
    override val description = "Conditional branching"
    override val inputs = listOf(
        NodePort("condition", "Condition", PortType.BOOLEAN, PortDirection.INPUT, true),
        NodePort("trueValue", "True", PortType.NUMBER, PortDirection.INPUT, 1f),
        NodePort("falseValue", "False", PortType.NUMBER, PortDirection.INPUT, 0f)
    )
    override val outputs = listOf(
        NodePort("result", "Result", PortType.NUMBER, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val condition = inputs["condition"] as? Boolean ?: true
        val trueVal = inputs["trueValue"]
        val falseVal = inputs["falseValue"]
        
        return mapOf("result" to if (condition) trueVal else falseVal)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Trigger node
 */
data class TriggerNode(
    override val id: String,
    val triggerType: TriggerType = TriggerType.ON_BEAT
) : EffectNode {
    override val name = "Trigger"
    override val category = NodeCategory.LOGIC
    override val description = "Event triggers"
    override val inputs = listOf(
        NodePort("threshold", "Threshold", PortType.NUMBER, PortDirection.INPUT, 0.5f, 0f, 1f)
    )
    override val outputs = listOf(
        NodePort("triggered", "Triggered", PortType.BOOLEAN, PortDirection.OUTPUT)
    )
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        val threshold = (inputs["threshold"] as? Float) ?: 0.5f
        val audio = context.audioData
        
        val triggered = when (triggerType) {
            TriggerType.ON_BEAT -> audio?.beatDetected ?: false
            TriggerType.BASS_HIGH -> (audio?.bass ?: 0f) > threshold
            TriggerType.TREBLE_HIGH -> (audio?.treble ?: 0f) > threshold
            TriggerType.AMPLITUDE_HIGH -> (audio?.amplitude ?: 0f) > threshold
            TriggerType.TIME_INTERVAL -> (context.time % threshold) < 0.1f
            TriggerType.RANDOM -> context.random.nextFloat() > (1 - threshold)
        }
        
        return mapOf("triggered" to triggered)
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

enum class TriggerType {
    ON_BEAT, BASS_HIGH, TREBLE_HIGH, AMPLITUDE_HIGH, TIME_INTERVAL, RANDOM
}

// ─────────────────────────────────────────────────────────────────────────────────
// OUTPUT NODES
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Final output node
 */
data class OutputNode(
    override val id: String
) : EffectNode {
    override val name = "Output"
    override val category = NodeCategory.OUTPUT
    override val description = "Final effect output"
    override val inputs = listOf(
        NodePort("color", "Color", PortType.COLOR, PortDirection.INPUT),
        NodePort("intensity", "Intensity", PortType.NUMBER, PortDirection.INPUT, 1f, 0f, 2f),
        NodePort("blendMode", "Blend Mode", PortType.NUMBER, PortDirection.INPUT, 0f)
    )
    override val outputs = emptyList<NodePort>()
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        return inputs
    }
    
    override fun clone(newId: String) = copy(id = newId)
}

/**
 * Particle output node
 */
data class ParticleOutputNode(
    override val id: String
) : EffectNode {
    override val name = "Particle Output"
    override val category = NodeCategory.OUTPUT
    override val description = "Final particle output"
    override val inputs = listOf(
        NodePort("particles", "Particles", PortType.PARTICLE, PortDirection.INPUT)
    )
    override val outputs = emptyList<NodePort>()
    
    override fun process(inputs: Map<String, Any?>, context: NodeContext): Map<String, Any?> {
        return inputs
    }
    
    override fun clone(newId: String) = copy(id = newId)
}