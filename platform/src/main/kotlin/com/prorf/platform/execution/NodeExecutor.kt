package com.prorf.platform.execution

/**
 * L0 Platform Core — pure computation contract for a node type.
 * No domain knowledge, no side effects, no UI.
 * Implementations live in domain packs (L3).
 */
interface NodeExecutor {
    val typeId: String

    /**
     * Execute this node given its resolved inputs and configured parameters.
     * Returns a map of output portId → computed value.
     * Must be a pure function: same inputs → same outputs.
     */
    fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any>
}
