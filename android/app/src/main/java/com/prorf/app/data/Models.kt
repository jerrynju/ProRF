package com.prorf.app.data

import kotlinx.serialization.Serializable

@Serializable
enum class NodeKind { TX, LOSS, PROPAGATION, RX }

@Serializable
data class RfNode(
    val id: String,
    val kind: NodeKind,
    val moduleId: String,
    val name: String,
    val params: Map<String, Double> = emptyMap(),
)

@Serializable
data class GlobalParams(
    val frequencyMHz: Double = 14000.0,
    val bandwidthMHz: Double = 36.0,
    val distanceKm: Double = 38000.0,
    val temperatureK: Double = 290.0,
)

@Serializable
data class Workflow(
    val id: String,
    val name: String,
    val tags: List<String> = emptyList(),
    val fav: Boolean = false,
    val updatedAt: String = "",
    val globals: GlobalParams = GlobalParams(),
    val nodes: List<RfNode> = emptyList(),
)

/** Parameter spec of a catalog module; labels carried in zh + en. */
data class ParamSpec(
    val key: String,
    val label: String,
    val labelEn: String,
    val unit: String,
    val default: Double,
    val min: Double,
    val max: Double,
    val infoOnly: Boolean = false,
) {
    fun displayLabel(zh: Boolean) = if (zh) label else labelEn
}

data class ModuleSpec(
    val id: String,
    val kind: NodeKind,
    val name: String,
    val en: String,
    val emoji: String,
    val params: List<ParamSpec> = emptyList(),
) {
    fun displayName(zh: Boolean) = if (zh) name else en
}

// ── Calculation results ────────────────────────────────────────
data class TraceEntry(
    val nodeId: String,
    val name: String,
    val kind: NodeKind,
    val gain: Double,
    val loss: Double,
    val pwr: Double,
    val label: String,
)

data class ChartPoint(val distKm: Double, val snr: Double, val margin: Double)

data class LinkResult(
    val eirp: Double,
    val rxPwr: Double,
    val sensitivity: Double,
    val margin: Double,
    val snr: Double,
    val noiseFloor: Double,
    val isValid: Boolean,
    val trace: List<TraceEntry>,
    val chart: List<ChartPoint>,
)

