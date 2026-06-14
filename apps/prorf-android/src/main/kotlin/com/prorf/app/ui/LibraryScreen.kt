package com.prorf.app.ui

import android.app.Application
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.ProRfApp
import com.prorf.platform.graph.NodeDefinition
import com.prorf.ui.canvas.categoryAbbr
import com.prorf.ui.canvas.categoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    val application = LocalContext.current.applicationContext as Application
    val pluginRegistry = (application as? ProRfApp)?.pluginRegistry
    val definitions = pluginRegistry?.allDefinitions() ?: emptyList()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("全部") }

    val categories = listOf("全部", "RF", "DSP", "控制", "仿真", "模板库")

    val categoryFiltered = when (selectedCategory) {
        "RF" -> definitions.filter {
            val name = it.typeId.substringAfterLast('.')
            name.contains("Signal") || name.contains("Noise") || name.contains("Amplifier") ||
                name.contains("Attenuator") || name.contains("Cable") || name.contains("Filter") ||
                name.contains("Loss") || name.contains("Path") || name.contains("Receiver") || name.contains("Sensitivity")
        }
        else -> definitions
    }

    val filteredDefs = if (searchQuery.isBlank()) categoryFiltered
    else categoryFiltered.filter {
        it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.typeId.substringAfterLast('.').contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
    }

    // Group into the design's two section model (source systems, receive systems)
    val sourceGroup = filteredDefs.filter {
        val name = it.typeId.substringAfterLast('.')
        name.contains("Signal") || name.contains("Noise") || name.contains("Amplifier") ||
            name.contains("Attenuator") || name.contains("Filter") || name.contains("Cable") ||
            name.contains("Loss") || name.contains("Path")
    }
    val receiveGroup = filteredDefs.filter {
        val name = it.typeId.substringAfterLast('.')
        name.contains("Receiver") || name.contains("Sensitivity")
    }
    val otherGroup = filteredDefs.filter { it !in sourceGroup && it !in receiveGroup }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "节点库",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${filteredDefs.size} / ${definitions.size} 种节点类型",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("按名称搜索…", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            "清除",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { searchQuery = "" }
                                .padding(end = 12.dp),
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            // ── Category filter chips ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedCategory = cat },
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            // ── Node grid ─────────────────────────────────────────────────────
            if (filteredDefs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isBlank()) "暂无节点类型" else "未找到\"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (sourceGroup.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(4) }) {
                        LibrarySectionHeader("信号处理")
                    }
                    items(sourceGroup, key = { it.typeId }) { def ->
                        NodeIconCard(def = def)
                    }
                }

                if (receiveGroup.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(4) }) {
                        LibrarySectionHeader("接收系统")
                    }
                    items(receiveGroup, key = { it.typeId }) { def ->
                        NodeIconCard(def = def)
                    }
                }

                if (otherGroup.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(4) }) {
                        LibrarySectionHeader("其他")
                    }
                    items(otherGroup, key = { it.typeId }) { def ->
                        NodeIconCard(def = def)
                    }
                }

                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(4) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "— 全部节点类型已展示 —",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun NodeIconCard(def: NodeDefinition) {
    val catColor = categoryColor(def.typeId)
    val abbr = categoryAbbr(def.typeId).take(1)

    Surface(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { },
        shape = RoundedCornerShape(14.dp),
        color = catColor.copy(alpha = 0.08f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Large letter circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(catColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = abbr,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = def.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp,
            )
        }
    }
}
