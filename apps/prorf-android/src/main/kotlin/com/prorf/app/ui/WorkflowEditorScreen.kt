package com.prorf.app.ui

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prorf.app.viewmodel.WorkflowEditorState
import com.prorf.app.viewmodel.WorkflowEditorViewModel
import com.prorf.platform.graph.NodeInstance
import com.prorf.ui.canvas.WorkflowCanvas
import com.prorf.ui.inspector.Inspector
import com.prorf.ui.model.EdgeDirection
import com.prorf.ui.model.NodeStatus
import com.prorf.ui.model.UiEdgeRow
import com.prorf.ui.model.UiNodeCard

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
    var isChainView by remember { mutableStateOf(false) }
    var canvasScale by remember { mutableStateOf(1f) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var resetViewKey by remember { mutableStateOf(0) }

    // Pre-compute selection state — shared by Scaffold content and ModalBottomSheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val readyState = state as? WorkflowEditorState.Ready
    val selectedNode = readyState?.selectedNodeId?.let { id -> readyState.graph.nodes.find { it.id == id } }
    val selectedDefinition = selectedNode?.let { n ->
        (application as? com.prorf.app.ProRfApp)?.pluginRegistry?.getDefinition(n.typeId)
    }
    val connectedEdges: List<UiEdgeRow> = if (readyState != null && selectedNode != null) {
        readyState.graph.edges.mapNotNull { edge ->
            when {
                edge.fromNodeId == selectedNode.id -> {
                    readyState.graph.nodes.find { it.id == edge.toNodeId }?.let {
                        UiEdgeRow(
                            edgeId = edge.id,
                            otherNodeLabel = it.label ?: it.typeId.substringAfterLast('.'),
                            direction = EdgeDirection.OUTGOING,
                        )
                    }
                }
                edge.toNodeId == selectedNode.id -> {
                    readyState.graph.nodes.find { it.id == edge.fromNodeId }?.let {
                        UiEdgeRow(
                            edgeId = edge.id,
                            otherNodeLabel = it.label ?: it.typeId.substringAfterLast('.'),
                            direction = EdgeDirection.INCOMING,
                        )
                    }
                }
                else -> null
            }
        }
    } else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = (state as? WorkflowEditorState.Ready)?.graph?.name ?: "Editor"
                    Column(modifier = Modifier.clickable { showRenameDialog = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Rename",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                        val ready = state as? WorkflowEditorState.Ready
                        if (ready != null) {
                            Text(
                                "${ready.graph.nodes.size} nodes · ${ready.graph.edges.size} edges",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
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
                        IconButton(onClick = { isChainView = !isChainView }) {
                            Icon(
                                if (isChainView) Icons.Default.ViewModule else Icons.Default.TableRows,
                                contentDescription = if (isChainView) "Canvas view" else "Chain view",
                            )
                        }
                        IconButton(onClick = { showAddNodeDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add node")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            if (state is WorkflowEditorState.Ready) {
                FloatingActionButton(
                    onClick = { vm.run() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run workflow")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val s = state) {
            WorkflowEditorState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WorkflowEditorState.Ready -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Full-width canvas or chain view (inspector is now a bottom sheet)
                    if (isChainView) {
                        ChainListView(
                            uiCards = s.uiCards,
                            nodeInstances = s.graph.nodes,
                            executionOutputs = s.executionOutputs,
                            selectedNodeId = s.selectedNodeId,
                            workflowName = s.graph.name,
                            onNodeSelected = { nodeId ->
                                if (s.connectingFromNodeId != null) vm.finishConnecting(nodeId)
                                else vm.selectNode(nodeId)
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            WorkflowCanvas(
                                nodes = s.uiCards,
                                edges = s.graph.edges,
                                onNodeSelected = { nodeId ->
                                    if (s.connectingFromNodeId != null) vm.finishConnecting(nodeId)
                                    else vm.selectNode(nodeId)
                                },
                                onNodeMoved = { id, x, y -> vm.moveNode(id, x, y) },
                                connectingFromNodeId = s.connectingFromNodeId,
                                executionOutputs = s.executionOutputs,
                                scale = canvasScale,
                                onScaleChanged = { canvasScale = it },
                                resetViewKey = resetViewKey,
                                modifier = Modifier.fillMaxSize(),
                            )

                            if (s.uiCards.isEmpty() && s.connectingFromNodeId == null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 32.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                                    CircleShape,
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                Icons.Default.AccountTree,
                                                contentDescription = null,
                                                modifier = Modifier.size(36.dp),
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                            )
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "Empty Workflow",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "Tap  +  to add the first node",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }

                            CanvasToolbar(
                                scale = canvasScale,
                                onZoomIn = { canvasScale = (canvasScale * 1.25f).coerceAtMost(2.5f) },
                                onZoomOut = { canvasScale = (canvasScale / 1.25f).coerceAtLeast(0.4f) },
                                onResetView = { canvasScale = 1f; resetViewKey++ },
                                onSwitchToChain = { isChainView = true },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp),
                            )
                        }
                    }

                    // Execution error strip at bottom
                    if (s.executionErrors.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.97f),
                            shadowElevation = 4.dp,
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                s.executionErrors.forEach { err ->
                                    Text(
                                        "⚠ [${err.nodeId.take(12)}]: ${err.message}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Inspector bottom sheet ────────────────────────────────────────────────────
    // Shown when a node is selected and not in connect mode (connect mode needs canvas visible).
    val showInspector = readyState != null && selectedNode != null && selectedDefinition != null
        && readyState.connectingFromNodeId == null
    if (showInspector) {
        val sheetContentHeight = LocalConfiguration.current.screenHeightDp.dp * 0.68f
        ModalBottomSheet(
            onDismissRequest = { vm.selectNode(null) },
            sheetState = sheetState,
        ) {
            Inspector(
                nodeInstance = selectedNode!!,
                definition = selectedDefinition!!,
                executionOutputs = readyState!!.executionOutputs[selectedNode.id] ?: emptyMap(),
                onParameterChanged = { key, value -> vm.updateParameter(selectedNode.id, key, value) },
                onConnectRequested = if (readyState.connectingFromNodeId == null) {
                    { vm.startConnecting(selectedNode.id) }
                } else null,
                connectedEdges = connectedEdges,
                onEdgeDeleteRequested = { edgeId -> vm.deleteEdge(edgeId) },
                onNodeDeleteRequested = { vm.deleteNode(selectedNode.id) },
                onNodeRenameRequested = { newLabel -> vm.renameNode(selectedNode.id, newLabel) },
                modifier = Modifier.fillMaxWidth().height(sheetContentHeight),
            )
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

    if (showRenameDialog) {
        val currentName = (state as? WorkflowEditorState.Ready)?.graph?.name ?: "Workflow"
        RenameWorkflowDialog(
            currentName = currentName,
            onRename = { newName ->
                vm.renameWorkflow(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }
}

// ── Canvas toolbar ─────────────────────────────────────────────────────────────

/** Floating pill toolbar overlaid at the bottom of the canvas. */
@Composable
private fun CanvasToolbar(
    scale: Float = 1f,
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    onResetView: () -> Unit = {},
    onSwitchToChain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.size(36.dp),
                enabled = scale > 0.41f,
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Zoom out",
                    modifier = Modifier.size(18.dp),
                    tint = if (scale > 0.41f) MaterialTheme.colorScheme.onSurfaceVariant
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                )
            }
            // Zoom level indicator
            Text(
                text = "${(scale * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.size(36.dp),
                enabled = scale < 2.49f,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Zoom in",
                    modifier = Modifier.size(18.dp),
                    tint = if (scale < 2.49f) MaterialTheme.colorScheme.onSurfaceVariant
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                )
            }
            IconButton(
                onClick = onResetView,
                modifier = Modifier.size(36.dp),
                enabled = scale != 1f,
            ) {
                Icon(
                    Icons.Default.ZoomOutMap,
                    contentDescription = "Reset view",
                    modifier = Modifier.size(18.dp),
                    tint = if (scale != 1f) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                )
            }
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onSwitchToChain, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.TableRows,
                    contentDescription = "Chain view",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// ── Chain list view ────────────────────────────────────────────────────────────

@Composable
private fun ChainListView(
    uiCards: List<UiNodeCard>,
    nodeInstances: List<NodeInstance>,
    executionOutputs: Map<String, Map<String, Any>>,
    selectedNodeId: String?,
    onNodeSelected: (String) -> Unit,
    workflowName: String? = null,
    modifier: Modifier = Modifier,
) {
    val instanceMap = remember(nodeInstances) { nodeInstances.associateBy { it.id } }
    val sortedCards = uiCards.sortedBy { it.x }

    if (sortedCards.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Empty Workflow",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tap  +  to add the first node",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    ) {
        item {
            ChainListHeader(
                stageCount = sortedCards.size,
                executionOutputs = executionOutputs,
                sortedCards = sortedCards,
                workflowName = workflowName,
            )
        }

        itemsIndexed(sortedCards, key = { _, card -> card.nodeId }) { index, card ->
            ChainNodeRow(
                card = card,
                stepNumber = index + 1,
                outputs = executionOutputs[card.nodeId] ?: emptyMap(),
                nodeParams = instanceMap[card.nodeId]?.parameters ?: emptyMap(),
                isSelected = card.nodeId == selectedNodeId,
                onClick = { onNodeSelected(card.nodeId) },
            )
            if (index < sortedCards.size - 1) {
                val primaryOutput = executionOutputs[card.nodeId]?.entries?.firstOrNull()?.value
                val flowingValue = primaryOutput?.let { WorkflowEditorViewModel.formatSummaryValue(it) }
                ChainConnector(flowingValue = flowingValue)
            }
        }

        item {
            SignalChainChart(
                sortedCards = sortedCards,
                executionOutputs = executionOutputs,
            )
        }
    }
}

@Composable
private fun ChainListHeader(
    stageCount: Int,
    executionOutputs: Map<String, Map<String, Any>>,
    sortedCards: List<UiNodeCard>,
    workflowName: String? = null,
) {
    val hasResults = executionOutputs.isNotEmpty()
    val allComputed = sortedCards.isNotEmpty() && sortedCards.all { executionOutputs.containsKey(it.nodeId) }
    val lastCard = sortedCards.lastOrNull()
    val lastOutput = lastCard?.let { executionOutputs[it.nodeId]?.entries?.firstOrNull() }
    val lastFormatted = lastOutput?.value?.let { WorkflowEditorViewModel.formatSummaryValue(it) }
    val isNegFinal = lastFormatted?.trimStart()?.startsWith("-") == true
    val finalColor = if (isNegFinal) MaterialTheme.colorScheme.error
                     else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = workflowName ?: "Signal Chain",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "$stageCount stage${if (stageCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                )
                if (allComputed) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color(0xFF22C55E), CircleShape),
                    )
                }
            }
        }
        if (hasResults && lastFormatted != null) {
            val (numPart, unitPart) = splitValueUnit(lastFormatted)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = finalColor.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Out ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                    )
                    Text(
                        text = numPart,
                        style = MaterialTheme.typography.labelMedium,
                        color = finalColor,
                        fontWeight = FontWeight.Bold,
                    )
                    if (unitPart.isNotEmpty()) {
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = unitPart,
                            style = MaterialTheme.typography.labelSmall,
                            color = finalColor.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainNodeRow(
    card: UiNodeCard,
    stepNumber: Int,
    outputs: Map<String, Any>,
    nodeParams: Map<String, Any>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val catColor = chainNodeColor(card.typeId)
    val abbr = categoryAbbr(card.typeId)
    val primaryOutput = outputs.entries.firstOrNull()
    val formattedValue = primaryOutput?.let { WorkflowEditorViewModel.formatSummaryValue(it.value) }
    val isNeg = formattedValue?.trimStart()?.startsWith("-") == true
    val paramSummary = buildParamSummary(nodeParams)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
            else MaterialTheme.colorScheme.surface,
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Step number + circular category icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(46.dp),
            ) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(catColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = abbr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Node display name + type identifier + param summary
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = card.typeId.substringAfterLast('.'),
                    style = MaterialTheme.typography.labelSmall,
                    color = catColor.copy(alpha = 0.75f),
                    maxLines = 1,
                )
                if (paramSummary.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = paramSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Primary output value + unit, or status indicator
            if (formattedValue != null) {
                val (numPart, unitPart) = splitValueUnit(formattedValue)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = numPart,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isNeg) MaterialTheme.colorScheme.error else catColor,
                    )
                    if (unitPart.isNotEmpty()) {
                        Text(
                            text = unitPart,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                        )
                    }
                }
            } else {
                val statusColor = when (card.status) {
                    NodeStatus.IDLE -> Color(0xFF9CA3AF)
                    NodeStatus.RUNNING -> Color(0xFF3B82F6)
                    NodeStatus.SUCCESS -> Color(0xFF22C55E)
                    NodeStatus.ERROR -> Color(0xFFEF4444)
                }
                Text(
                    text = when (card.status) {
                        NodeStatus.IDLE -> "—"
                        NodeStatus.RUNNING -> "…"
                        NodeStatus.SUCCESS -> "✓"
                        NodeStatus.ERROR -> "!"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = statusColor,
                )
            }
        }
    }
}

@Composable
private fun ChainConnector(flowingValue: String? = null) {
    val connectorColor = MaterialTheme.colorScheme.outlineVariant
    val positiveColor = MaterialTheme.colorScheme.primary
    val negativeColor = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier.padding(start = 25.dp, top = 1.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(20.dp).height(22.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val lineEnd = size.height * 0.62f
                drawLine(
                    color = connectorColor,
                    start = Offset(cx, 0f),
                    end = Offset(cx, lineEnd),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                val aw = 4.dp.toPx()
                val ah = 6.dp.toPx()
                drawPath(
                    path = Path().apply {
                        moveTo(cx - aw, lineEnd)
                        lineTo(cx, lineEnd + ah)
                        lineTo(cx + aw, lineEnd)
                    },
                    color = connectorColor,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
        if (flowingValue != null) {
            Spacer(Modifier.width(6.dp))
            val isNeg = flowingValue.trimStart().startsWith("-")
            Text(
                text = flowingValue,
                style = MaterialTheme.typography.labelSmall,
                color = if (isNeg) negativeColor.copy(alpha = 0.65f)
                        else positiveColor.copy(alpha = 0.65f),
                fontSize = 9.sp,
            )
        }
    }
}

/**
 * Bar chart comparing primary output values across the signal chain.
 * Only renders when ≥2 nodes have numeric execution outputs.
 */
@Composable
private fun SignalChainChart(
    sortedCards: List<UiNodeCard>,
    executionOutputs: Map<String, Map<String, Any>>,
) {
    val dataPoints = sortedCards.mapNotNull { card ->
        val rawValue = executionOutputs[card.nodeId]?.entries?.firstOrNull()?.value
            ?: return@mapNotNull null
        val formatted = WorkflowEditorViewModel.formatSummaryValue(rawValue)
        val numeric = extractNumeric(formatted) ?: return@mapNotNull null
        Triple(card, formatted, numeric)
    }
    if (dataPoints.size < 2) return

    val minVal = dataPoints.minOf { it.third }
    val maxVal = dataPoints.maxOf { it.third }
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val barColors = dataPoints.map { (card, _, _) -> chainNodeColor(card.typeId) }
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Spacer(Modifier.height(12.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Signal Level",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "%.1f … %.1f".format(minVal, maxVal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            ) {
                val n = dataPoints.size
                val slotW = size.width / n
                val barW = slotW * 0.52f
                val labelZoneH = 18.dp.toPx()
                val chartH = size.height - labelZoneH
                val baseline = chartH
                val maxBarH = chartH * 0.85f
                val minBarH = 4.dp.toPx()
                val cornerR = (barW / 3f).coerceAtMost(6.dp.toPx())

                drawLine(
                    color = outlineColor,
                    start = Offset(0f, baseline),
                    end = Offset(size.width, baseline),
                    strokeWidth = 1f,
                )

                dataPoints.forEachIndexed { i, (_, formatted, value) ->
                    val cx = slotW * i + slotW / 2f
                    val normalizedH = ((value - minVal) / range).toFloat() * maxBarH
                    val barH = normalizedH.coerceAtLeast(minBarH)
                    // Rounded top corners only — draw a full RoundRect then cover bottom corners
                    drawRoundRect(
                        color = barColors[i],
                        topLeft = Offset(cx - barW / 2f, baseline - barH),
                        size = Size(barW, barH + cornerR),
                        cornerRadius = CornerRadius(cornerR),
                    )
                    // Cover bottom half of corners with a plain rect
                    drawRect(
                        color = barColors[i],
                        topLeft = Offset(cx - barW / 2f, baseline - minBarH.coerceAtLeast(cornerR)),
                        size = Size(barW, minBarH.coerceAtLeast(cornerR) + cornerR),
                    )
                }
            }

            // Abbreviated category labels below bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                dataPoints.forEach { (card, _, _) ->
                    Text(
                        text = categoryAbbr(card.typeId),
                        style = MaterialTheme.typography.labelSmall,
                        color = chainNodeColor(card.typeId),
                        fontSize = 9.sp,
                    )
                }
            }
        }
    }
}

// ── Rename workflow dialog ────────────────────────────────────────────────────

@Composable
private fun RenameWorkflowDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nameText by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rename Workflow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Workflow name", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (nameText.isNotBlank()) onRename(nameText.trim())
                }),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (nameText.isNotBlank()) onRename(nameText.trim()) },
                enabled = nameText.isNotBlank(),
            ) { Text("Rename") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

// ── Add-node dialog ───────────────────────────────────────────────────────────

@Composable
private fun AddNodeDialog(
    nodeTypes: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = if (searchQuery.isBlank()) nodeTypes
    else nodeTypes.filter {
        it.substringAfterLast('.').contains(searchQuery, ignoreCase = true)
    }

    // Build ordered category groups preserving natural order
    val categoryOrder = listOf("Source", "Active", "Passive", "Channel", "Receiver", "Other")
    val byCategory = filtered
        .groupBy { typeToNodeCategory(it) }
        .let { map -> categoryOrder.mapNotNull { cat -> map[cat]?.let { cat to it } } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "Add Node",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search nodes…", style = MaterialTheme.typography.bodySmall)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                if (byCategory.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "No matches",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                byCategory.forEach { (category, types) ->
                    val catColor = nodeCategoryColor(category)
                    item(key = "cat_$category") {
                        Row(
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .background(catColor, CircleShape),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = catColor,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp,
                            )
                        }
                    }
                    items(types, key = { it }) { typeId ->
                        NodeTypePickerRow(
                            typeId = typeId,
                            catColor = catColor,
                            onClick = { onSelect(typeId) },
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

@Composable
private fun NodeTypePickerRow(typeId: String, catColor: Color, onClick: () -> Unit) {
    val name = typeId.substringAfterLast('.')
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(catColor.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = categoryAbbr(typeId).take(3),
                style = MaterialTheme.typography.labelSmall,
                color = catColor,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun chainNodeColor(typeId: String): Color {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Source") || name.contains("Signal") || name.contains("Noise") -> Color(0xFF2F80ED)
        name.contains("Amplifier") -> Color(0xFF27AE60)
        name.contains("Attenuator") || name.contains("Cable") || name.contains("Filter") -> Color(0xFFF2994A)
        name.contains("Loss") || name.contains("Channel") || name.contains("Path") -> Color(0xFF9B51E0)
        name.contains("Receiver") || name.contains("Sensitivity") -> Color(0xFFEB5757)
        else -> Color(0xFF64748B)
    }
}

private fun categoryAbbr(typeId: String): String {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Signal") -> "SRC"
        name.contains("Noise") -> "NSE"
        name.contains("Amplifier") -> "AMP"
        name.contains("Attenuator") -> "ATT"
        name.contains("Cable") -> "CBL"
        name.contains("Filter") -> "FLT"
        name.contains("Loss") || name.contains("Path") || name.contains("Channel") -> "CH"
        name.contains("Receiver") -> "RCV"
        name.contains("Sensitivity") -> "SNS"
        else -> name.take(3).uppercase()
    }
}

private fun typeToNodeCategory(typeId: String): String {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Source") || name.contains("Signal") || name.contains("Noise") -> "Source"
        name.contains("Amplifier") -> "Active"
        name.contains("Attenuator") || name.contains("Cable") || name.contains("Filter") -> "Passive"
        name.contains("Loss") || name.contains("Channel") || name.contains("Path") -> "Channel"
        name.contains("Receiver") || name.contains("Sensitivity") -> "Receiver"
        else -> "Other"
    }
}

private fun nodeCategoryColor(category: String): Color = when (category) {
    "Source" -> Color(0xFF2F80ED)
    "Active" -> Color(0xFF27AE60)
    "Passive" -> Color(0xFFF2994A)
    "Channel" -> Color(0xFF9B51E0)
    "Receiver" -> Color(0xFFEB5757)
    else -> Color(0xFF64748B)
}

/** Splits "−25.3 dBm" → ("−25.3", "dBm"). Handles values with no unit. */
private fun splitValueUnit(formatted: String): Pair<String, String> {
    val trimmed = formatted.trim()
    val spaceIdx = trimmed.indexOf(' ')
    return if (spaceIdx > 0) {
        Pair(trimmed.substring(0, spaceIdx), trimmed.substring(spaceIdx + 1))
    } else {
        Pair(trimmed, "")
    }
}

/** Extracts the leading numeric portion from a formatted value string. */
private fun extractNumeric(formatted: String): Double? =
    formatted.trim().split(" ").firstOrNull()?.toDoubleOrNull()

/**
 * Formats the first 2 node parameters as a compact engineering summary string.
 * Maps common RF parameter keys to short abbreviations for readability.
 */
private fun buildParamSummary(params: Map<String, Any>): String {
    if (params.isEmpty()) return ""
    val shortKey: (String) -> String = { key ->
        when (key.lowercase()) {
            "gain" -> "G"
            "noisefigure", "noise_figure" -> "NF"
            "loss" -> "L"
            "power" -> "P"
            "frequency" -> "f"
            "distance" -> "d"
            "bandwidth" -> "BW"
            "requiredsnr", "required_snr" -> "SNR"
            "temperature" -> "T"
            else -> key.take(3).replaceFirstChar { it.uppercase() }
        }
    }
    return params.entries
        .take(2)
        .joinToString("  ") { (key, value) ->
            val v = WorkflowEditorViewModel.formatSummaryValue(value)
            "${shortKey(key)}: $v"
        }
}
