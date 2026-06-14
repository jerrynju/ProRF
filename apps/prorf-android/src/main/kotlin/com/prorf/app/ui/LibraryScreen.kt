package com.prorf.app.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.ProRfApp
import com.prorf.platform.graph.NodeDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    val application = LocalContext.current.applicationContext as Application
    val pluginRegistry = (application as? ProRfApp)?.pluginRegistry
    val definitions = pluginRegistry?.allDefinitions() ?: emptyList()

    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }

    val filteredDefs = if (searchQuery.isBlank()) definitions
    else definitions.filter {
        it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.typeId.substringAfterLast('.').contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
    }

    val grouped = filteredDefs.groupBy { nodeCategory(it.typeId) }
    val categoryOrder = listOf("Source", "Active", "Passive", "Channel", "Receiver", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Node Library",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${filteredDefs.size} of ${definitions.size} node types",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGridView) "List view" else "Grid view",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search bar
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            if (filteredDefs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isBlank()) "No node types registered"
                        else "No matches for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }

            if (isGridView) {
                // 2-column grid view
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    items(filteredDefs) { def ->
                        val catColor = categoryColor(nodeCategory(def.typeId))
                        NodeTypeGridCard(def = def, catColor = catColor)
                    }
                }
            } else {
                // Grouped list view
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    categoryOrder.forEach { category ->
                        val catItems = grouped[category] ?: return@forEach
                        val catColor = categoryColor(category)

                        item(key = "header_$category") {
                            CategorySectionHeader(
                                name = category,
                                color = catColor,
                                count = catItems.size,
                            )
                        }

                        item(key = "group_$category") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                elevation = CardDefaults.cardElevation(1.dp),
                            ) {
                                catItems.forEachIndexed { idx, def ->
                                    NodeTypeListRow(def = def, catColor = catColor)
                                    if (idx < catItems.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySectionHeader(name: String, color: Color, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.12f),
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun NodeTypeListRow(def: NodeDefinition, catColor: Color) {
    val abbr = def.typeId.substringAfterLast('.').let { name ->
        when {
            name.contains("Signal", ignoreCase = true) -> "SRC"
            name.contains("Noise", ignoreCase = true) -> "NSE"
            name.contains("Amplifier", ignoreCase = true) -> "AMP"
            name.contains("Attenuator", ignoreCase = true) -> "ATT"
            name.contains("Cable", ignoreCase = true) -> "CBL"
            name.contains("Filter", ignoreCase = true) -> "FLT"
            name.contains("Loss", ignoreCase = true) || name.contains("Path", ignoreCase = true) -> "FSPL"
            name.contains("Receiver", ignoreCase = true) -> "RCV"
            name.contains("Sensitivity", ignoreCase = true) -> "SNS"
            else -> name.take(3).uppercase()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Colored circle with category abbreviation
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
                fontSize = 9.sp,
            )
        }

        Spacer(Modifier.width(12.dp))

        // Name + description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = def.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (def.description.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = def.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Port + param badges stacked on the right
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (def.inputs.isNotEmpty()) {
                PortBadge(
                    label = "${def.inputs.size}in",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            if (def.outputs.isNotEmpty()) {
                PortBadge(
                    label = "${def.outputs.size}out",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (def.parameters.isNotEmpty()) {
                PortBadge(
                    label = "${def.parameters.size}p",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NodeTypeGridCard(def: NodeDefinition, catColor: Color) {
    val abbr = def.typeId.substringAfterLast('.').let { name ->
        when {
            name.contains("Signal", ignoreCase = true) -> "SRC"
            name.contains("Noise", ignoreCase = true) -> "NSE"
            name.contains("Amplifier", ignoreCase = true) -> "AMP"
            name.contains("Attenuator", ignoreCase = true) -> "ATT"
            name.contains("Cable", ignoreCase = true) -> "CBL"
            name.contains("Filter", ignoreCase = true) -> "FLT"
            name.contains("Loss", ignoreCase = true) || name.contains("Path", ignoreCase = true) -> "FSPL"
            name.contains("Receiver", ignoreCase = true) -> "RCV"
            name.contains("Sensitivity", ignoreCase = true) -> "SNS"
            else -> name.take(4).uppercase()
        }
    }
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
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
            Spacer(Modifier.height(8.dp))
            Text(
                text = def.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(5.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (def.inputs.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = catColor.copy(alpha = 0.10f),
                    ) {
                        Text(
                            "${def.inputs.size}in",
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                    if (def.outputs.isNotEmpty()) Spacer(Modifier.width(3.dp))
                }
                if (def.outputs.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = catColor.copy(alpha = 0.10f),
                    ) {
                        Text(
                            "${def.outputs.size}out",
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PortBadge(label: String, color: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}

private fun nodeCategory(typeId: String): String {
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

private fun categoryColor(category: String): Color = when (category) {
    "Source" -> Color(0xFF2F80ED)
    "Active" -> Color(0xFF27AE60)
    "Passive" -> Color(0xFFF2994A)
    "Channel" -> Color(0xFF9B51E0)
    "Receiver" -> Color(0xFFEB5757)
    else -> Color(0xFF64748B)
}
