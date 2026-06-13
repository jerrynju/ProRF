package com.prorf.domains.rf.nodes

import com.prorf.domains.rf.RfPort
import com.prorf.domains.rf.bandwidthMhz
import com.prorf.domains.rf.db
import com.prorf.domains.rf.dbm
import com.prorf.domains.rf.inputPowerDbm
import com.prorf.domains.rf.kelvin
import com.prorf.domains.rf.noiseFigureDb
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.NodeExecutor
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.ParameterDefinition
import com.prorf.platform.graph.PortDefinition
import kotlin.math.log10
import kotlin.math.max

/**
 * Receiver termination node.
 * Computes thermal noise floor and link margin from the received power.
 * Noise floor = k·T·B (dBm) + NF
 */
object Receiver {
    const val TYPE_ID = "rf.receiver"

    val definition = NodeDefinition(
        typeId = TYPE_ID,
        displayName = "Receiver",
        inputs = listOf(PortDefinition(RfPort.RF_IN, "RF In", "power_dbm")),
        outputs = listOf(
            PortDefinition("rxPwr", "Received Power", "power_dbm"),
            PortDefinition("sensitivity", "Sensitivity", "power_dbm"),
            PortDefinition("margin", "Link Margin", "db"),
            PortDefinition("noiseFloor", "Noise Floor", "power_dbm"),
        ),
        parameters = listOf(
            ParameterDefinition("nfDb", "Noise Figure (dB)", "quantity", Quantity(3.0, PhysicalUnit.DB_NF)),
            ParameterDefinition("bandwidthMHz", "IF Bandwidth (MHz)", "quantity", Quantity(36.0, PhysicalUnit.BANDWIDTH_MHZ)),
            ParameterDefinition("temperatureK", "System Temperature (K)", "quantity", Quantity(290.0, PhysicalUnit.KELVIN)),
        ),
    )

    private const val BOLTZMANN_J_PER_K = 1.380649e-23

    fun sensitivity(nfDb: Double, bandwidthMHz: Double, temperatureK: Double): Double {
        val noiseDensityDbmHz = 10.0 * log10(BOLTZMANN_J_PER_K * max(temperatureK, 1.0)) + 30.0
        return noiseDensityDbmHz + 10.0 * log10(max(bandwidthMHz, 0.001) * 1e6) + nfDb
    }

    class Executor : NodeExecutor {
        override val typeId = TYPE_ID
        override fun execute(inputs: Map<String, Any>, parameters: Map<String, Any>): Map<String, Any> {
            val rfIn = inputs.inputPowerDbm(RfPort.RF_IN)
            val nfDb = parameters.noiseFigureDb("nfDb", 3.0).value
            val bwMHz = parameters.bandwidthMhz("bandwidthMHz", 36.0).value
            val tempK = parameters.kelvin("temperatureK", 290.0).value

            val sensitivity = sensitivity(nfDb, bwMHz, tempK)
            val margin = rfIn.value - sensitivity

            return mapOf(
                "rxPwr" to rfIn,
                "sensitivity" to dbm(sensitivity),
                "margin" to db(margin),
                "noiseFloor" to dbm(sensitivity),
            )
        }
    }
}
