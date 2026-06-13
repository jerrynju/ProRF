package com.prorf.domains.rf.nodes

import com.prorf.domains.rf.RfPort
import com.prorf.domains.rf.dbm
import com.prorf.domains.rf.gainDb
import com.prorf.domains.rf.inputPowerDbm
import com.prorf.domains.rf.noiseFigureDb
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.ParameterDefinition
import com.prorf.platform.graph.PortDefinition

/** RF amplifier — applies gain and has an associated noise figure. */
object Amplifier {
    const val TYPE_ID = "rf.amplifier"

    val definition = NodeDefinition(
        typeId = TYPE_ID,
        displayName = "Amplifier",
        inputs = listOf(PortDefinition(RfPort.RF_IN, "RF In", "power_dbm")),
        outputs = listOf(PortDefinition(RfPort.RF_OUT, "RF Out", "power_dbm")),
        parameters = listOf(
            ParameterDefinition("gainDb", "Gain (dB)", "quantity", Quantity(20.0, PhysicalUnit.DB)),
            ParameterDefinition("nfDb", "Noise Figure (dB)", "quantity", Quantity(3.0, PhysicalUnit.DB_NF)),
        ),
    )

    class Executor : NodeExecutor {
        override val typeId = TYPE_ID
        override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
            val rfIn = inputs.inputPowerDbm(RfPort.RF_IN)
            val gainDb = parameters.gainDb("gainDb", 20.0)
            val nfDb = parameters.noiseFigureDb("nfDb", 3.0)
            return mapOf(
                RfPort.RF_OUT to dbm(rfIn.value + gainDb.value),
                "gainDb" to gainDb,
                "nfDb" to nfDb,
            )
        }
    }
}
