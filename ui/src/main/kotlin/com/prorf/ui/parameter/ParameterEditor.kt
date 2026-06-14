package com.prorf.ui.parameter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.graph.ParameterDefinition

/**
 * L2 UI — Editable parameter form for a node.
 * One editor row per ParameterDefinition.
 * No validation logic: delegates changes via callback, UI reflects last valid state.
 * Never imports executor or domain logic.
 */
@Composable
fun ParameterEditor(
    parameters: List<ParameterDefinition>,
    currentValues: Map<String, Any>,
    onValueChanged: (key: String, value: Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        parameters.forEach { param ->
            val current = currentValues[param.key] ?: param.defaultValue
            ParameterRow(
                definition = param,
                currentValue = current,
                onValueChanged = { onValueChanged(param.key, it) },
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ParameterRow(
    definition: ParameterDefinition,
    currentValue: Any?,
    onValueChanged: (Any) -> Unit,
) {
    var textValue by remember(currentValue) { mutableStateOf(displayValue(currentValue)) }
    val unitSymbol = if (definition.dataType == "quantity") {
        (currentValue as? Quantity)?.unit?.symbol ?: ""
    } else ""

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = definition.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 3.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { raw ->
                    textValue = raw
                    parseValue(definition.dataType, raw, currentValue)?.let { onValueChanged(it) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (definition.dataType) {
                        "double", "float", "int" -> KeyboardType.Decimal
                        "quantity" -> KeyboardType.Decimal
                        else -> KeyboardType.Text
                    },
                ),
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            if (unitSymbol.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = unitSymbol,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

private fun displayValue(value: Any?): String = when (value) {
    is Quantity -> value.value.toString()
    else -> value?.toString() ?: ""
}

private fun parseValue(dataType: String, raw: String, currentValue: Any?): Any? = when (dataType) {
    "double" -> raw.toDoubleOrNull()
    "float" -> raw.toFloatOrNull()
    "int" -> raw.toIntOrNull()
    "quantity" -> {
        val currentQuantity = currentValue as? Quantity ?: return null
        raw.toDoubleOrNull()?.let { currentQuantity.copy(value = it) }
    }
    else -> raw.ifBlank { null }
}
