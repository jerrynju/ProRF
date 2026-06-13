package com.prorf.serialization

import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.NodePosition
import com.prorf.platform.graph.WorkflowGraph
import com.prorf.engineering.quantity.Dimension
import com.prorf.engineering.quantity.PhysicalUnit
import com.prorf.engineering.quantity.Quantity
import kotlinx.serialization.json.Json

/**
 * Converts between WorkflowGraph (runtime model) and JSON (on-disk format).
 * All schema migration logic is centralized here — nowhere else.
 *
 * Format contract (section 8 of Build Spec):
 *   { "schemaVersion": 1, "nodes": [...], "connections": [...] }
 */
class WorkflowSerializer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(graph: WorkflowGraph): String {
        val doc = WorkflowDocument(
            schemaVersion = WorkflowDocument.CURRENT_VERSION,
            nodes = graph.nodes.map { node ->
                NodeDocument(
                    id = node.id,
                    typeId = node.typeId,
                    parameters = node.parameters.mapValues { (_, v) -> encodeValue(v) },
                    x = node.position.x,
                    y = node.position.y,
                )
            },
            connections = graph.edges.map { edge ->
                ConnectionDocument(
                    id = edge.id,
                    fromNodeId = edge.fromNodeId,
                    fromPortId = edge.fromPortId,
                    toNodeId = edge.toNodeId,
                    toPortId = edge.toPortId,
                )
            },
            metadata = graph.metadata,
        )
        return json.encodeToString(WorkflowDocument.serializer(), doc)
    }

    fun deserialize(graphId: String, graphName: String, source: String): WorkflowGraph {
        val raw = json.decodeFromString(WorkflowDocument.serializer(), source)
        val doc = migrate(raw)
        return WorkflowGraph(
            id = graphId,
            name = graphName,
            nodes = doc.nodes.map { n ->
                NodeInstance(
                    id = n.id,
                    typeId = n.typeId,
                    parameters = n.parameters.mapValues { (_, v) -> parseValue(v) },
                    position = NodePosition(n.x, n.y),
                )
            },
            edges = doc.connections.map { c ->
                Edge(
                    id = c.id,
                    fromNodeId = c.fromNodeId,
                    fromPortId = c.fromPortId,
                    toNodeId = c.toNodeId,
                    toPortId = c.toPortId,
                )
            },
            metadata = doc.metadata,
        )
    }

    private fun migrate(doc: WorkflowDocument): WorkflowDocument {
        // Each version bump adds a migration step here.
        // v1 is current — no migration needed.
        return doc
    }

    private fun encodeValue(value: Any): String = when (value) {
        is Quantity -> "q:${value.value}|${value.unit.symbol}|${value.dimension.name}"
        else -> value.toString()
    }

    private fun parseValue(s: String): Any =
        parseQuantity(s) ?: s.toDoubleOrNull() ?: s.toBooleanStrictOrNull() ?: s

    private fun parseQuantity(s: String): Quantity? {
        if (!s.startsWith("q:")) return null
        val parts = s.removePrefix("q:").split("|")
        if (parts.size != 3) return null
        val value = parts[0].toDoubleOrNull() ?: return null
        val symbol = parts[1]
        val dimension = runCatching { Dimension.valueOf(parts[2]) }.getOrNull() ?: return null
        val unit = physicalUnit(symbol, dimension) ?: return null
        return Quantity(value, unit)
    }

    private fun physicalUnit(symbol: String, dimension: Dimension): PhysicalUnit? =
        knownUnits.firstOrNull { it.symbol == symbol && it.dimension == dimension }

    private companion object {
        val knownUnits = listOf(
            PhysicalUnit.DBM,
            PhysicalUnit.WATT,
            PhysicalUnit.DB,
            PhysicalUnit.DBi,
            PhysicalUnit.HZ,
            PhysicalUnit.MHZ,
            PhysicalUnit.GHZ,
            PhysicalUnit.BANDWIDTH_MHZ,
            PhysicalUnit.METER,
            PhysicalUnit.KM,
            PhysicalUnit.KELVIN,
            PhysicalUnit.DB_NF,
            PhysicalUnit.DB_PER_METER,
            PhysicalUnit.COUNT,
        )
    }
}
