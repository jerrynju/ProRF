package com.prorf.domains.rf.nodes

import com.prorf.domains.rf.RfPort
import com.prorf.domains.rf.powerDbm
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.ParameterDefinition
import com.prorf.platform.graph.PortDefinition

/** RF signal source — the entry point of any RF chain. Outputs transmit power in dBm. */
object SignalSource {
    const val TYPE_ID = "rf.signal_source"

    val definition = NodeDefinition(
        typeId = TYPE_ID,
        displayName = "Signal Source",
        inputs = emptyList(),
        outputs = listOf(PortDefinition(RfPort.RF_OUT, "RF Out", "power_dbm")),
        parameters = listOf(
            ParameterDefinition("powerDbm", "Transmit Power (dBm)", "quantity", Quantity(30.0, PhysicalUnit.DBM)),
        ),
    )

    class Executor : NodeExecutor {
        override val typeId = TYPE_ID
        override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
            val powerDbm = parameters.powerDbm("powerDbm", 30.0)
            return mapOf(RfPort.RF_OUT to powerDbm)
        }
    }
}
