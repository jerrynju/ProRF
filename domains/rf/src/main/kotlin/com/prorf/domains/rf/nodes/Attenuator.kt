package com.prorf.domains.rf.nodes

import com.prorf.domains.rf.RfPort
import com.prorf.domains.rf.db
import com.prorf.domains.rf.dbm
import com.prorf.domains.rf.gainDb
import com.prorf.domains.rf.inputPowerDbm
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.ParameterDefinition
import com.prorf.platform.graph.PortDefinition

/** Passive attenuator or cable — applies insertion loss to the signal path. */
object Attenuator {
    const val TYPE_ID = "rf.attenuator"

    val definition = NodeDefinition(
        typeId = TYPE_ID,
        displayName = "Attenuator",
        description = "Passive fixed-loss element — subtracts insertion loss from signal path",
        inputs = listOf(PortDefinition(RfPort.RF_IN, "RF In", "power_dbm")),
        outputs = listOf(PortDefinition(RfPort.RF_OUT, "RF Out", "power_dbm")),
        parameters = listOf(
            ParameterDefinition("lossDb", "Insertion Loss (dB)", "quantity", Quantity(3.0, PhysicalUnit.DB)),
        ),
    )

    class Executor : NodeExecutor {
        override val typeId = TYPE_ID
        override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
            val rfIn = inputs.inputPowerDbm(RfPort.RF_IN)
            val lossDb = parameters.gainDb("lossDb", 3.0)
            return mapOf(
                RfPort.RF_OUT to dbm(rfIn.value - lossDb.value),
                "lossDb" to db(lossDb.value),
            )
        }
    }
}
