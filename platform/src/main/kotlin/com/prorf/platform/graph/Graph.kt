package com.prorf.platform.graph

/**
 * Immutable workflow graph — the canonical data model for a saved workflow.
 * This is the structure that gets serialized to disk and diffed between saves.
 */
data class WorkflowGraph(
    val id: String,
    val name: String,
    val nodes: List<NodeInstance> = emptyList(),
    val edges: List<Edge> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
