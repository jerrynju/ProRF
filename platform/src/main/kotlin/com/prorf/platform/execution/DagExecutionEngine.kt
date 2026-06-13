package com.prorf.platform.execution

import com.prorf.platform.graph.WorkflowGraph
import com.prorf.platform.plugin.PluginRegistry

/**
 * DAG-based execution engine.
 * Topologically sorts the graph, then runs each node's executor in order,
 * threading outputs into downstream inputs.
 */
class DagExecutionEngine(
    private val registry: PluginRegistry,
) : ExecutionEngine {

    override fun execute(graph: WorkflowGraph, context: ExecutionContext): ExecutionResult {
        val order = topologicalSort(graph)
            ?: return ExecutionResult(
                outputs = emptyMap(),
                errors = listOf(ExecutionError("graph", "Cycle detected in workflow graph")),
            )

        val nodeOutputs = mutableMapOf<String, Map<String, Any>>()
        val errors = mutableListOf<ExecutionError>()

        for (nodeId in order) {
            val node = graph.nodes.find { it.id == nodeId } ?: continue
            val executor = registry.getExecutor(node.typeId) ?: run {
                errors += ExecutionError(nodeId, "No executor registered for type '${node.typeId}'")
                continue
            }
            val inputs = collectInputs(graph, nodeId, nodeOutputs)
            try {
                nodeOutputs[nodeId] = executor.execute(inputs, node.parameters)
            } catch (e: Exception) {
                errors += ExecutionError(nodeId, e.message ?: "Execution error")
            }
        }

        return ExecutionResult(outputs = nodeOutputs, errors = errors)
    }

    private fun topologicalSort(graph: WorkflowGraph): List<String>? {
        val inDegree = graph.nodes.associate { it.id to 0 }.toMutableMap()
        val adjacency = graph.nodes.associate { it.id to mutableListOf<String>() }.toMutableMap()

        for (edge in graph.edges) {
            adjacency.getOrPut(edge.fromNodeId) { mutableListOf() }.add(edge.toNodeId)
            inDegree[edge.toNodeId] = (inDegree[edge.toNodeId] ?: 0) + 1
        }

        val queue = ArrayDeque(inDegree.filter { it.value == 0 }.keys.toList())
        val sorted = mutableListOf<String>()

        while (queue.isNotEmpty()) {
            val nodeId = queue.removeFirst()
            sorted += nodeId
            adjacency[nodeId]?.forEach { neighbor ->
                val deg = (inDegree[neighbor] ?: 1) - 1
                inDegree[neighbor] = deg
                if (deg == 0) queue.add(neighbor)
            }
        }

        return if (sorted.size == graph.nodes.size) sorted else null
    }

    private fun collectInputs(
        graph: WorkflowGraph,
        nodeId: String,
        nodeOutputs: Map<String, Map<String, Any>>,
    ): Map<String, Any> = buildMap {
        for (edge in graph.edges) {
            if (edge.toNodeId != nodeId) continue
            val value = nodeOutputs[edge.fromNodeId]?.get(edge.fromPortId) ?: continue
            put(edge.toPortId, value)
        }
    }
}
