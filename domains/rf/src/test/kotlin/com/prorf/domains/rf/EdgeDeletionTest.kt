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
 * Verifies that removing an edge from the graph correctly breaks signal flow.
 * All tests simulate what WorkflowEditorViewModel.deleteEdge() does:
 * filter the edge list and re-execute.
 */
class EdgeDeletionTest {

    private lateinit var engine: DagExecutionEngine

    @BeforeEach
    fun setup() {
        val registry = PluginRegistry()
        RfDomainPlugin.register(registry)
        engine = DagExecutionEngine(registry)
    }

    @Test
    fun `deleting the only edge breaks signal propagation`() {
        val srcPowerDbm = 20.0
        val gainDb = 10.0
        val before = WorkflowGraph(
            id = "del-test", name = "Del Test",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(srcPowerDbm))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(gainDb), "nfDb" to nfDb(3.0))),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "amp", "rf_in")),
        )
        val beforeResult = engine.execute(before, ExecutionContext())
        val ampBefore = beforeResult.outputValue("amp", "rf_out")
        assertEquals(srcPowerDbm + gainDb, ampBefore!!, 0.001, "Before deletion should propagate")

        // Simulate deleteEdge("e1")
        val after = before.copy(edges = before.edges.filter { it.id != "e1" })
        val afterResult = engine.execute(after, ExecutionContext())
        val ampAfter = afterResult.outputValue("amp", "rf_out")
        assertNotNull(ampAfter)
        assertNotEquals(srcPowerDbm + gainDb, ampAfter!!, 0.001, "After deletion should not propagate from source")
    }

    @Test
    fun `deleting middle edge breaks downstream but not upstream`() {
        val srcPower = 10.0
        val graph = WorkflowGraph(
            id = "mid-del", name = "Mid Del",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(srcPower))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(10.0), "nfDb" to nfDb(2.0))),
                NodeInstance("att", Attenuator.TYPE_ID, mapOf("lossDb" to gDb(3.0))),
            ),
            edges = listOf(
                Edge("e1", "src", "rf_out", "amp", "rf_in"),
                Edge("e2", "amp", "rf_out", "att", "rf_in"),
            ),
        )
        val full = engine.execute(graph, ExecutionContext())
        val attFull = full.outputValue("att", "rf_out")
        assertEquals(srcPower + 10.0 - 3.0, attFull!!, 0.001)

        // Delete the amp→att edge
        val trimmed = graph.copy(edges = graph.edges.filter { it.id != "e2" })
        val partial = engine.execute(trimmed, ExecutionContext())

        // src→amp still connected, amp should still compute
        val ampOut = partial.outputValue("amp", "rf_out")
        assertEquals(srcPower + 10.0, ampOut!!, 0.001, "Upstream amp still receives src signal")

        // att is now disconnected — its output should not equal the chained value
        val attOut = partial.outputValue("att", "rf_out")
        assertNotNull(attOut)
        assertNotEquals(srcPower + 10.0 - 3.0, attOut!!, 0.001, "att should not receive amp output after edge deletion")
    }

    @Test
    fun `deleting nonexistent edge id is a no-op`() {
        val graph = WorkflowGraph(
            id = "noop-del", name = "Noop Del",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(5.0))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(5.0), "nfDb" to nfDb(3.0))),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "amp", "rf_in")),
        )
        val before = engine.execute(graph, ExecutionContext())

        // Delete an id that doesn't exist
        val same = graph.copy(edges = graph.edges.filter { it.id != "does-not-exist" })
        assertEquals(graph.edges.size, same.edges.size, "Edge list should be unchanged")

        val after = engine.execute(same, ExecutionContext())
        val ampBefore = before.outputValue("amp", "rf_out")
        val ampAfter = after.outputValue("amp", "rf_out")
        assertEquals(ampBefore!!, ampAfter!!, 0.001, "No-op delete should not change execution output")
    }
}
