package com.prorf.app.ui

import android.app.Application
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.ProRfApp
import com.prorf.app.data.WorkflowRepository
import com.prorf.app.data.WorkflowSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen() {
    val application = LocalContext.current.applicationContext as Application
    val pluginRegistry = (application as? ProRfApp)?.pluginRegistry
    val repository = remember(application) { WorkflowRepository(application.filesDir) }

    var summaries by remember { mutableStateOf<List<WorkflowSummary>>(emptyList()) }
    LaunchedEffect(Unit) {
        summaries = withContext(Dispatchers.IO) { repository.list() }
    }

    val definitions = pluginRegistry?.allDefinitions() ?: emptyList()
    val categoryOrder = listOf("Source", "Active", "Passive", "Channel", "Receiver")
    val categoryCounts = categoryOrder.associateWith { cat ->
        definitions.count { def -> analysisNodeCategory(def.typeId) == cat }
    }
    val totalNodes = summaries.sumOf { it.nodeCount }
    val totalEdges = summaries.sumOf { it.edgeCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "结果",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "RF 工程概览",
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // ── Stat cards ──────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatCard(
                        label = "工作流",
                        value = "${summaries.size}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "总节点",
                        value = "$totalNodes",
                        color = Color(0xFFE05A00),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "节点类型",
                        value = "${definitions.size}",
                        color = Color(0xFF7B2FBE),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Node category distribution chart ─────────────────────────────
            item {
                AnalysisSectionHeader(title = "Node Type Distribution")
            }
            item {
                NodeCategoryChart(
                    categoryCounts = categoryCounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── RF reference values ───────────────────────────────────────────
            item {
                AnalysisSectionHeader(title = "RF Engineering Reference")
            }
            item {
                RfReferenceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Saved workflows overview ──────────────────────────────────────
            if (summaries.isNotEmpty()) {
                item { AnalysisSectionHeader(title = "Saved Workflow Metrics") }
                items(summaries, key = { it.id }) { summary ->
                    WorkflowMetricRow(summary = summary)
                }
            } else {
                item {
                    EmptyAnalysisState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                    )
                }
            }
        }
    }
}

// ── Section header ─────────────────────────────────────────────────────────────

@Composable
private fun AnalysisSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

// ── Stat card ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Accent top stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        color,
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Node category distribution bar chart ─────────────────────────────────────

@Composable
private fun NodeCategoryChart(
    categoryCounts: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    val maxCount = categoryCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val colors = mapOf(
        "Source" to Color(0xFF2F80ED),
        "Active" to Color(0xFFE05A00),
        "Passive" to Color(0xFFF2994A),
        "Channel" to Color(0xFF27AE60),
        "Receiver" to Color(0xFF7B2FBE),
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Available Node Types",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            categoryCounts.entries.forEach { (category, count) ->
                val barColor = colors[category] ?: Color(0xFF64748B)
                CategoryBarRow(
                    category = category,
                    count = count,
                    fraction = count.toFloat() / maxCount,
                    color = barColor,
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryBarRow(category: String, count: Int, fraction: Float, color: Color) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    // Gradient: opaque category color → lighter tint
    val barGradient = Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.45f)))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).height(18.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background track
                val trackH = size.height * 0.35f
                val trackY = (size.height - trackH) / 2f
                val cornerR = CornerRadius(trackH / 2f)
                drawRoundRect(
                    color = outlineColor,
                    topLeft = Offset(0f, trackY),
                    size = Size(size.width, trackH),
                    cornerRadius = cornerR,
                )
                // Gradient fill bar
                if (fraction > 0f) {
                    val fillH = size.height * 0.65f
                    val fillY = (size.height - fillH) / 2f
                    val fillW = (size.width * fraction).coerceAtLeast(fillH)
                    drawRoundRect(
                        brush = barGradient,
                        topLeft = Offset(0f, fillY),
                        size = Size(fillW, fillH),
                        cornerRadius = CornerRadius(fillH / 2f),
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(16.dp),
        )
    }
}

// ── RF engineering reference ──────────────────────────────────────────────────

private data class RfConstant(val symbol: String, val value: String, val description: String)

private val RF_CONSTANTS = listOf(
    RfConstant("k", "−174 dBm/Hz", "Thermal noise floor (290 K)"),
    RfConstant("c", "3×10⁸ m/s", "Speed of light"),
    RfConstant("Lfs", "20lg(d)+20lg(f)+92.4 dB", "Free-space path loss formula"),
    RfConstant("NF", "Friss formula", "Cascaded noise figure (Friss)"),
    RfConstant("SNR", "Signal − Noise", "Signal-to-noise ratio"),
)

@Composable
private fun RfReferenceCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Engineering Constants",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            HorizontalDivider()
            RF_CONSTANTS.forEachIndexed { i, constant ->
                RfConstantRow(constant = constant)
                if (i < RF_CONSTANTS.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun RfConstantRow(constant: RfConstant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text = constant.symbol,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = constant.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = constant.value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
        )
    }
}

// ── Workflow metric row ───────────────────────────────────────────────────────

@Composable
private fun WorkflowMetricRow(summary: WorkflowSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = summary.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${summary.nodeCount} nodes · ${summary.edgeCount} edges",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
            // Node count visual indicator
            val nodeColor = when {
                summary.nodeCount == 0 -> MaterialTheme.colorScheme.outlineVariant
                summary.nodeCount < 3 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = nodeColor.copy(alpha = 0.12f),
            ) {
                Text(
                    text = "${summary.nodeCount}n",
                    style = MaterialTheme.typography.labelSmall,
                    color = nodeColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyAnalysisState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No workflows yet",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Create a workflow in the Editor to see metrics here",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun analysisNodeCategory(typeId: String): String {
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
