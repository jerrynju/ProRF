package com.prorf.app.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * Simple file-backed workflow repository.
 * Seeds with sample workflows on first launch; persists as JSON in filesDir.
 */
class WorkflowStore(context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val jsonPretty = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file = File(context.filesDir, "workflows.json")
    private val serializer = ListSerializer(Workflow.serializer())

    val workflows = mutableStateListOf<Workflow>()

    /** Background scope for non-blocking file writes. */
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val loaded = runCatching {
            if (file.exists()) json.decodeFromString(serializer, file.readText()) else null
        }.onFailure { e ->
            Log.e("WorkflowStore", "Failed to load workflows: ${e.message}", e)
        }.getOrNull()
        workflows.addAll(loaded ?: Catalog.sampleWorkflows())
        if (loaded == null) persist()
    }

    private fun persist() {
        val snapshot = workflows.toList()  // capture on calling thread
        ioScope.launch {
            runCatching { file.writeText(json.encodeToString(serializer, snapshot)) }
        }
    }

    private fun now(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US))

    /** Thread-safe counter for unique ID generation. */
    private val idCounter = AtomicLong(System.currentTimeMillis())

    private fun nextId(): String = "wf-" + idCounter.getAndIncrement()

    fun get(id: String): Workflow? = workflows.find { it.id == id }

    fun save(wf: Workflow) {
        val stamped = wf.copy(updatedAt = now())
        val i = workflows.indexOfFirst { it.id == wf.id }
        if (i >= 0) workflows[i] = stamped else workflows.add(0, stamped)
        persist()
    }

    fun delete(id: String) {
        workflows.removeAll { it.id == id }
        persist()
    }

    /** Check whether a workflow with the given id still exists. */
    fun exists(id: String): Boolean = workflows.any { it.id == id }

    /** Generate a non-conflicting copy name, e.g. "Link (2)", "Link (3)", … */
    fun uniqueCopyName(baseName: String): String {
        val existing = workflows.map { it.name }.toSet()
        var n = 2
        while ("$baseName ($n)" in existing) n++
        return "$baseName ($n)"
    }

    /** Export a single workflow as pretty JSON string. */
    fun exportJson(wf: Workflow): String =
        jsonPretty.encodeToString(Workflow.serializer(), wf)

    /** Export all workflows as pretty JSON string. */
    fun exportAllJson(): String =
        jsonPretty.encodeToString(serializer, workflows.toList())

    /** Export selected workflows as pretty JSON string. */
    fun exportAllJson(wfs: List<Workflow>): String =
        jsonPretty.encodeToString(serializer, wfs)

    /** Import workflow(s) from JSON text. Handles both single object and array. */
    fun importJson(text: String): Int {
        var count = 0
        // Try as array first
        val imported: List<Workflow> = runCatching {
            json.decodeFromString(serializer, text)
        }.getOrElse {
            // Try as single workflow
            runCatching {
                listOf(json.decodeFromString(Workflow.serializer(), text))
            }.getOrElse {
                Log.e("WorkflowStore", "Failed to import JSON: ${it.message}", it)
                emptyList()
            }
        }
        for (wf in imported) {
            val newId = nextId()
            val renamed = wf.copy(
                id = newId,
                name = uniqueCopyName(wf.name),
                nodes = wf.nodes.mapIndexed { i, n -> n.copy(id = "$newId-n$i") },
            )
            save(renamed)
            count++
        }
        return count
    }

    /** Clear all workflows and re-initialize with sample data. */
    fun clearAllData() {
        workflows.clear()
        workflows.addAll(Catalog.sampleWorkflows())
        persist()
    }

    /** Create a workflow from a template id ("sat" / "5g" / "mw" / "radar" / "blank" / null). */
    fun create(template: String? = null): Workflow {
        val id = nextId()
        val zh = Catalog.isZh()
        val samples = Catalog.sampleWorkflows(zh)
        val base = when (template) {
            "sat" -> samples[0]
            "5g" -> samples[1]
            "mw" -> samples[2]
            "radar" -> samples[3]
            else -> null
        }
        val wf = if (base != null) {
            base.copy(
                id = id,
                name = "${base.name} ${workflows.size + 1}",
                tags = base.tags,
                nodes = base.nodes.mapIndexed { i, n -> n.copy(id = "$id-n$i") },
            )
        } else {
            Workflow(
                id = id,
                name = if (zh) "新建链路 ${workflows.size + 1}" else "New Link ${workflows.size + 1}",
                tags = listOf(if (zh) "自定义" else "Custom"),
                globals = GlobalParams(),
                nodes = listOf(
                    RfNode("$id-n0", NodeKind.TX, "tx_source", Catalog.modules["tx_source"]!!.displayName(zh), mapOf("powerDbm" to 20.0)),
                    RfNode("$id-n1", NodeKind.TX, "ant_tx", Catalog.modules["ant_tx"]!!.displayName(zh), mapOf("gainDbi" to 30.0)),
                    RfNode("$id-n2", NodeKind.PROPAGATION, "fspl", Catalog.modules["fspl"]!!.displayName(zh)),
                    RfNode("$id-n3", NodeKind.RX, "ant_rx", Catalog.modules["ant_rx"]!!.displayName(zh), mapOf("gainDbi" to 30.0)),
                    RfNode("$id-n4", NodeKind.RX, "receiver", Catalog.modules["receiver"]!!.displayName(zh), mapOf("nfDb" to 3.0, "bwMHz" to 36.0)),
                ),
            )
        }
        save(wf)
        return wf
    }
}
