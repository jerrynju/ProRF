package com.prorf.ui.inspector

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.NodeInstance
import com.prorf.ui.model.EdgeDirection
import com.prorf.ui.model.UiEdgeRow
import com.prorf.ui.parameter.ParameterEditor

/**
 * L2 UI — Inspector panel shown when a node is selected.
 *
 * Structure: fixed header (node name + type badge) → tab row → scrollable tab body → action bar.
 * Tabs: Parameters | Ports | Results | Connections.
 * Does NOT call any executor or domain logic.
 */
@Composable
fun Inspector(
    nodeInstance: NodeInstance,
    definition: NodeDefinition,
    executionOutputs: Map<String, Any>,
    onParameterChanged: (key: String, value: Any) -> Unit,
    onConnectRequested: (() -> Unit)? = null,
    connectedEdges: List<UiEdgeRow> = emptyList(),
    onEdgeDeleteRequested: ((edgeId: String) -> Unit)? = null,
    onNodeDeleteRequested: (() -> Unit)? = null,
    onNodeRenameRequested: ((newLabel: String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember(nodeInstance.id) { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        // ── Header ──
        NodeHeader(
            nodeInstance = nodeInstance,
            definition = definition,
            onNodeRenameRequested = onNodeRenameRequested,
        )
        HorizontalDivider()

        // ── Quick result summary (shown when execution output is available) ──
        if (executionOutputs.isNotEmpty()) {
            val primaryPort = definition.outputs.firstOrNull()
            val primaryValue = (primaryPort?.let { executionOutputs[it.id] }
                ?: executionOutputs.entries.firstOrNull()?.value)
            val primaryLabel = primaryPort?.name ?: executionOutputs.entries.firstOrNull()?.key ?: ""
            if (primaryValue != null && primaryLabel.isNotEmpty()) {
                QuickSummaryBanner(
                    label = primaryLabel,
                    value = formatOutput(primaryValue),
                    catColor = nodeTypeColor(nodeInstance.typeId),
                )
            }
        }

        // ── Tabs ──
        val tabLabels = listOf("Params", "Ports", "Results", "Links")
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabLabels.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold
                            else FontWeight.Normal,
                        )
                    },
                )
            }
        }
        HorizontalDivider()

        // ── Tab body (scrollable) ──
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> ParametersTab(definition, nodeInstance, onParameterChanged)
                1 -> PortsTab(definition)
                2 -> ResultsTab(definition, executionOutputs)
                3 -> ConnectionsTab(connectedEdges, onEdgeDeleteRequested)
            }
        }

        // ── Bottom action bar ──
        if (onConnectRequested != null || onNodeDeleteRequested != null) {
            HorizontalDivider()
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (onConnectRequested != null) {
                    OutlinedButton(onClick = onConnectRequested, modifier = Modifier.fillMaxWidth()) {
                        Text("Connect to node →")
                    }
                }
                if (onNodeDeleteRequested != null) {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onNodeDeleteRequested,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete Node")
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun NodeHeader(
    nodeInstance: NodeInstance,
    definition: NodeDefinition,
    onNodeRenameRequested: ((String) -> Unit)?,
) {
    val catColor = nodeTypeColor(nodeInstance.typeId)
    val abbr = nodeTypeAbbr(nodeInstance.typeId)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Top row: circular icon + name + port counts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Circular category icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(catColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = abbr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
            Spacer(Modifier.width(10.dp))

            // Name + type badge column
            Column(modifier = Modifier.weight(1f)) {
                if (onNodeRenameRequested != null) {
                    var nameText by remember(nodeInstance.id) {
                        mutableStateOf(nodeInstance.label ?: definition.displayName)
                    }
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Name", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onNodeRenameRequested(nameText.trim().ifEmpty { definition.displayName })
                        }),
                        textStyle = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focus ->
                                if (!focus.isFocused) {
                                    onNodeRenameRequested(nameText.trim().ifEmpty { definition.displayName })
                                }
                            },
                    )
                } else {
                    Text(
                        text = nodeInstance.label ?: definition.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    // Type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = catColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = nodeInstance.typeId.substringAfterLast('.'),
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    // Port count chips
                    if (definition.inputs.isNotEmpty()) {
                        PortCountChip(label = "${definition.inputs.size}in", isInput = true)
                    }
                    if (definition.outputs.isNotEmpty()) {
                        PortCountChip(label = "${definition.outputs.size}out", isInput = false)
                    }
                }
            }
        }

        // Description if available
        if (definition.description.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = definition.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun PortCountChip(label: String, isInput: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isInput) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isInput) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
    }
}

private fun nodeTypeAbbr(typeId: String): String {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Signal", ignoreCase = true) -> "SRC"
        name.contains("Noise", ignoreCase = true) -> "NSE"
        name.contains("Amplifier", ignoreCase = true) -> "AMP"
        name.contains("Attenuator", ignoreCase = true) -> "ATT"
        name.contains("Cable", ignoreCase = true) -> "CBL"
        name.contains("Filter", ignoreCase = true) -> "FLT"
        name.contains("Loss", ignoreCase = true) || name.contains("Path", ignoreCase = true) -> "CH"
        name.contains("Receiver", ignoreCase = true) -> "RCV"
        name.contains("Sensitivity", ignoreCase = true) -> "SNS"
        else -> name.take(3).uppercase()
    }
}

/** Derives a structural category color from typeId — no domain knowledge, pure string matching. */
private fun nodeTypeColor(typeId: String): Color {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Source", ignoreCase = true) || name.contains("Signal", ignoreCase = true) || name.contains("Noise", ignoreCase = true) -> Color(0xFF2F80ED)
        name.contains("Amplifier", ignoreCase = true) -> Color(0xFF27AE60)
        name.contains("Attenuator", ignoreCase = true) || name.contains("Cable", ignoreCase = true) || name.contains("Filter", ignoreCase = true) -> Color(0xFFF2994A)
        name.contains("Loss", ignoreCase = true) || name.contains("Channel", ignoreCase = true) || name.contains("Path", ignoreCase = true) || name.contains("Fspl", ignoreCase = true) -> Color(0xFF9B51E0)
        name.contains("Receiver", ignoreCase = true) || name.contains("Sensitivity", ignoreCase = true) -> Color(0xFFEB5757)
        else -> Color(0xFF64748B)
    }
}

// ── Quick summary banner ──────────────────────────────────────────────────────

@Composable
private fun QuickSummaryBanner(label: String, value: String, catColor: Color) {
    val isNeg = value.trimStart().startsWith("-")
    val effectiveColor = if (isNeg) Color(0xFFEB5757) else catColor
    val spaceIdx = value.indexOf(' ')
    val numPart = if (spaceIdx > 0) value.substring(0, spaceIdx) else value
    val unitPart = if (spaceIdx > 0) value.substring(spaceIdx + 1) else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(effectiveColor.copy(alpha = 0.09f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = numPart,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = effectiveColor,
                )
                if (unitPart.isNotEmpty()) {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = unitPart,
                        style = MaterialTheme.typography.titleSmall,
                        color = effectiveColor.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ── Tab bodies ────────────────────────────────────────────────────────────────

@Composable
private fun ParametersTab(
    definition: NodeDefinition,
    nodeInstance: NodeInstance,
    onParameterChanged: (key: String, value: Any) -> Unit,
) {
    if (definition.parameters.isEmpty()) {
        EmptyHint("No parameters")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
    ) {
        // Parameter count hint
        Text(
            text = "${definition.parameters.size} parameter${if (definition.parameters.size != 1) "s" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(4.dp))
        ParameterEditor(
            parameters = definition.parameters,
            currentValues = nodeInstance.parameters,
            onValueChanged = onParameterChanged,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PortsTab(definition: NodeDefinition) {
    if (definition.inputs.isEmpty() && definition.outputs.isEmpty()) {
        EmptyHint("No ports defined")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        if (definition.inputs.isNotEmpty()) {
            SectionLabel("Inputs")
            definition.inputs.forEach { port ->
                PortRow(name = port.name, dataType = port.dataType, isInput = true)
            }
            Spacer(Modifier.height(12.dp))
        }
        if (definition.outputs.isNotEmpty()) {
            SectionLabel("Outputs")
            definition.outputs.forEach { port ->
                PortRow(name = port.name, dataType = port.dataType, isInput = false)
            }
        }
    }
}

@Composable
private fun PortRow(name: String, dataType: String, isInput: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = if (isInput) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text = dataType,
                style = MaterialTheme.typography.labelSmall,
                color = if (isInput) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun ResultsTab(
    definition: NodeDefinition,
    executionOutputs: Map<String, Any>,
) {
    if (executionOutputs.isEmpty()) {
        EmptyHint("Run the workflow to see results")
        return
    }

    // Collect ordered result entries (definition outputs first, then extras)
    val definedIds = definition.outputs.map { it.id }.toSet()
    val orderedResults: List<Pair<String, Any>> = buildList {
        definition.outputs.forEach { port ->
            val v = executionOutputs[port.id] ?: return@forEach
            add(port.name to v)
        }
        executionOutputs.filterKeys { it !in definedIds }.forEach { (key, value) ->
            add(key to value)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        // Results count header
        Text(
            text = "${orderedResults.size} output${if (orderedResults.size != 1) "s" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        orderedResults.forEach { (label, value) ->
            ResultCard(label = label, value = formatOutput(value))
            Spacer(Modifier.height(6.dp))
        }

        // Mini bar chart when multiple numeric outputs exist
        if (orderedResults.size >= 2) {
            Spacer(Modifier.height(4.dp))
            ResultMiniChart(results = orderedResults)
        }
    }
}

@Composable
private fun ResultCard(label: String, value: String) {
    val isNegative = value.trimStart().startsWith("-")
    val valueColor = if (isNegative) MaterialTheme.colorScheme.error
                     else MaterialTheme.colorScheme.primary
    val spaceIdx = value.indexOf(' ')
    val numPart = if (spaceIdx > 0) value.substring(0, spaceIdx) else value
    val unitPart = if (spaceIdx > 0) value.substring(spaceIdx + 1) else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent stripe
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .background(valueColor.copy(alpha = 0.7f)),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                // Numeric + unit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = numPart,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = valueColor,
                    )
                    if (unitPart.isNotEmpty()) {
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = unitPart,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultMiniChart(results: List<Pair<String, Any>>) {
    val numerics = results.mapNotNull { (label, value) ->
        val formatted = formatOutput(value)
        val numStr = formatted.trim().split(" ").firstOrNull()
        val num = numStr?.toDoubleOrNull() ?: return@mapNotNull null
        Triple(label, formatted, num)
    }
    if (numerics.size < 2) return

    val minVal = numerics.minOf { it.third }
    val maxVal = numerics.maxOf { it.third }
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Spacer(Modifier.height(4.dp))
    Text(
        text = "Output Comparison",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        val n = numerics.size
        val slotW = size.width / n
        val barW = slotW * 0.5f
        val baseline = size.height
        val maxBarH = size.height * 0.88f
        val minBarH = 3.dp.toPx()

        drawLine(
            color = outlineColor,
            start = Offset(0f, baseline),
            end = Offset(size.width, baseline),
            strokeWidth = 1f,
        )
        numerics.forEachIndexed { i, (_, _, value) ->
            val isNeg = value < 0
            val color = if (isNeg) errorColor else primaryColor
            val cx = slotW * i + slotW / 2f
            val normalizedH = ((value - minVal) / range).toFloat() * maxBarH
            val barH = normalizedH.coerceAtLeast(minBarH)
            drawRect(
                color = color.copy(alpha = 0.7f),
                topLeft = Offset(cx - barW / 2f, baseline - barH),
                size = Size(barW, barH),
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        numerics.forEach { (label, _, _) ->
            Text(
                text = label.take(5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 8.sp,
            )
        }
    }
}

@Composable
private fun ConnectionsTab(
    connectedEdges: List<UiEdgeRow>,
    onEdgeDeleteRequested: ((String) -> Unit)?,
) {
    if (connectedEdges.isEmpty()) {
        EmptyHint("No connections — tap 'Connect to node' below")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        val incoming = connectedEdges.filter { it.direction == EdgeDirection.INCOMING }
        val outgoing = connectedEdges.filter { it.direction == EdgeDirection.OUTGOING }

        if (incoming.isNotEmpty()) {
            Text(
                "INCOMING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            incoming.forEach { row ->
                EdgeRow(row = row, onEdgeDeleteRequested = onEdgeDeleteRequested)
                HorizontalDivider()
            }
            Spacer(Modifier.height(10.dp))
        }
        if (outgoing.isNotEmpty()) {
            Text(
                "OUTGOING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            outgoing.forEach { row ->
                EdgeRow(row = row, onEdgeDeleteRequested = onEdgeDeleteRequested)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun EdgeRow(row: UiEdgeRow, onEdgeDeleteRequested: ((String) -> Unit)?) {
    val isOutgoing = row.direction == EdgeDirection.OUTGOING
    val dirColor = if (isOutgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = dirColor.copy(alpha = 0.1f),
        ) {
            Text(
                text = if (isOutgoing) "→" else "←",
                style = MaterialTheme.typography.labelSmall,
                color = dirColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = row.otherNodeLabel,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        if (onEdgeDeleteRequested != null) {
            IconButton(
                onClick = { onEdgeDeleteRequested(row.edgeId) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove connection",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun EmptyHint(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatOutput(value: Any): String = when (value) {
    is Quantity -> "%.3f %s".format(value.value, value.unit.symbol)
    is Double -> "%.3f".format(value)
    is Float -> "%.3f".format(value)
    else -> value.toString()
}
