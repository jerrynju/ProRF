package com.prorf.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prorf.app.ProRfApp
import com.prorf.app.data.WorkflowRepository
import com.prorf.domains.rf.WorkflowTemplates
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.execution.DagExecutionEngine
import com.prorf.platform.execution.ExecutionContext
import com.prorf.platform.execution.ExecutionError
import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.NodePosition
import com.prorf.platform.graph.WorkflowGraph
import com.prorf.ui.model.NodeStatus
import com.prorf.ui.model.UiNodeCard
import com.prorf.ui.model.toUiCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface WorkflowEditorState {
    object Loading : WorkflowEditorState
    data class Ready(
        val graph: WorkflowGraph,
        val selectedNodeId: String? = null,
        val executionOutputs: Map<String, Map<String, Any>> = emptyMap(),
        val executionErrors: List<ExecutionError> = emptyList(),
        val isRunning: Boolean = false,
        val connectingFromNodeId: String? = null,
    ) : WorkflowEditorState {
        val uiCards: List<UiNodeCard>
            get() = graph.nodes.map { node ->
                val outputs = executionOutputs[node.id]
                val status = when {
                    isRunning -> NodeStatus.RUNNING
                    executionErrors.any { it.nodeId == node.id } -> NodeStatus.ERROR
                    outputs != null -> NodeStatus.SUCCESS
                    else -> NodeStatus.IDLE
                }
                val summary = outputs?.entries
                    ?.take(2)
                    ?.joinToString("  ") { (k, v) ->
                        "$k: ${formatSummaryValue(v)}"
                    }
                    ?: ""
                node.toUiCard(
                    displayName = node.typeId.substringAfterLast('.'),
                    isSelected = node.id == selectedNodeId,
                    status = status,
                    outputSummary = summary,
                )
            }
    }
}

class WorkflowEditorViewModel(
    application: Application,
    private val workflowId: String,
) : AndroidViewModel(application) {

    private val app = application as ProRfApp
    private val repository = WorkflowRepository(application.filesDir)
    private val engine = DagExecutionEngine(app.pluginRegistry)

    private val _state = MutableStateFlow<WorkflowEditorState>(WorkflowEditorState.Loading)
    val state: StateFlow<WorkflowEditorState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = WorkflowEditorState.Ready(graph = resolveGraph())
        }
    }

    private fun resolveGraph(): WorkflowGraph = when {
        workflowId.startsWith("template:") -> {
            val template = WorkflowTemplates.findById(workflowId)
                ?: WorkflowTemplates.all.first()
            // Materialize template as a new saved workflow
            val copy = template.copy(id = UUID.randomUUID().toString())
            repository.save(copy)
            copy
        }
        workflowId == "new" -> repository.create("New Workflow")
        else -> repository.load(workflowId) ?: repository.create("Recovered Workflow")
    }

    fun run() {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        _state.value = ready.copy(isRunning = true)
        viewModelScope.launch(Dispatchers.Default) {
            val result = engine.execute(ready.graph, ExecutionContext())
            _state.value = ready.copy(
                isRunning = false,
                executionOutputs = result.outputs,
                executionErrors = result.errors,
            )
        }
    }

    fun selectNode(nodeId: String?) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        _state.value = ready.copy(selectedNodeId = nodeId)
    }

    fun moveNode(nodeId: String, x: Float, y: Float) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        val updated = ready.graph.copy(
            nodes = ready.graph.nodes.map { n ->
                if (n.id == nodeId) n.copy(position = NodePosition(x, y)) else n
            },
        )
        _state.value = ready.copy(graph = updated)
        autoSave(updated)
    }

    fun addNode(typeId: String) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        val definition = app.pluginRegistry.getDefinition(typeId) ?: return
        val defaults = definition.parameters.associate { it.key to (it.defaultValue ?: 0.0) }
        val newNode = NodeInstance(
            id = "node_${UUID.randomUUID().toString().take(8)}",
            typeId = typeId,
            parameters = defaults,
            position = NodePosition(60f + ready.graph.nodes.size * 180f, 100f),
        )
        val updated = ready.graph.copy(nodes = ready.graph.nodes + newNode)
        _state.value = ready.copy(graph = updated)
        autoSave(updated)
    }

    fun updateParameter(nodeId: String, key: String, value: Any) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        val updatedGraph = ready.graph.copy(
            nodes = ready.graph.nodes.map { n ->
                if (n.id == nodeId) n.copy(parameters = n.parameters + (key to value)) else n
            },
        )
        _state.value = ready.copy(graph = updatedGraph)
        autoSave(updatedGraph)
        viewModelScope.launch(Dispatchers.Default) {
            val result = engine.execute(updatedGraph, ExecutionContext())
            val current = _state.value as? WorkflowEditorState.Ready ?: return@launch
            _state.value = current.copy(
                executionOutputs = result.outputs,
                executionErrors = result.errors,
            )
        }
    }

    fun startConnecting(fromNodeId: String) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        _state.value = ready.copy(connectingFromNodeId = fromNodeId)
    }

    fun finishConnecting(toNodeId: String) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        val fromNodeId = ready.connectingFromNodeId ?: return
        if (fromNodeId == toNodeId) { cancelConnecting(); return }

        val fromNode = ready.graph.nodes.find { it.id == fromNodeId } ?: return
        val toNode = ready.graph.nodes.find { it.id == toNodeId } ?: return

        val fromDef = app.pluginRegistry.getDefinition(fromNode.typeId) ?: return
        val toDef = app.pluginRegistry.getDefinition(toNode.typeId) ?: return

        val fromPortId = fromDef.outputs.firstOrNull()?.id ?: return
        val toPortId = toDef.inputs.firstOrNull()?.id ?: return

        val alreadyConnected = ready.graph.edges.any {
            it.fromNodeId == fromNodeId && it.toNodeId == toNodeId &&
                it.fromPortId == fromPortId && it.toPortId == toPortId
        }
        if (alreadyConnected) { cancelConnecting(); return }

        val edge = Edge(
            id = "edge_${UUID.randomUUID().toString().take(8)}",
            fromNodeId = fromNodeId,
            fromPortId = fromPortId,
            toNodeId = toNodeId,
            toPortId = toPortId,
        )
        val updated = ready.graph.copy(edges = ready.graph.edges + edge)
        _state.value = ready.copy(graph = updated, connectingFromNodeId = null)
        autoSave(updated)
        viewModelScope.launch(Dispatchers.Default) {
            val result = engine.execute(updated, ExecutionContext())
            val current = _state.value as? WorkflowEditorState.Ready ?: return@launch
            _state.value = current.copy(
                executionOutputs = result.outputs,
                executionErrors = result.errors,
            )
        }
    }

    fun cancelConnecting() {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        _state.value = ready.copy(connectingFromNodeId = null)
    }

    fun deleteEdge(edgeId: String) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        val updated = ready.graph.copy(edges = ready.graph.edges.filter { it.id != edgeId })
        _state.value = ready.copy(graph = updated)
        autoSave(updated)
        viewModelScope.launch(Dispatchers.Default) {
            val result = engine.execute(updated, ExecutionContext())
            val current = _state.value as? WorkflowEditorState.Ready ?: return@launch
            _state.value = current.copy(
                executionOutputs = result.outputs,
                executionErrors = result.errors,
            )
        }
    }

    fun deleteNode(nodeId: String) {
        val ready = _state.value as? WorkflowEditorState.Ready ?: return
        val updatedGraph = ready.graph.copy(
            nodes = ready.graph.nodes.filter { it.id != nodeId },
            edges = ready.graph.edges.filter { it.fromNodeId != nodeId && it.toNodeId != nodeId },
        )
        val newSelected = if (ready.selectedNodeId == nodeId) null else ready.selectedNodeId
        _state.value = ready.copy(graph = updatedGraph, selectedNodeId = newSelected)
        autoSave(updatedGraph)
        viewModelScope.launch(Dispatchers.Default) {
            val result = engine.execute(updatedGraph, ExecutionContext())
            val current = _state.value as? WorkflowEditorState.Ready ?: return@launch
            _state.value = current.copy(
                executionOutputs = result.outputs,
                executionErrors = result.errors,
            )
        }
    }

    fun availableNodeTypes(): List<String> = app.pluginRegistry.allDefinitions().map { it.typeId }

    private fun autoSave(graph: WorkflowGraph) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.save(graph)
        }
    }

    private fun formatSummaryValue(value: Any): String = when (value) {
        is Quantity -> "%.1f %s".format(value.value, value.unit.symbol)
        is Number -> "%.1f".format(value.toDouble())
        else -> value.toString()
    }

    class Factory(
        private val application: Application,
        private val workflowId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WorkflowEditorViewModel(application, workflowId) as T
    }
}
