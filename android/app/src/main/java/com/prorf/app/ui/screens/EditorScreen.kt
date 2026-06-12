package com.prorf.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.prorf.app.R
import com.prorf.app.data.*
import com.prorf.app.ui.components.*
import com.prorf.app.ui.theme.NumericFont
import com.prorf.app.ui.theme.Prf
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/** Snapshot for undo/redo history — file-level to avoid class reallocation per recomposition. */
private data class EditorSnapshot(val nodes: List<RfNode>, val globals: GlobalParams)

/** Global params preset entry — file-level to avoid class reallocation per recomposition. */
private data class GlobalPreset(val label: String, val freq: Double, val bw: Double, val dist: Double, val temp: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    workflow: Workflow,
    onBack: () -> Unit,
    onSave: (Workflow) -> Unit,
    onViewResults: (Workflow) -> Unit,
    onRename: (Workflow, String) -> Unit = { _, _ -> },
    showResultsInline: Boolean = false,
    onExport: () -> Unit = {},
) {
    val p = Prf.colors
    val zh = LocalConfiguration.current.locales[0].language == "zh"
    val haptic = LocalHapticFeedback.current
    var nodes by remember(workflow.id) { mutableStateOf(workflow.nodes) }
    var globals by remember(workflow.id) { mutableStateOf(workflow.globals) }
    var wfName by remember(workflow.id) { mutableStateOf(workflow.name) }
    var selId by remember { mutableStateOf<String?>(null) }
    var sheet by remember { mutableStateOf<String?>(null) } // node / add / params
    var addAfter by remember { mutableIntStateOf(-1) }
    var saved by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }
    var showRenameWorkflowDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val nodeIdCounter = remember { mutableStateOf(System.currentTimeMillis()) }

    // First-time editor guide dialog
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prorf", android.content.Context.MODE_PRIVATE) }
    var showEditorGuide by remember { mutableStateOf(!prefs.getBoolean("editor_guide_done", false)) }

    // Track whether there are unsaved changes
    val hasUnsavedChanges = !saved && (nodes != workflow.nodes || globals != workflow.globals)

    fun requestBack() {
        if (hasUnsavedChanges) showBackConfirm = true else onBack()
    }

    // Intercept system back button when there are unsaved changes
    BackHandler(enabled = hasUnsavedChanges) { requestBack() }

    // Undo/redo history
    val undoStack = remember(workflow.id) { mutableStateListOf<EditorSnapshot>() }
    val redoStack = remember(workflow.id) { mutableStateListOf<EditorSnapshot>() }
    val maxUndo = 50

    fun pushUndo() {
        undoStack.add(EditorSnapshot(nodes, globals))
        if (undoStack.size > maxUndo) undoStack.removeAt(0)
        redoStack.clear()
    }
    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(EditorSnapshot(nodes, globals))
            val prev = undoStack.removeLast()
            nodes = prev.nodes; globals = prev.globals
        }
    }
    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(EditorSnapshot(nodes, globals))
            val next = redoStack.removeLast()
            nodes = next.nodes; globals = next.globals
        }
    }

    val result = remember(nodes, globals) { RfEngine.evaluate(nodes, globals) }
    val selNode = nodes.find { it.id == selId }
    val selIdx = nodes.indexOfFirst { it.id == selId }
    val current = workflow.copy(nodes = nodes, globals = globals)

    // Auto-save: debounced 2s after any node/global change
    val currentRef = rememberUpdatedState(current)
    val onSaveRef = rememberUpdatedState(onSave)
    LaunchedEffect(nodes, globals) {
        saved = false  // reset saved badge immediately when content changes
        if (nodes != workflow.nodes || globals != workflow.globals) {
            saving = true
            delay(2000)
            saving = false
            onSaveRef.value(currentRef.value)
            saved = true
        }
    }

    Column(Modifier.fillMaxSize().background(p.bg)) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().background(p.surf).padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { requestBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = p.txt1) }
            IconButton(onClick = { undo() }, enabled = undoStack.isNotEmpty()) {
                Icon(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.action_undo), tint = if (undoStack.isNotEmpty()) p.txt2 else p.txt4)
            }
            IconButton(onClick = { redo() }, enabled = redoStack.isNotEmpty()) {
                Icon(Icons.AutoMirrored.Filled.Redo, stringResource(R.string.action_redo), tint = if (redoStack.isNotEmpty()) p.txt2 else p.txt4)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(wfName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.txt1, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    IconButton(onClick = { showRenameWorkflowDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, stringResource(R.string.action_rename), tint = p.txt3, modifier = Modifier.size(14.dp))
                    }
                }
                Text(workflow.tags.joinToString(" · "), fontSize = 11.sp, color = p.txt3)
            }
            IconButton(onClick = onExport) {
                Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.action_export), tint = p.txt2, modifier = Modifier.size(20.dp))
            }
            TextButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onSave(current); saved = true }, enabled = !saving) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = p.txt3)
                    Text(" ${stringResource(R.string.saving)}", color = p.txt3, fontWeight = FontWeight.Normal)
                } else if (saved) {
                    Icon(Icons.Default.Check, null, tint = p.ok, modifier = Modifier.size(18.dp))
                    Text(" ${stringResource(R.string.saved)}", color = p.ok, fontWeight = FontWeight.Bold)
                } else {
                    Text(stringResource(R.string.action_save), color = p.prim, fontWeight = FontWeight.Bold)
                }
            }
        }
        LaunchedEffect(saved) { if (saved) { delay(3000); saved = false } }
        HorizontalDivider(color = p.line2)

        // Global params quick bar
        Row(
            Modifier
                .fillMaxWidth()
                .background(p.bg2)
                .clickable { sheet = "params" }
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val gFreq = if (globals.frequencyMHz >= 1000) String.format(Locale.US, "%.1fGHz", globals.frequencyMHz / 1000) else "${trimNum(globals.frequencyMHz)}MHz"
            listOf(
                stringResource(R.string.g_freq) to gFreq,
                stringResource(R.string.g_bw) to "${trimNum(globals.bandwidthMHz)}MHz",
                stringResource(R.string.g_dist) to "${trimNum(globals.distanceKm)}km",
                stringResource(R.string.g_temp) to "${trimNum(globals.temperatureK)}K",
            ).forEach { (l, v) ->
                Column(Modifier.weight(1f)) {
                    Text(l, fontSize = 9.sp, color = p.txt4)
                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.txt1, fontFamily = FontFamily.Monospace, maxLines = 1)
                }
            }
            Icon(Icons.Default.KeyboardArrowDown, null, tint = p.txt3, modifier = Modifier.size(18.dp))
        }
        HorizontalDivider(color = p.line2)

        Row(Modifier.weight(1f)) {
            // Vertical node canvas
            Box(Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 40.dp, top = 10.dp, bottom = 14.dp),
                ) {
                    item { AddDot { addAfter = -1; sheet = "add" } }
                    if (nodes.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.editor_empty_hint),
                                fontSize = 13.sp, color = p.txt4,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            )
                        }
                    }
                    items(nodes.size, key = { nodes[it].id }) { i ->
                        val nd = nodes[i]
                        NodeCard(
                            node = nd,
                            trace = result.trace.getOrNull(i),
                            selected = selId == nd.id,
                            onClick = {
                                if (selId == nd.id) sheet = "node" else selId = nd.id
                            },
                        )
                        NodeConnector(
                            pwr = result.trace.getOrNull(i)?.pwr,
                            isLast = i == nodes.size - 1,
                            onAdd = { addAfter = i; sheet = "add" },
                        )
                    }
                }
                QuickNav(
                    nodes = nodes, selId = selId,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
                ) { id ->
                    selId = id
                    val idx = nodes.indexOfFirst { it.id == id }
                    if (idx >= 0) scope.launch { listState.animateScrollToItem(idx + 1) }
                }
            }

            // Inline results panel on large screens
            if (showResultsInline) {
                VerticalDivider(color = p.line2)
                Box(Modifier.width(380.dp).fillMaxHeight().background(p.bg2)) {
                    ResultsPanel(result = result, globals = globals)
                }
            }
        }

        // Results preview bar (compact only — large screens show the panel)
        if (!showResultsInline) {
            ResultsBar(result) { onSave(current); onViewResults(current) }
        }
    }

    // ── Sheets ─────────────────────────────────────────────
    if (sheet == "node" && selNode != null) {
        ModalBottomSheet(
            onDismissRequest = { sheet = null; selId = null },
            containerColor = p.surf,
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(40.dp, 4.dp)
                        .background(p.line2, CircleShape)
                )
            },
        ) {
            NodePropsContent(
                node = selNode,
                canUp = selIdx > 0, canDown = selIdx < nodes.size - 1,
                onUpdate = { upd -> pushUndo(); nodes = nodes.map { if (it.id == upd.id) upd else it } },
                onDelete = { showDeleteConfirm = true },
                onCopy = {
                    pushUndo()
                    val idx = nodes.indexOfFirst { it.id == selId }
                    if (idx >= 0) {
                        val copied = nodes[idx].copy(id = "n" + (++nodeIdCounter.value))
                        nodes = nodes.toMutableList().apply { add(idx + 1, copied) }
                        sheet = null
                    }
                },
                onMove = { dir ->
                    pushUndo()
                    val i = nodes.indexOfFirst { it.id == selId }
                    val j = i + dir
                    if (i >= 0 && j in nodes.indices) {
                        val a = nodes.toMutableList(); val t = a[i]; a[i] = a[j]; a[j] = t; nodes = a
                    }
                },
            )
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_node_title)) },
            text = { Text(stringResource(R.string.delete_node_message, selNode?.name ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    pushUndo()
                    nodes = nodes.filter { it.id != selId }; sheet = null; selId = null
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
    // CX15: Unsaved changes confirmation dialog
    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onSave(current)
                    showBackConfirm = false
                    onBack()
                }) { Text(stringResource(R.string.action_save_and_exit)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackConfirm = false
                    onBack()
                }) { Text(stringResource(R.string.action_discard)) }
            },
        )
    }
    // CX23: Workflow rename dialog
    if (showRenameWorkflowDialog) {
        var newName by remember { mutableStateOf(workflow.name) }
        AlertDialog(
            onDismissRequest = { showRenameWorkflowDialog = false },
            title = { Text(stringResource(R.string.rename_link_title)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.rename_link_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        onRename(workflow, newName)
                        wfName = newName.trim()
                    }
                    showRenameWorkflowDialog = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameWorkflowDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
    if (sheet == "add") {
        ModalBottomSheet(
            onDismissRequest = { sheet = null },
            containerColor = p.surf,
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(40.dp, 4.dp)
                        .background(p.line2, CircleShape)
                )
            },
        ) {
            ModulePickerContent { mod ->
                pushUndo()
                val nd = RfNode(
                    id = "n" + (++nodeIdCounter.value),
                    kind = mod.kind, moduleId = mod.id, name = mod.displayName(zh),
                    params = mod.params.associate { it.key to it.default },
                )
                val a = nodes.toMutableList(); a.add(addAfter + 1, nd); nodes = a
                // UX1: Auto-scroll canvas to newly-added node so users see it immediately.
                // LazyColumn layout: [0] AddDot header, [1..nodes.size] node items; new node index = addAfter + 1.
                val scrollTarget = addAfter + 2
                scope.launch { listState.animateScrollToItem(scrollTarget) }
                sheet = null
            }
        }
    }
    if (sheet == "params") {
        ModalBottomSheet(
            onDismissRequest = { sheet = null },
            containerColor = p.surf,
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(40.dp, 4.dp)
                        .background(p.line2, CircleShape)
                )
            },
        ) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text(stringResource(R.string.global_params), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = p.txt1, modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        GlobalPreset(stringResource(R.string.preset_5g), 3500.0, 100.0, 0.5, 290.0),
                        GlobalPreset(stringResource(R.string.preset_satellite), 14000.0, 36.0, 38000.0, 290.0),
                        GlobalPreset(stringResource(R.string.preset_microwave), 18000.0, 28.0, 20.0, 290.0),
                        GlobalPreset(stringResource(R.string.preset_radar), 9500.0, 10.0, 100.0, 290.0),
                    )
                    presets.forEach { pre ->
                        TextButton(
                            onClick = { pushUndo(); globals = globals.copy(frequencyMHz = pre.freq, bandwidthMHz = pre.bw, distanceKm = pre.dist, temperatureK = pre.temp) },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(pre.label, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
                ParamRow(stringResource(R.string.g_freq_full), globals.frequencyMHz, "MHz", 1.0, 300000.0) { pushUndo(); globals = globals.copy(frequencyMHz = it) }
                ParamRow(stringResource(R.string.g_bw_full), globals.bandwidthMHz, "MHz", 0.001, 10000.0) { pushUndo(); globals = globals.copy(bandwidthMHz = it) }
                ParamRow(stringResource(R.string.g_dist_full), globals.distanceKm, "km", 0.001, 1000000.0) { pushUndo(); globals = globals.copy(distanceKm = it) }
                ParamRow(stringResource(R.string.g_temp_full), globals.temperatureK, "K", 10.0, 1000.0) { pushUndo(); globals = globals.copy(temperatureK = it) }
                Spacer(Modifier.height(12.dp))
                PrimaryButton(stringResource(R.string.apply_params), Modifier.fillMaxWidth()) { sheet = null }
            }
        }
    }

    // First-time editor guide dialog (uses existing strings, shown once per install)
    if (showEditorGuide) {
        val dismissGuide = {
            showEditorGuide = false
            prefs.edit().putBoolean("editor_guide_done", true).apply()
        }
        AlertDialog(
            onDismissRequest = dismissGuide,
            title = { Text(stringResource(R.string.editor_guide_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.editor_guide_desc), fontSize = 13.sp, color = p.txt2)
                    listOf(
                        stringResource(R.string.editor_guide_step1),
                        stringResource(R.string.editor_guide_step2),
                        stringResource(R.string.editor_guide_step3),
                    ).forEach { step ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                Modifier
                                    .padding(top = 5.dp)
                                    .size(6.dp)
                                    .background(p.prim, androidx.compose.foundation.shape.CircleShape)
                            )
                            Text(step, fontSize = 13.sp, color = p.txt1)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = dismissGuide) {
                    Text(stringResource(R.string.editor_guide_dismiss), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = p.surf,
        )
    }
}

@Composable
private fun AddDot(onClick: () -> Unit) {
    val p = Prf.colors
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(48.dp)
                .border(1.5.dp, p.line, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Text("+", color = p.txt4, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun NodeCard(node: RfNode, trace: TraceEntry?, selected: Boolean, onClick: () -> Unit) {
    val p = Prf.colors
    val zh = LocalConfiguration.current.locales[0].language == "zh"
    val meta = p.kind(node.kind)
    val mod = Catalog.modules[node.moduleId]
    Row(
        Modifier
            .fillMaxWidth()
            .scale(if (selected) 1.015f else 1f)
            .background(if (selected) meta.tint else p.surf, RoundedCornerShape(14.dp))
            .border(2.dp, if (selected) meta.col else p.line2, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(meta.tint, RoundedCornerShape(10.dp))
                .border(1.5.dp, meta.col.copy(alpha = .3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) { ModuleIcon(mod?.emoji ?: "?", node.kind, size = 20.dp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Catalog.kindLabel(node.kind, zh), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                    modifier = Modifier.background(meta.col, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 1.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(node.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.txt1, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            if (mod != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    node.params.entries.filter { (k, _) -> mod.params.find { it.key == k }?.infoOnly != true }.take(3).forEach { (k, v) ->
                        val spec = mod.params.find { it.key == k } ?: return@forEach
                        Text(
                            "${spec.displayLabel(zh)}: ${trimNum(v)}${spec.unit}",
                            fontSize = 11.sp, color = p.txt3, maxLines = 1,
                        )
                    }
                }
            } else {
                Text(stringResource(R.string.module_unknown), fontSize = 11.sp, color = p.warn)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(trace?.label ?: "—", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = meta.col, fontFamily = NumericFont)
            if (trace != null) Text("→ ${RfEngine.fmt(trace.pwr)} dBm", fontSize = 10.sp, color = p.txt4, fontFamily = NumericFont)
        }
    }
}

@Composable
private fun NodeConnector(pwr: Double?, isLast: Boolean, onAdd: () -> Unit) {
    val p = Prf.colors
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(2.dp).height(8.dp).background(p.line))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (pwr != null) {
                Text(
                    "${RfEngine.fmt(pwr)} dBm",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.prim, fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(p.primTint, RoundedCornerShape(8.dp))
                        .border(1.dp, p.line, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                )
            }
            Box(
                Modifier.size(48.dp).border(1.5.dp, p.line, CircleShape).clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) { Text("+", color = p.txt4, fontSize = 14.sp, lineHeight = 14.sp) }
        }
        if (!isLast) {
            Box(Modifier.width(2.dp).height(8.dp).background(p.line))
            Text("▼", fontSize = 8.sp, color = p.txt3)
        } else Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun QuickNav(nodes: List<RfNode>, selId: String?, modifier: Modifier = Modifier, onJump: (String) -> Unit) {
    val p = Prf.colors
    Column(
        modifier
            .background(p.surf, RoundedCornerShape(20.dp))
            .padding(horizontal = 5.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        nodes.forEach { nd ->
            val meta = p.kind(nd.kind)
            val active = nd.id == selId
            Box(
                Modifier
                    .size(if (active) 10.dp else 8.dp)
                    .background(if (active) meta.col else meta.col.copy(alpha = .35f), CircleShape)
                    .clickable { onJump(nd.id) },
            )
        }
    }
}

@Composable
fun ResultsBar(result: LinkResult, onViewFull: () -> Unit) {
    val p = Prf.colors
    val hasNodes = result.trace.isNotEmpty()
    Column(Modifier.fillMaxWidth().background(p.surf).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.results_bar_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.txt1)
                if (hasNodes) {
                    StatusBadge(result.isValid, if (result.isValid) stringResource(R.string.status_ok) else stringResource(R.string.status_warn))
                }
            }
            if (hasNodes) {
                Text(
                    stringResource(R.string.view_details), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.prim,
                    modifier = Modifier.clickable(onClick = onViewFull),
                )
            }
        }
        if (hasNodes) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniStat(stringResource(R.string.metric_margin), RfEngine.fmt(result.margin, 2), "dB", if (result.isValid) p.ok else p.err, Modifier.weight(1f))
                MiniStat(stringResource(R.string.metric_rx_power), RfEngine.fmt(result.rxPwr, 2), "dBm", p.prim, Modifier.weight(1f))
                MiniStat(stringResource(R.string.metric_eirp), RfEngine.fmt(result.eirp), "dBm", p.txt1, Modifier.weight(1f))
                MiniStat(stringResource(R.string.metric_snr), RfEngine.fmt(result.snr), "dB", p.sec, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NodePropsContent(
    node: RfNode,
    canUp: Boolean, canDown: Boolean,
    onUpdate: (RfNode) -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onMove: (Int) -> Unit,
) {
    val p = Prf.colors
    val zh = LocalConfiguration.current.locales[0].language == "zh"
    val meta = p.kind(node.kind)
    val mod = Catalog.modules[node.moduleId]
    var showRenameDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        // Colored header
        Column(Modifier.fillMaxWidth().background(meta.col).padding(horizontal = 20.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.module_of_kind, Catalog.kindLabel(node.kind, zh)).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .75f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(node.name, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        IconButton(onClick = { showRenameDialog = true }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(13.dp))
                        }
                    }
                    if (mod != null) {
                        // In Chinese mode show English module name as cross-reference; in English mode it's redundant
                        if (zh) Text(mod.en, fontSize = 12.sp, color = Color.White.copy(alpha = .7f))
                    } else {
                        Text(stringResource(R.string.module_unknown), fontSize = 12.sp, color = Color.White.copy(alpha = .7f))
                    }
                }
                ModuleIcon(mod?.emoji ?: "?", node.kind, size = 32.dp)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetActionBtn(stringResource(R.string.move_up), enabled = canUp, modifier = Modifier.weight(1f)) { onMove(-1) }
                SheetActionBtn(stringResource(R.string.move_down), enabled = canDown, modifier = Modifier.weight(1f)) { onMove(1) }
                SheetActionBtn(stringResource(R.string.action_copy_node), enabled = true, modifier = Modifier.weight(1f)) { onCopy() }
                SheetActionBtn(stringResource(R.string.action_delete), enabled = true, danger = true) { onDelete() }
            }
        }
        // Params
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(stringResource(R.string.params_section), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.txt3, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            if (mod != null && mod.params.isNotEmpty()) {
                val activeParams = mod.params.filter { !it.infoOnly }
                val infoParams = mod.params.filter { it.infoOnly }
                activeParams.forEach { spec ->
                    ParamRow(spec.displayLabel(zh), node.params[spec.key] ?: spec.default, spec.unit, spec.min, spec.max) { v ->
                        onUpdate(node.copy(params = node.params + (spec.key to v)))
                    }
                }
                if (infoParams.isNotEmpty()) {
                    Text(stringResource(R.string.params_reference), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.txt4, modifier = Modifier.padding(top = 12.dp, bottom = 2.dp))
                    infoParams.forEach { spec ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(spec.displayLabel(zh), fontSize = 13.sp, color = p.txt4)
                            Text(
                                "${trimNum(node.params[spec.key] ?: spec.default)} ${spec.unit}",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.txt4,
                            )
                        }
                    }
                }
                val hasChanged = mod.params.any { spec -> node.params[spec.key] != spec.default }
                if (hasChanged) {
                    TextButton(onClick = {
                        val resetParams = mod.params.associate { it.key to it.default }
                        onUpdate(node.copy(params = resetParams))
                    }) {
                        Text(stringResource(R.string.action_reset_params), color = p.txt3, fontSize = 12.sp)
                    }
                }
            } else {
                Text(stringResource(R.string.params_by_global), fontSize = 14.sp, color = p.txt3, modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(node.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.rename_node_title)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.rename_link_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) onUpdate(node.copy(name = newName))
                    showRenameDialog = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SheetActionBtn(text: String, enabled: Boolean, modifier: Modifier = Modifier, danger: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        danger -> Color(0xFFFF5050).copy(alpha = .3f)
        enabled -> Color.White.copy(alpha = .2f)
        else -> Color.White.copy(alpha = .07f)
    }
    Box(
        modifier
            .background(bg, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) Color.White else Color.White.copy(alpha = .3f))
    }
}

@Composable
private fun ModulePickerContent(onPick: (ModuleSpec) -> Unit) {
    val p = Prf.colors
    val zh = LocalConfiguration.current.locales[0].language == "zh"
    var cat by remember { mutableStateOf("all") }
    val cats = listOf(
        "all" to stringResource(R.string.cat_all),
        "TX" to stringResource(R.string.cat_tx),
        "LOSS" to stringResource(R.string.cat_loss),
        "PROPAGATION" to stringResource(R.string.cat_prop),
        "RX" to stringResource(R.string.cat_rx),
    )
    val mods = Catalog.modules.values.filter { cat == "all" || it.kind.name == cat }
    Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
        Text(stringResource(R.string.pick_module), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = p.txt1, modifier = Modifier.padding(vertical = 8.dp))
        Chips(cats, cat) { cat = it }
        Spacer(Modifier.height(10.dp))
        val chunks = remember(mods) { mods.chunked(2) }
        LazyColumn(Modifier.heightIn(max = 420.dp)) {
            items(chunks.size) { row ->
                val pair = chunks[row]
                Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { m ->
                        val meta = p.kind(m.kind)
                        Column(
                            Modifier
                                .weight(1f)
                                .background(p.surf, RoundedCornerShape(12.dp))
                                .border(1.5.dp, meta.col.copy(alpha = .3f), RoundedCornerShape(12.dp))
                                .clickable { onPick(m) }
                                .padding(12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ModuleIcon(m.emoji, m.kind, size = 22.dp)
                                Text(
                                    Catalog.kindLabel(m.kind, zh), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                                    modifier = Modifier.background(meta.col, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(m.displayName(zh), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.txt1)
                            Text(if (zh) m.en else m.name, fontSize = 11.sp, color = p.txt3)
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
