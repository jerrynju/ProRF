package com.prorf.platform.graph

/**
 * L0 Platform Core — static schema for a node type.
 * Contains NO RF concepts, NO business logic, NO UI logic.
 */
data class NodeDefinition(
    val typeId: String,
    val displayName: String,
    val description: String = "",
    val inputs: List<PortDefinition> = emptyList(),
    val outputs: List<PortDefinition> = emptyList(),
    val parameters: List<ParameterDefinition> = emptyList(),
)

data class PortDefinition(
    val id: String,
    val name: String,
    val dataType: String,
)

data class ParameterDefinition(
    val key: String,
    val displayName: String,
    val dataType: String,
    val defaultValue: Any? = null,
)

/**
 * Runtime instance of a node in a workflow graph.
 * Contains only: type reference, parameter values, layout position, optional user label.
 * No computation, no domain knowledge.
 */
data class NodeInstance(
    val id: String,
    val typeId: String,
    val parameters: Map<String, Any> = emptyMap(),
    val position: NodePosition = NodePosition(),
    val label: String? = null,
)

data class NodePosition(val x: Float = 0f, val y: Float = 0f)
