package com.prorf.domains.rf

import com.prorf.domains.rf.nodes.Amplifier
import com.prorf.domains.rf.nodes.Attenuator
import com.prorf.domains.rf.nodes.FreeSpacePathLoss
import com.prorf.domains.rf.nodes.Receiver
import com.prorf.domains.rf.nodes.SignalSource
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.NodePosition
import com.prorf.platform.graph.WorkflowGraph

/**
 * L3 — Bundled RF workflow templates.
 * Each template is a pre-wired WorkflowGraph with realistic default parameters.
 * IDs use the "template:" prefix — they are never persisted directly; the app
 * shell must copy them to a UUID-based id before saving.
 */
object WorkflowTemplates {

    fun kuBandGeoLink(): WorkflowGraph = WorkflowGraph(
        id = "template:ku-band-geo",
        name = "Ku-band GEO Link Budget",
        nodes = listOf(
            NodeInstance(
                id = "src",
                typeId = SignalSource.TYPE_ID,
                parameters = mapOf("powerDbm" to q(40.0, PhysicalUnit.DBM)),
                position = NodePosition(40f, 180f),
            ),
            NodeInstance(
                id = "hpa",
                typeId = Amplifier.TYPE_ID,
                parameters = mapOf("gainDb" to q(54.0, PhysicalUnit.DB), "nfDb" to q(3.0, PhysicalUnit.DB_NF)),
                position = NodePosition(220f, 180f),
            ),
            NodeInstance(
                id = "fspl",
                typeId = FreeSpacePathLoss.TYPE_ID,
                parameters = mapOf("frequencyMHz" to q(14250.0, PhysicalUnit.MHZ), "distanceKm" to q(35786.0, PhysicalUnit.KM)),
                position = NodePosition(420f, 180f),
            ),
            NodeInstance(
                id = "rx",
                typeId = Receiver.TYPE_ID,
                parameters = mapOf(
                    "nfDb" to q(2.0, PhysicalUnit.DB_NF),
                    "bandwidthMHz" to q(36.0, PhysicalUnit.BANDWIDTH_MHZ),
                    "temperatureK" to q(290.0, PhysicalUnit.KELVIN),
                ),
                position = NodePosition(630f, 180f),
            ),
        ),
        edges = listOf(
            Edge("e1", "src", RfPort.RF_OUT, "hpa", RfPort.RF_IN),
            Edge("e2", "hpa", RfPort.RF_OUT, "fspl", RfPort.RF_IN),
            Edge("e3", "fspl", RfPort.RF_OUT, "rx", RfPort.RF_IN),
        ),
        metadata = mapOf("name" to "Ku-band GEO Link Budget"),
    )

    fun mmWave5gLink(): WorkflowGraph = WorkflowGraph(
        id = "template:5g-mmwave",
        name = "5G mmWave Link",
        nodes = listOf(
            NodeInstance(
                id = "src",
                typeId = SignalSource.TYPE_ID,
                parameters = mapOf("powerDbm" to q(23.0, PhysicalUnit.DBM)),
                position = NodePosition(40f, 180f),
            ),
            NodeInstance(
                id = "pa",
                typeId = Amplifier.TYPE_ID,
                parameters = mapOf("gainDb" to q(26.0, PhysicalUnit.DB), "nfDb" to q(5.0, PhysicalUnit.DB_NF)),
                position = NodePosition(220f, 180f),
            ),
            NodeInstance(
                id = "cable",
                typeId = Attenuator.TYPE_ID,
                parameters = mapOf("lossDb" to q(2.0, PhysicalUnit.DB)),
                position = NodePosition(400f, 180f),
            ),
            NodeInstance(
                id = "fspl",
                typeId = FreeSpacePathLoss.TYPE_ID,
                parameters = mapOf("frequencyMHz" to q(28000.0, PhysicalUnit.MHZ), "distanceKm" to q(0.3, PhysicalUnit.KM)),
                position = NodePosition(580f, 180f),
            ),
            NodeInstance(
                id = "rx",
                typeId = Receiver.TYPE_ID,
                parameters = mapOf(
                    "nfDb" to q(7.0, PhysicalUnit.DB_NF),
                    "bandwidthMHz" to q(400.0, PhysicalUnit.BANDWIDTH_MHZ),
                    "temperatureK" to q(290.0, PhysicalUnit.KELVIN),
                ),
                position = NodePosition(780f, 180f),
            ),
        ),
        edges = listOf(
            Edge("e1", "src", RfPort.RF_OUT, "pa", RfPort.RF_IN),
            Edge("e2", "pa", RfPort.RF_OUT, "cable", RfPort.RF_IN),
            Edge("e3", "cable", RfPort.RF_OUT, "fspl", RfPort.RF_IN),
            Edge("e4", "fspl", RfPort.RF_OUT, "rx", RfPort.RF_IN),
        ),
        metadata = mapOf("name" to "5G mmWave Link"),
    )

    val all: List<WorkflowGraph> = listOf(kuBandGeoLink(), mmWave5gLink())

    fun findById(id: String): WorkflowGraph? = all.find { it.id == id }

    private fun q(value: Double, unit: PhysicalUnit): Quantity = Quantity(value, unit)
}
