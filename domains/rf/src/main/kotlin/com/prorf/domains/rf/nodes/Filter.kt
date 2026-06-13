package com.prorf.domains.rf.nodes

import com.prorf.domains.rf.RfPort
import com.prorf.domains.rf.db
import com.prorf.domains.rf.dbm
import com.prorf.domains.rf.frequencyMhz
import com.prorf.domains.rf.gainDb
import com.prorf.domains.rf.inputPowerDbm
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.ParameterDefinition
import com.prorf.platform.graph.PortDefinition

/**
 * RF filter model.
 * Models in-band insertion loss only; out-of-band rejection is not computed in MVP.
 */
object Filter {
    const val TYPE_ID = "rf.filter"

    val definition = NodeDefinition(
        typeId = TYPE_ID,
        displayName = "Filter",
        inputs = listOf(PortDefinition(RfPort.RF_IN, "RF In", "power_dbm")),
        outputs = listOf(
            PortDefinition(RfPort.RF_OUT, "RF Out", "power_dbm"),
            PortDefinition("insertionLossDb", "Insertion Loss", "db"),
        ),
        parameters = listOf(
            ParameterDefinition("insertionLossDb", "Insertion Loss (dB)", "quantity", Quantity(1.0, PhysicalUnit.DB)),
            ParameterDefinition("centerFreqMHz", "Center Frequency (MHz)", "quantity", Quantity(14000.0, PhysicalUnit.MHZ)),
        ),
    )

    class Executor : NodeExecutor {
        override val typeId = TYPE_ID
        override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
            val rfIn = inputs.inputPowerDbm(RfPort.RF_IN)
            val lossDb = parameters.gainDb("insertionLossDb", 1.0)
            parameters.frequencyMhz("centerFreqMHz", 14000.0)
            return mapOf(
                RfPort.RF_OUT to dbm(rfIn.value - lossDb.value),
                "insertionLossDb" to db(lossDb.value),
            )
        }
    }
}
