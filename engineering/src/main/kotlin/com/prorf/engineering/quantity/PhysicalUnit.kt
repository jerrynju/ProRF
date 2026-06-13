package com.prorf.engineering.quantity

import kotlin.math.log10
import kotlin.math.pow

/**
 * A unit of measurement with conversion to/from SI baseline.
 * No naked doubles — all values must carry a PhysicalUnit.
 */
data class PhysicalUnit(
    val symbol: String,
    val dimension: Dimension,
    val toSi: (Double) -> Double,
    val fromSi: (Double) -> Double,
) {
    companion object {
        // Power
        val DBM = PhysicalUnit("dBm", Dimension.POWER,
            toSi = { dbm -> 10.0.pow((dbm - 30.0) / 10.0) },
            fromSi = { watts -> 10.0 * log10(watts) + 30.0 },
        )
        val WATT = PhysicalUnit("W", Dimension.POWER,
            toSi = { it }, fromSi = { it },
        )

        // Gain / Loss
        val DB = PhysicalUnit("dB", Dimension.GAIN,
            toSi = { it }, fromSi = { it },
        )
        val DBi = PhysicalUnit("dBi", Dimension.GAIN,
            toSi = { it }, fromSi = { it },
        )

        // Frequency
        val HZ = PhysicalUnit("Hz", Dimension.FREQUENCY,
            toSi = { it }, fromSi = { it },
        )
        val MHZ = PhysicalUnit("MHz", Dimension.FREQUENCY,
            toSi = { mhz -> mhz * 1e6 }, fromSi = { hz -> hz / 1e6 },
        )
        val GHZ = PhysicalUnit("GHz", Dimension.FREQUENCY,
            toSi = { ghz -> ghz * 1e9 }, fromSi = { hz -> hz / 1e9 },
        )
        val BANDWIDTH_MHZ = PhysicalUnit("MHz", Dimension.BANDWIDTH,
            toSi = { mhz -> mhz * 1e6 }, fromSi = { hz -> hz / 1e6 },
        )

        // Distance
        val METER = PhysicalUnit("m", Dimension.DISTANCE,
            toSi = { it }, fromSi = { it },
        )
        val KM = PhysicalUnit("km", Dimension.DISTANCE,
            toSi = { km -> km * 1000.0 }, fromSi = { m -> m / 1000.0 },
        )

        // Temperature
        val KELVIN = PhysicalUnit("K", Dimension.TEMPERATURE,
            toSi = { it }, fromSi = { it },
        )

        // Noise figure
        val DB_NF = PhysicalUnit("dB", Dimension.NOISE_FIGURE,
            toSi = { it }, fromSi = { it },
        )

        val DB_PER_METER = PhysicalUnit("dB/m", Dimension.GAIN_PER_DISTANCE,
            toSi = { it }, fromSi = { it },
        )
        val COUNT = PhysicalUnit("count", Dimension.DIMENSIONLESS,
            toSi = { it }, fromSi = { it },
        )
    }
}
