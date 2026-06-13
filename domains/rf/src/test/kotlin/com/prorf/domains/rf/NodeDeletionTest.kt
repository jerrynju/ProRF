package com.prorf.domains.rf

import com.prorf.domains.rf.nodes.Amplifier
import com.prorf.domains.rf.nodes.Attenuator
import com.prorf.domains.rf.nodes.SignalSource
import com.prorf.platform.execution.DagExecutionEngine
import com.prorf.platform.execution.ExecutionContext
import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.WorkflowGraph
import com.prorf.platform.plugin.PluginRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Verifies that removing a node (plus its connected edges) from the graph
 * correctly breaks signal flow. All tests simulate what
 * WorkflowEditorViewModel.deleteNode() does: filter nodes + incident edges, then re-execute.
 */
class NodeDeletionTest {

    private lateinit var engine: DagExecutionEngine

    @BeforeEach
    fun setup() {
        val registry = PluginRegistry()
        RfDomainPlugin.register(registry)
        engine = DagExecutionEngine(registry)
    }

    private fun deleteNode(graph: WorkflowGraph, nodeId: String): WorkflowGraph =
        graph.copy(
            nodes = graph.nodes.filter { it.id != nodeId },
            edges = graph.edges.filter { it.fromNodeId != nodeId && it.toNodeId != nodeId },
        )

    @Test
    fun `deleting a node removes it from execution outputs`() {
        val graph = WorkflowGraph(
            id = "del-node-1", name = "Del Node",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(10.0))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(10.0), "nfDb" to nfDb(3.0))),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "amp", "rf_in")),
        )
        val before = engine.execute(graph, ExecutionContext())
        assertNotNull(before.outputs["amp"], "amp should execute before deletion")

        val trimmed = deleteNode(graph, "amp")
        val after = engine.execute(trimmed, ExecutionContext())
        assertNull(after.outputs["amp"], "amp should not appear in outputs after deletion")
        assertNotNull(after.outputs["src"], "src should still execute unaffected")
    }

    @Test
    fun `deleting a middle node removes all its incident edges`() {
        val graph = WorkflowGraph(
            id = "del-node-2", name = "Del Middle Node",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(10.0))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(5.0), "nfDb" to nfDb(2.0))),
                NodeInstance("att", Attenuator.TYPE_ID, mapOf("lossDb" to gDb(2.0))),
            ),
            edges = listOf(
                Edge("e1", "src", "rf_out", "amp", "rf_in"),
                Edge("e2", "amp", "rf_out", "att", "rf_in"),
            ),
        )
        val trimmed = deleteNode(graph, "amp")
        assertEquals(0, trimmed.edges.size, "Both edges touching amp must be removed")
        assertEquals(2, trimmed.nodes.size, "Only amp is removed from nodes")
    }

    @Test
    fun `deleting middle node breaks downstream propagation`() {
        val srcPower = 10.0
        val gainDb = 8.0
        val lossDb = 3.0
        val graph = WorkflowGraph(
            id = "del-node-3", name = "Del Propagation",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(srcPower))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(gainDb), "nfDb" to nfDb(2.0))),
                NodeInstance("att", Attenuator.TYPE_ID, mapOf("lossDb" to gDb(lossDb))),
            ),
            edges = listOf(
                Edge("e1", "src", "rf_out", "amp", "rf_in"),
                Edge("e2", "amp", "rf_out", "att", "rf_in"),
            ),
        )
        val before = engine.execute(graph, ExecutionContext())
        val attBefore = before.outputValue("att", "rf_out")
        assertEquals(srcPower + gainDb - lossDb, attBefore!!, 0.001)

        val trimmed = deleteNode(graph, "amp")
        val after = engine.execute(trimmed, ExecutionContext())

        // att is now disconnected — should fall back to its standalone output (input = 0)
        val attAfter = after.outputValue("att", "rf_out")
        assertNotNull(attAfter)
        assertNotEquals(srcPower + gainDb - lossDb, attAfter!!, 0.001, "att should not receive amp output")
    }

    @Test
    fun `deleting a nonexistent node id is a no-op`() {
        val graph = WorkflowGraph(
            id = "del-node-4", name = "Del Noop",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(5.0))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(5.0), "nfDb" to nfDb(3.0))),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "amp", "rf_in")),
        )
        val before = engine.execute(graph, ExecutionContext())

        val same = deleteNode(graph, "does-not-exist")
        assertEquals(graph.nodes.size, same.nodes.size)
        assertEquals(graph.edges.size, same.edges.size)

        val after = engine.execute(same, ExecutionContext())
        val ampBefore = before.outputValue("amp", "rf_out")
        val ampAfter = after.outputValue("amp", "rf_out")
        assertEquals(ampBefore!!, ampAfter!!, 0.001)
    }
}
