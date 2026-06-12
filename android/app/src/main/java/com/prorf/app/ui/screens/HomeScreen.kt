package com.prorf.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.R
import com.prorf.app.data.Catalog
import com.prorf.app.data.RfEngine
import com.prorf.app.data.Workflow
import com.prorf.app.ui.components.*
import com.prorf.app.ui.theme.Prf

@Composable
fun HomeScreen(
    workflows: List<Workflow>,
    onOpen: (Workflow) -> Unit,
    onCreate: (String?) -> Unit,
    onDelete: (Workflow) -> Unit,
    onDuplicate: (Workflow) -> Unit,
    onRename: (Workflow, String) -> Unit,
    onToggleFav: (Workflow) -> Unit,
    onImport: () -> Unit = {},
    onBatchDelete: (List<Workflow>) -> Unit = {},
    onBatchExport: (List<Workflow>) -> Unit = {},
    onSeeAllTemplates: (() -> Unit)? = null,
    onEditTags: (Workflow, List<String>) -> Unit = { _, _ -> },
    twoColumn: Boolean = false,
) {
    val p = Prf.colors
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var search by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<Workflow?>(null) }
    var sortBy by remember { mutableStateOf("recent") }
    var showFavOnly by remember { mutableStateOf(false) }

    // Batch selection mode
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    // Collect all tags for filter — derivedStateOf tracks SnapshotStateList changes
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    val allTags by remember { derivedStateOf { workflows.flatMap { it.tags }.distinct() } }

    // Filtering + sorting in one derivedStateOf so RfEngine.evaluate is not called on every scroll
    val sorted by remember {
        derivedStateOf {
            val filtered = workflows.filter { wf ->
                (search.isBlank() || wf.name.contains(search, ignoreCase = true) ||
                    wf.tags.any { it.contains(search, ignoreCase = true) }) &&
                    (selectedTags.isEmpty() || wf.tags.any { it in selectedTags }) &&
                    (!showFavOnly || wf.fav)
            }
            when (sortBy) {
                "name" -> filtered.sortedBy { it.name }
                "margin" -> filtered.sortedByDescending {
                    RfEngine.evaluate(it.nodes, it.globals).margin
                }
                else -> filtered.sortedByDescending { it.updatedAt }
            }
        }
    }

    // Two-column layout chunks for wide screens
    val chunks by remember(sorted) { derivedStateOf { sorted.chunked(2) } }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().background(p.bg)) {
        // Batch selection action bar (shown when in selection mode)
        if (selectionMode) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = p.prim,
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                selectionMode = false
                                selectedIds = emptySet()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel), tint = Color.White)
                            }
                            Text(
                                stringResource(R.string.selected_count, selectedIds.size),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Row {
                            TextButton(onClick = {
                                selectedIds = if (selectedIds.size == sorted.size) emptySet() else sorted.map { it.id }.toSet()
                            }) {
                                Text(
                                    if (selectedIds.size == sorted.size) stringResource(R.string.action_deselect_all) else stringResource(R.string.action_select_all),
                                    color = Color.White,
                                )
                            }
                            if (selectedIds.isNotEmpty()) {
                                TextButton(onClick = { showBatchDeleteConfirm = true }) {
                                    Text(stringResource(R.string.action_delete_selected), color = Color.White)
                                }
                                TextButton(onClick = {
                                    val selected = sorted.filter { it.id in selectedIds }
                                    onBatchExport(selected)
                                }) {
                                    Text(stringResource(R.string.action_export_selected), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hero header
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(p.primDim, p.prim)))
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("ProRF", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(stringResource(R.string.home_subtitle), fontSize = 12.sp, color = Color.White.copy(alpha = .75f))
                    }
                    Box(
                        Modifier.size(44.dp).background(Color.White.copy(alpha = .15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Text("📡", fontSize = 22.sp) }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = .18f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = .7f), modifier = Modifier.size(20.dp))
                    TextField(
                        value = search, onValueChange = { search = it },
                        placeholder = { Text(stringResource(R.string.search_hint), color = Color.White.copy(alpha = .6f), fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                        ),
                    )
                    if (search.isNotBlank()) {
                        IconButton(onClick = { search = "" }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel), tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { onCreate("blank") },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = p.prim,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Text("+ ${stringResource(R.string.action_new_link)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { onSeeAllTemplates?.invoke() },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Text(stringResource(R.string.recommended_templates), fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
        }

        // Tag filter row
        if (allTags.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(allTags.size) { i ->
                        val tag = allTags[i]
                        val selected = tag in selectedTags
                        Text(
                            tag,
                            fontSize = 12.sp,
                            color = if (selected) p.onPrim else p.txt2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .background(
                                    if (selected) p.prim else p.surf2,
                                    RoundedCornerShape(999.dp),
                                )
                                .clickable {
                                    selectedTags = if (selected) selectedTags - tag else selectedTags + tag
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }
        }

        // Section label + sort control
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    SectionLabel(stringResource(R.string.my_links), stringResource(R.string.action_new)) { onCreate(null) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!selectionMode) {
                        IconButton(onClick = { selectionMode = true }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.action_select), tint = p.txt3)
                        }
                    }
                    IconButton(onClick = onImport) {
                        Icon(Icons.Default.FileOpen, contentDescription = stringResource(R.string.action_import), tint = p.txt3)
                    }
                    IconButton(onClick = { showFavOnly = !showFavOnly }) {
                        Icon(
                            if (showFavOnly) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = stringResource(R.string.filter_fav),
                            tint = if (showFavOnly) p.warn else p.txt3,
                        )
                    }
                    IconButton(onClick = {
                        sortBy = when (sortBy) {
                            "recent" -> "name"
                            "name" -> "margin"
                            else -> "recent"
                        }
                    }) {
                        Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.sort_label), tint = if (sortBy != "recent") p.prim else p.txt3)
                    }
                    Text(
                        when (sortBy) {
                            "name" -> stringResource(R.string.sort_name)
                            "margin" -> stringResource(R.string.sort_margin)
                            else -> stringResource(R.string.sort_recent)
                        },
                        fontSize = 11.sp, color = p.txt3,
                    )
                }
            }
        }

        // Stats line: show link count and favorites count
        if (workflows.isNotEmpty()) {
            item {
                val favCount = workflows.count { it.fav }
                val isFiltered = sorted.size < workflows.size
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isFiltered) "${sorted.size} / ${workflows.size}" else "${workflows.size}",
                        fontSize = 12.sp, color = p.txt3,
                    )
                    if (favCount > 0) {
                        Text("·", fontSize = 12.sp, color = p.txt3)
                        Text("★ $favCount", fontSize = 12.sp, color = p.warn)
                    }
                }
            }
        }

        // Empty state: no workflows at all
        if (workflows.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🔗", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.empty_links_title),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.txt1,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.empty_links_subtitle),
                        fontSize = 14.sp, color = p.txt3, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { onCreate("blank") }) {
                        Text(stringResource(R.string.action_new_link))
                    }
                }
            }
        }

        // Search no results
        if (sorted.isEmpty() && search.isNotBlank() && workflows.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🔍", fontSize = 32.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.empty_search), color = p.txt3, fontSize = 14.sp)
                }
            }
        }

        // Workflow cards
        if (twoColumn) {
            items(chunks.size) { row ->
                val pair = chunks[row]
                Row(
                    Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    pair.forEach { wf ->
                        Box(Modifier.weight(1f)) {
                            WorkflowCard(
                                wf,
                                selected = wf.id in selectedIds,
                                selectionMode = selectionMode,
                                onClick = {
                                    if (selectionMode) {
                                        selectedIds = if (wf.id in selectedIds) selectedIds - wf.id else selectedIds + wf.id
                                    } else {
                                        onOpen(wf)
                                    }
                                },
                                onDelete = { deleting = wf },
                                onDuplicate = { onDuplicate(wf) },
                                onRename = { newName -> onRename(wf, newName) },
                                onToggleFav = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onToggleFav(wf) },
                                onEditTags = { tags -> onEditTags(wf, tags) },
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        } else {
            items(sorted.size) { i ->
                val wf = sorted[i]
                Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                    WorkflowCard(
                        wf,
                        selected = wf.id in selectedIds,
                        selectionMode = selectionMode,
                        onClick = {
                            if (selectionMode) {
                                selectedIds = if (wf.id in selectedIds) selectedIds - wf.id else selectedIds + wf.id
                            } else {
                                onOpen(wf)
                            }
                        },
                        onDelete = { deleting = wf },
                        onDuplicate = { onDuplicate(wf) },
                        onRename = { newName -> onRename(wf, newName) },
                        onToggleFav = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onToggleFav(wf) },
                        onEditTags = { tags -> onEditTags(wf, tags) },
                    )
                }
            }
        }

        item {
            Box(Modifier.padding(horizontal = 16.dp)) {
                SectionLabel(
                    stringResource(R.string.recommended_templates),
                    stringResource(R.string.see_all),
                ) { onSeeAllTemplates?.invoke() }
            }
            // CX22: Only show template chips when workflow count < 3
            if (workflows.size < 3) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(Catalog.templates.size) { i ->
                        val (tplId, emoji) = Catalog.templates[i]
                        // CX22: compact chips instead of full cards
                        Row(
                            Modifier
                                .background(p.surf, RoundedCornerShape(999.dp))
                                .border(1.dp, p.line2, RoundedCornerShape(999.dp))
                                .clickable { onCreate(tplId) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(emoji, fontSize = 16.sp)
                            Text(templateName(tplId), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.txt1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    deleting?.let { wf ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_link_title)) },
            text = { Text(stringResource(R.string.delete_link_message, wf.name)) },
            confirmButton = {
                TextButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onDelete(wf); deleting = null }) {
                    Text(stringResource(R.string.action_delete), color = p.err)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.action_cancel)) }
            },
            containerColor = p.surf,
        )
    }

    // Batch delete confirmation dialog
    if (showBatchDeleteConfirm && selectedIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text(stringResource(R.string.batch_delete_title)) },
            text = { Text(stringResource(R.string.batch_delete_message, selectedIds.size)) },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val selected = sorted.filter { it.id in selectedIds }
                    onBatchDelete(selected)
                    selectedIds = emptySet()
                    selectionMode = false
                    showBatchDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.action_delete), color = p.err)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = p.surf,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkflowCard(
    wf: Workflow,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: (String) -> Unit,
    onToggleFav: () -> Unit,
    onEditTags: (List<String>) -> Unit = {},
) {
    val p = Prf.colors
    val res = remember(wf) { RfEngine.evaluate(wf.nodes, wf.globals) }
    // UX4: Pre-format values that are used inside multiple MiniStat calls so recomposition of the
    // card does not repeat RfEngine.fmt() work — especially visible when there are 100+ links and
    // the sort order changes.
    val marginText by remember(wf) { derivedStateOf { RfEngine.fmt(res.margin) } }
    val rxPwrText by remember(wf) { derivedStateOf { RfEngine.fmt(res.rxPwr) } }
    val eirpText by remember(wf) { derivedStateOf { RfEngine.fmt(res.eirp) } }
    val nodeCountText by remember(wf) { derivedStateOf { wf.nodes.size.toString() } }
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEditTagsDialog by remember { mutableStateOf(false) }

    Box {
        Column(
            Modifier
                .fillMaxWidth()
                .background(if (selected) p.primTint else p.surf, RoundedCornerShape(16.dp))
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) p.prim else p.line2,
                    shape = RoundedCornerShape(16.dp)
                )
                .combinedClickable(onClick = onClick, onLongClick = { if (!selectionMode) showMenu = true })
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectionMode) {
                            Icon(
                                if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (selected) p.prim else p.txt3,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(wf.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.txt1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (wf.fav) {
                            Spacer(Modifier.width(4.dp))
                            Text("⭐", fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        wf.tags.forEach { t ->
                            Text(
                                t, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.prim,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .background(p.primTint, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                if (!selectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleFav, modifier = Modifier.size(32.dp)) {
                            Icon(
                                if (wf.fav) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(R.string.action_toggle_fav),
                                tint = if (wf.fav) p.warn else p.txt3,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        StatusBadge(res.isValid, stringResource(if (res.isValid) R.string.status_ok else R.string.status_warn))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(stringResource(R.string.metric_margin), marginText, "dB", if (res.isValid) p.ok else p.err, Modifier.weight(1f))
                MiniStat(stringResource(R.string.metric_rx_power), rxPwrText, "dBm", p.prim, Modifier.weight(1f))
                MiniStat(stringResource(R.string.metric_eirp), eirpText, "dBm", p.txt1, Modifier.weight(1f))
                val (freqVal, freqUnit) = remember(wf.globals.frequencyMHz) { fmtFreqShort(wf.globals.frequencyMHz) }
                MiniStat(stringResource(R.string.g_freq), freqVal, freqUnit, p.txt2, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.last_modified, relativeTime(wf.updatedAt)), fontSize = 11.sp, color = p.txt4)
        }

        // Context menu (only show when not in selection mode)
        if (!selectionMode) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_rename)) },
                    onClick = { showMenu = false; showRenameDialog = true },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_edit_tags)) },
                    onClick = { showMenu = false; showEditTagsDialog = true },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_duplicate)) },
                    onClick = { showMenu = false; onDuplicate() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                    onClick = { showMenu = false; onDelete() },
                )
            }
        }
    }

    // Rename dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(wf.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
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
                TextButton(
                    onClick = {
                        onRename(newName.trim())
                        showRenameDialog = false
                    },
                    enabled = newName.isNotBlank(),
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    // Edit tags dialog
    if (showEditTagsDialog) {
        var tagText by remember { mutableStateOf(wf.tags.joinToString(", ")) }
        AlertDialog(
            onDismissRequest = { showEditTagsDialog = false },
            title = { Text(stringResource(R.string.edit_tags_title)) },
            text = {
                OutlinedTextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    label = { Text(stringResource(R.string.edit_tags_hint)) },
                    singleLine = false,
                    minLines = 2,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val tags = tagText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onEditTags(tags)
                    showEditTagsDialog = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditTagsDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
