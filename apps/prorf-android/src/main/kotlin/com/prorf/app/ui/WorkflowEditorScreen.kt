package com.prorf.app.ui

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.prorf.ui.canvas.categoryAbbr
import com.prorf.ui.canvas.categoryColor
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
    var isChainView by remember { mutableStateOf(true) }
    var canvasScale by remember { mutableStateOf(1f) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var resetViewKey by remember { mutableStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val readyState = state as? WorkflowEditorState.Ready
    val selectedNode = readyState?.selectedNodeId
        ?.let { id -> readyState.graph.nodes.find { it.id == id } }
    val selectedDefinition = selectedNode?.let { n ->
        (application as? com.prorf.app.ProRfApp)?.pluginRegistry?.getDefinition(n.typeId)
    }
    val connectedEdges: List<UiEdgeRow> = if (readyState != null && selectedNode != null) {
        readyState.graph.edges.mapNotNull { edge ->
            when {
                edge.fromNodeId == selectedNode.id ->
                    readyState.graph.nodes.find { it.id == edge.toNodeId }?.let {
                        UiEdgeRow(edge.id, it.label ?: it.typeId.substringAfterLast('.'), EdgeDirection.OUTGOING)
                    }
                edge.toNodeId == selectedNode.id ->
                    readyState.graph.nodes.find { it.id == edge.fromNodeId }?.let {
                        UiEdgeRow(edge.id, it.label ?: it.typeId.substringAfterLast('.'), EdgeDirection.INCOMING)
                    }
                else -> null
            }
        }
    } else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.clickable { showRenameDialog = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                (state as? WorkflowEditorState.Ready)?.graph?.name ?: "Editor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
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
                        // View toggle
                        IconButton(onClick = { isChainView = !isChainView }) {
                            Icon(
                                if (isChainView) Icons.Default.ViewModule else Icons.Default.AccountTree,
                                contentDescription = if (isChainView) "Canvas" else "Flow",
                            )
                        }
                        IconButton(onClick = { showAddNodeDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add node")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            if (state is WorkflowEditorState.Ready && !isChainView) {
                FloatingActionButton(
                    onClick = { vm.run() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
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
                Box(Modifier.fillMaxSize().padding(padding)) {
                    if (isChainView) {
                        // ── Flow / chain view ────────────────────────────────────────
                        FlowView(
                            uiCards = s.uiCards,
                            nodeInstances = s.graph.nodes,
                            executionOutputs = s.executionOutputs,
                            selectedNodeId = s.selectedNodeId,
                            workflowName = s.graph.name,
                            nodeCount = s.graph.nodes.size,
                            edgeCount = s.graph.edges.size,
                            onNodeSelected = { nodeId ->
                                if (s.connectingFromNodeId != null) vm.finishConnecting(nodeId)
                                else vm.selectNode(nodeId)
                            },
                            onRun = { vm.run() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        // ── Canvas view ──────────────────────────────────────────────
                        Box(Modifier.fillMaxSize()) {
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

                            if (s.uiCards.isEmpty()) {
                                EmptyCanvasHint(modifier = Modifier.fillMaxSize())
                            }

                            CanvasToolbar(
                                scale = canvasScale,
                                onZoomIn = { canvasScale = (canvasScale * 1.25f).coerceAtMost(2.5f) },
                                onZoomOut = { canvasScale = (canvasScale / 1.25f).coerceAtLeast(0.4f) },
                                onResetView = { canvasScale = 1f; resetViewKey++ },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp),
                            )
                        }
                    }

                    if (s.executionErrors.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.97f),
                            shadowElevation = 4.dp,
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
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

    // ── Node Inspector bottom sheet ───────────────────────────────────────────
    val showInspector = readyState != null && selectedNode != null && selectedDefinition != null
        && readyState.connectingFromNodeId == null
    if (showInspector) {
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
                onEdgeDeleteRequested = { vm.deleteEdge(it) },
                onNodeDeleteRequested = { vm.deleteNode(selectedNode.id) },
                onNodeRenameRequested = { vm.renameNode(selectedNode.id, it) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showAddNodeDialog) {
        val ready = state as? WorkflowEditorState.Ready
        AddNodeDialog(
            nodeTypes = ready?.let { vm.availableNodeTypes() } ?: emptyList(),
            onSelect = { vm.addNode(it); showAddNodeDialog = false },
            onDismiss = { showAddNodeDialog = false },
        )
    }

    if (showRenameDialog) {
        val currentName = (state as? WorkflowEditorState.Ready)?.graph?.name ?: "Workflow"
        RenameWorkflowDialog(
            currentName = currentName,
            onRename = { vm.renameWorkflow(it); showRenameDialog = false },
            onDismiss = { showRenameDialog = false },
        )
    }
}

// ── Flow / Chain View ─────────────────────────────────────────────────────────

@Composable
private fun FlowView(
    uiCards: List<UiNodeCard>,
    nodeInstances: List<NodeInstance>,
    executionOutputs: Map<String, Map<String, Any>>,
    selectedNodeId: String?,
    workflowName: String?,
    nodeCount: Int,
    edgeCount: Int,
    onNodeSelected: (String) -> Unit,
    onRun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val instanceMap = remember(nodeInstances) { nodeInstances.associateBy { it.id } }
    val sortedCards = uiCards.sortedBy { it.x }

    if (sortedCards.isEmpty()) {
        EmptyFlowHint(modifier = modifier)
        return
    }

    // Compute summary metrics from last node output and total gain
    val lastCard = sortedCards.lastOrNull()
    val lastOutput = lastCard?.let { executionOutputs[it.nodeId]?.entries?.firstOrNull()?.value }
    val lastFormatted = lastOutput?.let { WorkflowEditorViewModel.formatSummaryValue(it) }

    // Total gain = sum of positive gain values
    val totalGainDb = sortedCards.sumOf { card ->
        val v = executionOutputs[card.nodeId]?.entries?.firstOrNull()?.value
        val n = v?.let { WorkflowEditorViewModel.formatSummaryValue(it).trim().split(" ").firstOrNull()?.toDoubleOrNull() } ?: 0.0
        if (n > 0) n else 0.0
    }

    Column(modifier = modifier) {
        // ── Summary bar ───────────────────────────────────────────────────────
        if (lastFormatted != null) {
            FlowSummaryBar(
                primaryLabel = "Output",
                primaryValue = lastFormatted,
                secondaryLabel = "Total Gain",
                secondaryValue = if (totalGainDb > 0) "+%.1f dB".format(totalGainDb) else "—",
            )
        }

        // ── Node quick-jump chips ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sortedCards.forEachIndexed { idx, card ->
                val catColor = categoryColor(card.typeId)
                val abbr = categoryAbbr(card.typeId)
                val isSelected = card.nodeId == selectedNodeId
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) catColor else catColor.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { onNodeSelected(card.nodeId) },
                ) {
                    Text(
                        text = abbr,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else catColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            if (executionOutputs.isEmpty()) {
                Spacer(Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onRun),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Run",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        )
                    }
                }
            } else {
                Spacer(Modifier.width(12.dp))
                val finalColor = if (lastFormatted?.trimStart()?.startsWith("-") == true)
                    MaterialTheme.colorScheme.error else Color(0xFF22C55E)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = finalColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = lastFormatted ?: "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = finalColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // ── Chain rows ────────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            itemsIndexed(sortedCards, key = { _, c -> c.nodeId }) { index, card ->
                FlowNodeRow(
                    card = card,
                    stepNumber = index + 1,
                    outputs = executionOutputs[card.nodeId] ?: emptyMap(),
                    nodeParams = instanceMap[card.nodeId]?.parameters ?: emptyMap(),
                    isSelected = card.nodeId == selectedNodeId,
                    onClick = { onNodeSelected(card.nodeId) },
                )
                if (index < sortedCards.lastIndex) {
                    val val_ = executionOutputs[card.nodeId]?.entries?.firstOrNull()?.value
                    FlowConnector(val_?.let { WorkflowEditorViewModel.formatSummaryValue(it) })
                }
            }
        }

        // ── Bottom status bar ─────────────────────────────────────────────────
        FlowStatusBar(
            nodeCount = nodeCount,
            edgeCount = edgeCount,
            lastFormatted = lastFormatted,
            totalGainDb = if (totalGainDb > 0) totalGainDb else null,
        )
    }
}

@Composable
private fun FlowSummaryBar(
    primaryLabel: String,
    primaryValue: String,
    secondaryLabel: String,
    secondaryValue: String,
) {
    val (pNum, pUnit) = splitValueUnit(primaryValue)
    val isNeg = pNum.trimStart().startsWith("-")
    val primaryColor = if (isNeg) MaterialTheme.colorScheme.error else Color(0xFF22C55E)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            // Primary metric
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    primaryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.Baseline) {
                    Text(
                        pNum,
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                    )
                    if (pUnit.isNotEmpty()) {
                        Spacer(Modifier.width(3.dp))
                        Text(
                            pUnit,
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    .align(Alignment.CenterVertically),
            )
            Spacer(Modifier.width(16.dp))

            // Secondary metric
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    secondaryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val (sNum, sUnit) = splitValueUnit(secondaryValue)
                Row(verticalAlignment = Alignment.Baseline) {
                    Text(
                        sNum,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    if (sUnit.isNotEmpty()) {
                        Spacer(Modifier.width(3.dp))
                        Text(
                            sUnit,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowNodeRow(
    card: UiNodeCard,
    stepNumber: Int,
    outputs: Map<String, Any>,
    nodeParams: Map<String, Any>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val catColor = categoryColor(card.typeId)
    val abbr = categoryAbbr(card.typeId)
    val primaryOutput = outputs.entries.firstOrNull()
    val formattedValue = primaryOutput?.let { WorkflowEditorViewModel.formatSummaryValue(it.value) }
    val isNeg = formattedValue?.trimStart()?.startsWith("-") == true
    val valueColor = if (isNeg) MaterialTheme.colorScheme.error else catColor
    val paramSummary = buildParamSummary(nodeParams)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                else Color.Transparent,
            )
            .padding(vertical = 2.dp),
    ) {
        // Left: colored border stripe
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(72.dp)
                .background(catColor, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)),
        )

        Spacer(Modifier.width(2.dp))

        // Step number
        Box(
            modifier = Modifier
                .padding(top = 14.dp)
                .width(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$stepNumber",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
            )
        }

        Spacer(Modifier.width(8.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
        ) {
            // Category badge + node name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = catColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = abbr,
                        style = MaterialTheme.typography.labelSmall,
                        color = catColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = card.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            // Param summary
            if (paramSummary.isNotEmpty()) {
                Text(
                    text = paramSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Output value (right side)
        if (formattedValue != null) {
            val (numPart, unitPart) = splitValueUnit(formattedValue)
            Column(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .padding(end = 4.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = numPart,
                    style = MaterialTheme.typography.titleSmall,
                    color = valueColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                if (unitPart.isNotEmpty()) {
                    Text(
                        text = unitPart,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp, end = 8.dp)
                    .align(Alignment.CenterVertically),
            ) {
                val statusColor = when (card.status) {
                    NodeStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    NodeStatus.RUNNING -> MaterialTheme.colorScheme.primary
                    NodeStatus.SUCCESS -> Color(0xFF22C55E)
                    NodeStatus.ERROR -> MaterialTheme.colorScheme.error
                }
                Text("—", style = MaterialTheme.typography.titleSmall, color = statusColor)
            }
        }

        // Chevron
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.CenterVertically)
                .padding(end = 4.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun FlowConnector(flowingValue: String? = null) {
    Row(
        modifier = Modifier.padding(start = 37.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(20.dp).height(18.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val lineEnd = size.height * 0.60f
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(cx, 0f),
                    end = Offset(cx, lineEnd),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                val aw = 3.5.dp.toPx()
                val ah = 5.dp.toPx()
                drawPath(
                    path = Path().apply {
                        moveTo(cx - aw, lineEnd)
                        lineTo(cx, lineEnd + ah)
                        lineTo(cx + aw, lineEnd)
                    },
                    color = Color(0xFFCBD5E1),
                    style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
        if (flowingValue != null) {
            Spacer(Modifier.width(6.dp))
            val isNeg = flowingValue.trimStart().startsWith("-")
            Text(
                text = flowingValue,
                style = MaterialTheme.typography.labelSmall,
                color = (if (isNeg) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    .copy(alpha = 0.6f),
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun FlowStatusBar(
    nodeCount: Int,
    edgeCount: Int,
    lastFormatted: String?,
    totalGainDb: Double?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "共 $nodeCount 节点 · $edgeCount 连接",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f),
            )
            if (totalGainDb != null) {
                MetricBadge(
                    value = "+%.1f".format(totalGainDb),
                    unit = "dB",
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
            }
            if (lastFormatted != null) {
                val isNeg = lastFormatted.trimStart().startsWith("-")
                val color = if (isNeg) MaterialTheme.colorScheme.error else Color(0xFF22C55E)
                MetricBadge(value = lastFormatted.split(" ").firstOrNull() ?: lastFormatted, unit = lastFormatted.split(" ").getOrNull(1) ?: "", color = color)
            }
        }
    }
}

@Composable
private fun MetricBadge(value: String, unit: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.Baseline,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(
                    unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                )
            }
        }
    }
}

// ── Canvas toolbar ────────────────────────────────────────────────────────────

@Composable
private fun CanvasToolbar(
    scale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onZoomOut, modifier = Modifier.size(36.dp), enabled = scale > 0.41f) {
                Icon(
                    Icons.Default.Remove, null, modifier = Modifier.size(18.dp),
                    tint = if (scale > 0.41f) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
            Text(
                "${(scale * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            IconButton(onClick = onZoomIn, modifier = Modifier.size(36.dp), enabled = scale < 2.49f) {
                Icon(
                    Icons.Default.Add, null, modifier = Modifier.size(18.dp),
                    tint = if (scale < 2.49f) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
            Box(
                Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            IconButton(onClick = onResetView, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.ZoomOutMap, null, modifier = Modifier.size(18.dp),
                    tint = if (scale != 1f) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }
    }
}

// ── Empty states ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyFlowHint(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.AccountTree, null,
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

@Composable
private fun EmptyCanvasHint(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.AccountTree, null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Empty Canvas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Tap  +  to add the first node",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Rename dialog ─────────────────────────────────────────────────────────────

@Composable
private fun RenameWorkflowDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nameText by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Workflow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Workflow name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (nameText.isNotBlank()) onRename(nameText.trim()) }),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (nameText.isNotBlank()) onRename(nameText.trim()) }, enabled = nameText.isNotBlank()) {
                Text("Rename")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
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
    else nodeTypes.filter { it.substringAfterLast('.').contains(searchQuery, ignoreCase = true) }

    val categoryOrder = listOf("Source", "Active", "Passive", "Channel", "Receiver", "Other")
    val byCategory = filtered.groupBy { typeToNodeCategory(it) }
        .let { map -> categoryOrder.mapNotNull { cat -> map[cat]?.let { cat to it } } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add Node", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search…", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        text = {
            LazyColumn {
                byCategory.forEach { (category, types) ->
                    val catColor = nodeCategoryColor(category)
                    item(key = "h_$category") {
                        Row(
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(7.dp).background(catColor, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = catColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    items(types.size) { idx ->
                        val typeId = types[idx]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelect(typeId) }
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
                                    categoryAbbr(typeId).take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = catColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(typeId.substringAfterLast('.'), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

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
    "Active" -> Color(0xFFE05A00)
    "Passive" -> Color(0xFFF2994A)
    "Channel" -> Color(0xFF27AE60)
    "Receiver" -> Color(0xFF7B2FBE)
    else -> Color(0xFF64748B)
}

private fun splitValueUnit(formatted: String): Pair<String, String> {
    val trimmed = formatted.trim()
    val idx = trimmed.indexOf(' ')
    return if (idx > 0) trimmed.substring(0, idx) to trimmed.substring(idx + 1) else trimmed to ""
}

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
    return params.entries.take(3).joinToString("  ") { (k, v) ->
        "${shortKey(k)}: ${WorkflowEditorViewModel.formatSummaryValue(v)}"
    }
}
