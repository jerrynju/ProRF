package com.prorf.domains.rf

import com.prorf.platform.execution.DagExecutionEngine
import com.prorf.platform.execution.ExecutionContext
import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.WorkflowGraph
import com.prorf.platform.plugin.PluginRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * M5 — verifies that updating a node parameter and re-running the engine produces
 * correctly updated output (live re-run correctness).
 */
class ParameterEditTest {

    private lateinit var engine: DagExecutionEngine

    @BeforeEach
    fun setup() {
        val registry = PluginRegistry()
        RfDomainPlugin.register(registry)
        engine = DagExecutionEngine(registry)
    }

    @Test
    fun `changing amplifier gain updates output power`() {
        val baseGraph = twoNodeGraph(gainDb = 10.0)
        val result1 = engine.execute(baseGraph, ExecutionContext())
        assertTrue(result1.isSuccess)
        val out1 = result1.outputValue("amp", "rf_out")
        assertEquals(40.0, out1, 0.001, "30 dBm + 10 dB gain = 40 dBm")

        val updatedGraph = baseGraph.copy(
            nodes = baseGraph.nodes.map { n ->
                if (n.id == "amp") n.copy(parameters = n.parameters + ("gainDb" to gDb(20.0))) else n
            },
        )
        val result2 = engine.execute(updatedGraph, ExecutionContext())
        assertTrue(result2.isSuccess)
        val out2 = result2.outputValue("amp", "rf_out")
        assertEquals(50.0, out2, 0.001, "30 dBm + 20 dB gain = 50 dBm")
    }

    @Test
    fun `changing signal source power propagates through chain`() {
        val graph = twoNodeGraph(powerDbm = 0.0, gainDb = 10.0)
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess)
        val out = result.outputValue("amp", "rf_out")
        assertEquals(10.0, out, 0.001, "0 dBm + 10 dB gain = 10 dBm")
    }

    @Test
    fun `parameter edit does not affect other nodes`() {
        val graph = twoNodeGraph(gainDb = 15.0)
        val result = engine.execute(graph, ExecutionContext())
        val srcOut = result.outputValue("src", "rf_out")
        assertEquals(30.0, srcOut, 0.001, "Source output must remain 30 dBm")
    }

    private fun twoNodeGraph(
        powerDbm: Double = 30.0,
        gainDb: Double = 20.0,
    ) = WorkflowGraph(
        id = "param-edit-test",
        name = "Parameter Edit Test",
        nodes = listOf(
            NodeInstance("src", "rf.signal_source", mapOf("powerDbm" to pDbm(powerDbm))),
            NodeInstance("amp", "rf.amplifier", mapOf("gainDb" to gDb(gainDb), "nfDb" to nfDb(3.0))),
        ),
        edges = listOf(Edge("e1", "src", "rf_out", "amp", "rf_in")),
    )
}
