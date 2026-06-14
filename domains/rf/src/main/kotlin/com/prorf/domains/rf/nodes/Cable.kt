package com.prorf.domains.rf.nodes

import com.prorf.domains.rf.RfPort
import com.prorf.domains.rf.count
import com.prorf.domains.rf.db
import com.prorf.domains.rf.dbPerMeter
import com.prorf.domains.rf.dbm
import com.prorf.domains.rf.gainDb
import com.prorf.domains.rf.inputPowerDbm
import com.prorf.domains.rf.meters
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.ParameterDefinition
import com.prorf.platform.graph.PortDefinition

/**
 * Coaxial cable model.
 * Total loss = lossPerMeterDb * lengthM + connectorCount * connectorLossDb
 */
object Cable {
    const val TYPE_ID = "rf.cable"

    val definition = NodeDefinition(
        typeId = TYPE_ID,
        displayName = "Cable",
        description = "Coaxial cable — total loss from dB/m × length + connector losses",
        inputs = listOf(PortDefinition(RfPort.RF_IN, "RF In", "power_dbm")),
        outputs = listOf(
            PortDefinition(RfPort.RF_OUT, "RF Out", "power_dbm"),
            PortDefinition("totalLossDb", "Total Loss", "db"),
        ),
        parameters = listOf(
            ParameterDefinition("lossPerMeterDb", "Loss per Meter (dB/m)", "quantity", Quantity(0.1, PhysicalUnit.DB_PER_METER)),
            ParameterDefinition("lengthM", "Length (m)", "quantity", Quantity(1.0, PhysicalUnit.METER)),
            ParameterDefinition("connectorCount", "Connector Count", "quantity", Quantity(2.0, PhysicalUnit.COUNT)),
            ParameterDefinition("connectorLossDb", "Loss per Connector (dB)", "quantity", Quantity(0.5, PhysicalUnit.DB)),
        ),
    )

    fun totalLossDb(lossPerMeterDb: Double, lengthM: Double, connectorCount: Int, connectorLossDb: Double): Double =
        lossPerMeterDb * lengthM + connectorCount * connectorLossDb

    class Executor : NodeExecutor {
        override val typeId = TYPE_ID
        override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
            val rfIn = inputs.inputPowerDbm(RfPort.RF_IN)
            val lossPerM = parameters.dbPerMeter("lossPerMeterDb", 0.1).value
            val length = parameters.meters("lengthM", 1.0).value
            val connCount = parameters.count("connectorCount", 2.0).value.toInt()
            val connLoss = parameters.gainDb("connectorLossDb", 0.5).value
            val totalLoss = totalLossDb(lossPerM, length, connCount, connLoss)
            return mapOf(
                RfPort.RF_OUT to dbm(rfIn.value - totalLoss),
                "totalLossDb" to db(totalLoss),
            )
        }
    }
}
