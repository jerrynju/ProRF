package com.prorf.serialization

import com.prorf.engineering.quantity.Dimension
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.WorkflowGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WorkflowSerializerTest {

    private val serializer = WorkflowSerializer()

    @Test
    fun `quantity parameters round trip with unit and dimension`() {
        val graph = WorkflowGraph(
            id = "g1",
            name = "Quantity Round Trip",
            nodes = listOf(
                NodeInstance(
                    id = "src",
                    typeId = "rf.signal_source",
                    parameters = mapOf("powerDbm" to Quantity(30.0, PhysicalUnit.DBM)),
                ),
            ),
        )

        val json = serializer.serialize(graph)
        val decoded = serializer.deserialize("g1", "Quantity Round Trip", json)
        val power = decoded.nodes.single().parameters["powerDbm"]

        assertTrue(power is Quantity)
        power as Quantity
        assertEquals(30.0, power.value, 1e-9)
        assertEquals("dBm", power.unit.symbol)
        assertEquals(Dimension.POWER, power.dimension)
    }

    @Test
    fun `node label round trips through serialization`() {
        val graph = WorkflowGraph(
            id = "g2",
            name = "Label Round Trip",
            nodes = listOf(
                NodeInstance(id = "n1", typeId = "rf.amplifier", label = "Front-end LNA"),
            ),
        )

        val json = serializer.serialize(graph)
        val decoded = serializer.deserialize("g2", "Label Round Trip", json)
        assertEquals("Front-end LNA", decoded.nodes.single().label)
    }

    @Test
    fun `null label deserializes as null`() {
        val graph = WorkflowGraph(
            id = "g3",
            name = "Null Label",
            nodes = listOf(NodeInstance(id = "n1", typeId = "rf.amplifier")),
        )

        val json = serializer.serialize(graph)
        val decoded = serializer.deserialize("g3", "Null Label", json)
        assertNull(decoded.nodes.single().label)
    }
}
