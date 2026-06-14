package com.prorf.domains.rf.nodes

import com.prorf.domains.rf.RfPort
import com.prorf.domains.rf.db
import com.prorf.domains.rf.dbm
import com.prorf.domains.rf.distanceKm
import com.prorf.domains.rf.frequencyMhz
import com.prorf.domains.rf.inputPowerDbm
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.ParameterDefinition
import com.prorf.platform.graph.PortDefinition
import kotlin.math.log10
import kotlin.math.max

/**
 * Free Space Path Loss channel model.
 * FSPL(dB) = 32.44 + 20·log10(f_MHz) + 20·log10(d_km)
 */
object FreeSpacePathLoss {
    const val TYPE_ID = "rf.fspl"

    val definition = NodeDefinition(
        typeId = TYPE_ID,
        displayName = "Free Space Path Loss",
        description = "Friis FSPL channel: 32.44 + 20log₁₀(f_MHz) + 20log₁₀(d_km)",
        inputs = listOf(PortDefinition(RfPort.RF_IN, "RF In", "power_dbm")),
        outputs = listOf(
            PortDefinition(RfPort.RF_OUT, "RF Out", "power_dbm"),
            PortDefinition("fsplDb", "FSPL", "db"),
        ),
        parameters = listOf(
            ParameterDefinition("frequencyMHz", "Frequency (MHz)", "quantity", Quantity(14000.0, PhysicalUnit.MHZ)),
            ParameterDefinition("distanceKm", "Distance (km)", "quantity", Quantity(38000.0, PhysicalUnit.KM)),
        ),
    )

    fun fsplDb(frequencyMHz: Double, distanceKm: Double): Double {
        if (distanceKm <= 0 || frequencyMHz <= 0) return 0.0
        return 32.44 + 20.0 * log10(max(frequencyMHz, 1.0)) + 20.0 * log10(max(distanceKm, 0.001))
    }

    class Executor : NodeExecutor {
        override val typeId = TYPE_ID
        override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
            val rfIn = inputs.inputPowerDbm(RfPort.RF_IN)
            val freqMHz = parameters.frequencyMhz("frequencyMHz", 14000.0).value
            val distKm = parameters.distanceKm("distanceKm", 38000.0).value
            val loss = fsplDb(freqMHz, distKm)
            return mapOf(
                RfPort.RF_OUT to dbm(rfIn.value - loss),
                "fsplDb" to db(loss),
            )
        }
    }
}
