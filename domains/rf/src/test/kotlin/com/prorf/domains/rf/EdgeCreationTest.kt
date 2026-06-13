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
 * Verifies incremental edge addition — the core graph mutation that the
 * edge-creation UI triggers. Tests that isolated nodes produce no propagation,
 * that adding a first edge enables flow, and that chaining edges is correct.
 */
class EdgeCreationTest {

    private lateinit var engine: DagExecutionEngine

    @BeforeEach
    fun setup() {
        val registry = PluginRegistry()
        RfDomainPlugin.register(registry)
        engine = DagExecutionEngine(registry)
    }

    @Test
    fun `isolated nodes produce no cross-propagation`() {
        val graph = WorkflowGraph(
            id = "isolated", name = "Isolated",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(30.0))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(20.0), "nfDb" to nfDb(3.0))),
            ),
            edges = emptyList(),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess)
        val ampOut = result.outputValue("amp", "rf_out")
        // No edge → amp receives no input, defaults to 0 + 20 = 20 dBm (gain applied to 0)
        // The amp output should NOT equal 30 + 20 since there is no connection
        assertNotNull(ampOut)
        assertNotEquals(50.0, ampOut!!, 0.001, "No edge should mean no signal propagation from source")
    }

    @Test
    fun `adding edge from source to amplifier propagates power correctly`() {
        val srcPowerDbm = 10.0
        val gainDb = 15.0
        val graph = WorkflowGraph(
            id = "one-edge", name = "One Edge",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(srcPowerDbm))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(gainDb), "nfDb" to nfDb(3.0))),
            ),
            edges = listOf(
                Edge("e1", "src", "rf_out", "amp", "rf_in"),
            ),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess, "Errors: ${result.errors}")
        val ampOut = result.outputValue("amp", "rf_out")
        assertNotNull(ampOut)
        assertEquals(srcPowerDbm + gainDb, ampOut!!, 0.001)
    }

    @Test
    fun `chaining two edges propagates through both nodes in order`() {
        val srcPowerDbm = 20.0
        val gainDb = 10.0
        val lossDb = 3.0
        val graph = WorkflowGraph(
            id = "chain", name = "Chain",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(srcPowerDbm))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(gainDb), "nfDb" to nfDb(2.0))),
                NodeInstance("att", Attenuator.TYPE_ID, mapOf("lossDb" to gDb(lossDb))),
            ),
            edges = listOf(
                Edge("e1", "src", "rf_out", "amp", "rf_in"),
                Edge("e2", "amp", "rf_out", "att", "rf_in"),
            ),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess, "Errors: ${result.errors}")
        val attOut = result.outputValue("att", "rf_out")
        assertNotNull(attOut)
        assertEquals(srcPowerDbm + gainDb - lossDb, attOut!!, 0.001)
    }

    @Test
    fun `duplicate edge does not break execution`() {
        val graph = WorkflowGraph(
            id = "dup-edge", name = "Dup Edge",
            nodes = listOf(
                NodeInstance("src", SignalSource.TYPE_ID, mapOf("powerDbm" to pDbm(0.0))),
                NodeInstance("amp", Amplifier.TYPE_ID, mapOf("gainDb" to gDb(10.0), "nfDb" to nfDb(3.0))),
            ),
            edges = listOf(
                Edge("e1", "src", "rf_out", "amp", "rf_in"),
                Edge("e2", "src", "rf_out", "amp", "rf_in"),
            ),
        )
        val result = engine.execute(graph, ExecutionContext())
        // Engine should run; the second edge simply overwrites the first input — no crash
        assertNotNull(result)
    }
}
