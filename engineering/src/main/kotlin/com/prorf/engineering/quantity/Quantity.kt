package com.prorf.engineering.quantity

/**
 * L1 Engineering Foundation — typed numeric value with unit.
 *
 * The rule: never store a naked Double. Every value that represents a physical
 * quantity must carry its unit. Conversion is explicit and dimension-checked.
 */
data class Quantity(
    val value: Double,
    val unit: PhysicalUnit,
) {
    val dimension: Dimension get() = unit.dimension

    fun toSi(): Double = unit.toSi(value)

    fun convertTo(target: PhysicalUnit): Quantity {
        require(target.dimension == dimension) {
            "Cannot convert ${unit.symbol} (${dimension}) to ${target.symbol} (${target.dimension})"
        }
        return Quantity(target.fromSi(toSi()), target)
    }

    operator fun plus(other: Quantity): Quantity {
        require(other.dimension == dimension) {
            "Dimension mismatch: cannot add $dimension and ${other.dimension}"
        }
        return Quantity(unit.fromSi(toSi() + other.toSi()), unit)
    }

    operator fun minus(other: Quantity): Quantity {
        require(other.dimension == dimension) {
            "Dimension mismatch: cannot subtract ${other.dimension} from $dimension"
        }
        return Quantity(unit.fromSi(toSi() - other.toSi()), unit)
    }

    operator fun unaryMinus(): Quantity = Quantity(-value, unit)

    override fun toString(): String = "$value ${unit.symbol}"
}

// Extension constructors for readability
fun Double.dBm() = Quantity(this, PhysicalUnit.DBM)
fun Double.dB() = Quantity(this, PhysicalUnit.DB)
fun Double.dBi() = Quantity(this, PhysicalUnit.DBi)
fun Double.MHz() = Quantity(this, PhysicalUnit.MHZ)
fun Double.GHz() = Quantity(this, PhysicalUnit.GHZ)
fun Double.bandwidthMHz() = Quantity(this, PhysicalUnit.BANDWIDTH_MHZ)
fun Double.km() = Quantity(this, PhysicalUnit.KM)
fun Double.meters() = Quantity(this, PhysicalUnit.METER)
fun Double.kelvin() = Quantity(this, PhysicalUnit.KELVIN)
fun Double.noiseFigureDb() = Quantity(this, PhysicalUnit.DB_NF)
fun Double.dbPerMeter() = Quantity(this, PhysicalUnit.DB_PER_METER)
fun Double.count() = Quantity(this, PhysicalUnit.COUNT)
