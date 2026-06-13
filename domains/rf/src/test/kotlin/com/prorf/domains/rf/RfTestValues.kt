package com.prorf.domains.rf

import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.ExecutionResult

fun pDbm(value: Double) = Quantity(value, PhysicalUnit.DBM)
fun gDb(value: Double) = Quantity(value, PhysicalUnit.DB)
fun nfDb(value: Double) = Quantity(value, PhysicalUnit.DB_NF)
fun fMhz(value: Double) = Quantity(value, PhysicalUnit.MHZ)
fun bwMhz(value: Double) = Quantity(value, PhysicalUnit.BANDWIDTH_MHZ)
fun distKm(value: Double) = Quantity(value, PhysicalUnit.KM)
fun kelvinQ(value: Double) = Quantity(value, PhysicalUnit.KELVIN)
fun metersQ(value: Double) = Quantity(value, PhysicalUnit.METER)
fun dbPerMeterQ(value: Double) = Quantity(value, PhysicalUnit.DB_PER_METER)
fun countQ(value: Double) = Quantity(value, PhysicalUnit.COUNT)

fun ExecutionResult.outputValue(nodeId: String, portId: String): Double =
    (outputs[nodeId]?.get(portId) as? Quantity)?.value
        ?: error("No Quantity output '$portId' on node '$nodeId'. Available: ${outputs[nodeId]?.keys}")
