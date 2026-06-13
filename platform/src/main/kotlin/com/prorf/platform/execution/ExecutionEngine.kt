package com.prorf.platform.execution

import com.prorf.platform.graph.WorkflowGraph

interface ExecutionEngine {
    fun execute(graph: WorkflowGraph, context: ExecutionContext): ExecutionResult
}

data class ExecutionContext(
    val parameters: Map<String, Any> = emptyMap(),
)

data class ExecutionResult(
    val outputs: Map<String, Map<String, Any>>,
    val errors: List<ExecutionError> = emptyList(),
) {
    val isSuccess: Boolean get() = errors.isEmpty()
}

data class ExecutionError(
    val nodeId: String,
    val message: String,
)
