package com.prorf.platform.plugin

import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition

/**
 * Central registry for node definitions and their executors.
 * Domain packs (L3) call register() during application startup.
 * Platform code (L0) queries this registry — no RF knowledge needed.
 */
class PluginRegistry {
    private val definitions = mutableMapOf<String, NodeDefinition>()
    private val executors = mutableMapOf<String, NodeExecutor>()

    fun register(definition: NodeDefinition, executor: NodeExecutor) {
        require(definition.typeId == executor.typeId) {
            "Definition typeId '${definition.typeId}' must match executor typeId '${executor.typeId}'"
        }
        definitions[definition.typeId] = definition
        executors[definition.typeId] = executor
    }

    fun getDefinition(typeId: String): NodeDefinition? = definitions[typeId]
    fun getExecutor(typeId: String): NodeExecutor? = executors[typeId]
    fun allDefinitions(): List<NodeDefinition> = definitions.values.toList()
}
