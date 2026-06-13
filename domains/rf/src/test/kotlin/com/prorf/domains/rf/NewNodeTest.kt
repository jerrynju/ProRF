package com.prorf.domains.rf

import com.prorf.domains.rf.nodes.Cable
import com.prorf.domains.rf.nodes.Filter
import com.prorf.domains.rf.nodes.NoiseSource
import com.prorf.platform.execution.DagExecutionEngine
import com.prorf.platform.execution.ExecutionContext
import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.WorkflowGraph
import com.prorf.platform.plugin.PluginRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NewNodeTest {

    private lateinit var engine: DagExecutionEngine

    @BeforeEach
    fun setup() {
        val registry = PluginRegistry()
        RfDomainPlugin.register(registry)
        engine = DagExecutionEngine(registry)
    }

    @Test
    fun `noise source outputs finite negative dBm`() {
        val power = NoiseSource.noisePowerDbm(temperatureK = 290.0, bandwidthMHz = 36.0)
        assertTrue(power.isFinite())
        assertTrue(power < 0.0, "Thermal noise power must be negative dBm, got $power")
    }

    @Test
    fun `noise source power increases with bandwidth`() {
        val narrow = NoiseSource.noisePowerDbm(temperatureK = 290.0, bandwidthMHz = 1.0)
        val wide = NoiseSource.noisePowerDbm(temperatureK = 290.0, bandwidthMHz = 100.0)
        assertTrue(wide > narrow, "Wider bandwidth must produce more noise power")
    }

    @Test
    fun `noise source power increases with temperature`() {
        val cold = NoiseSource.noisePowerDbm(temperatureK = 50.0, bandwidthMHz = 36.0)
        val warm = NoiseSource.noisePowerDbm(temperatureK = 290.0, bandwidthMHz = 36.0)
        assertTrue(warm > cold, "Higher temperature must produce more noise power")
    }

    @Test
    fun `cable total loss is loss-per-meter times length plus connector loss`() {
        val loss = Cable.totalLossDb(
            lossPerMeterDb = 0.1,
            lengthM = 10.0,
            connectorCount = 2,
            connectorLossDb = 0.5,
        )
        assertEquals(2.0, loss, 1e-9)
    }

    @Test
    fun `cable reduces signal by total loss`() {
        val graph = WorkflowGraph(
            id = "cable-test", name = "Cable Test",
            nodes = listOf(
                NodeInstance("src", "rf.signal_source", mapOf("powerDbm" to pDbm(20.0))),
                NodeInstance("cable", "rf.cable", mapOf(
                    "lossPerMeterDb" to dbPerMeterQ(0.2),
                    "lengthM" to metersQ(5.0),
                    "connectorCount" to countQ(2.0),
                    "connectorLossDb" to gDb(0.5),
                )),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "cable", "rf_in")),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess)
        val out = result.outputValue("cable", "rf_out")
        assertEquals(18.0, out!!, 1e-9)
    }

    @Test
    fun `filter reduces signal by insertion loss`() {
        val graph = WorkflowGraph(
            id = "filter-test", name = "Filter Test",
            nodes = listOf(
                NodeInstance("src", "rf.signal_source", mapOf("powerDbm" to pDbm(15.0))),
                NodeInstance("flt", "rf.filter", mapOf("insertionLossDb" to gDb(2.0))),
            ),
            edges = listOf(Edge("e1", "src", "rf_out", "flt", "rf_in")),
        )
        val result = engine.execute(graph, ExecutionContext())
        assertTrue(result.isSuccess)
        val out = result.outputValue("flt", "rf_out")
        assertEquals(13.0, out!!, 1e-9)
    }

    @Test
    fun `all eight RF nodes are registered`() {
        val registry = PluginRegistry()
        RfDomainPlugin.register(registry)
        val typeIds = registry.allDefinitions().map { it.typeId }.toSet()
        val expected = setOf(
            "rf.signal_source", "rf.noise_source",
            "rf.amplifier", "rf.attenuator", "rf.cable", "rf.filter",
            "rf.fspl", "rf.receiver",
        )
        assertEquals(expected, typeIds)
    }
}
