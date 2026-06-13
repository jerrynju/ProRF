package com.prorf.ui.inspector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.graph.NodeDefinition
import com.prorf.platform.graph.NodeInstance
import com.prorf.ui.model.EdgeDirection
import com.prorf.ui.model.UiEdgeRow
import com.prorf.ui.parameter.ParameterEditor

/**
 * L2 UI — Inspector panel shown when a node is selected.
 * Spec: must show inputs, parameters, outputs, diagnostics.
 * Must NOT call any executor or domain logic directly.
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
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        if (onNodeRenameRequested != null) {
            var nameText by remember(nodeInstance.id) {
                mutableStateOf(nodeInstance.label ?: definition.displayName)
            }
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Node Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onNodeRenameRequested(nameText.trim().ifEmpty { definition.displayName })
                }),
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
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = "ID: ${nodeInstance.id}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (definition.inputs.isNotEmpty()) {
            InspectorSection(title = "Inputs") {
                definition.inputs.forEach { port ->
                    LabeledRow(label = port.name, value = port.dataType)
                }
            }
        }

        if (definition.parameters.isNotEmpty()) {
            InspectorSection(title = "Parameters") {
                ParameterEditor(
                    parameters = definition.parameters,
                    currentValues = nodeInstance.parameters,
                    onValueChanged = onParameterChanged,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (definition.outputs.isNotEmpty()) {
            InspectorSection(title = "Outputs") {
                definition.outputs.forEach { port ->
                    val value = executionOutputs[port.id]
                    LabeledRow(
                        label = port.name,
                        value = value?.let { formatOutput(it) } ?: "—",
                    )
                }
            }
        }

        if (executionOutputs.isNotEmpty()) {
            InspectorSection(title = "Diagnostics") {
                Text(
                    text = "${executionOutputs.size} output(s) computed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (connectedEdges.isNotEmpty() && onEdgeDeleteRequested != null) {
            InspectorSection(title = "Connections") {
                connectedEdges.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val label = when (row.direction) {
                            EdgeDirection.OUTGOING -> "→ ${row.otherNodeLabel}"
                            EdgeDirection.INCOMING -> "← ${row.otherNodeLabel}"
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onEdgeDeleteRequested(row.edgeId) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete edge",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        if (onConnectRequested != null) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onConnectRequested,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Connect to node →")
            }
        }

        if (onNodeDeleteRequested != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNodeDeleteRequested,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text("Delete Node")
            }
        }
    }
}

@Composable
private fun InspectorSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(4.dp))
    content()
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatOutput(value: Any): String = when (value) {
    is Quantity -> "%.3f %s".format(value.value, value.unit.symbol)
    is Double -> "%.3f".format(value)
    is Float -> "%.3f".format(value)
    else -> value.toString()
}
