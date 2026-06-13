package com.prorf.domains.rf

import com.prorf.engineering.quantity.Dimension
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity

internal fun Map<String, Any>.quantity(key: String, default: Quantity): Quantity {
    val raw = this[key] ?: return default
    val quantity = raw as? Quantity
        ?: error("Parameter '$key' must be a Quantity with value + unit + dimension")
    require(quantity.dimension == default.dimension) {
        "Parameter '$key' must have dimension ${default.dimension}, got ${quantity.dimension}"
    }
    return quantity.convertTo(default.unit)
}

internal fun Map<String, Any>.powerDbm(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.DBM))

internal fun Map<String, Any>.gainDb(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.DB))

internal fun Map<String, Any>.noiseFigureDb(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.DB_NF))

internal fun Map<String, Any>.frequencyMhz(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.MHZ))

internal fun Map<String, Any>.bandwidthMhz(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.BANDWIDTH_MHZ))

internal fun Map<String, Any>.distanceKm(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.KM))

internal fun Map<String, Any>.meters(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.METER))

internal fun Map<String, Any>.kelvin(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.KELVIN))

internal fun Map<String, Any>.dbPerMeter(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.DB_PER_METER))

internal fun Map<String, Any>.count(key: String, defaultValue: Double): Quantity =
    quantity(key, Quantity(defaultValue, PhysicalUnit.COUNT))

internal fun Quantity.requireDimension(dimension: Dimension, label: String): Quantity {
    require(this.dimension == dimension) {
        "$label must have dimension $dimension, got ${this.dimension}"
    }
    return this
}

internal fun Map<String, Any>.inputPowerDbm(key: String): Quantity =
    (this[key] as? Quantity)
        ?.requireDimension(Dimension.POWER, key)
        ?.convertTo(PhysicalUnit.DBM)
        ?: Quantity(0.0, PhysicalUnit.DBM)

internal fun dbm(value: Double): Quantity = Quantity(value, PhysicalUnit.DBM)

internal fun db(value: Double): Quantity = Quantity(value, PhysicalUnit.DB)
