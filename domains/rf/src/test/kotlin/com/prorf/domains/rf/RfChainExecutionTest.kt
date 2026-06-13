package com.prorf.domains.rf

import com.prorf.domains.rf.nodes.FreeSpacePathLoss
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.DagExecutionEngine
import com.prorf.platform.execution.ExecutionContext
import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.WorkflowGraph
import com.prorf.platform.plugin.PluginRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RfChainExecutionTest {

    private lateinit var engine: DagExecutionEngine

    @BeforeEach
    fun setup() {
        val registry = PluginRegistry()
        RfDomainPlugin.register(registry)
        engine = DagExecutionEngine(registry)
    }

    @Test
    fun `full link budget chain executes without errors`() {
        val graph = satLinkGraph()
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess, "Execution errors: ${result.errors}")
    }

    @Test
    fun `receiver output is non-null and margin is finite`() {
        val result = engine.execute(satLinkGraph(), ExecutionContext())
        val rxOutputs = result.outputs["rx"]
        assertNotNull(rxOutputs, "Receiver node must produce output")
        val margin = rxOutputs!!["margin"] as? Quantity
        assertNotNull(margin)
        assertTrue(margin!!.value.isFinite(), "Link margin must be finite, got: $margin")
    }

    @Test
    fun `FSPL increases with distance`() {
        val closeLoss = FreeSpacePathLoss.fsplDb(14000.0, 100.0)
        val farLoss = FreeSpacePathLoss.fsplDb(14000.0, 38000.0)
        assertTrue(farLoss > closeLoss,
            "Far FSPL ($farLoss dB) must exceed close FSPL ($closeLoss dB)")
    }

    @Test
    fun `amplifier raises output power by gain`() {
        val graph = WorkflowGraph(
            id = "amp-test", name = "Amp Test",
            nodes = listOf(
                NodeInstance("src", "rf.signal_source", mapOf("powerDbm" to pDbm(10.0))),
                NodeInstance("amp", "rf.amplifier", mapOf("gainDb" to gDb(20.0), "nfDb" to nfDb(3.0))),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "amp", "rf_in")),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess)
        val out = result.requireOutputValue("amp", "rf_out")
        assertEquals(30.0, out, 0.001)
    }

    @Test
    fun `attenuator reduces output power by loss`() {
        val graph = WorkflowGraph(
            id = "att-test", name = "Att Test",
            nodes = listOf(
                NodeInstance("src", "rf.signal_source", mapOf("powerDbm" to pDbm(30.0))),
                NodeInstance("att", "rf.attenuator", mapOf("lossDb" to gDb(5.0))),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "att", "rf_in")),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess)
        val out = result.requireOutputValue("att", "rf_out")
        assertEquals(25.0, out, 0.001)
    }

    // Ku-band GEO satellite link: 30 dBm TX → 20 dB amplifier → 207 dB FSPL → receiver
    private fun satLinkGraph() = WorkflowGraph(
        id = "sat-link", name = "Ku-band GEO Link",
        nodes = listOf(
            NodeInstance("src", "rf.signal_source", mapOf("powerDbm" to pDbm(30.0))),
            NodeInstance("amp", "rf.amplifier", mapOf("gainDb" to gDb(20.0), "nfDb" to nfDb(3.0))),
            NodeInstance("fspl", "rf.fspl", mapOf("frequencyMHz" to fMhz(14000.0), "distanceKm" to distKm(38000.0))),
            NodeInstance("rx", "rf.receiver", mapOf("nfDb" to nfDb(3.0), "bandwidthMHz" to bwMhz(36.0), "temperatureK" to kelvinQ(290.0))),
        ),
        edges = listOf(
            Edge("e1", "src", "rf_out", "amp", "rf_in"),
            Edge("e2", "amp", "rf_out", "fspl", "rf_in"),
            Edge("e3", "fspl", "rf_out", "rx", "rf_in"),
        ),
    )
}
