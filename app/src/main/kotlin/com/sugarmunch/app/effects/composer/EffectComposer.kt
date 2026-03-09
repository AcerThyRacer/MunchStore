package com.sugarmunch.app.effects.composer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Phase 2: Effect Composer Engine
 * 
 * Manages the node graph, connections, and execution of effect chains.
 * Supports real-time preview, serialization, and preset management.
 */

// ─────────────────────────────────────────────────────────────────────────────────
// NODE CONNECTION
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Represents a connection between two node ports
 */
data class NodeConnection(
    val id: String = UUID.randomUUID().toString(),
    val sourceNodeId: String,
    val sourcePortId: String,
    val targetNodeId: String,
    val targetPortId: String
)

// ─────────────────────────────────────────────────────────────────────────────────
// NODE GRAPH
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Represents a complete effect graph with nodes and connections
 */
data class EffectGraph(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Untitled Effect",
    var description: String = "",
    val nodes: MutableList<EffectNode> = mutableListOf(),
    val connections: MutableList<NodeConnection> = mutableListOf(),
    var createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis()
) {
    fun addNode(node: EffectNode) {
        nodes.add(node)
        modifiedAt = System.currentTimeMillis()
    }
    
    fun removeNode(nodeId: String) {
        nodes.removeAll { it.id == nodeId }
        connections.removeAll { it.sourceNodeId == nodeId || it.targetNodeId == nodeId }
        modifiedAt = System.currentTimeMillis()
    }
    
    fun addConnection(connection: NodeConnection): Boolean {
        // Validate connection
        val sourceNode = nodes.find { it.id == connection.sourceNodeId } ?: return false
        val targetNode = nodes.find { it.id == connection.targetNodeId } ?: return false
        
        val sourcePort = sourceNode.outputs.find { it.id == connection.sourcePortId } ?: return false
        val targetPort = targetNode.inputs.find { it.id == connection.targetPortId } ?: return false
        
        // Check type compatibility
        if (!areTypesCompatible(sourcePort.type, targetPort.type)) return false
        
        // Remove existing connection to this input
        connections.removeAll { 
            it.targetNodeId == connection.targetNodeId && it.targetPortId == connection.targetPortId 
        }
        
        connections.add(connection)
        modifiedAt = System.currentTimeMillis()
        return true
    }
    
    fun removeConnection(connectionId: String) {
        connections.removeAll { it.id == connectionId }
        modifiedAt = System.currentTimeMillis()
    }
    
    private fun areTypesCompatible(source: PortType, target: PortType): Boolean {
        if (source == target) return true
        // Allow number to be connected to most numeric inputs
        return when (source) {
            PortType.NUMBER -> target in listOf(PortType.NUMBER, PortType.BOOLEAN)
            else -> false
        }
    }
    
    /**
     * Get the execution order (topological sort)
     */
    fun getExecutionOrder(): List<EffectNode> {
        val result = mutableListOf<EffectNode>()
        val visited = mutableSetOf<String>()
        val visiting = mutableSetOf<String>()
        
        fun visit(node: EffectNode) {
            if (node.id in visited) return
            if (node.id in visiting) return // Cycle detected, skip
            
            visiting.add(node.id)
            
            // Visit dependencies first (nodes connected to our inputs)
            for (conn in connections.filter { it.targetNodeId == node.id }) {
                val depNode = nodes.find { it.id == conn.sourceNodeId }
                depNode?.let { visit(it) }
            }
            
            visiting.remove(node.id)
            visited.add(node.id)
            result.add(node)
        }
        
        // Start from output nodes
        for (node in nodes.filter { it.category == NodeCategory.OUTPUT }) {
            visit(node)
        }
        
        // Also visit any unconnected nodes
        for (node in nodes) {
            visit(node)
        }
        
        return result
    }
    
    /**
     * Serialize to JSON-compatible map
     */
    fun serialize(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "nodes" to nodes.map { serializeNode(it) },
            "connections" to connections.map { mapOf(
                "id" to it.id,
                "sourceNodeId" to it.sourceNodeId,
                "sourcePortId" to it.sourcePortId,
                "targetNodeId" to it.targetNodeId,
                "targetPortId" to it.targetPortId
            )},
            "createdAt" to createdAt,
            "modifiedAt" to modifiedAt
        )
    }
    
    private fun serializeNode(node: EffectNode): Map<String, Any?> {
        val data = mutableMapOf<String, Any?>(
            "id" to node.id,
            "type" to node::class.simpleName,
            "name" to node.name
        )
        
        // Add type-specific properties
        when (node) {
            is TimeInputNode -> data["speedMultiplier"] = node.speedMultiplier
            is ColorNode -> data["color"] = colorToHex(node.color)
            is ColorBlendNode -> data["blendMode"] = node.blendMode.name
            is MathNode -> data["operation"] = node.operation.name
            is WaveNode -> {
                data["waveType"] = node.waveType.name
            }
            is ShaderEffectNode -> data["effectType"] = node.effectType.name
            is CompareNode -> data["comparison"] = node.comparison.name
            is TriggerNode -> data["triggerType"] = node.triggerType.name
            is RandomInputNode -> {
                data["min"] = node.min
                data["max"] = node.max
                data["seed"] = node.seed
            }
            is GradientNode -> data["colors"] = node.colors.map { colorToHex(it) }
        }
        
        return data
    }
    
    private fun colorToHex(color: Color): String {
        return String.format("#%08X", 
            (color.alpha * 255).toInt().shl(24) or
            (color.red * 255).toInt().shl(16) or
            (color.green * 255).toInt().shl(8) or
            (color.blue * 255).toInt()
        )
    }
    
    companion object {
        fun deserialize(data: Map<String, Any?>): EffectGraph {
            val graph = EffectGraph(
                id = data["id"] as? String ?: UUID.randomUUID().toString(),
                name = data["name"] as? String ?: "Untitled",
                description = data["description"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                modifiedAt = (data["modifiedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
            
            // Deserialize nodes
            @Suppress("UNCHECKED_CAST")
            val nodesData = data["nodes"] as? List<Map<String, Any?>> ?: emptyList()
            for (nodeData in nodesData) {
                val node = deserializeNode(nodeData)
                if (node != null) graph.nodes.add(node)
            }
            
            // Deserialize connections
            @Suppress("UNCHECKED_CAST")
            val connectionsData = data["connections"] as? List<Map<String, Any?>> ?: emptyList()
            for (connData in connectionsData) {
                graph.connections.add(NodeConnection(
                    id = connData["id"] as? String ?: UUID.randomUUID().toString(),
                    sourceNodeId = connData["sourceNodeId"] as? String ?: "",
                    sourcePortId = connData["sourcePortId"] as? String ?: "",
                    targetNodeId = connData["targetNodeId"] as? String ?: "",
                    targetPortId = connData["targetPortId"] as? String ?: ""
                ))
            }
            
            return graph
        }
        
        private fun deserializeNode(data: Map<String, Any?>): EffectNode? {
            val id = data["id"] as? String ?: return null
            val type = data["type"] as? String ?: return null
            
            return when (type) {
                "TimeInputNode" -> TimeInputNode(id, (data["speedMultiplier"] as? Number)?.toFloat() ?: 1f)
                "TouchInputNode" -> TouchInputNode(id)
                "AudioInputNode" -> AudioInputNode(id)
                "ScreenInputNode" -> ScreenInputNode(id)
                "RandomInputNode" -> RandomInputNode(
                    id,
                    (data["min"] as? Number)?.toFloat() ?: 0f,
                    (data["max"] as? Number)?.toFloat() ?: 1f,
                    (data["seed"] as? Number)?.toLong()
                )
                "ColorNode" -> ColorNode(id, hexToColor(data["color"] as? String))
                "ColorBlendNode" -> ColorBlendNode(
                    id,
                    try { ColorBlendMode.valueOf(data["blendMode"] as? String ?: "NORMAL") } 
                    catch (e: Exception) { ColorBlendMode.NORMAL }
                )
                "HSVColorNode" -> HSVColorNode(id)
                "GradientNode" -> GradientNode(
                    id,
                    @Suppress("UNCHECKED_CAST")
                    (data["colors"] as? List<String>)?.map { hexToColor(it) } ?: listOf(Color.Red, Color.Blue)
                )
                "MathNode" -> MathNode(
                    id,
                    try { MathOperation.valueOf(data["operation"] as? String ?: "ADD") }
                    catch (e: Exception) { MathOperation.ADD }
                )
                "WaveNode" -> WaveNode(
                    id,
                    try { WaveType.valueOf(data["waveType"] as? String ?: "SINE") }
                    catch (e: Exception) { WaveType.SINE }
                )
                "ClampNode" -> ClampNode(id)
                "SmoothStepNode" -> SmoothStepNode(id)
                "ParticleEmitterNode" -> ParticleEmitterNode(id)
                "ParticleForceNode" -> ParticleForceNode(id)
                "ShaderEffectNode" -> ShaderEffectNode(
                    id,
                    try { ShaderEffectType.valueOf(data["effectType"] as? String ?: "BLOOM") }
                    catch (e: Exception) { ShaderEffectType.BLOOM }
                )
                "CompareNode" -> CompareNode(
                    id,
                    try { ComparisonType.valueOf(data["comparison"] as? String ?: "GREATER_THAN") }
                    catch (e: Exception) { ComparisonType.GREATER_THAN }
                )
                "BranchNode" -> BranchNode(id)
                "TriggerNode" -> TriggerNode(
                    id,
                    try { TriggerType.valueOf(data["triggerType"] as? String ?: "ON_BEAT") }
                    catch (e: Exception) { TriggerType.ON_BEAT }
                )
                "OutputNode" -> OutputNode(id)
                "ParticleOutputNode" -> ParticleOutputNode(id)
                else -> null
            }
        }
        
        private fun hexToColor(hex: String?): Color {
            if (hex == null) return Color.White
            try {
                val cleanHex = hex.removePrefix("#")
                val colorValue = cleanHex.toLong(16)
                return when (cleanHex.length) {
                    8 -> Color(
                        ((colorValue shr 16) and 0xFF) / 255f,
                        ((colorValue shr 8) and 0xFF) / 255f,
                        (colorValue and 0xFF) / 255f,
                        ((colorValue shr 24) and 0xFF) / 255f
                    )
                    6 -> Color(
                        ((colorValue shr 16) and 0xFF) / 255f,
                        ((colorValue shr 8) and 0xFF) / 255f,
                        (colorValue and 0xFF) / 255f
                    )
                    else -> Color.White
                }
            } catch (e: Exception) {
                return Color.White
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// EFFECT COMPOSER ENGINE
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Main engine for managing and executing effect graphs
 */
class EffectComposerEngine {
    private val _graphs = MutableStateFlow<List<EffectGraph>>(emptyList())
    val graphs: StateFlow<List<EffectGraph>> = _graphs.asStateFlow()
    
    private val _activeGraph = MutableStateFlow<EffectGraph?>(null)
    val activeGraph: StateFlow<EffectGraph?> = _activeGraph.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private var lastContext: NodeContext? = null
    
    /**
     * Create a new effect graph
     */
    fun createGraph(name: String = "New Effect"): EffectGraph {
        val graph = EffectGraph(name = name)
        _graphs.value = _graphs.value + graph
        return graph
    }
    
    /**
     * Load an existing graph
     */
    fun loadGraph(graph: EffectGraph) {
        if (!_graphs.value.any { it.id == graph.id }) {
            _graphs.value = _graphs.value + graph
        }
    }
    
    /**
     * Set the active graph for preview/execution
     */
    fun setActiveGraph(graph: EffectGraph?) {
        _activeGraph.value = graph
    }
    
    /**
     * Delete a graph
     */
    fun deleteGraph(graphId: String) {
        _graphs.value = _graphs.value.filter { it.id != graphId }
        if (_activeGraph.value?.id == graphId) {
            _activeGraph.value = null
        }
    }
    
    /**
     * Start playback
     */
    fun play() {
        _isPlaying.value = true
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        _isPlaying.value = false
    }
    
    /**
     * Execute the active graph with the given context
     */
    fun execute(context: NodeContext): Map<String, Any?> {
        val graph = _activeGraph.value ?: return emptyMap()
        lastContext = context
        
        val executionOrder = graph.getExecutionOrder()
        val nodeOutputs = mutableMapOf<String, Map<String, Any?>>()
        
        for (node in executionOrder) {
            // Gather inputs from connected nodes
            val inputs = mutableMapOf<String, Any?>()
            
            for (port in node.inputs) {
                val connection = graph.connections.find { 
                    it.targetNodeId == node.id && it.targetPortId == port.id 
                }
                
                if (connection != null) {
                    val sourceOutput = nodeOutputs[connection.sourceNodeId]
                    if (sourceOutput != null) {
                        inputs[port.id] = sourceOutput[connection.sourcePortId] ?: port.defaultValue
                    } else {
                        inputs[port.id] = port.defaultValue
                    }
                } else {
                    inputs[port.id] = port.defaultValue
                }
            }
            
            // Process the node
            nodeOutputs[node.id] = node.process(inputs, context)
        }
        
        // Return output from output nodes
        val results = mutableMapOf<String, Any?>()
        for (node in executionOrder.filter { it.category == NodeCategory.OUTPUT }) {
            results.putAll(nodeOutputs[node.id] ?: emptyMap())
        }
        
        return results
    }
    
    /**
     * Get available node templates for creating new nodes
     */
    fun getNodeTemplates(): List<NodeTemplate> {
        return listOf(
            // Input nodes
            NodeTemplate("Time", NodeCategory.INPUT, "Time input") { TimeInputNode(it) },
            NodeTemplate("Touch", NodeCategory.INPUT, "Touch input") { TouchInputNode(it) },
            NodeTemplate("Audio", NodeCategory.INPUT, "Audio input") { AudioInputNode(it) },
            NodeTemplate("Screen", NodeCategory.INPUT, "Screen dimensions") { ScreenInputNode(it) },
            NodeTemplate("Random", NodeCategory.INPUT, "Random values") { RandomInputNode(it) },
            
            // Color nodes
            NodeTemplate("Color", NodeCategory.COLOR, "Solid color") { ColorNode(it) },
            NodeTemplate("Color Blend", NodeCategory.COLOR, "Blend colors") { ColorBlendNode(it) },
            NodeTemplate("HSV Color", NodeCategory.COLOR, "HSV to RGB") { HSVColorNode(it) },
            NodeTemplate("Gradient", NodeCategory.COLOR, "Color gradient") { GradientNode(it) },
            
            // Math nodes
            NodeTemplate("Math", NodeCategory.MATH, "Math operations") { MathNode(it) },
            NodeTemplate("Wave", NodeCategory.MATH, "Wave functions") { WaveNode(it) },
            NodeTemplate("Clamp", NodeCategory.MATH, "Clamp value") { ClampNode(it) },
            NodeTemplate("Smooth Step", NodeCategory.MATH, "Smooth interpolation") { SmoothStepNode(it) },
            
            // Particle nodes
            NodeTemplate("Particle Emitter", NodeCategory.PARTICLE, "Emit particles") { ParticleEmitterNode(it) },
            NodeTemplate("Particle Force", NodeCategory.PARTICLE, "Apply forces") { ParticleForceNode(it) },
            
            // Shader nodes
            NodeTemplate("Shader Effect", NodeCategory.SHADER, "Shader effects") { ShaderEffectNode(it) },
            
            // Logic nodes
            NodeTemplate("Compare", NodeCategory.LOGIC, "Compare values") { CompareNode(it) },
            NodeTemplate("Branch", NodeCategory.LOGIC, "If/else") { BranchNode(it) },
            NodeTemplate("Trigger", NodeCategory.LOGIC, "Event triggers") { TriggerNode(it) },
            
            // Output nodes
            NodeTemplate("Output", NodeCategory.OUTPUT, "Effect output") { OutputNode(it) },
            NodeTemplate("Particle Output", NodeCategory.OUTPUT, "Particle output") { ParticleOutputNode(it) }
        )
    }
    
    /**
     * Create a node from a template
     */
    fun createNodeFromTemplate(template: NodeTemplate): EffectNode {
        return template.factory(UUID.randomUUID().toString())
    }
    
    companion object {
        @Volatile
        private var instance: EffectComposerEngine? = null
        
        fun getInstance(): EffectComposerEngine {
            return instance ?: synchronized(this) {
                instance ?: EffectComposerEngine().also { instance = it }
            }
        }
    }
}

/**
 * Template for creating new nodes
 */
data class NodeTemplate(
    val name: String,
    val category: NodeCategory,
    val description: String,
    val factory: (String) -> EffectNode
)

// ─────────────────────────────────────────────────────────────────────────────────
// PRESET EFFECT GRAPHS
// ─────────────────────────────────────────────────────────────────────────────────

object EffectPresets {
    
    /**
     * Create a music-reactive color effect
     */
    fun createMusicReactiveEffect(): EffectGraph {
        val graph = EffectGraph(name = "Music Reactive Colors")
        
        // Audio input
        val audioNode = AudioInputNode("audio_1")
        graph.addNode(audioNode)
        
        // HSV color driven by bass
        val hsvNode = HSVColorNode("hsv_1")
        graph.addNode(hsvNode)
        
        // Time for hue cycling
        val timeNode = TimeInputNode("time_1", 0.5f)
        graph.addNode(timeNode)
        
        // Output
        val outputNode = OutputNode("output_1")
        graph.addNode(outputNode)
        
        // Connect: time -> hue
        graph.addConnection(NodeConnection(
            sourceNodeId = "time_1", sourcePortId = "time",
            targetNodeId = "hsv_1", targetPortId = "hue"
        ))
        
        // Connect: audio bass -> saturation
        graph.addConnection(NodeConnection(
            sourceNodeId = "audio_1", sourcePortId = "bass",
            targetNodeId = "hsv_1", targetPortId = "saturation"
        ))
        
        // Connect: audio amplitude -> value
        graph.addConnection(NodeConnection(
            sourceNodeId = "audio_1", sourcePortId = "amplitude",
            targetNodeId = "hsv_1", targetPortId = "value"
        ))
        
        // Connect: color -> output
        graph.addConnection(NodeConnection(
            sourceNodeId = "hsv_1", sourcePortId = "color",
            targetNodeId = "output_1", targetPortId = "color"
        ))
        
        // Connect: bass -> intensity
        graph.addConnection(NodeConnection(
            sourceNodeId = "audio_1", sourcePortId = "bass",
            targetNodeId = "output_1", targetPortId = "intensity"
        ))
        
        return graph
    }
    
    /**
     * Create a pulsing glow effect
     */
    fun createPulsingGlowEffect(): EffectGraph {
        val graph = EffectGraph(name = "Pulsing Glow")
        
        val timeNode = TimeInputNode("time_1", 2f)
        graph.addNode(timeNode)
        
        val colorNode = ColorNode("color_1", Color(0xFFFFB6C1))
        graph.addNode(colorNode)
        
        val waveNode = WaveNode("wave_1", WaveType.SINE)
        graph.addNode(waveNode)
        
        val outputNode = OutputNode("output_1")
        graph.addNode(outputNode)
        
        // Connect time to wave
        graph.addConnection(NodeConnection(
            sourceNodeId = "time_1", sourcePortId = "time",
            targetNodeId = "wave_1", targetPortId = "input"
        ))
        
        // Connect wave to intensity
        graph.addConnection(NodeConnection(
            sourceNodeId = "wave_1", sourcePortId = "output",
            targetNodeId = "output_1", targetPortId = "intensity"
        ))
        
        // Connect color to output
        graph.addConnection(NodeConnection(
            sourceNodeId = "color_1", sourcePortId = "color",
            targetNodeId = "output_1", targetPortId = "color"
        ))
        
        return graph
    }
    
    /**
     * Create a rainbow wave effect
     */
    fun createRainbowWaveEffect(): EffectGraph {
        val graph = EffectGraph(name = "Rainbow Wave")
        
        val timeNode = TimeInputNode("time_1", 1f)
        graph.addNode(timeNode)
        
        val mathNode = MathNode("math_1", MathOperation.MULTIPLY)
        graph.addNode(mathNode)
        
        val hsvNode = HSVColorNode("hsv_1")
        graph.addNode(hsvNode)
        
        val outputNode = OutputNode("output_1")
        graph.addNode(outputNode)
        
        // Connect time * 60 for hue
        graph.addConnection(NodeConnection(
            sourceNodeId = "time_1", sourcePortId = "time",
            targetNodeId = "math_1", targetPortId = "a"
        ))
        
        // Connect: math result -> hue
        graph.addConnection(NodeConnection(
            sourceNodeId = "math_1", sourcePortId = "result",
            targetNodeId = "hsv_1", targetPortId = "hue"
        ))
        
        // Connect: color -> output
        graph.addConnection(NodeConnection(
            sourceNodeId = "hsv_1", sourcePortId = "color",
            targetNodeId = "output_1", targetPortId = "color"
        ))
        
        return graph
    }
    
    /**
     * Create a touch ripple effect
     */
    fun createTouchRippleEffect(): EffectGraph {
        val graph = EffectGraph(name = "Touch Ripple")
        
        val touchNode = TouchInputNode("touch_1")
        graph.addNode(touchNode)
        
        val timeNode = TimeInputNode("time_1", 1f)
        graph.addNode(timeNode)
        
        val waveNode = WaveNode("wave_1", WaveType.SINE)
        graph.addNode(waveNode)
        
        val colorNode = ColorNode("color_1", Color(0xFF4FC3F7))
        graph.addNode(colorNode)
        
        val outputNode = OutputNode("output_1")
        graph.addNode(outputNode)
        
        // Connect time to wave
        graph.addConnection(NodeConnection(
            sourceNodeId = "time_1", sourcePortId = "time",
            targetNodeId = "wave_1", targetPortId = "input"
        ))
        
        // Connect wave to intensity
        graph.addConnection(NodeConnection(
            sourceNodeId = "wave_1", sourcePortId = "output",
            targetNodeId = "output_1", targetPortId = "intensity"
        ))
        
        // Connect color to output
        graph.addConnection(NodeConnection(
            sourceNodeId = "color_1", sourcePortId = "color",
            targetNodeId = "output_1", targetPortId = "color"
        ))
        
        return graph
    }
    
    /**
     * Create a beat-triggered flash effect
     */
    fun createBeatFlashEffect(): EffectGraph {
        val graph = EffectGraph(name = "Beat Flash")
        
        val audioNode = AudioInputNode("audio_1")
        graph.addNode(audioNode)
        
        val triggerNode = TriggerNode("trigger_1", TriggerType.ON_BEAT)
        graph.addNode(triggerNode)
        
        val colorNode = ColorNode("color_1", Color.White)
        graph.addNode(colorNode)
        
        val branchNode = BranchNode("branch_1")
        graph.addNode(branchNode)
        
        val outputNode = OutputNode("output_1")
        graph.addNode(outputNode)
        
        // Connect beat trigger
        graph.addConnection(NodeConnection(
            sourceNodeId = "audio_1", sourcePortId = "amplitude",
            targetNodeId = "trigger_1", targetPortId = "threshold"
        ))
        
        // Connect trigger to branch
        graph.addConnection(NodeConnection(
            sourceNodeId = "trigger_1", sourcePortId = "triggered",
            targetNodeId = "branch_1", targetPortId = "condition"
        ))
        
        // Connect color to output
        graph.addConnection(NodeConnection(
            sourceNodeId = "color_1", sourcePortId = "color",
            targetNodeId = "output_1", targetPortId = "color"
        ))
        
        // Connect branch to intensity
        graph.addConnection(NodeConnection(
            sourceNodeId = "branch_1", sourcePortId = "result",
            targetNodeId = "output_1", targetPortId = "intensity"
        ))
        
        return graph
    }
    
    /**
     * Get all preset effects
     */
    fun getAllPresets(): List<EffectGraph> {
        return listOf(
            createMusicReactiveEffect(),
            createPulsingGlowEffect(),
            createRainbowWaveEffect(),
            createTouchRippleEffect(),
            createBeatFlashEffect()
        )
    }
}