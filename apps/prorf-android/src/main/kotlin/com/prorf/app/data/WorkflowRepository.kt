package com.prorf.app.data

import com.prorf.platform.graph.WorkflowGraph
import com.prorf.serialization.WorkflowSerializer
import java.io.File
import java.util.UUID

data class WorkflowSummary(
    val id: String,
    val name: String,
    val nodeCount: Int,
)

/**
 * L4 — File-based workflow persistence.
 * Each workflow is stored as a JSON file under <filesDir>/workflows/<id>.json.
 * The display name is stored in graph.metadata["name"] so it survives round-trips.
 */
class WorkflowRepository(filesDir: File) {

    private val dir = File(filesDir, "workflows").also { it.mkdirs() }
    private val serializer = WorkflowSerializer()

    fun list(): List<WorkflowSummary> = dir.listFiles { f -> f.extension == "json" }
        ?.mapNotNull { file ->
            runCatching {
                val id = file.nameWithoutExtension
                val graph = serializer.deserialize(id, id, file.readText())
                WorkflowSummary(
                    id = id,
                    name = graph.metadata["name"] ?: id,
                    nodeCount = graph.nodes.size,
                )
            }.getOrNull()
        }
        ?.sortedBy { it.name }
        ?: emptyList()

    fun load(id: String): WorkflowGraph? = runCatching {
        val file = File(dir, "$id.json")
        if (!file.exists()) return@runCatching null
        val graph = serializer.deserialize(id, id, file.readText())
        graph.copy(name = graph.metadata["name"] ?: id)
    }.getOrNull()

    fun save(graph: WorkflowGraph) {
        val withName = graph.copy(metadata = graph.metadata + ("name" to graph.name))
        File(dir, "${graph.id}.json").writeText(serializer.serialize(withName))
    }

    fun create(name: String): WorkflowGraph {
        val id = UUID.randomUUID().toString()
        val graph = WorkflowGraph(id = id, name = name)
        save(graph)
        return graph
    }

    fun delete(id: String) {
        File(dir, "$id.json").delete()
    }
}
