package com.prorf.platform.execution

import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.WorkflowGraph
import com.prorf.platform.plugin.PluginRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DagExecutionEngineTest {

    private lateinit var registry: PluginRegistry
    private lateinit var engine: DagExecutionEngine

    @BeforeEach
    fun setup() {
        registry = PluginRegistry()
        engine = DagExecutionEngine(registry)

        // "add" node: takes input "a", adds parameter "value", outputs "result"
        registry.register(
            NodeDefinition(typeId = "add", displayName = "Add"),
            object : NodeExecutor {
                override val typeId = "add"
                override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
                    val a = (inputs["a"] as? Number)?.toDouble() ?: 0.0
                    val v = (parameters["value"] as? Number)?.toDouble() ?: 0.0
                    return mapOf("result" to (a + v))
                }
            }
        )
    }

    @Test
    fun `empty graph produces empty outputs`() {
        val result = engine.execute(WorkflowGraph("g0", "Empty"), ExecutionContext())
        assertTrue(result.isSuccess)
        assertTrue(result.outputs.isEmpty())
    }

    @Test
    fun `single node executes with default input`() {
        val graph = WorkflowGraph(
            id = "g1", name = "Single",
            nodes = listOf(NodeInstance("n1", "add", mapOf("value" to 5.0))),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess, "Expected success, errors: ${result.errors}")
        assertEquals(5.0, result.outputs["n1"]?.get("result"))
    }

    @Test
    fun `chained nodes propagate values in topological order`() {
        // n1 outputs 10.0, n2 adds 5.0 → expect 15.0
        val graph = WorkflowGraph(
            id = "g2", name = "Chain",
            nodes = listOf(
                NodeInstance("n1", "add", mapOf("value" to 10.0)),
                NodeInstance("n2", "add", mapOf("value" to 5.0)),
            ),
            edges = listOf(Edge("e1", "n1", "result", "n2", "a")),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess, "Errors: ${result.errors}")
        assertEquals(15.0, result.outputs["n2"]?.get("result"))
    }

    @Test
    fun `cycle detection returns error and does not throw`() {
        val graph = WorkflowGraph(
            id = "g3", name = "Cycle",
            nodes = listOf(
                NodeInstance("n1", "add", mapOf("value" to 1.0)),
                NodeInstance("n2", "add", mapOf("value" to 1.0)),
            ),
            edges = listOf(
                Edge("e1", "n1", "result", "n2", "a"),
                Edge("e2", "n2", "result", "n1", "a"),
            ),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertFalse(result.isSuccess)
        assertTrue(result.errors.any { "Cycle" in it.message })
    }

    @Test
    fun `unknown node type records error and continues`() {
        val graph = WorkflowGraph(
            id = "g4", name = "MissingType",
            nodes = listOf(NodeInstance("n1", "nonexistent")),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertFalse(result.isSuccess)
        assertTrue(result.errors.any { it.nodeId == "n1" })
    }
}
