package com.prorf.app.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prorf.app.viewmodel.WorkflowEditorState
import com.prorf.app.viewmodel.WorkflowEditorViewModel
import com.prorf.ui.canvas.WorkflowCanvas
import com.prorf.ui.inspector.Inspector
import com.prorf.ui.model.EdgeDirection
import com.prorf.ui.model.UiEdgeRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditorScreen(workflowId: String, onBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val vm: WorkflowEditorViewModel = viewModel(
        key = workflowId,
        factory = WorkflowEditorViewModel.Factory(application, workflowId),
    )
    val state by vm.state.collectAsState()
    var showAddNodeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = (state as? WorkflowEditorState.Ready)?.graph?.name ?: "Editor"
                    Text(name)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val ready = state as? WorkflowEditorState.Ready
                    if (ready?.connectingFromNodeId != null) {
                        TextButton(onClick = { vm.cancelConnecting() }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        IconButton(onClick = { showAddNodeDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add node")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (state is WorkflowEditorState.Ready) {
                FloatingActionButton(onClick = { vm.run() }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                }
            }
        },
    ) { padding ->
        when (val s = state) {
            WorkflowEditorState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WorkflowEditorState.Ready -> {
                Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                    WorkflowCanvas(
                        nodes = s.uiCards,
                        edges = s.graph.edges,
                        onNodeSelected = { nodeId ->
                            if (s.connectingFromNodeId != null) vm.finishConnecting(nodeId)
                            else vm.selectNode(nodeId)
                        },
                        onNodeMoved = { id, x, y -> vm.moveNode(id, x, y) },
                        connectingFromNodeId = s.connectingFromNodeId,
                        modifier = Modifier.weight(1f),
                    )

                    val selectedNode = s.selectedNodeId?.let { id ->
                        s.graph.nodes.find { it.id == id }
                    }
                    val selectedDefinition = selectedNode?.let { n ->
                        (application as? com.prorf.app.ProRfApp)
                            ?.pluginRegistry?.getDefinition(n.typeId)
                    }

                    if (selectedNode != null && selectedDefinition != null) {
                        val connectedEdges = s.graph.edges.mapNotNull { edge ->
                            when {
                                edge.fromNodeId == selectedNode.id -> {
                                    val other = s.graph.nodes.find { it.id == edge.toNodeId }
                                    other?.let {
                                        UiEdgeRow(
                                            edgeId = edge.id,
                                            otherNodeLabel = it.typeId.substringAfterLast('.'),
                                            direction = EdgeDirection.OUTGOING,
                                        )
                                    }
                                }
                                edge.toNodeId == selectedNode.id -> {
                                    val other = s.graph.nodes.find { it.id == edge.fromNodeId }
                                    other?.let {
                                        UiEdgeRow(
                                            edgeId = edge.id,
                                            otherNodeLabel = it.typeId.substringAfterLast('.'),
                                            direction = EdgeDirection.INCOMING,
                                        )
                                    }
                                }
                                else -> null
                            }
                        }

                        HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
                        Inspector(
                            nodeInstance = selectedNode,
                            definition = selectedDefinition,
                            executionOutputs = s.executionOutputs[selectedNode.id] ?: emptyMap(),
                            onParameterChanged = { key, value -> vm.updateParameter(selectedNode.id, key, value) },
                            onConnectRequested = if (s.connectingFromNodeId == null) {
                                { vm.startConnecting(selectedNode.id) }
                            } else null,
                            connectedEdges = connectedEdges,
                            onEdgeDeleteRequested = { edgeId -> vm.deleteEdge(edgeId) },
                            onNodeDeleteRequested = { vm.deleteNode(selectedNode.id) },
                            modifier = Modifier.width(280.dp).fillMaxHeight(),
                        )
                    }
                }

                if (s.executionErrors.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.BottomCenter) {
                        Column {
                            s.executionErrors.forEach { err ->
                                Text(
                                    "Error [${err.nodeId}]: ${err.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNodeDialog) {
        val ready = state as? WorkflowEditorState.Ready
        AddNodeDialog(
            nodeTypes = ready?.let { vm.availableNodeTypes() } ?: emptyList(),
            onSelect = { typeId ->
                vm.addNode(typeId)
                showAddNodeDialog = false
            },
            onDismiss = { showAddNodeDialog = false },
        )
    }
}

@Composable
private fun AddNodeDialog(
    nodeTypes: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Node") },
        text = {
            LazyColumn {
                items(nodeTypes) { typeId ->
                    TextButton(
                        onClick = { onSelect(typeId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            typeId.substringAfterLast('.'),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
