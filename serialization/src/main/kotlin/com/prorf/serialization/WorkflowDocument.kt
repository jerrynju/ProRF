package com.prorf.serialization

import kotlinx.serialization.Serializable

/**
 * Stable on-disk format for a workflow graph.
 * schemaVersion must be incremented whenever a breaking change is made.
 * All migration logic lives in WorkflowSerializer.migrate().
 */
@Serializable
data class WorkflowDocument(
    val schemaVersion: Int = CURRENT_VERSION,
    val nodes: List<NodeDocument> = emptyList(),
    val connections: List<ConnectionDocument> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class NodeDocument(
    val id: String,
    val typeId: String,
    val parameters: Map<String, String> = emptyMap(),
    val x: Float = 0f,
    val y: Float = 0f,
    val label: String? = null,
)

@Serializable
data class ConnectionDocument(
    val id: String,
    val fromNodeId: String,
    val fromPortId: String,
    val toNodeId: String,
    val toPortId: String,
)
